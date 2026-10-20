/**
 * SC Ice Cream Management System - entry point.
 * One shared Inventory is used by all roles, and each role object is created once,
 * so data (like the pending order queue) is kept while switching roles.
 */
public class Main {

    public static void main(String[] args) {
        Inventory inventory = new Inventory();
        loadSampleProducts(inventory);

        IceCreamMaker iceCreamMaker = new IceCreamMaker(inventory);
        Cashier cashier = new Cashier(inventory, iceCreamMaker);
        Manager manager = new Manager(inventory, iceCreamMaker);

        while (true) {
            System.out.println("\n========================================");
            System.out.println("    SC ICE CREAM MANAGEMENT SYSTEM");
            System.out.println("========================================");
            System.out.println("[1] Manager");
            System.out.println("[2] Cashier");
            System.out.println("[3] Ice Cream Maker");
            System.out.println("[0] Exit");
            int choice = InputHelper.readMenuChoice(3);

            if (choice == 0) {
                break;
            } else if (choice == 1 && Authentication.login(Authentication.MANAGER)) {
                manager.showMenu();
            } else if (choice == 2 && Authentication.login(Authentication.CASHIER)) {
                cashier.showMenu();
            } else if (choice == 3 && Authentication.login(Authentication.ICE_CREAM_MAKER)) {
                iceCreamMaker.showMenu();
            }
        }
        System.out.println("Thank you for using the SC Ice Cream Management System. Goodbye!");
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
