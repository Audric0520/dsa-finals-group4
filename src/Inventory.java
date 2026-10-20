import java.util.ArrayList;
import java.util.HashMap;

/**
 * Stores and manages all ice cream products.
 *
 * ArrayList - keeps every product in the order it was added (used for display, sorting and linear search).
 * HashMap   - maps a product ID to its product for fast O(1) lookup and duplicate-ID checks.
 *
 * All rules (unique ID, price range, stock never negative) are checked here, so an invalid
 * product can never be saved, no matter which menu calls these methods.
 */
public class Inventory {
    public static final int MAX_STOCK = 999;

    private final ArrayList<Product> products = new ArrayList<>();
    private final HashMap<String, Product> productMap = new HashMap<>();

    /** Fails if the ID already exists, the price is out of range or the quantity is invalid. */
    public boolean addProduct(Product product) {
        if (productMap.containsKey(product.getProductId())
                || !Product.isValidPrice(product.getPrice())
                || product.getQuantity() < 0 || product.getQuantity() > MAX_STOCK) {
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

    /** Used by the Manager to set the stock to an exact number (for example after restocking). */
    public boolean updateQuantity(String productId, int newQuantity) {
        Product product = productMap.get(productId);
        if (product == null || newQuantity < 0 || newQuantity > MAX_STOCK) {
            return false;
        }
        product.setQuantity(newQuantity);
        return true;
    }

    /**
     * Adds (positive change) or subtracts (negative change) stock. Used to reserve stock
     * for an order and to give it back when the order is cancelled.
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

    public static void displayProducts(ArrayList<Product> list) {
        if (list.isEmpty()) {
            System.out.println("No products to display.");
            return;
        }
        String line = "----------------------------------------------------------";
        System.out.println(line);
        System.out.printf("%-6s | %-18s | %-4s | %-4s | %5s | %8s%n", "ID", "Flavor", "Size", "Type", "Stock",
                "Price");
        System.out.println(line);
        for (Product product : list) {
            System.out.println(product);
        }
        System.out.println(line);
    }
}
