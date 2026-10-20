/**
 * One ice cream product in the inventory.
 */
public class IceCream {
    private final String productId;
    private String flavor;
    private char servingSize;     // S, M or L
    private int quantity;
    private double price;
    private String containerType; // "Cone" or "Cup"

    public IceCream(String productId, String flavor, char servingSize, int quantity, double price,
            String containerType) {
        this.productId = productId;
        this.flavor = flavor;
        this.servingSize = servingSize;
        this.quantity = quantity;
        this.price = price;
        this.containerType = containerType;
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

    public void setPrice(double price) {
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
        return String.format("%-6s | %-18s | %-4c | %-4s | %5d | P%8.2f",
                productId, flavor, servingSize, containerType, quantity, price);
    }
}
