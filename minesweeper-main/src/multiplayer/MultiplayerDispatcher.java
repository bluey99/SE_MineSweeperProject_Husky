package multiplayer;

import controller.BoardController;
import controller.GameController;
import javafx.application.Platform;

public class MultiplayerDispatcher {

    public enum Role { OFFLINE, HOST, CLIENT }

    private final GameController gameController;
    private final Role role;
    private final NetworkSession session; // null if OFFLINE

    public MultiplayerDispatcher(GameController gc, Role role, NetworkSession session) {
        this.gameController = gc;
        this.role = role;
        this.session = session;
    }

    public Role getRole() { return role; }

    // Called from mouse click handlers (instead of calling bc.handleLeftClick directly)
    public void onLocalLeftClick(int playerNum, int row, int col) {
        if (role == Role.OFFLINE) {
            BoardController.getInstance(playerNum).handleLeftClick(row, col);
            return;
        }

        GameAction action = new GameAction(GameAction.Type.LEFT_CLICK, playerNum, row, col);

        if (role == Role.CLIENT) {
            sendActionToHost(action);
        } else { // HOST
            applyActionHostAndBroadcast(action);
        }
    }

    public void onLocalRightClick(int playerNum, int row, int col) {
        if (role == Role.OFFLINE) {
            BoardController.getInstance(playerNum).handleRightClick(row, col);
            return;
        }

        GameAction action = new GameAction(GameAction.Type.RIGHT_CLICK, playerNum, row, col);

        if (role == Role.CLIENT) {
            sendActionToHost(action);
        } else {
            applyActionHostAndBroadcast(action);
        }
    }

    private void sendActionToHost(GameAction action) {
        try {
            session.send(new MpMessage(MpMessageType.GAME_ACTION, action));
        } catch (Exception ignored) {}
    }

    // Host: apply + broadcast so both sides do EXACTLY the same
    private void applyActionHostAndBroadcast(GameAction action) {
        // Apply locally first
        applyActionLocally(action);

        // Broadcast to client
        try {
            session.send(new MpMessage(MpMessageType.GAME_ACTION, action));
        } catch (Exception ignored) {}
    }

    // When we receive an action from network (client receives from host OR host receives from client)
    public void onNetworkAction(GameAction action) {
        if (role == Role.HOST) {
            // Host received from client: validate turn, then broadcast and apply
            // (Simple version: just apply+send. Later we add strict validation.)
            applyActionHostAndBroadcast(action);
        } else if (role == Role.CLIENT) {
            // Client receives authoritative action from host: apply only
            applyActionLocally(action);
        }
    }

    private void applyActionLocally(GameAction action) {
        Platform.runLater(() -> {
            if (action.type == GameAction.Type.LEFT_CLICK) {
                BoardController.getInstance(action.playerNum).handleLeftClick(action.row, action.col);
            } else {
                BoardController.getInstance(action.playerNum).handleRightClick(action.row, action.col);
            }
        });
    }
}
