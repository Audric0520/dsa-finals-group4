import java.util.ArrayList;
import java.util.Comparator;

/**
 * Base class for the three roles. View, search and sort are shared by every role,
 * so they are written once here. Each role defines its own menu.
 */
public abstract class User {
    protected final Inventory inventory;

    public User(Inventory inventory) {
        this.inventory = inventory;
    }

    public abstract void showMenu();

    public void viewInventory() {
        System.out.println("\n=== PRODUCT INVENTORY ===");
        Inventory.displayProducts(inventory.getProducts());
    }

    public void searchProducts() {
        if (inventory.isEmpty()) {
            System.out.println("The inventory is empty. Nothing to search.");
            return;
        }
        System.out.println("\nSearch by:");
        System.out.println("[1] Product ID (Binary Search)");
        System.out.println("[2] Flavor keyword (Linear Search)");
        int choice = InputHelper.readInt("Choice: ", 1, 2);

        if (choice == 1) {
            String id = InputHelper.readProductId("Enter product ID: ");
            IceCream found = SearchAlgorithms.binarySearchById(inventory.getProducts(), id);
            if (found == null) {
                System.out.println("No product found with ID " + id + ".");
            } else {
                ArrayList<IceCream> result = new ArrayList<>();
                result.add(found);
                Inventory.displayProducts(result);
            }
        } else {
            String keyword = InputHelper.readNonEmpty("Enter flavor keyword: ");
            ArrayList<IceCream> matches = SearchAlgorithms.linearSearchByFlavor(inventory.getProducts(), keyword);
            if (matches.isEmpty()) {
                System.out.println("No product flavor contains \"" + keyword + "\".");
            } else {
                System.out.println(matches.size() + " match(es) found:");
                Inventory.displayProducts(matches);
            }
        }
    }

    /** Sorts a copy of the inventory, so the stored order of products is not changed. */
    public void sortProducts() {
        if (inventory.isEmpty()) {
            System.out.println("The inventory is empty. Nothing to sort.");
            return;
        }
        System.out.println("\nSort by:");
        System.out.println("[1] Product ID");
        System.out.println("[2] Flavor (A-Z)");
        System.out.println("[3] Price (lowest first)");
        System.out.println("[4] Stock quantity (lowest first)");
        int keyChoice = InputHelper.readInt("Choice: ", 1, 4);
        Comparator<IceCream> comparator;
        switch (keyChoice) {
            case 1:
                comparator = SortAlgorithms.BY_ID;
                break;
            case 2:
                comparator = SortAlgorithms.BY_FLAVOR;
                break;
            case 3:
                comparator = SortAlgorithms.BY_PRICE;
                break;
            default:
                comparator = SortAlgorithms.BY_QUANTITY;
        }

        System.out.println("\nSorting algorithm:");
        System.out.println("[1] Merge Sort");
        System.out.println("[2] Quick Sort");
        System.out.println("[3] Insertion Sort");
        int algorithmChoice = InputHelper.readInt("Choice: ", 1, 3);

        ArrayList<IceCream> sorted = inventory.getProducts();
        if (algorithmChoice == 1) {
            SortAlgorithms.mergeSort(sorted, comparator);
            System.out.println("Sorted with Merge Sort:");
        } else if (algorithmChoice == 2) {
            SortAlgorithms.quickSort(sorted, comparator);
            System.out.println("Sorted with Quick Sort:");
        } else {
            SortAlgorithms.insertionSort(sorted, comparator);
            System.out.println("Sorted with Insertion Sort:");
        }
        Inventory.displayProducts(sorted);
    }
}
