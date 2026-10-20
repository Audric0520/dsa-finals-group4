import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Cashier: views, searches and sorts the inventory, enters customer orders and views
 * the pending orders. The orders themselves are stored in the IceCreamMaker's queue.
 */
public class Cashier extends User {
    private final IceCreamMaker iceCreamMaker;

    public Cashier(Inventory inventory, IceCreamMaker iceCreamMaker) {
        super(inventory);
        this.iceCreamMaker = iceCreamMaker;
    }

    @Override
    protected String getRoleName() {
        return Authentication.CASHIER;
    }

    @Override
    protected void printMenu() {
        System.out.println("========== CASHIER MENU ==========");
        System.out.println("[1] View inventory");
        System.out.println("[2] Search products");
        System.out.println("[3] Sort products");
        System.out.println("[4] Add new order to queue");
        System.out.println("[5] View pending orders");
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
                addNewOrder();
                break;
            case 5:
                iceCreamMaker.displayPendingOrders();
                break;
            default:
                break;
        }
    }

    /**
     * Builds one customer order that may contain several products.
     * The items are kept in an ArrayDeque used as a stack, so U (undo) removes the
     * most recently added item (LIFO). Nothing is reserved or queued until D (done),
     * so cancelling with C leaves the stock and the queue unchanged.
     */
    private void addNewOrder() {
        if (inventory.isEmpty()) {
            System.out.println("The inventory is empty. No orders can be made.");
            return;
        }
        System.out.println("\n--- New Customer Order ---");
        System.out.println("Enter a product ID to add it. D = done, U = undo last item, C = cancel order.");
        ArrayDeque<OrderItem> cart = new ArrayDeque<>();

        while (true) {
            String input = InputHelper.readText("Product ID (or D / U / C): ").toUpperCase();
            if (input.equals("D")) {
                if (cart.isEmpty()) {
                    System.out.println("The order has no items yet. Add a product or enter C to cancel.");
                    continue;
                }
                break;
            }
            if (input.equals("U")) {
                if (cart.isEmpty()) {
                    System.out.println("There is no item to undo.");
                } else {
                    System.out.println("Removed from order: " + cart.pop());
                }
                continue;
            }
            addItemToCart(cart, input);
        }

        Order order = iceCreamMaker.placeOrder(toListInOrderAdded(cart));
        if (order == null) {
            System.out.println("The order could not be placed because there is not enough stock.");
        } else {
            System.out.println("Order added to the queue: " + order);
            System.out.println("Orders waiting: " + iceCreamMaker.getPendingOrders().size());
        }
    }

    private void addItemToCart(ArrayDeque<OrderItem> cart, String productId) {
        if (!InputHelper.isValidProductId(productId)) {
            System.out.println("Product ID must look like IC001 (IC + 3 digits).");
            return;
        }
        Product product = inventory.findById(productId);
        if (product == null) {
            System.out.println("No product found with ID " + productId + ".");
            return;
        }
        int available = product.getQuantity() - quantityInCart(cart, product);
        if (available <= 0) {
            System.out.println(product.getFlavor() + " (" + productId + ") has no more stock available.");
            return;
        }
        int quantity = InputHelper.readInt("Quantity (1-" + available + ", or C to cancel): ", 1, available);
        OrderItem item = new OrderItem(product, quantity);
        cart.push(item);
        System.out.println("Added: " + item + " | Items in order: " + cart.size());
    }

    // Stock already placed in this order for the same product, so it cannot be over-ordered.
    private int quantityInCart(ArrayDeque<OrderItem> cart, Product product) {
        int total = 0;
        for (OrderItem item : cart) {
            if (item.getProduct() == product) {
                total += item.getQuantity();
            }
        }
        return total;
    }

    // push() adds to the front of the deque, so read it from the back to get the original order.
    private ArrayList<OrderItem> toListInOrderAdded(ArrayDeque<OrderItem> cart) {
        ArrayList<OrderItem> items = new ArrayList<>();
        Iterator<OrderItem> iterator = cart.descendingIterator();
        while (iterator.hasNext()) {
            items.add(iterator.next());
        }
        return items;
    }
}
