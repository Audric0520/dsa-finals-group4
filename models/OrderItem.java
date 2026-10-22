package models;

/**
 * One line of a customer order: a product and how many of it.
 *
 * The flavor, serving size, container and price are copied when the order is taken,
 * like a printed receipt, so the customer's order and total stay exactly as ordered
 * even if the Manager edits the product while the order is waiting in the queue.
 */
public class OrderItem {
    private final Product product; // used to find the stock to subtract when the order is processed
    private final int quantity;
    private final String flavor;
    private final char servingSize;
    private final String containerType;
    private final double unitPrice;

    public OrderItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
        this.flavor = product.getFlavor();
        this.servingSize = product.getServingSize();
        this.containerType = product.getContainerType();
        this.unitPrice = product.getPrice();
    }

    public Product getProduct() {
        return product;
    }

    public int getQuantity() {
        return quantity;
    }

    /** The price of one piece when the order was taken. */
    public double getUnitPrice() {
        return unitPrice;
    }

    public double getSubtotal() {
        return unitPrice * quantity;
    }

    @Override
    public String toString() {
        return String.format("%dx %s %s (%c, %s) = P%.2f", quantity, product.getProductId(), flavor, servingSize,
                containerType, getSubtotal());
    }
}
