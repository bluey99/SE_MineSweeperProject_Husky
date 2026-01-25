package multiplayer;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * MultiplayerSession:
 * - Wraps NetworkSession and routes MpMessages to callbacks.
 * - Keeps approval flow state.
 * - Adds an optional "beforeApplyGameSettings" hook (use it on CLIENT to reset Stage constraints
 *   BEFORE rebuilding the game view, fixing bad sizing after restarting/new game).
 */
public class MultiplayerSession {

    private final NetworkSession network;

    // ✅ Hook: run before applying GAME_SETTINGS (use on CLIENT to reset Stage constraints)
    private Runnable beforeApplyGameSettings;

    // Core callbacks
    private Consumer<GameAction> onActionReceived;

    // Game over
    private Consumer<GameOverPayload> onGameOver;

    // Approval flow
    private Consumer<MenuAction> onActionRequest;
    private Consumer<MenuAction> onExecuteAction;
    private Consumer<MenuAction.Type> onActionDeclined;

    // Player-left notification
    private Consumer<String> onPlayerLeft;

    // Question result sync (popup only local; remote receives result)
    private Consumer<QuestionResultPayload> onQuestionResult;

    // Game settings sync (difficulty/seed + optional UI sizing)
    private Consumer<GameSettings> onGameSettings;

    private boolean isHost = false;

    private MenuAction pendingIncomingRequest = null;
    private MenuAction pendingHostOutgoingRequest = null;

    public MultiplayerSession(NetworkSession network) {
        this.network = Objects.requireNonNull(network, "network");
    }

    // ------------------ Role ------------------

    public void setRole(boolean isHost) {
        this.isHost = isHost;
    }

    public boolean isHost() {
        return isHost;
    }

    // ------------------ Hooks / Handlers ------------------

    /**
     * Called right before firing onGameSettings when a GAME_SETTINGS message arrives.
     * Use this on the CLIENT to reset Stage min/max constraints BEFORE rebuilding the UI.
     */
    public void setBeforeApplyGameSettings(Runnable r) {
        this.beforeApplyGameSettings = r;
    }

    public void setOnActionReceived(Consumer<GameAction> handler) {
        this.onActionReceived = handler;
    }

    public void setOnGameOver(Consumer<GameOverPayload> h) {
        this.onGameOver = h;
    }

    public void setOnActionRequest(Consumer<MenuAction> h) {
        this.onActionRequest = h;
    }

    public void setOnExecuteAction(Consumer<MenuAction> h) {
        this.onExecuteAction = h;
    }

    public void setOnActionDeclined(Consumer<MenuAction.Type> h) {
        this.onActionDeclined = h;
    }

    public void setOnPlayerLeft(Consumer<String> h) {
        this.onPlayerLeft = h;
    }

    public void setOnQuestionResult(Consumer<QuestionResultPayload> h) {
        this.onQuestionResult = h;
    }

    public void setOnGameSettings(Consumer<GameSettings> h) {
        this.onGameSettings = h;
    }

    // ------------------ Sending ------------------

    public void sendMessage(MpMessage msg) {
        try {
            network.send(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void notifyPlayerLeft(String name) {
        sendMessage(new MpMessage(MpMessageType.PLAYER_LEFT, name));
    }

    public void sendQuestionResult(QuestionResultPayload payload) {
        sendMessage(new MpMessage(MpMessageType.QUESTION_RESULT, payload));
    }

    public void sendGameSettings(GameSettings settings) {
        sendMessage(new MpMessage(MpMessageType.GAME_SETTINGS, settings));
    }

    public void send(GameAction action) {
        sendMessage(new MpMessage(MpMessageType.GAME_ACTION, action));
    }

    // ------------------ Shared-action approval flow ------------------

    /**
     * Request a shared menu action (you said you only use this for NEW_GAME).
     */
    public void requestSharedAction(MenuAction.Type type, String requestedBy) {
        MenuAction action = new MenuAction(type, requestedBy);

        // host also uses the same message; host keeps state for matching the response
        if (isHost) pendingHostOutgoingRequest = action;

        sendMessage(new MpMessage(MpMessageType.REQUEST_ACTION, action));
    }

    /**
     * Called AFTER user presses Approve/Decline on THIS side for the last REQUEST_ACTION received.
     */
    public void respondToPendingRequest(boolean approved) {
        if (pendingIncomingRequest == null) return;

        MenuAction action = pendingIncomingRequest;
        pendingIncomingRequest = null;

        // Send response first
        sendMessage(new MpMessage(
                MpMessageType.ACTION_RESPONSE,
                new ActionResponse(approved, action.type)
        ));

        if (!isHost) {
            // client just responds; host will decide what to execute/broadcast
            return;
        }

        // Host responding to client's request:
        if (approved) {
            // ✅ host executes locally
            if (onExecuteAction != null) onExecuteAction.accept(action);

            // and tells client to execute
            sendMessage(new MpMessage(MpMessageType.EXECUTE_ACTION, action));
        } else {
            if (onActionDeclined != null) onActionDeclined.accept(action.type);
        }
    }

    // ------------------ Incoming routing ------------------

    public void handleIncoming(MpMessage msg) {
        if (msg == null) return;

        switch (msg.type) {

            case GAME_ACTION -> {
                GameAction action = (GameAction) msg.payload;
                if (onActionReceived != null) onActionReceived.accept(action);
            }

            case GAME_SETTINGS -> {
                GameSettings settings = (GameSettings) msg.payload;

                // ✅ IMPORTANT: run hook BEFORE rebuilding the game UI (client fix)
                if (beforeApplyGameSettings != null) {
                    beforeApplyGameSettings.run();
                }

                if (onGameSettings != null) onGameSettings.accept(settings);
            }

            case GAME_OVER -> {
                if (onGameOver != null) onGameOver.accept((GameOverPayload) msg.payload);
            }

            case PLAYER_LEFT -> {
                String name = (String) msg.payload;
                if (onPlayerLeft != null) onPlayerLeft.accept(name);
            }

            case QUESTION_RESULT -> {
                QuestionResultPayload payload = (QuestionResultPayload) msg.payload;
                if (onQuestionResult != null) onQuestionResult.accept(payload);
            }

            // Approval flow
            case REQUEST_ACTION -> {
                MenuAction action = (MenuAction) msg.payload;
                pendingIncomingRequest = action;
                if (onActionRequest != null) onActionRequest.accept(action);
            }

            case ACTION_RESPONSE -> {
                ActionResponse res = (ActionResponse) msg.payload;

                // Host waiting for client response for host-initiated request
                if (isHost && pendingHostOutgoingRequest != null
                        && pendingHostOutgoingRequest.type == res.type) {

                    MenuAction action = pendingHostOutgoingRequest;
                    pendingHostOutgoingRequest = null;

                    if (res.approved) {
                        // ✅ host executes locally
                        if (onExecuteAction != null) onExecuteAction.accept(action);

                        // and tells client to execute
                        sendMessage(new MpMessage(MpMessageType.EXECUTE_ACTION, action));
                    } else {
                        if (onActionDeclined != null) onActionDeclined.accept(action.type);
                    }
                } else {
                    // Client can optionally show decline feedback
                    if (!res.approved && onActionDeclined != null) onActionDeclined.accept(res.type);
                }
            }

            case EXECUTE_ACTION -> {
                MenuAction action = (MenuAction) msg.payload;
                if (onExecuteAction != null) onExecuteAction.accept(action);
            }

            default -> {
                // ignore
            }
        }
    }

    // ------------------ Close ------------------

    public void close() {
        network.close();
    }
}
