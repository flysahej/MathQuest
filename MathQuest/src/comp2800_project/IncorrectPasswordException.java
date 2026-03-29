package comp2800_project;

/**
 * Exception raised when an incorrect PIN is supplied to
 * Instructor.setPermission() or Developer.setPermission().
 */
public class IncorrectPasswordException extends Exception {

    public IncorrectPasswordException() {
        super("Incorrect password");
    }

    public IncorrectPasswordException(String message) {
        super(message);
    }
}
