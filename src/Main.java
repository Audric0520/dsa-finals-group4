/**
 * SC Ice Cream Management System - entry point.
 * One shared Inventory is used by all roles, and each role object is created once
 * so data (like the pending order queue) is kept while switching roles.
 */
public class Main {

    public static void main(String[] args) {
        Inventory inventory = new Inventory();
        loadSampleProducts(inventory);

        Manager manager = new Manager(inventory);
        Cashier cashier = new Cashier(inventory);
        IceCreamMaker iceCreamMaker = new IceCreamMaker(inventory);

        boolean running = true;
        while (running) {
            System.out.println("\n========================================");
            System.out.println("    SC ICE CREAM MANAGEMENT SYSTEM");
            System.out.println("========================================");
            System.out.println("[1] Manager");
            System.out.println("[2] Cashier");
            System.out.println("[3] Ice Cream Maker");
            System.out.println("[0] Exit");
            int choice = InputHelper.readInt("Select role: ", 0, 3);

            switch (choice) {
                case 1:
                    manager.showMenu();
                    break;
                case 2:
                    cashier.showMenu();
                    break;
                case 3:
                    iceCreamMaker.showMenu();
                    break;
                default:
                    running = false;
            }
        }
        System.out.println("Thank you for using the SC Ice Cream Management System. Goodbye!");
    }

    // Starting products so every feature can be tried right away.
    private static void loadSampleProducts(Inventory inventory) {
        inventory.addProduct(new IceCream("IC003", "Chocolate", 'M', 40, 65.00, "Cup"));
        inventory.addProduct(new IceCream("IC001", "Vanilla", 'S', 50, 45.00, "Cone"));
        inventory.addProduct(new IceCream("IC005", "Strawberry", 'L', 15, 85.00, "Cup"));
        inventory.addProduct(new IceCream("IC002", "Mango", 'M', 25, 60.00, "Cone"));
        inventory.addProduct(new IceCream("IC004", "Cookies and Cream", 'L', 30, 90.00, "Cup"));
    }
}
