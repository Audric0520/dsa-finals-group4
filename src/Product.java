/**
 * One ice cream product in the inventory.
 */
public class Product {
    public static final double MIN_PRICE = 30.00;
    public static final double MAX_PRICE = 120.00;

    private final String productId;
    private String flavor;
    private char servingSize;     // S, M or L
    private int quantity;
    private double price;
    private String containerType; // "Cone" or "Cup"

    public Product(String productId, String flavor, char servingSize, int quantity, double price,
            String containerType) {
        this.productId = productId;
        this.flavor = flavor;
        this.servingSize = servingSize;
        this.quantity = quantity;
        this.containerType = containerType;
        setPrice(price);
    }

    public static boolean isValidPrice(double price) {
        return price >= MIN_PRICE && price <= MAX_PRICE;
    }

    public String getProductId() {
        return productId;
    }

    public String getFlavor() {
        return flavor;
    }

    public void setFlavor(String flavor) {
        this.flavor = flavor;
    }

    public char getServingSize() {
        return servingSize;
    }

    public void setServingSize(char servingSize) {
        this.servingSize = servingSize;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getPrice() {
        return price;
    }

    /** Last line of defence: an invalid price can never be stored, even if a menu forgets to check. */
    public void setPrice(double price) {
        if (!isValidPrice(price)) {
            throw new IllegalArgumentException("Price must be from " + MIN_PRICE + " to " + MAX_PRICE + ".");
        }
        this.price = price;
    }

    public String getContainerType() {
        return containerType;
    }

    public void setContainerType(String containerType) {
        this.containerType = containerType;
    }

    @Override
    public String toString() {
        return String.format(TABLE_ROW_FORMAT, productId, flavor, String.valueOf(servingSize), containerType,
                String.valueOf(quantity), String.format("P%.2f", price));
    }

    /** One format for the table header and every row, so the columns always line up. */
    public static final String TABLE_ROW_FORMAT = "%-6s | %-18s | %-4s | %-4s | %5s | %8s";

    public static String tableHeader() {
        return String.format(TABLE_ROW_FORMAT, "ID", "Flavor", "Size", "Type", "Stock", "Price");
    }
}
