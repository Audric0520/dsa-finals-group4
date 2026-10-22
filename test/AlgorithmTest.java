import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;

import algorithms.SearchAlgorithms;
import algorithms.SortAlgorithms;
import console.InputHelper;
import console.Terminal;
import models.Inventory;
import models.Order;
import models.OrderItem;
import models.Product;
import roles.IceCreamMaker;

/**
 * Plain-Java checks for the sorting, searching, inventory and order-queue logic (no JUnit needed).
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
        testPriceValidation();
        testInventory();
        testOrderQueue();
        testPendingOrderList();
        testProcessingRechecksStock();
        testStack();
        testInputAndTerminal();

        System.out.println("\n" + passed + " passed, " + failed + " failed.");
        if (failed > 0) {
            System.exit(1);
        }
    }

    private static ArrayList<Product> sampleProducts() {
        ArrayList<Product> list = new ArrayList<>();
        list.add(new Product("IC003", "Chocolate", 'M', 40, 65.00, "Cup"));
        list.add(new Product("IC001", "Vanilla", 'S', 50, 45.00, "Cone"));
        list.add(new Product("IC005", "Strawberry", 'L', 15, 85.00, "Cup"));
        list.add(new Product("IC002", "Mango", 'M', 25, 60.00, "Cone"));
        list.add(new Product("IC004", "Cookies and Cream", 'L', 30, 90.00, "Cup"));
        return list;
    }

    private static Inventory sampleInventory() {
        Inventory inventory = new Inventory();
        for (Product product : sampleProducts()) {
            inventory.addProduct(product);
        }
        return inventory;
    }

    private static String ids(ArrayList<Product> list) {
        StringBuilder builder = new StringBuilder();
        for (Product product : list) {
            builder.append(product.getProductId()).append(' ');
        }
        return builder.toString().trim();
    }

    private static String orderIds(ArrayList<Order> orders) {
        StringBuilder builder = new StringBuilder();
        for (Order order : orders) {
            builder.append(order.getOrderId()).append(' ');
        }
        return builder.toString().trim();
    }

    private static ArrayList<OrderItem> items(Inventory inventory, String productId, int quantity) {
        ArrayList<OrderItem> list = new ArrayList<>();
        list.add(new OrderItem(inventory.findById(productId), quantity));
        return list;
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

    private static void checkAllSorts(String keyName, Comparator<Product> comparator, String expected) {
        ArrayList<Product> insertion = sampleProducts();
        SortAlgorithms.insertionSort(insertion, comparator);
        check("Insertion Sort by " + keyName + " -> " + ids(insertion), ids(insertion).equals(expected));

        ArrayList<Product> merge = sampleProducts();
        SortAlgorithms.mergeSort(merge, comparator);
        check("Merge Sort by " + keyName + " -> " + ids(merge), ids(merge).equals(expected));

        ArrayList<Product> quick = sampleProducts();
        SortAlgorithms.quickSort(quick, comparator);
        check("Quick Sort by " + keyName + " -> " + ids(quick), ids(quick).equals(expected));
    }

    private static void testSorting() {
        checkAllSorts("price", SortAlgorithms.BY_PRICE, "IC001 IC002 IC003 IC005 IC004");
        checkAllSorts("ID", SortAlgorithms.BY_ID, "IC001 IC002 IC003 IC004 IC005");
        checkAllSorts("quantity", SortAlgorithms.BY_QUANTITY, "IC005 IC002 IC004 IC003 IC001");

        // The Sort products menu options: Insertion Sort by quantity or price, ascending or descending
        ArrayList<Product> quantityDesc = sampleProducts();
        SortAlgorithms.insertionSort(quantityDesc, SortAlgorithms.BY_QUANTITY.reversed());
        check("Insertion Sort by quantity, descending -> " + ids(quantityDesc),
                ids(quantityDesc).equals("IC001 IC003 IC004 IC002 IC005"));
        ArrayList<Product> priceDesc = sampleProducts();
        SortAlgorithms.insertionSort(priceDesc, SortAlgorithms.BY_PRICE.reversed());
        check("Insertion Sort by price, descending -> " + ids(priceDesc),
                ids(priceDesc).equals("IC004 IC005 IC003 IC002 IC001"));

        ArrayList<Product> ties = new ArrayList<>();
        ties.add(new Product("IC009", "Ube", 'S', 1, 50.00, "Cone"));
        ties.add(new Product("IC007", "Mocha", 'S', 1, 40.00, "Cone"));
        ties.add(new Product("IC008", "Avocado", 'S', 1, 50.00, "Cup"));
        SortAlgorithms.insertionSort(ties, SortAlgorithms.BY_PRICE);
        check("Insertion Sort keeps equal prices in original order (stable)",
                ids(ties).equals("IC007 IC009 IC008"));
    }

    private static void testEmptyAndSingleItemSorting() {
        ArrayList<Product> empty = new ArrayList<>();
        SortAlgorithms.insertionSort(empty, SortAlgorithms.BY_PRICE);
        SortAlgorithms.mergeSort(empty, SortAlgorithms.BY_ID);
        SortAlgorithms.quickSort(empty, SortAlgorithms.BY_ID);
        check("All sorts handle an empty list", empty.isEmpty());

        ArrayList<Product> single = new ArrayList<>();
        single.add(new Product("IC009", "Ube", 'S', 1, 30.00, "Cone"));
        SortAlgorithms.insertionSort(single, SortAlgorithms.BY_PRICE);
        SortAlgorithms.mergeSort(single, SortAlgorithms.BY_PRICE);
        SortAlgorithms.quickSort(single, SortAlgorithms.BY_PRICE);
        check("All sorts handle a single-item list", ids(single).equals("IC009"));
    }

    private static void testLinearSearch() {
        ArrayList<Product> list = sampleProducts();
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
        ArrayList<Product> list = sampleProducts(); // deliberately unsorted
        boolean allFound = true;
        for (String id : new String[] { "IC001", "IC002", "IC003", "IC004", "IC005" }) {
            Product found = SearchAlgorithms.binarySearchById(list, id);
            if (found == null || !found.getProductId().equals(id)) {
                allFound = false;
            }
        }
        check("Binary search finds every ID in an unsorted inventory", allFound);
        check("Binary search returns null for missing ID", SearchAlgorithms.binarySearchById(list, "IC999") == null);
        check("Binary search on empty list returns null",
                SearchAlgorithms.binarySearchById(new ArrayList<>(), "IC001") == null);
        check("Binary search does not reorder the inventory", ids(list).equals("IC003 IC001 IC005 IC002 IC004"));
    }

    private static boolean constructorRejects(double price) {
        try {
            new Product("IC010", "Test", 'S', 1, price, "Cup");
            return false;
        } catch (IllegalArgumentException e) {
            return true;
        }
    }

    private static void testPriceValidation() {
        check("Price 30.00 (minimum) is valid", Product.isValidPrice(30.00));
        check("Price 120.00 (maximum) is valid", Product.isValidPrice(120.00));
        check("Price 29.99 is invalid", !Product.isValidPrice(29.99));
        check("Price 120.01 is invalid", !Product.isValidPrice(120.01));
        check("Product cannot be created with price 25.00", constructorRejects(25.00));
        check("Product cannot be created with price 150.00", constructorRejects(150.00));

        Inventory inventory = sampleInventory();
        check("updatePrice accepts 120.00", inventory.updatePrice("IC001", 120.00)
                && inventory.findById("IC001").getPrice() == 120.00);
        check("updatePrice rejects 10.00 and keeps the old price",
                !inventory.updatePrice("IC001", 10.00) && inventory.findById("IC001").getPrice() == 120.00);
        check("updatePrice rejects 130.00", !inventory.updatePrice("IC001", 130.00));
        check("updatePrice on missing ID fails", !inventory.updatePrice("IC404", 50.00));
    }

    private static void testInventory() {
        Inventory inventory = new Inventory();
        check("New inventory is empty", inventory.isEmpty());
        check("Add product", inventory.addProduct(new Product("IC001", "Vanilla", 'S', 10, 45.0, "Cone")));
        check("Duplicate ID is rejected", !inventory.addProduct(new Product("IC001", "Other", 'M', 5, 50.0, "Cup")));
        check("Negative stock product is rejected",
                !inventory.addProduct(new Product("IC002", "Mango", 'M', -1, 50.0, "Cup")));
        check("New product with quantity 0 is rejected (minimum is 1)",
                !inventory.addProduct(new Product("IC002", "Mango", 'M', 0, 50.0, "Cup")));
        check("New product with quantity 101 is rejected (maximum is 100)",
                !inventory.addProduct(new Product("IC002", "Mango", 'M', 101, 50.0, "Cup")));
        check("New product with quantity 1 is accepted",
                inventory.addProduct(new Product("IC002", "Mango", 'M', 1, 50.0, "Cup")));
        check("New product with quantity 100 is accepted",
                inventory.addProduct(new Product("IC003", "Ube", 'L', 100, 50.0, "Cone")));
        check("updateQuantity allows 0 (sold out) for an existing product", inventory.updateQuantity("IC002", 0));
        check("updateQuantity rejects 101", !inventory.updateQuantity("IC002", 101));
        inventory.removeProduct("IC002");
        inventory.removeProduct("IC003");
        check("HashMap lookup finds product", inventory.findById("IC001").getFlavor().equals("Vanilla"));
        check("Lookup of missing ID returns null", inventory.findById("IC404") == null);
        check("updateQuantity sets stock", inventory.updateQuantity("IC001", 15)
                && inventory.findById("IC001").getQuantity() == 15);
        check("updateQuantity rejects negative", !inventory.updateQuantity("IC001", -1));
        check("Stock cannot go negative", !inventory.adjustStock("IC001", -16));
        check("Remove missing ID returns null", inventory.removeProduct("IC404") == null);
        check("Remove existing product", inventory.removeProduct("IC001") != null && inventory.isEmpty());
    }

    private static void testOrderQueue() {
        Inventory inventory = sampleInventory();
        IceCreamMaker maker = new IceCreamMaker(inventory);

        check("Empty queue: completeNextOrder returns null", maker.completeNextOrder() == null);
        check("Order with no items is rejected", maker.placeOrder(new ArrayList<>()) == null);
        check("Order for more than the stock is rejected and stock unchanged",
                maker.placeOrder(items(inventory, "IC005", 16)) == null
                        && inventory.findById("IC005").getQuantity() == 15);

        ArrayList<OrderItem> twoLines = items(inventory, "IC005", 10);
        twoLines.add(new OrderItem(inventory.findById("IC005"), 6));
        check("Same product twice exceeding stock in total is rejected", maker.placeOrder(twoLines) == null);

        Order first = maker.placeOrder(items(inventory, "IC001", 5));
        ArrayList<OrderItem> multi = items(inventory, "IC003", 2);
        multi.add(new OrderItem(inventory.findById("IC002"), 1));
        Order second = maker.placeOrder(multi);
        Order third = maker.placeOrder(items(inventory, "IC005", 15));

        check("Order IDs are unique and readable", first.getOrderId().equals("ORD-001")
                && second.getOrderId().equals("ORD-002") && third.getOrderId().equals("ORD-003"));
        check("Multi-item order total = 2x65 + 1x60", second.getTotal() == 190.0);
        check("Placing orders does NOT change the inventory", inventory.findById("IC001").getQuantity() == 50
                && inventory.findById("IC003").getQuantity() == 40 && inventory.findById("IC005").getQuantity() == 15);
        check("Available stock subtracts pending orders (IC005: 15 - 15 = 0)",
                maker.getAvailableQuantity(inventory.findById("IC005")) == 0
                        && maker.getPendingQuantity(inventory.findById("IC005")) == 15);
        check("No new order can use stock already promised to pending orders",
                maker.placeOrder(items(inventory, "IC005", 1)) == null);
        check("Product in a pending order is detected", maker.hasPendingOrderFor("IC002"));

        check("FIFO: first processed is ORD-001", maker.completeNextOrder() == first);
        check("Processing ORD-001 subtracts its stock (IC001: 50 -> 45)",
                inventory.findById("IC001").getQuantity() == 45);
        check("FIFO: second processed is ORD-002", maker.completeNextOrder() == second);
        check("Processing a multi-item order subtracts every item (IC003 38, IC002 24)",
                inventory.findById("IC003").getQuantity() == 38 && inventory.findById("IC002").getQuantity() == 24);
        check("FIFO: third processed is ORD-003", maker.completeNextOrder() == third);
        check("IC005 stock is 0 after its order is processed", inventory.findById("IC005").getQuantity() == 0);
        check("Queue is empty after processing all", maker.getPendingOrders().isEmpty());
    }

    private static void testProcessingRechecksStock() {
        Inventory inventory = sampleInventory();
        IceCreamMaker maker = new IceCreamMaker(inventory);
        Order order = maker.placeOrder(items(inventory, "IC002", 10));
        inventory.updateQuantity("IC002", 4); // the Manager lowers the stock after the order was placed

        check("Order cannot be processed when stock dropped below what it needs", !maker.hasStockToProcess(order));
        check("completeNextOrder returns null and keeps the order in the queue",
                maker.completeNextOrder() == null && maker.getPendingOrders().size() == 1);
        check("Stock is unchanged after the failed attempt", inventory.findById("IC002").getQuantity() == 4);

        inventory.updateQuantity("IC002", 30); // the Manager restocks
        check("After restocking the order is processed", maker.completeNextOrder() == order
                && inventory.findById("IC002").getQuantity() == 20);
    }

    private static void testPendingOrderList() {
        Inventory inventory = sampleInventory();
        IceCreamMaker maker = new IceCreamMaker(inventory);
        maker.placeOrder(items(inventory, "IC001", 1));
        maker.placeOrder(items(inventory, "IC002", 4));
        maker.placeOrder(items(inventory, "IC003", 1));

        check("Pending orders are listed in arrival order",
                orderIds(maker.getPendingOrders()).equals("ORD-001 ORD-002 ORD-003"));
        maker.completeNextOrder();
        check("After processing one, the rest keep their order",
                orderIds(maker.getPendingOrders()).equals("ORD-002 ORD-003"));
        check("A new order joins the back of the queue",
                maker.placeOrder(items(inventory, "IC004", 1)) != null
                        && orderIds(maker.getPendingOrders()).equals("ORD-002 ORD-003 ORD-004"));
    }

    private static void testStack() {
        // Same ArrayDeque operations the Cashier uses for "U = undo last item"
        Inventory inventory = sampleInventory();
        ArrayDeque<OrderItem> cart = new ArrayDeque<>();
        cart.push(new OrderItem(inventory.findById("IC001"), 1));
        cart.push(new OrderItem(inventory.findById("IC002"), 2));
        check("ArrayDeque pop() undoes the most recent item (LIFO)",
                cart.pop().getProduct().getProductId().equals("IC002"));
        check("Remaining item is the first one added", cart.peek().getProduct().getProductId().equals("IC001"));
    }

    private static void testInputAndTerminal() {
        // Used by menu choices and by the cashier's one-step "IC001 2" entry
        check("parseIntInRange accepts a number in range", Integer.valueOf(3).equals(InputHelper.parseIntInRange("3", 1, 5)));
        check("parseIntInRange accepts both ends of the range", InputHelper.parseIntInRange("1", 1, 5) != null
                && InputHelper.parseIntInRange("5", 1, 5) != null);
        check("parseIntInRange rejects numbers out of range", InputHelper.parseIntInRange("0", 1, 5) == null
                && InputHelper.parseIntInRange("6", 1, 5) == null);
        check("parseIntInRange rejects text and decimals", InputHelper.parseIntInRange("abc", 1, 5) == null
                && InputHelper.parseIntInRange("2.5", 1, 5) == null && InputHelper.parseIntInRange("", 1, 5) == null);

        // Status lines shown under the role menus
        Inventory inventory = sampleInventory();
        check("Inventory size counts every product", inventory.size() == 5);
        check("No product is out of stock at the start", inventory.countOutOfStock() == 0);
        inventory.updateQuantity("IC002", 0);
        check("A product set to 0 counts as out of stock", inventory.countOutOfStock() == 1);

        // Without Terminal.init (plain mode) no escape codes are added, so saved output stays plain text
        check("Plain mode output has no colour codes",
                Terminal.style("Order added", Terminal.Style.SUCCESS).equals("Order added"));
    }
}
