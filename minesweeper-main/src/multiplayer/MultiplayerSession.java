package multiplayer;

import java.util.function.Consumer;

public class MultiplayerSession {

    private final NetworkSession network;
    private Consumer<GameAction> onActionReceived;

    public MultiplayerSession(NetworkSession network) {
        this.network = network;
    }

    public void setOnActionReceived(Consumer<GameAction> handler) {
        this.onActionReceived = handler;
    }

    // Called by the ONE receiver loop you already have in MultiplayerSetupView
    public void handleIncoming(MpMessage msg) {
        if (msg.type == MpMessageType.GAME_ACTION) {
            GameAction action = (GameAction) msg.payload;
            if (onActionReceived != null) onActionReceived.accept(action);
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
