import java.util.ArrayList;

/**
 * Base class for the three roles.
 *
 * - showMenu() runs the menu loop for every role, so the loop and the cancel handling
 *   are written only once. Each role only says what its menu looks like (printMenu)
 *   and what each option does (handleChoice).
 * - View, search and sort are shared by every role, so they are written once here.
 */
public abstract class User {
    protected final Inventory inventory;

    public User(Inventory inventory) {
        this.inventory = inventory;
    }

    protected abstract String getRoleName();

    protected abstract void printMenu();

    /** Number of the highest menu option ([0] is always Log out). */
    protected abstract int getLastOption();

    protected abstract void handleChoice(int choice);

    public void showMenu() {
        while (true) {
            System.out.println();
            printMenu();
            int choice = InputHelper.readMenuChoice(getLastOption());
            if (choice == 0) {
                System.out.println(getRoleName() + " logged out.");
                return;
            }
            try {
                handleChoice(choice);
            } catch (OperationCancelledException e) {
                // The user typed C somewhere in the operation; nothing was saved.
                System.out.println("Operation cancelled. Returning to " + getRoleName() + " Menu...");
            }
        }
    }

    /** Shows every product, listed by product ID (sorted with Quick Sort). */
    public void viewInventory() {
        System.out.println("\n=== PRODUCT INVENTORY (by product ID) ===");
        ArrayList<Product> products = inventory.getProducts();
        SortAlgorithms.quickSort(products, SortAlgorithms.BY_ID);
        Inventory.displayProducts(products);
    }

    public void searchProducts() {
        if (inventory.isEmpty()) {
            System.out.println("The inventory is empty. Nothing to search.");
            return;
        }
        System.out.println("\nSearch by:");
        System.out.println("[1] Product ID (Binary Search)");
        System.out.println("[2] Flavor keyword (Linear Search)");
        int choice = InputHelper.readInt("Choice (or C to cancel): ", 1, 2);

        if (choice == 1) {
            String id = InputHelper.readProductId("Enter product ID (or C to cancel): ");
            Product found = SearchAlgorithms.binarySearchById(inventory.getProducts(), id);
            if (found == null) {
                System.out.println("No product found with ID " + id + ".");
            } else {
                ArrayList<Product> result = new ArrayList<>();
                result.add(found);
                Inventory.displayProducts(result);
            }
        } else {
            String keyword = InputHelper.readText("Enter flavor keyword (or C to cancel): ");
            ArrayList<Product> matches = SearchAlgorithms.linearSearchByFlavor(inventory.getProducts(), keyword);
            if (matches.isEmpty()) {
                System.out.println("No product flavor contains \"" + keyword + "\".");
            } else {
                System.out.println(matches.size() + " match(es) found:");
                Inventory.displayProducts(matches);
            }
        }
    }

    /**
     * Sorts a copy of the inventory by price (lowest first) using Insertion Sort.
     * No algorithm menu is shown. Insertion Sort is stable, so products with the same
     * price keep their original order.
     */
    public void sortProducts() {
        if (inventory.isEmpty()) {
            System.out.println("The inventory is empty. Nothing to sort.");
            return;
        }
        ArrayList<Product> sorted = inventory.getProducts();
        SortAlgorithms.insertionSort(sorted, SortAlgorithms.BY_PRICE);
        System.out.println("\nSorting complete: products sorted by price (lowest to highest) using Insertion Sort.");
        Inventory.displayProducts(sorted);
    }
}
