package roles;

import java.util.ArrayList;
import java.util.Comparator;

import algorithms.SearchAlgorithms;
import algorithms.SortAlgorithms;
import console.InputHelper;
import console.OperationCancelledException;
import console.Terminal;
import models.Inventory;
import models.Product;

/**
 * Base class for the three roles.
 *
 * - showMenu() runs the menu loop for every role, so the loop and the cancel
 * handling
 * are written only once. Each role only says what its menu looks like
 * (printMenu)
 * and what each option does (handleChoice).
 * - View, search and sort are shared by every role, so they are written once
 * here.
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

    /** The queue of pending orders, which the IceCreamMaker keeps. */
    protected abstract IceCreamMaker getOrderQueue();

    public void showMenu() {
        while (true) {
            Terminal.newScreen();
            printMenu();
            System.out.println();
            System.out.println(getStatusLine());
            int choice = InputHelper.readMenuChoice(getLastOption());
            if (choice == 0) {
                Terminal.notice(getRoleName() + " logged out.");
                return;
            }
            try {
                handleChoice(choice);
                Terminal.pause();
            } catch (OperationCancelledException e) {
                // The user typed C somewhere in the operation; nothing was saved.
                Terminal.notice(e.getMessage() + " Returning to " + getRoleName() + " Menu...");
            }
        }
    }

    /**
     * Shows every product, listed by product ID (sorted with Quick Sort).
     * Stock is only subtracted when an order is processed, so the table alone would show
     * stock that pending orders have already claimed. The note under it shows what is
     * really left for new orders.
     */
    public void viewInventory() {
        ArrayList<Product> products = inventory.getProducts();
        SortAlgorithms.quickSort(products, SortAlgorithms.BY_ID);
        Inventory.displayProducts("PRODUCT INVENTORY (by product ID)", products);

        boolean first = true;
        for (Product product : products) {
            int reserved = getOrderQueue().getPendingQuantity(product);
            if (reserved > 0) {
                if (first) {
                    System.out.println("Reserved for pending orders (subtracted when each order is processed):");
                    first = false;
                }
                System.out.printf("  %s %s: %d reserved, %d left for new orders%n", product.getProductId(),
                        product.getFlavor(), reserved, Math.max(0, product.getQuantity() - reserved));
            }
        }
    }

    public void searchProducts() {
        if (inventory.isEmpty()) {
            System.out.println("The inventory is empty. Nothing to search.");
            return;
        }
        System.out.println("\nSearch by:");
        System.out.println("[1] Product ID");
        System.out.println("[2] Flavor keyword");
        int choice = InputHelper.readInt("Choice [1-2] (or C to cancel): ", 1, 2);

        if (choice == 1) {
            String id = InputHelper.readProductId("Enter product ID (or C to cancel): ");
            Product found = SearchAlgorithms.binarySearchById(inventory.getProducts(), id);
            if (found == null) {
                System.out.println("No product found with ID " + id + ".");
            } else {
                ArrayList<Product> result = new ArrayList<>();
                result.add(found);
                Inventory.displayProducts("SEARCH RESULT (product ID " + id + ")", result);
            }
        } else {
            String keyword = InputHelper.readText("Enter flavor keyword (or C to cancel): ");
            ArrayList<Product> matches = SearchAlgorithms.linearSearchByFlavor(inventory.getProducts(), keyword);
            if (matches.isEmpty()) {
                System.out.println("No product flavor contains \"" + keyword + "\".");
            } else {
                Inventory.displayProducts("SEARCH RESULTS (flavor contains \"" + keyword + "\") - "
                        + matches.size() + " found", matches);
            }
        }
    }

    /**
     * Asks what to sort by (quantity or price) and in which order, then sorts a
     * copy
     * of the inventory with Insertion Sort. Insertion Sort is stable, so products
     * with
     * the same value keep their original order.
     */
    public void sortProducts() {
        if (inventory.isEmpty()) {
            System.out.println("The inventory is empty. Nothing to sort.");
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
        System.out.println("\nSorting complete: products sorted by " + key + " (" + order + ")");
        Inventory.displayProducts("SORTED PRODUCTS (" + key + ", " + order + ")", sorted);
    }
}
