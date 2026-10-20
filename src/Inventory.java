import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Stores and manages all ice cream products.
 *
 * ArrayList - keeps every product in the order it was added (used for display, sorting and linear search).
 * HashMap   - maps a product ID to its product for fast O(1) lookup and duplicate-ID checks.
 * ArrayDeque - used as a stack of removed products so the Manager can undo the most recent removal (LIFO).
 */
public class Inventory {
    public static final int MAX_STOCK = 999;

    private final ArrayList<IceCream> products = new ArrayList<>();
    private final HashMap<String, IceCream> productMap = new HashMap<>();
    private final ArrayDeque<IceCream> removedProducts = new ArrayDeque<>();

    public boolean addProduct(IceCream product) {
        if (productMap.containsKey(product.getProductId())) {
            return false;
        }
        products.add(product);
        productMap.put(product.getProductId(), product);
        return true;
    }

    public IceCream findById(String productId) {
        return productMap.get(productId);
    }

    public boolean containsId(String productId) {
        return productMap.containsKey(productId);
    }

    /** Removes a product and pushes it onto the undo stack. Returns null if the ID does not exist. */
    public IceCream removeProduct(String productId) {
        IceCream product = productMap.remove(productId);
        if (product != null) {
            products.remove(product);
            removedProducts.push(product);
        }
        return product;
    }

    public boolean hasRemovedProducts() {
        return !removedProducts.isEmpty();
    }

    /**
     * Pops the most recently removed product and puts it back.
     * Returns null if a new product with the same ID was added in the meantime.
     */
    public IceCream undoLastRemoval() {
        IceCream product = removedProducts.pop();
        if (!addProduct(product)) {
            return null;
        }
        return product;
    }

    /**
     * Adds (positive change) or subtracts (negative change) stock.
     * Fails if the product does not exist or the new quantity would be below 0 or above MAX_STOCK.
     */
    public boolean adjustStock(String productId, int change) {
        IceCream product = productMap.get(productId);
        if (product == null) {
            return false;
        }
        int newQuantity = product.getQuantity() + change;
        if (newQuantity < 0 || newQuantity > MAX_STOCK) {
            return false;
        }
        product.setQuantity(newQuantity);
        return true;
    }

    /** Returns a copy so sorting or searching never changes the stored order. */
    public ArrayList<IceCream> getProducts() {
        return new ArrayList<>(products);
    }

    public boolean isEmpty() {
        return products.isEmpty();
    }

    public static void displayProducts(ArrayList<IceCream> list) {
        if (list.isEmpty()) {
            System.out.println("No products to display.");
            return;
        }
        String line = "-----------------------------------------------------------";
        System.out.println(line);
        System.out.printf("%-6s | %-18s | %-4s | %-4s | %5s | %9s%n", "ID", "Flavor", "Size", "Type", "Stock",
                "Price");
        System.out.println(line);
        for (IceCream product : list) {
            System.out.println(product);
        }
        System.out.println(line);
    }
}
