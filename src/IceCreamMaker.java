import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

/**
 * IceCreamMaker: views, searches and sorts the inventory, and owns the queue of
 * pending orders. Orders are processed FIFO (first in, first out).
 *
 * Stock policy: stock is reserved (subtracted) when the Cashier places an order, so
 * pending orders can never ask for more than what is in stock. Processing an order
 * just completes it. Cancelling an order gives its reserved stock back.
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
        System.out.println("[5] Cancel an order");
        System.out.println("[6] Process next order");
        System.out.println("[0] Log out");
    }

    @Override
    protected int getLastOption() {
        return 6;
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
                cancelOrderFromMenu();
                break;
            case 6:
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

    /**
     * Removes only the order with the given ID and gives its reserved stock back.
     * The queue is rotated once: every order is taken from the front and put back at the
     * end, except the one being cancelled, so the remaining orders keep their FIFO order.
     * Returns the cancelled order, or null if no pending order has that ID.
     */
    public Order cancelPendingOrder(String orderId) {
        Order cancelled = null;
        int size = pendingOrders.size();
        for (int i = 0; i < size; i++) {
            Order order = pendingOrders.poll();
            if (cancelled == null && order.getOrderId().equals(orderId)) {
                cancelled = order;
            } else {
                pendingOrders.offer(order);
            }
        }
        if (cancelled != null) {
            for (OrderItem item : cancelled.getItems()) {
                inventory.adjustStock(item.getProduct().getProductId(), item.getQuantity());
            }
        }
        return cancelled;
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

    private void cancelOrderFromMenu() {
        if (pendingOrders.isEmpty()) {
            System.out.println("No pending orders to cancel.");
            return;
        }
        displayPendingOrders();
        String orderId = InputHelper.readText("Enter order ID to cancel, e.g. ORD-001 (or C to cancel): ")
                .toUpperCase();
        Order cancelled = cancelPendingOrder(orderId);
        if (cancelled == null) {
            System.out.println("No pending order with ID " + orderId + ". Nothing was cancelled.");
        } else {
            System.out.println("Order " + orderId + " cancelled. Reserved stock was returned to the inventory.");
        }
    }
}
