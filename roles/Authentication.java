package roles;

import console.InputHelper;
import console.Terminal;

/**
 * Simple password check for each role (for a school demonstration, not real security).
 * The passwords are stored only here and are never printed. Like sudo, only a few
 * attempts are allowed.
 */
public class Authentication {
    private static final String CASHIER_PASSWORD = "ICCashier123";
    private static final String ICECREAMMAKER_PASSWORD = "ICMaker123";
    private static final String MANAGER_PASSWORD = "ICManager123";

    public static final String MANAGER = "Manager";
    public static final String CASHIER = "Cashier";
    public static final String ICE_CREAM_MAKER = "Ice Cream Maker";

    public static final int MAX_ATTEMPTS = 3;

    /**
     * Asks for the role's password until it is correct, the user cancels with C, or
     * MAX_ATTEMPTS wrong passwords were entered. Returns true only for the correct password.
     */
    public static boolean login(String roleName) {
        String correctPassword = getPassword(roleName);
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            String entered = InputHelper.readPassword("Enter " + roleName + " password (or C to cancel): ");
            if (entered.equalsIgnoreCase(InputHelper.CANCEL)) {
                Terminal.notice("Login cancelled. Returning to Main Menu...");
                return false;
            }
            if (entered.equals(correctPassword)) {
                Terminal.notice("Access granted. Welcome, " + roleName + "!");
                return true;
            }
            int attemptsLeft = MAX_ATTEMPTS - attempt;
            if (attemptsLeft > 0) {
                System.out.println("Incorrect password for " + roleName + ". " + attemptsLeft
                        + (attemptsLeft == 1 ? " attempt" : " attempts") + " left.");
            }
        }
        Terminal.notice(MAX_ATTEMPTS + " incorrect password attempts for " + roleName
                + ". Returning to Main Menu...");
        return false;
    }

    private static String getPassword(String roleName) {
        switch (roleName) {
            case MANAGER:
                return MANAGER_PASSWORD;
            case CASHIER:
                return CASHIER_PASSWORD;
            case ICE_CREAM_MAKER:
                return ICECREAMMAKER_PASSWORD;
            default:
                throw new IllegalArgumentException("Unknown role: " + roleName);
        }
    }
}
