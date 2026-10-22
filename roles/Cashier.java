package roles;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;

import console.InputHelper;
import console.Terminal;
import models.Inventory;
import models.Order;
import models.OrderItem;
import models.Product;

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
        Terminal.title("========== CASHIER MENU ==========");
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
    protected String getStatusLine() {
        return "Products: " + inventory.size() + " | Pending orders: " + iceCreamMaker.getPendingOrders().size();
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
     * most recently added item (LIFO). Nothing is queued until D (done),
     * so cancelling with C leaves the stock and the queue unchanged.
     */
    private void addNewOrder() {
        if (inventory.isEmpty()) {
            Terminal.warning("The inventory is empty. No orders can be made.");
            return;
        }
        System.out.println();
        Terminal.title("--- New Customer Order ---");
        System.out.println("How to enter an order:");
        System.out.println("  - Type a product ID (e.g. IC001) to add that product to the order.");
        System.out.println("    To skip the quantity question, type the quantity after it (e.g. IC001 2).");
        System.out.println("  - Type D when the order is complete to send it to the queue.");
        System.out.println("  - Type U to remove the last product you added.");
        System.out.println("  - Type C to cancel the whole order.");
        ArrayDeque<OrderItem> cart = new ArrayDeque<>();

        while (true) {
            String input = InputHelper.readText(
                    "\nEnter product ID to add (D = Done, U = Undo last item, C = Cancel order): ").toUpperCase();
            if (input.equals("D")) {
                if (cart.isEmpty()) {
                    Terminal.error("The order is still empty. Add at least one product, or type C to cancel.");
                    continue;
                }
                break;
            }
            if (input.equals("U")) {
                if (cart.isEmpty()) {
                    Terminal.error("There is nothing to undo. The order is still empty.");
                } else {
                    Terminal.warning("Removed from order: " + cart.pop());
                    printOrderSoFar(cart);
                }
                continue;
            }
            // "IC001" or "IC001 2": the product ID, optionally followed by the quantity
            String[] parts = input.split("\\s+");
            if (parts.length > 2) {
                Terminal.error("\"" + input + "\" is not valid. Type a product ID like IC001 (or IC001 2), or D, U or C.");
                continue;
            }
            addItemToCart(cart, parts[0], parts.length == 2 ? parts[1] : null);
        }

        Order order = iceCreamMaker.placeOrder(toListInOrderAdded(cart));
        if (order == null) {
            Terminal.error("The order could not be placed because there is not enough stock.");
        } else {
            Terminal.success("Order added to the queue: " + order);
            System.out.println("Orders waiting: " + iceCreamMaker.getPendingOrders().size());
        }
    }

    /** quantityText is the quantity typed after the ID, or null to ask for it. */
    private void addItemToCart(ArrayDeque<OrderItem> cart, String productId, String quantityText) {
        if (!InputHelper.isValidProductId(productId)) {
            Terminal.error("\"" + productId + "\" is not valid. Type a product ID like IC001 (or IC001 2), or D, U or C.");
            return;
        }
        Product product = inventory.findById(productId);
        if (product == null) {
            Terminal.error("No product found with ID " + productId + ".");
            return;
        }
        // Stock not already needed by pending orders or by this order so far
        int available = iceCreamMaker.getAvailableQuantity(product) - quantityInCart(cart, product);
        if (available <= 0) {
            Terminal.error(product.getFlavor() + " (" + productId + ") has no more stock available"
                    + " (in stock: " + product.getQuantity() + ", in pending orders: "
                    + iceCreamMaker.getPendingQuantity(product) + ", already in this order: "
                    + quantityInCart(cart, product) + ").");
            return;
        }
        int quantity;
        if (quantityText == null) {
            quantity = InputHelper.readInt("How many " + product.getFlavor() + " (" + productId + ")? Enter 1-"
                    + available + " (or C to cancel the order): ", 1, available);
        } else {
            Integer typed = InputHelper.parseIntInRange(quantityText, 1, available);
            if (typed == null) {
                Terminal.error("Quantity for " + product.getFlavor() + " (" + productId + ") must be a whole number"
                        + " from 1 to " + available + ". Nothing was added.");
                return;
            }
            quantity = typed;
        }
        OrderItem item = new OrderItem(product, quantity);
        cart.push(item);
        Terminal.success("Added to order: " + item);
        printOrderSoFar(cart);
    }

    // The running total lets the cashier tell the customer the amount at any time.
    private void printOrderSoFar(ArrayDeque<OrderItem> cart) {
        double total = 0;
        for (OrderItem item : cart) {
            total += item.getSubtotal();
        }
        System.out.printf("Items in this order: %d | Running total: P%.2f%n", cart.size(), total);
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
