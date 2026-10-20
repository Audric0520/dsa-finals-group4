/**
 * A pending customer order: which product and how many.
 */
public class Order {
    private final int orderNumber;
    private final IceCream product;
    private final int quantity;

    public Order(int orderNumber, IceCream product, int quantity) {
        this.orderNumber = orderNumber;
        this.product = product;
        this.quantity = quantity;
    }

    public int getOrderNumber() {
        return orderNumber;
    }

    public IceCream getProduct() {
        return product;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getTotal() {
        return product.getPrice() * quantity;
    }

    @Override
    public String toString() {
        return String.format("Order #%d | %s %s (%c, %s) x%d | Total: P%.2f",
                orderNumber, product.getProductId(), product.getFlavor(), product.getServingSize(),
                product.getContainerType(), quantity, getTotal());
    }
}
