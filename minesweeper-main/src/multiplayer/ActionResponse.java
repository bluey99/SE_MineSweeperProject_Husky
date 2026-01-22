package multiplayer;

import java.io.Serializable;

public class ActionResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    public final boolean approved;
    public final MenuAction.Type type;

    public ActionResponse(boolean approved, MenuAction.Type type) {
        this.approved = approved;
        this.type = type;
    }
}
