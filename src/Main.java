import java.io.PrintStream;

/**
 * SC Ice Cream Management System - entry point.
 * One shared Inventory is used by all roles, and each role object is created once,
 * so data (like the pending order queue) is kept while switching roles.
 */
public class Main {

    public static void main(String[] args) {
        boolean plain = false;
        boolean noColor = false;
        for (String arg : args) {
            switch (arg) {
                case "--plain":
                    plain = true;
                    break;
                case "--no-color":
                    noColor = true;
                    break;
                case "-h":
                case "--help":
                    printUsage(System.out);
                    return;
                default:
                    System.err.println("Unknown option: " + arg);
                    printUsage(System.err);
                    System.exit(2);
            }
        }
        Terminal.init(plain, noColor);

        Inventory inventory = new Inventory();
        loadSampleProducts(inventory);

        IceCreamMaker iceCreamMaker = new IceCreamMaker(inventory);
        Cashier cashier = new Cashier(inventory, iceCreamMaker);
        Manager manager = new Manager(inventory, iceCreamMaker);

        while (true) {
            Terminal.newScreen();
            Terminal.title("========================================");
            Terminal.title("    SC ICE CREAM MANAGEMENT SYSTEM");
            Terminal.title("========================================");
            System.out.println("[1] Cashier");
            System.out.println("[2] Ice Cream Maker");
            System.out.println("[3] Manager");
            System.out.println("[0] Exit");
            int choice = InputHelper.readMenuChoice(3);

            if (choice == 0) {
                if (confirmExit(iceCreamMaker)) {
                    break;
                }
            } else if (choice == 1 && Authentication.login(Authentication.CASHIER)) {
                cashier.showMenu();
            } else if (choice == 2 && Authentication.login(Authentication.ICE_CREAM_MAKER)) {
                iceCreamMaker.showMenu();
            } else if (choice == 3 && Authentication.login(Authentication.MANAGER)) {
                manager.showMenu();
            }
        }
        System.out.println("Thank you for using the SC Ice Cream Management System. Goodbye!");
    }

    // Data is kept only while the program runs, so warn before unprocessed orders are lost.
    private static boolean confirmExit(IceCreamMaker iceCreamMaker) {
        int pending = iceCreamMaker.getPendingOrders().size();
        if (pending == 0) {
            return true;
        }
        Terminal.warning(pending + (pending == 1 ? " order is" : " orders are")
                + " still pending and will be lost when the program closes.");
        try {
            return InputHelper.readYesNo("Exit anyway?", false);
        } catch (OperationCancelledException e) {
            return false;
        }
    }

    private static void printUsage(PrintStream out) {
        out.println("Usage: java -cp out Main [options]");
        out.println();
        out.println("Options:");
        out.println("  --plain      plain scrolling output: no screen clearing, no pauses, no colours");
        out.println("  --no-color   turn colours off (setting the NO_COLOR environment variable does the same)");
        out.println("  -h, --help   show this help and exit");
        out.println();
        out.println("At any menu, q, quit or exit work the same as [0].");
    }

    // Starting products so every feature can be tried right away.
    private static void loadSampleProducts(Inventory inventory) {
        inventory.addProduct(new Product("IC003", "Chocolate", 'M', 40, 65.00, "Cup"));
        inventory.addProduct(new Product("IC001", "Vanilla", 'S', 50, 45.00, "Cone"));
        inventory.addProduct(new Product("IC005", "Strawberry", 'L', 15, 85.00, "Cup"));
        inventory.addProduct(new Product("IC002", "Mango", 'M', 25, 60.00, "Cone"));
        inventory.addProduct(new Product("IC004", "Cookies and Cream", 'L', 30, 90.00, "Cup"));
    }
}
