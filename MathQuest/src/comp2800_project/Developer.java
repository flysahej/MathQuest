package comp2800_project;

/**
 * Allows Developers more access to the game for testing and debugging.
 * Permission level 2.
 */
public class Developer {

    private final int developerPin    = 374666;
    private int       permissionValue = 0;

    public void setPermission(int pinInput) throws IncorrectPasswordException {
        if (pinInput == developerPin) {
            permissionValue = 2;
        } else {
            permissionValue = 0;
            throw new IncorrectPasswordException("Developer Password Incorrect.");
        }
    }

    public int getPermission() {
        return permissionValue;
    }
}
