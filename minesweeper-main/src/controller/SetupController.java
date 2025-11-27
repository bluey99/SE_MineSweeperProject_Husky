package controller;

public class SetupController {

    // Return null if OK, OR return the error message
    public String validatePlayerNames(String p1, String p2) {

        if (p1 == null || p1.trim().isEmpty())
            return "- Player 1 name cannot be empty.";

        if (p2 == null || p2.trim().isEmpty())
            return "- Player 2 name cannot be empty.";

        if (p1.length() > 12)
            return "- Player 1 name must be at most 12 characters.";

        if (p2.length() > 12)
            return "- Player 2 name must be at most 12 characters.";

        if (p1.trim().equalsIgnoreCase(p2.trim()))
            return "- Players must have different names.";

        return null; // no errors
    }

}
