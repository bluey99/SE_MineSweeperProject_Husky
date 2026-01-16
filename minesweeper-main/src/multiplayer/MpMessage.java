package multiplayer;

import java.io.Serializable;

public class MpMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    public final MpMessageType type;
    public final Object payload;

    public MpMessage(MpMessageType type, Object payload) {
        this.type = type;
        this.payload = payload;
    }
}
