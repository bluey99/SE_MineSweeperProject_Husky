package multiplayer;

import java.util.function.Consumer;

public class MultiplayerSession {

    private final NetworkSession network;

    private Consumer<GameAction> onActionReceived;

    // Game over
    private Consumer<GameOverPayload> onGameOver;

    // Approval flow
    private Consumer<MenuAction> onActionRequest;
    private Consumer<MenuAction> onExecuteAction;
    private Consumer<MenuAction.Type> onActionDeclined;

    // Player-left notification
    private Consumer<String> onPlayerLeft;

    // ✅ NEW: Question result sync (popup only local; remote receives result)
    private Consumer<QuestionResultPayload> onQuestionResult;

    private boolean isHost = false;

    private MenuAction pendingIncomingRequest = null;
    private MenuAction pendingHostOutgoingRequest = null;

    public MultiplayerSession(NetworkSession network) {
        this.network = network;
    }

    public void setRole(boolean isHost) {
        this.isHost = isHost;
    }

    public boolean isHost() {
        return isHost;
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

    // ✅ NEW
    public void setOnQuestionResult(Consumer<QuestionResultPayload> h) {
        this.onQuestionResult = h;
    }

    public void sendMessage(MpMessage msg) {
        try {
            network.send(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ✅ Notify other side & close
    public void notifyPlayerLeft(String name) {
        sendMessage(new MpMessage(MpMessageType.PLAYER_LEFT, name));
    }

    // ✅ NEW: send question result to other side (no popup on remote)
    public void sendQuestionResult(QuestionResultPayload payload) {
        sendMessage(new MpMessage(MpMessageType.QUESTION_RESULT, payload));
    }

    // ✅ Step 3: request shared action (we will use only NEW_GAME)
    public void requestSharedAction(MenuAction.Type type, String requestedBy) {
        MenuAction action = new MenuAction(type, requestedBy);

        if (isHost) {
            pendingHostOutgoingRequest = action;
            sendMessage(new MpMessage(MpMessageType.REQUEST_ACTION, action));
        } else {
            // client asks host to coordinate
            sendMessage(new MpMessage(MpMessageType.REQUEST_ACTION, action));
        }
    }

    // Called AFTER user presses Approve/Decline on THIS side
    public void respondToPendingRequest(boolean approved) {
        if (pendingIncomingRequest == null) return;

        MenuAction action = pendingIncomingRequest;
        pendingIncomingRequest = null;

        if (isHost) {
            // host responding to client's request
            sendMessage(new MpMessage(MpMessageType.ACTION_RESPONSE, new ActionResponse(approved, action.type)));

            if (approved) {
                // ✅ IMPORTANT FIX: host executes locally too
                if (onExecuteAction != null) onExecuteAction.accept(action);

                // and tells client to execute
                sendMessage(new MpMessage(MpMessageType.EXECUTE_ACTION, action));
            } else {
                if (onActionDeclined != null) onActionDeclined.accept(action.type);
            }

        } else {
            // client responding to host request
            sendMessage(new MpMessage(MpMessageType.ACTION_RESPONSE, new ActionResponse(approved, action.type)));
        }
    }

    public void handleIncoming(MpMessage msg) {

        if (msg.type == MpMessageType.GAME_ACTION) {
            GameAction action = (GameAction) msg.payload;
            if (onActionReceived != null) onActionReceived.accept(action);
            return;
        }

        if (msg.type == MpMessageType.GAME_OVER) {
            if (onGameOver != null) onGameOver.accept((GameOverPayload) msg.payload);
            return;
        }

        if (msg.type == MpMessageType.PLAYER_LEFT) {
            String name = (String) msg.payload;
            if (onPlayerLeft != null) onPlayerLeft.accept(name);
            return;
        }

        // ✅ NEW: Question result received
        if (msg.type == MpMessageType.QUESTION_RESULT) {
            QuestionResultPayload payload = (QuestionResultPayload) msg.payload;
            if (onQuestionResult != null) onQuestionResult.accept(payload);
            return;
        }

        // Approval flow
        if (msg.type == MpMessageType.REQUEST_ACTION) {
            MenuAction action = (MenuAction) msg.payload;
            pendingIncomingRequest = action;
            if (onActionRequest != null) onActionRequest.accept(action);
            return;
        }

        if (msg.type == MpMessageType.ACTION_RESPONSE) {
            ActionResponse res = (ActionResponse) msg.payload;

            // Host waiting for client response for host-initiated request
            if (isHost && pendingHostOutgoingRequest != null && pendingHostOutgoingRequest.type == res.type) {
                MenuAction action = pendingHostOutgoingRequest;
                pendingHostOutgoingRequest = null;

                if (res.approved) {
                    // ✅ IMPORTANT FIX: host executes locally too
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
            return;
        }

        if (msg.type == MpMessageType.EXECUTE_ACTION) {
            MenuAction action = (MenuAction) msg.payload;
            if (onExecuteAction != null) onExecuteAction.accept(action);
            return;
        }
    }

    public void send(GameAction action) {
        try {
            network.send(new MpMessage(MpMessageType.GAME_ACTION, action));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void close() {
        network.close();
    }
}
