package console;

/**
 * Thrown by InputHelper when the user types C at an input prompt.
 * The role menu catches it in one place and returns to the menu, so a cancelled
 * operation never saves partial data and never needs its own cancel checks.
 */
public class OperationCancelledException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public OperationCancelledException() {
        this("Operation cancelled.");
    }

    /** message is shown to the user, e.g. "Order discarded." */
    public OperationCancelledException(String message) {
        super(message);
    }
}
