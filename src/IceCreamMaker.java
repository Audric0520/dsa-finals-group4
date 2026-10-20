import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

/**
 * IceCreamMaker: views, searches and sorts the inventory, and owns the queue of
 * pending orders. Orders are processed FIFO (first in, first out).
 *
 * Stock policy: stock is reserved (subtracted) when the Cashier places an order, so
 * pending orders can never ask for more than what is in stock. Processing an order
 * just completes it.
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
        System.out.println("========== ICE CREAM MAKER MENU ==========");
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
     * Reserves stock for every item and adds a new order to the back of the queue.
     * Returns the new order, or null (and changes nothing) if the items are empty or
     * there is not enough stock for all of them.
     */
    public Order placeOrder(ArrayList<OrderItem> items) {
        if (items.isEmpty() || !hasEnoughStock(items)) {
            return null;
        }
        for (OrderItem item : items) {
            inventory.adjustStock(item.getProduct().getProductId(), -item.getQuantity());
        }
        Order order = new Order(String.format("ORD-%03d", nextOrderNumber++), items);
        pendingOrders.offer(order);
        return order;
    }

    // Checks the total asked for each product (the same product may appear in several items).
    private boolean hasEnoughStock(ArrayList<OrderItem> items) {
        for (OrderItem item : items) {
            Product product = item.getProduct();
            if (item.getQuantity() <= 0 || inventory.findById(product.getProductId()) != product) {
                return false;
            }
            int totalWanted = 0;
            for (OrderItem other : items) {
                if (other.getProduct() == product) {
                    totalWanted += other.getQuantity();
                }
            }
            if (totalWanted > product.getQuantity()) {
                return false;
            }
        }
        return true;
    }

    /** Removes and returns the oldest order (FIFO), or null if the queue is empty. */
    public Order completeNextOrder() {
        return pendingOrders.poll();
    }

    /** Used by the Manager: a product that is part of a pending order cannot be removed. */
    public boolean hasPendingOrderFor(String productId) {
        for (Order order : pendingOrders) {
            if (order.containsProduct(productId)) {
                return true;
            }
        }
        return false;
    }

    public ArrayList<Order> getPendingOrders() {
        return new ArrayList<>(pendingOrders);
    }

    public void displayPendingOrders() {
        System.out.println("\n--- Pending Orders (first = next to process) ---");
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
            System.out.println("No pending orders to process.");
            return;
        }
        System.out.println("\nNext order: " + next);
        if (!InputHelper.readYesNo("Process this order? (Y/N, or C to cancel): ")) {
            System.out.println("Order " + next.getOrderId() + " was left in the queue.");
            return;
        }
        completeNextOrder();
        System.out.println("Order " + next.getOrderId() + " processed successfully.");
        System.out.println("Orders still waiting: " + pendingOrders.size());
    }
}
