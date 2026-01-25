package controller;

public class SetupController {

    // returns error message ONLY if names are the same
    public String validateSameNames(String p1, String p2) {

        if (p1 == null || p2 == null)
            return null;

        p1 = p1.trim();
        p2 = p2.trim();

        if (!p1.isEmpty() && !p2.isEmpty()
                && p1.equalsIgnoreCase(p2)) {
            return "Players must have different names.";
        }

        return null;
    }
}
