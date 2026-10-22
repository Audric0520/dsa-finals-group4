import java.util.ArrayList;
import java.util.Comparator;

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

    /** One line of live information shown under the menu, e.g. how many orders are waiting. */
    protected abstract String getStatusLine();

    public void showMenu() {
        while (true) {
            Terminal.newScreen();
            printMenu();
            System.out.println();
            Terminal.hint(getStatusLine());
            int choice = InputHelper.readMenuChoice(getLastOption());
            if (choice == 0) {
                Terminal.notice(Terminal.Style.SUCCESS, getRoleName() + " logged out.");
                return;
            }
            try {
                handleChoice(choice);
                Terminal.pause();
            } catch (OperationCancelledException e) {
                // The user typed C somewhere in the operation; nothing was saved.
                Terminal.notice(Terminal.Style.WARNING,
                        "Operation cancelled. Returning to " + getRoleName() + " Menu...");
            }
        }
    }

    /** Shows every product, listed by product ID (sorted with Quick Sort). */
    public void viewInventory() {
        ArrayList<Product> products = inventory.getProducts();
        SortAlgorithms.quickSort(products, SortAlgorithms.BY_ID);
        Inventory.displayProducts("PRODUCT INVENTORY (by product ID)", products);
    }

    public void searchProducts() {
        if (inventory.isEmpty()) {
            Terminal.warning("The inventory is empty. Nothing to search.");
            return;
        }
        System.out.println("\nSearch by:");
        System.out.println("[1] Product ID (Binary Search)");
        System.out.println("[2] Flavor keyword (Linear Search)");
        int choice = InputHelper.readInt("Choice [1-2] (or C to cancel): ", 1, 2);

        if (choice == 1) {
            String id = InputHelper.readProductId("Enter product ID (or C to cancel): ");
            Product found = SearchAlgorithms.binarySearchById(inventory.getProducts(), id);
            if (found == null) {
                Terminal.error("No product found with ID " + id + ".");
            } else {
                ArrayList<Product> result = new ArrayList<>();
                result.add(found);
                Inventory.displayProducts("SEARCH RESULT (product ID " + id + ")", result);
            }
        } else {
            String keyword = InputHelper.readText("Enter flavor keyword (or C to cancel): ");
            ArrayList<Product> matches = SearchAlgorithms.linearSearchByFlavor(inventory.getProducts(), keyword);
            if (matches.isEmpty()) {
                Terminal.error("No product flavor contains \"" + keyword + "\".");
            } else {
                Inventory.displayProducts("SEARCH RESULTS (flavor contains \"" + keyword + "\") - "
                        + matches.size() + " found", matches);
            }
        }
    }

    /**
     * Asks what to sort by (quantity or price) and in which order, then sorts a copy
     * of the inventory with Insertion Sort. Insertion Sort is stable, so products with
     * the same value keep their original order.
     */
    public void sortProducts() {
        if (inventory.isEmpty()) {
            Terminal.warning("The inventory is empty. Nothing to sort.");
            return;
        }
        System.out.println("\nSort products by:");
        System.out.println("[1] Quantity");
        System.out.println("[2] Price");
        boolean byQuantity = InputHelper.readInt("Choice [1-2] (or C to cancel): ", 1, 2) == 1;

        System.out.println("\nSort order:");
        System.out.println("[1] Ascending (lowest to highest)");
        System.out.println("[2] Descending (highest to lowest)");
        boolean ascending = InputHelper.readInt("Choice [1-2] (or C to cancel): ", 1, 2) == 1;

        Comparator<Product> comparator = byQuantity ? SortAlgorithms.BY_QUANTITY : SortAlgorithms.BY_PRICE;
        if (!ascending) {
            comparator = comparator.reversed();
        }
        ArrayList<Product> sorted = inventory.getProducts();
        SortAlgorithms.insertionSort(sorted, comparator);

        String key = byQuantity ? "quantity" : "price";
        String order = ascending ? "lowest to highest" : "highest to lowest";
        System.out.println();
        Terminal.success("Sorting complete: products sorted by " + key + " (" + order + ") using Insertion Sort.");
        Inventory.displayProducts("SORTED PRODUCTS (" + key + ", " + order + ")", sorted);
    }
}
