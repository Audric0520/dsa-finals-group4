package IceCream;

public abstract class IceCreamType {
    private String icecreamID;
    private String flavor;
    private char size;
    private int quantity;
    private double price;

    public IceCreamType(String icecreamID, String flavor, char size, int quantity, double price) {
        this.icecreamID = icecreamID;
        this.flavor = flavor;
        this.size = size;
        this.quantity = quantity;
        this.price = price;
    }

    public String getIcecreamID() {
        return icecreamID;
    }

    public void setIcecreamID(String icecreamID) {
        this.icecreamID = icecreamID;
    }

    public String getFlavor() {
        return flavor;
    }

    public void setFlavor(String flavor) {
        this.flavor = flavor;
    }

    public char getSize() {
        return size;
    }

    public void setSize(char size) {
        this.size = size;
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
}
