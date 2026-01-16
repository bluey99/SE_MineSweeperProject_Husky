package multiplayer;

public enum MpMessageType {
    HELLO_JOIN,       // client -> host (contains joiner name)
    GAME_SETTINGS,    // host -> client (difficulty, names, etc.)
    BOARD_SNAPSHOT,   // host -> client (full board state)
    GAME_ACTION,      // host <-> client (clicks, etc.)
    CHAT,             // host <-> client (chat strings)
    DISCONNECT
}
