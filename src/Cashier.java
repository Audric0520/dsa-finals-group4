/**
 * Cashier: can only view, search and sort the inventory.
 */
public class Cashier extends User {

    public Cashier(Inventory inventory) {
        super(inventory);
    }

    @Override
    public void showMenu() {
        boolean running = true;
        while (running) {
            System.out.println("\n===== CASHIER MENU =====");
            System.out.println("[1] View inventory");
            System.out.println("[2] Search products");
            System.out.println("[3] Sort products");
            System.out.println("[0] Log out");
            int choice = InputHelper.readInt("Choice: ", 0, 3);

            switch (choice) {
                case 1:
                    viewInventory();
                    break;
                case 2:
                    searchProducts();
                    break;
                case 3:
                    sortProducts();
                    break;
                default:
                    running = false;
            }
        }
        System.out.println("Cashier logged out.");
    }
}
