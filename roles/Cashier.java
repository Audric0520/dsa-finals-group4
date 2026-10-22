package roles;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;

import console.InputHelper;
import console.OperationCancelledException;
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
    protected IceCreamMaker getOrderQueue() {
        return iceCreamMaker;
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
     * Builds one customer order that may contain several products, at an "order>" prompt
     * that works like a small shell: a product ID (with an optional quantity) adds an item,
     * and the words done, undo and cancel finish, undo or discard (D, U and C also work).
     *
     * The items are kept in an ArrayDeque used as a stack, so undo removes the most recently
     * added item (LIFO). Nothing is queued until done, so cancelling leaves the stock and
     * the queue unchanged.
     */
    private void addNewOrder() {
        if (inventory.isEmpty()) {
            System.out.println("The inventory is empty. No orders can be made.");
            return;
        }
        System.out.println();
        System.out.println("--- New Customer Order ---");
        System.out.println("Type a product ID (and quantity, e.g. IC001 2) to add it,");
        System.out.println("or one of: done, undo, cancel");
        ArrayDeque<OrderItem> cart = new ArrayDeque<>();

        while (true) {
            String input = InputHelper.readLine("order> ");
            String command = input.toLowerCase();
            if (input.isEmpty()) {
                continue; // like a shell, Enter alone just shows the prompt again
            }
            if (command.equals("done") || command.equals("d")) {
                if (!cart.isEmpty()) {
                    break;
                }
                System.out.println("The order is still empty. Add a product first, or type cancel.");
            } else if (command.equals("undo") || command.equals("u")) {
                if (cart.isEmpty()) {
                    System.out.println("There is nothing to undo. The order is still empty.");
                } else {
                    System.out.println("Removed " + cart.pop() + " | Total: " + formatTotal(cart));
                }
            } else if (command.equals("cancel") || command.equals("c")) {
                confirmCancel(cart);
            } else {
                addItemToCart(cart, input);
            }
        }

        Order order = iceCreamMaker.placeOrder(toListInOrderAdded(cart));
        if (order == null) {
            System.out.println("The order could not be placed because there is not enough stock.");
        } else {
            System.out.printf("Order %s added to the queue. Total: P%.2f%n", order.getOrderId(), order.getTotal());
            for (OrderItem item : order.getItems()) {
                System.out.println("    - " + item);
            }
        }
    }

    // Losing a long order to one mistyped letter would mean asking the customer again, so ask first.
    private void confirmCancel(ArrayDeque<OrderItem> cart) {
        if (cart.isEmpty() || InputHelper.confirm("Discard this order (" + describe(cart) + ")?", false)) {
            throw new OperationCancelledException("Order discarded.");
        }
        System.out.println("Order kept.");
    }

    /** input is "IC001" or "IC001 2": the product ID, optionally followed by the quantity. */
    private void addItemToCart(ArrayDeque<OrderItem> cart, String input) {
        String[] parts = input.toUpperCase().split("\\s+");
        if (parts.length > 2 || !InputHelper.isValidProductId(parts[0])) {
            System.out.println("\"" + input + "\" is not a product ID or a command. Type a product ID like IC001"
                    + " (or IC001 2), or done, undo, cancel.");
            return;
        }
        String productId = parts[0];
        Product product = inventory.findById(productId);
        if (product == null) {
            System.out.println("No product found with ID " + productId + ".");
            return;
        }
        if (product.getQuantity() == 0) {
            System.out.println(product.getFlavor() + " (" + productId + ") is out of stock.");
            return;
        }
        // Stock not already needed by pending orders or by this order so far
        int available = iceCreamMaker.getAvailableQuantity(product) - quantityInCart(cart, product);
        if (available <= 0) {
            System.out.println(product.getFlavor() + " (" + productId + ") has no stock left for new orders"
                    + " (in stock: " + product.getQuantity() + ", reserved by pending orders: "
                    + iceCreamMaker.getPendingQuantity(product) + ", already in this order: "
                    + quantityInCart(cart, product) + ").");
            return;
        }
        int quantity;
        if (parts.length == 1) {
            try {
                quantity = InputHelper.readInt("How many " + product.getFlavor() + " (" + productId + ")? [1-"
                        + available + "] (or C to skip): ", 1, available);
            } catch (OperationCancelledException e) {
                // C here only skips this product; the rest of the order is kept
                System.out.println(product.getFlavor() + " not added.");
                return;
            }
        } else {
            Integer typed = InputHelper.parseIntInRange(parts[1], 1, available);
            if (typed == null) {
                System.out.println("Quantity for " + product.getFlavor() + " (" + productId + ") must be a whole"
                        + " number from 1 to " + available + ". Nothing was added.");
                return;
            }
            quantity = typed;
        }
        OrderItem item = new OrderItem(product, quantity);
        cart.push(item);
        System.out.println("Added " + item + " | Total: " + formatTotal(cart));
    }

    // The running total lets the cashier tell the customer the amount at any time.
    private static String formatTotal(ArrayDeque<OrderItem> cart) {
        double total = 0;
        for (OrderItem item : cart) {
            total += item.getSubtotal();
        }
        return String.format("P%.2f", total);
    }

    // e.g. "2 items, P155.00"
    private static String describe(ArrayDeque<OrderItem> cart) {
        return cart.size() + (cart.size() == 1 ? " item, " : " items, ") + formatTotal(cart);
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

    /**
     * push() adds to the front of the deque, so read it from the back to get the original order.
     * A product added more than once becomes one line with the total quantity (like a receipt),
     * so the Ice Cream Maker sees "5x Vanilla" instead of "2x Vanilla" and "3x Vanilla".
     */
    private ArrayList<OrderItem> toListInOrderAdded(ArrayDeque<OrderItem> cart) {
        ArrayList<OrderItem> items = new ArrayList<>();
        Iterator<OrderItem> iterator = cart.descendingIterator();
        while (iterator.hasNext()) {
            OrderItem item = iterator.next();
            boolean merged = false;
            for (int i = 0; i < items.size(); i++) {
                if (items.get(i).getProduct() == item.getProduct()) {
                    items.set(i, new OrderItem(item.getProduct(), items.get(i).getQuantity() + item.getQuantity()));
                    merged = true;
                    break;
                }
            }
            if (!merged) {
                items.add(item);
            }
        }
        return items;
    }
}
