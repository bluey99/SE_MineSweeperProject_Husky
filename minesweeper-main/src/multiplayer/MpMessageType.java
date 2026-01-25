package multiplayer;

public enum MpMessageType {
    HELLO_JOIN,
    GAME_SETTINGS,
    BOARD_SNAPSHOT,
    GAME_ACTION,
    CHAT,
    DISCONNECT,

    // Game Over sync
    GAME_OVER,

    // Approval (we will use ONLY for NEW_GAME)
    REQUEST_ACTION,
    ACTION_RESPONSE,
    EXECUTE_ACTION,

    // notify other side that player left to menu/exit
    PLAYER_LEFT,

    // ✅ NEW: Question popup sync (answer result only)
    QUESTION_RESULT
}
