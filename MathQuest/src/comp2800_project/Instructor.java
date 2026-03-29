package comp2800_project;

/**
 * Allows Instructors to view all player scores and progression.
 * Permission level 1.
 */
public class Instructor {

    private final int instructorPin   = 666473;
    private int       permissionValue = 0;

    public void setPermission(int pinInput) throws IncorrectPasswordException {
        if (pinInput == instructorPin) {
            permissionValue = 1;
        } else {
            permissionValue = 0;
            throw new IncorrectPasswordException("Instructor Password Incorrect.");
        }
    }

    public int getPermission() {
        return permissionValue;
    }
}
