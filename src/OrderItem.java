/**
 * One line of a customer order: a product and how many of it.
 */
public class OrderItem {
    private final Product product;
    private final int quantity;

    public OrderItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    public Product getProduct() {
        return product;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getSubtotal() {
        return product.getPrice() * quantity;
    }

    @Override
    public String toString() {
        return String.format("%dx %s %s (%c, %s) = P%.2f", quantity, product.getProductId(), product.getFlavor(),
                product.getServingSize(), product.getContainerType(), getSubtotal());
    }
}
