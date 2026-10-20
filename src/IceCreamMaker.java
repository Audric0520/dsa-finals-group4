import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

/**
 * IceCreamMaker: can view, search and sort the inventory, and handles the
 * pending order queue in FIFO (first in, first out) order.
 *
 * Stock is reserved (subtracted) when an order is added to the queue, so two
 * orders can never use the same stock. Canceling an order gives the stock back.
 */
public class IceCreamMaker extends User {
    private final Queue<Order> pendingOrders = new LinkedList<>();
    private int nextOrderNumber = 1;

    public IceCreamMaker(Inventory inventory) {
        super(inventory);
    }

    @Override
    public void showMenu() {
        boolean running = true;
        while (running) {
            System.out.println("\n===== ICE CREAM MAKER MENU =====");
            System.out.println("[1] View inventory");
            System.out.println("[2] Search products");
            System.out.println("[3] Sort products");
            System.out.println("[4] Add new order to queue");
            System.out.println("[5] View pending orders");
            System.out.println("[6] Process next order");
            System.out.println("[7] Cancel an order");
            System.out.println("[0] Log out");
            int choice = InputHelper.readInt("Choice: ", 0, 7);

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
                    addOrder();
                    break;
                case 5:
                    viewPendingOrders();
                    break;
                case 6:
                    processNextOrder();
                    break;
                case 7:
                    cancelOrder();
                    break;
                default:
                    running = false;
            }
        }
        System.out.println("Ice Cream Maker logged out.");
    }

    private void addOrder() {
        System.out.println("\n--- Add Order ---");
        if (inventory.isEmpty()) {
            System.out.println("The inventory is empty. No orders can be made.");
            return;
        }
        String id = InputHelper.readProductId("Product ID: ");
        IceCream product = inventory.findById(id);
        if (product == null) {
            System.out.println("No product found with ID " + id + ".");
            return;
        }
        if (product.getQuantity() == 0) {
            System.out.println(product.getFlavor() + " is out of stock.");
            return;
        }
        int quantity = InputHelper.readInt("Quantity (1-" + product.getQuantity() + "): ", 1,
                product.getQuantity());

        inventory.adjustStock(id, -quantity); // reserve the stock
        Order order = new Order(nextOrderNumber++, product, quantity);
        pendingOrders.offer(order);
        System.out.println("Added to queue: " + order);
        System.out.println("Orders waiting: " + pendingOrders.size());
    }

    private void viewPendingOrders() {
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

    /** Shows the order at the front of the queue (FIFO) and removes it once confirmed. */
    private void processNextOrder() {
        Order order = pendingOrders.peek(); // look at the oldest order without removing it
        if (order == null) {
            System.out.println("No pending orders to process.");
            return;
        }
        System.out.println("Next order: " + order);
        if (!InputHelper.readYesNo("Process this order? (Y/N): ")) {
            System.out.println("Order left in the queue.");
            return;
        }
        pendingOrders.poll();
        System.out.println("Processed: " + order);
        System.out.println("Orders still waiting: " + pendingOrders.size());
    }

    private void cancelOrder() {
        if (pendingOrders.isEmpty()) {
            System.out.println("No pending orders to cancel.");
            return;
        }
        viewPendingOrders();
        int orderNumber = InputHelper.readInt("Order # to cancel: ", 1, Integer.MAX_VALUE);

        Iterator<Order> iterator = pendingOrders.iterator();
        while (iterator.hasNext()) {
            Order order = iterator.next();
            if (order.getOrderNumber() == orderNumber) {
                iterator.remove();
                returnStock(order);
                System.out.println("Canceled: Order #" + orderNumber);
                return;
            }
        }
        System.out.println("No pending order #" + orderNumber + ".");
    }

    // Gives reserved stock back. If the Manager removed the product meanwhile, there is nothing to return to.
    private void returnStock(Order order) {
        String id = order.getProduct().getProductId();
        if (inventory.findById(id) == order.getProduct() && inventory.adjustStock(id, order.getQuantity())) {
            System.out.println(order.getQuantity() + " returned to stock of " + id + ".");
        } else {
            System.out.println("Stock not returned: product " + id + " is no longer in the inventory"
                    + " or would exceed the stock limit.");
        }
    }
}
