package multiplayer;

import java.io.Serializable;

public class MenuAction implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum Type { NEW_GAME, EXIT, RETURN_MENU }

    public final Type type;
    public final String requestedBy;

    public MenuAction(Type type, String requestedBy) {
        this.type = type;
        this.requestedBy = requestedBy;
    }
}
