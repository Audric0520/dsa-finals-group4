/**
 * Simple password check for each role (for a school demonstration, not real security).
 * The passwords are stored only here and are never printed.
 */
public class Authentication {
    private static final String CASHIER_PASSWORD = "ICCashier123";
    private static final String ICECREAMMAKER_PASSWORD = "ICMaker123";
    private static final String MANAGER_PASSWORD = "ICManager123";

    public static final String MANAGER = "Manager";
    public static final String CASHIER = "Cashier";
    public static final String ICE_CREAM_MAKER = "Ice Cream Maker";

    /**
     * Keeps asking for the role's password until it is correct or the user cancels with C.
     * Returns true only when the correct password was entered.
     */
    public static boolean login(String roleName) {
        String correctPassword = getPassword(roleName);
        while (true) {
            String entered = InputHelper.readLine("Enter " + roleName + " password (or C to cancel): ");
            if (entered.equalsIgnoreCase(InputHelper.CANCEL)) {
                System.out.println("Login cancelled. Returning to Main Menu...");
                return false;
            }
            if (entered.equals(correctPassword)) {
                System.out.println("Access granted. Welcome, " + roleName + "!");
                return true;
            }
            System.out.println("Incorrect password for " + roleName + ". Please try again.");
        }
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
