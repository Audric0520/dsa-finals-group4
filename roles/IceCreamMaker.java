package roles;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import console.InputHelper;
import console.Terminal;
import models.Inventory;
import models.Order;
import models.OrderItem;
import models.Product;

/**
 * IceCreamMaker: views, searches and sorts the inventory, and owns the queue of
 * pending orders. Orders are processed FIFO (first in, first out).
 *
 * Stock policy: the inventory is only updated when an order is processed.
 * Placing an order does not change the stock, but it is only accepted if the stock
 * left after all earlier pending orders is enough, so the queue never promises more
 * than what is in stock. Stock is checked again just before processing, in case the
 * Manager changed it in the meantime.
 */
public class IceCreamMaker extends User {
    private final Queue<Order> pendingOrders = new LinkedList<>();
    private int nextOrderNumber = 1;

    public IceCreamMaker(Inventory inventory) {
        super(inventory);
    }

    @Override
    protected String getRoleName() {
        return Authentication.ICE_CREAM_MAKER;
    }

    @Override
    protected void printMenu() {
        Terminal.title("========== ICE CREAM MAKER MENU ==========");
        System.out.println("[1] View inventory");
        System.out.println("[2] Search products");
        System.out.println("[3] Sort products");
        System.out.println("[4] View pending orders");
        System.out.println("[5] Process next order");
        System.out.println("[0] Log out");
    }

    @Override
    protected int getLastOption() {
        return 5;
    }

    @Override
    protected IceCreamMaker getOrderQueue() {
        return this;
    }

    @Override
    protected String getStatusLine() {
        Order next = pendingOrders.peek();
        return "Pending orders: " + pendingOrders.size() + " | Next: " + (next == null ? "none" : next.getOrderId());
    }

    @Override
    protected void handleChoice(int choice) {
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
            case 4:
                displayPendingOrders();
                break;
            case 5:
                processNextOrderFromMenu();
                break;
            default:
                break;
        }
    }

    // ===================== Queue operations (also used by the Cashier) =====================

    /**
     * Adds a new order to the back of the queue. The inventory is NOT changed here.
     * Returns the new order, or null (and adds nothing) if the items are empty or the
     * stock not already promised to pending orders is not enough.
     */
    public Order placeOrder(ArrayList<OrderItem> items) {
        if (items.isEmpty()) {
            return null;
        }
        for (OrderItem item : items) {
            Product product = item.getProduct();
            if (item.getQuantity() <= 0 || inventory.findById(product.getProductId()) != product
                    || totalQuantity(items, product) > getAvailableQuantity(product)) {
                return null;
            }
        }
        Order order = new Order(String.format("ORD-%03d", nextOrderNumber++), items);
        pendingOrders.offer(order);
        return order;
    }

    /** Total quantity of one product across a list of items (a product may appear more than once). */
    private static int totalQuantity(ArrayList<OrderItem> items, Product product) {
        int total = 0;
        for (OrderItem item : items) {
            if (item.getProduct() == product) {
                total += item.getQuantity();
            }
        }
        return total;
    }

    /** Quantity of a product that is already promised to orders waiting in the queue. */
    public int getPendingQuantity(Product product) {
        int total = 0;
        for (Order order : pendingOrders) {
            total += totalQuantity(order.getItems(), product);
        }
        return total;
    }

    /** Stock that new orders can still use: current stock minus what pending orders need. */
    public int getAvailableQuantity(Product product) {
        return product.getQuantity() - getPendingQuantity(product);
    }

    /** True if every product in the order is still in the inventory with enough stock. */
    public boolean hasStockToProcess(Order order) {
        ArrayList<OrderItem> items = order.getItems();
        for (OrderItem item : items) {
            Product product = item.getProduct();
            if (inventory.findById(product.getProductId()) != product
                    || totalQuantity(items, product) > product.getQuantity()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Processes the oldest order (FIFO): subtracts its quantities from the inventory and
     * removes it from the queue. Returns null and changes nothing if the queue is empty
     * or there is not enough stock, so the order is never lost.
     */
    public Order completeNextOrder() {
        Order order = pendingOrders.peek();
        if (order == null || !hasStockToProcess(order)) {
            return null;
        }
        for (OrderItem item : order.getItems()) {
            inventory.adjustStock(item.getProduct().getProductId(), -item.getQuantity());
        }
        return pendingOrders.poll();
    }

    /**
     * IDs of the pending orders that contain the product, e.g. "ORD-001, ORD-003", or "" if none.
     * Used by the Manager, who cannot remove such a product or change what it is.
     */
    public String getPendingOrderIdsFor(String productId) {
        StringBuilder ids = new StringBuilder();
        for (Order order : pendingOrders) {
            if (order.containsProduct(productId)) {
                if (ids.length() > 0) {
                    ids.append(", ");
                }
                ids.append(order.getOrderId());
            }
        }
        return ids.toString();
    }

    public ArrayList<Order> getPendingOrders() {
        return new ArrayList<>(pendingOrders);
    }

    public void displayPendingOrders() {
        System.out.println();
        Terminal.title("--- Pending Orders (first = next to process) ---");
        if (pendingOrders.isEmpty()) {
            System.out.println("No pending orders.");
            return;
        }
        int position = 1;
        for (Order order : pendingOrders) {
            System.out.println(position + ". " + order);
            position++;
        }
    }

    // ===================== Menu actions =====================

    private void processNextOrderFromMenu() {
        Order next = pendingOrders.peek(); // look at the oldest order without removing it
        if (next == null) {
            Terminal.warning("No pending orders to process.");
            return;
        }
        System.out.println("\nNext order: " + next);
        if (!hasStockToProcess(next)) {
            Terminal.error("Cannot process " + next.getOrderId() + ": not enough stock for this order.");
            ArrayList<OrderItem> items = next.getItems();
            for (OrderItem item : items) {
                Product product = item.getProduct();
                int needed = totalQuantity(items, product);
                if (needed > product.getQuantity()) {
                    Terminal.error("    - " + product.getProductId() + " " + product.getFlavor() + ": needs "
                            + needed + ", only " + product.getQuantity() + " in stock");
                }
            }
            Terminal.error("The order stays at the front of the queue. Ask the Manager to restock first.");
            return;
        }
        if (!InputHelper.readYesNo("Process this order?", true)) {
            Terminal.warning("Order " + next.getOrderId() + " was left in the queue.");
            return;
        }
        completeNextOrder();
        Terminal.success("Order " + next.getOrderId() + " processed successfully. Inventory updated:");
        for (OrderItem item : next.getItems()) {
            Product product = item.getProduct();
            System.out.println("    - " + product.getProductId() + " " + product.getFlavor() + ": -"
                    + item.getQuantity() + ", stock now " + product.getQuantity());
        }
        System.out.println("Orders still waiting: " + pendingOrders.size());
    }
}
