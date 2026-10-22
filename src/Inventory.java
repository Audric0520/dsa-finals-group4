import java.util.ArrayList;
import java.util.HashMap;

/**
 * Stores and manages all ice cream products.
 *
 * ArrayList - keeps every product in the order it was added (used for display, sorting and linear search).
 * HashMap   - maps a product ID to its product for fast O(1) lookup and duplicate-ID checks.
 *
 * All rules (unique ID, price range, quantity range) are checked here, so an invalid
 * product can never be saved, no matter which menu calls these methods.
 */
public class Inventory {
    public static final int MIN_NEW_QUANTITY = 1;  // a new product must start with at least 1 in stock
    public static final int MAX_STOCK = 100;

    private final ArrayList<Product> products = new ArrayList<>();
    private final HashMap<String, Product> productMap = new HashMap<>();

    /** Fails if the ID already exists, the price is out of range or the quantity is not 1-100. */
    public boolean addProduct(Product product) {
        if (productMap.containsKey(product.getProductId())
                || !Product.isValidPrice(product.getPrice())
                || product.getQuantity() < MIN_NEW_QUANTITY || product.getQuantity() > MAX_STOCK) {
            return false;
        }
        products.add(product);
        productMap.put(product.getProductId(), product);
        return true;
    }

    public Product findById(String productId) {
        return productMap.get(productId);
    }

    public boolean containsId(String productId) {
        return productMap.containsKey(productId);
    }

    /** Returns the removed product, or null if the ID does not exist. */
    public Product removeProduct(String productId) {
        Product product = productMap.remove(productId);
        if (product != null) {
            products.remove(product);
        }
        return product;
    }

    public boolean updatePrice(String productId, double newPrice) {
        Product product = productMap.get(productId);
        if (product == null || !Product.isValidPrice(newPrice)) {
            return false;
        }
        product.setPrice(newPrice);
        return true;
    }

    /**
     * Used by the Manager to set the stock to an exact number (for example after restocking).
     * 0 is allowed here because an existing product can be sold out.
     */
    public boolean updateQuantity(String productId, int newQuantity) {
        Product product = productMap.get(productId);
        if (product == null || newQuantity < 0 || newQuantity > MAX_STOCK) {
            return false;
        }
        product.setQuantity(newQuantity);
        return true;
    }

    /**
     * Adds (positive change) or subtracts (negative change) stock. Used to subtract stock
     * when an order is processed.
     * Fails if the product does not exist or the stock would go below 0.
     */
    public boolean adjustStock(String productId, int change) {
        Product product = productMap.get(productId);
        if (product == null || product.getQuantity() + change < 0) {
            return false;
        }
        product.setQuantity(product.getQuantity() + change);
        return true;
    }

    /** Returns a copy, so sorting or searching never changes the stored order. */
    public ArrayList<Product> getProducts() {
        return new ArrayList<>(products);
    }

    public boolean isEmpty() {
        return products.isEmpty();
    }

    public int size() {
        return products.size();
    }

    /** Number of products with 0 in stock (shown to the Manager as products to restock). */
    public int countOutOfStock() {
        int count = 0;
        for (Product product : products) {
            if (product.getQuantity() == 0) {
                count++;
            }
        }
        return count;
    }

    /** Prints a title and the products as a table. Used by view, search and sort. */
    public static void displayProducts(String title, ArrayList<Product> list) {
        System.out.println();
        Terminal.title("=== " + title + " ===");
        if (list.isEmpty()) {
            System.out.println("No products to display.");
            return;
        }
        String header = Product.tableHeader();
        String line = "-".repeat(header.length());
        System.out.println(line);
        System.out.println(header);
        System.out.println(line);
        for (Product product : list) {
            // Sold-out rows stand out in red (the row is coloured whole, so the columns stay aligned)
            System.out.println(product.getQuantity() == 0 ? Terminal.style(product.toString(), Terminal.Style.ERROR)
                    : product.toString());
        }
        System.out.println(line);
    }
}
