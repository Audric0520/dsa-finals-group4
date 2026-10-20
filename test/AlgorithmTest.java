import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Plain-Java checks for the sorting, searching and data structure logic (no JUnit needed).
 * Prints PASS/FAIL for each check and a summary at the end.
 */
public class AlgorithmTest {
    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        testSorting();
        testEmptyAndSingleItemSorting();
        testLinearSearch();
        testBinarySearch();
        testInventory();
        testUndoStack();
        testQueueIsFifo();

        System.out.println("\n" + passed + " passed, " + failed + " failed.");
        if (failed > 0) {
            System.exit(1);
        }
    }

    private static ArrayList<IceCream> sampleProducts() {
        ArrayList<IceCream> list = new ArrayList<>();
        list.add(new IceCream("IC003", "Chocolate", 'M', 40, 65.00, "Cup"));
        list.add(new IceCream("IC001", "Vanilla", 'S', 50, 45.00, "Cone"));
        list.add(new IceCream("IC005", "Strawberry", 'L', 15, 85.00, "Cup"));
        list.add(new IceCream("IC002", "Mango", 'M', 25, 60.00, "Cone"));
        list.add(new IceCream("IC004", "Cookies and Cream", 'L', 30, 90.00, "Cup"));
        return list;
    }

    private static String ids(ArrayList<IceCream> list) {
        StringBuilder builder = new StringBuilder();
        for (IceCream product : list) {
            builder.append(product.getProductId()).append(' ');
        }
        return builder.toString().trim();
    }

    private static void check(String name, boolean condition) {
        if (condition) {
            passed++;
            System.out.println("PASS  " + name);
        } else {
            failed++;
            System.out.println("FAIL  " + name);
        }
    }

    private static void checkAllSorts(String keyName, Comparator<IceCream> comparator, String expected) {
        ArrayList<IceCream> merge = sampleProducts();
        SortAlgorithms.mergeSort(merge, comparator);
        check("Merge Sort by " + keyName + " -> " + ids(merge), ids(merge).equals(expected));

        ArrayList<IceCream> quick = sampleProducts();
        SortAlgorithms.quickSort(quick, comparator);
        check("Quick Sort by " + keyName + " -> " + ids(quick), ids(quick).equals(expected));

        ArrayList<IceCream> insertion = sampleProducts();
        SortAlgorithms.insertionSort(insertion, comparator);
        check("Insertion Sort by " + keyName + " -> " + ids(insertion), ids(insertion).equals(expected));
    }

    private static void testSorting() {
        checkAllSorts("ID", SortAlgorithms.BY_ID, "IC001 IC002 IC003 IC004 IC005");
        checkAllSorts("flavor", SortAlgorithms.BY_FLAVOR, "IC003 IC004 IC002 IC005 IC001");
        checkAllSorts("price", SortAlgorithms.BY_PRICE, "IC001 IC002 IC003 IC005 IC004");
        checkAllSorts("quantity", SortAlgorithms.BY_QUANTITY, "IC005 IC002 IC004 IC003 IC001");
    }

    private static void testEmptyAndSingleItemSorting() {
        ArrayList<IceCream> empty = new ArrayList<>();
        SortAlgorithms.mergeSort(empty, SortAlgorithms.BY_ID);
        SortAlgorithms.quickSort(empty, SortAlgorithms.BY_ID);
        SortAlgorithms.insertionSort(empty, SortAlgorithms.BY_ID);
        check("All sorts handle an empty list", empty.isEmpty());

        ArrayList<IceCream> single = new ArrayList<>();
        single.add(new IceCream("IC009", "Ube", 'S', 1, 30.00, "Cone"));
        SortAlgorithms.mergeSort(single, SortAlgorithms.BY_PRICE);
        SortAlgorithms.quickSort(single, SortAlgorithms.BY_PRICE);
        SortAlgorithms.insertionSort(single, SortAlgorithms.BY_PRICE);
        check("All sorts handle a single-item list", ids(single).equals("IC009"));
    }

    private static void testLinearSearch() {
        ArrayList<IceCream> list = sampleProducts();
        check("Linear search 'co' finds Chocolate and Cookies and Cream",
                ids(SearchAlgorithms.linearSearchByFlavor(list, "co")).equals("IC003 IC004"));
        check("Linear search is case-insensitive",
                ids(SearchAlgorithms.linearSearchByFlavor(list, "MANGO")).equals("IC002"));
        check("Linear search with no match returns empty list",
                SearchAlgorithms.linearSearchByFlavor(list, "Pistachio").isEmpty());
        check("Linear search on empty list returns empty list",
                SearchAlgorithms.linearSearchByFlavor(new ArrayList<>(), "a").isEmpty());
    }

    private static void testBinarySearch() {
        ArrayList<IceCream> list = sampleProducts(); // deliberately unsorted
        boolean allFound = true;
        for (String id : new String[] { "IC001", "IC002", "IC003", "IC004", "IC005" }) {
            IceCream found = SearchAlgorithms.binarySearchById(list, id);
            if (found == null || !found.getProductId().equals(id)) {
                allFound = false;
            }
        }
        check("Binary search finds every ID in an unsorted inventory", allFound);
        check("Binary search returns null for missing ID",
                SearchAlgorithms.binarySearchById(list, "IC999") == null);
        check("Binary search on empty list returns null",
                SearchAlgorithms.binarySearchById(new ArrayList<>(), "IC001") == null);
        check("Binary search does not reorder the inventory",
                ids(list).equals("IC003 IC001 IC005 IC002 IC004"));
    }

    private static void testInventory() {
        Inventory inventory = new Inventory();
        check("New inventory is empty", inventory.isEmpty());
        check("Add product", inventory.addProduct(new IceCream("IC001", "Vanilla", 'S', 10, 45.0, "Cone")));
        check("Duplicate ID is rejected",
                !inventory.addProduct(new IceCream("IC001", "Other", 'M', 5, 50.0, "Cup")));
        check("HashMap lookup finds product", inventory.findById("IC001").getFlavor().equals("Vanilla"));
        check("Lookup of missing ID returns null", inventory.findById("IC404") == null);
        check("Adjust stock +5", inventory.adjustStock("IC001", 5) && inventory.findById("IC001").getQuantity() == 15);
        check("Stock cannot go negative", !inventory.adjustStock("IC001", -16));
        check("Stock cannot exceed max", !inventory.adjustStock("IC001", Inventory.MAX_STOCK));
        check("Adjust stock on missing ID fails", !inventory.adjustStock("IC404", 1));
        check("Remove missing ID returns null", inventory.removeProduct("IC404") == null);
        check("Remove existing product", inventory.removeProduct("IC001") != null && inventory.isEmpty());
    }

    private static void testUndoStack() {
        Inventory inventory = new Inventory();
        inventory.addProduct(new IceCream("IC001", "Vanilla", 'S', 10, 45.0, "Cone"));
        inventory.addProduct(new IceCream("IC002", "Mango", 'M', 10, 60.0, "Cone"));
        inventory.removeProduct("IC001");
        inventory.removeProduct("IC002");
        // ArrayDeque stack: last removed (IC002) comes back first
        check("Undo restores the most recent removal first (LIFO)",
                inventory.undoLastRemoval().getProductId().equals("IC002"));
        check("Second undo restores the earlier removal",
                inventory.undoLastRemoval().getProductId().equals("IC001"));
        check("Undo stack is empty afterwards", !inventory.hasRemovedProducts());

        inventory.removeProduct("IC001");
        inventory.addProduct(new IceCream("IC001", "New Vanilla", 'L', 3, 70.0, "Cup"));
        check("Undo is refused when the ID was reused", inventory.undoLastRemoval() == null);
    }

    private static void testQueueIsFifo() {
        // Same queue type and operations used by IceCreamMaker
        IceCream product = new IceCream("IC001", "Vanilla", 'S', 10, 45.0, "Cone");
        Queue<Order> queue = new LinkedList<>();
        queue.offer(new Order(1, product, 1));
        queue.offer(new Order(2, product, 2));
        queue.offer(new Order(3, product, 3));
        boolean fifo = queue.poll().getOrderNumber() == 1
                && queue.poll().getOrderNumber() == 2
                && queue.poll().getOrderNumber() == 3;
        check("Queue processes orders first in, first out", fifo);
        check("Polling an empty queue returns null instead of crashing", queue.poll() == null);
        check("Order total = price x quantity", new Order(4, product, 3).getTotal() == 135.0);

        ArrayDeque<String> stack = new ArrayDeque<>();
        stack.push("first");
        stack.push("second");
        check("ArrayDeque push/pop behaves as a stack", stack.pop().equals("second"));
    }
}
