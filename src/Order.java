import java.util.ArrayList;

/**
 * A customer order waiting in the queue. It can contain one or more products.
 */
public class Order {
    private final String orderId; // e.g. ORD-001
    private final ArrayList<OrderItem> items;

    public Order(String orderId, ArrayList<OrderItem> items) {
        this.orderId = orderId;
        this.items = new ArrayList<>(items);
    }

    public String getOrderId() {
        return orderId;
    }

    public ArrayList<OrderItem> getItems() {
        return new ArrayList<>(items);
    }

    public double getTotal() {
        double total = 0;
        for (OrderItem item : items) {
            total += item.getSubtotal();
        }
        return total;
    }

    public boolean containsProduct(String productId) {
        for (OrderItem item : items) {
            if (item.getProduct().getProductId().equals(productId)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder text = new StringBuilder(orderId + " | Total: P" + String.format("%.2f", getTotal()));
        for (OrderItem item : items) {
            text.append(System.lineSeparator()).append("    - ").append(item);
        }
        return text.toString();
    }
}
