package roles;

import console.InputHelper;
import models.Inventory;
import models.Product;

/**
 * Manager: views, sorts and searches the inventory and adds, updates and removes products.
 */
public class Manager extends User {
    private final IceCreamMaker iceCreamMaker;

    public Manager(Inventory inventory, IceCreamMaker iceCreamMaker) {
        super(inventory);
        this.iceCreamMaker = iceCreamMaker;
    }

    @Override
    protected String getRoleName() {
        return Authentication.MANAGER;
    }

    @Override
    protected void printMenu() {
        System.out.println("========== MANAGER MENU ==========");
        System.out.println("[1] View inventory");
        System.out.println("[2] Sort products");
        System.out.println("[3] Search products");
        System.out.println("[4] Add product");
        System.out.println("[5] Update product details");
        System.out.println("[6] Remove product");
        System.out.println("[0] Log out");
    }

    @Override
    protected int getLastOption() {
        return 6;
    }

    @Override
    protected IceCreamMaker getOrderQueue() {
        return iceCreamMaker;
    }

    @Override
    protected String getStatusLine() {
        return "Products: " + inventory.size() + " | Out of stock: " + inventory.countOutOfStock()
                + " | Pending orders: " + iceCreamMaker.getPendingOrders().size();
    }

    @Override
    protected void handleChoice(int choice) {
        switch (choice) {
            case 1:
                viewInventory();
                break;
            case 2:
                sortProducts();
                break;
            case 3:
                searchProducts();
                break;
            case 4:
                addProduct();
                break;
            case 5:
                updateProduct();
                break;
            case 6:
                removeProduct();
                break;
            default:
                break;
        }
    }

    /**
     * Every value is collected first. The product is saved only after the last input.
     * Flavor, size and container come first, so a product that already exists is caught
     * before the quantity and price are typed.
     */
    private void addProduct() {
        System.out.println();
        System.out.println("--- Add Product (enter C at any prompt to cancel) ---");
        String id = InputHelper.readProductId("Enter product ID, e.g. IC006: ");
        if (inventory.containsId(id)) {
            System.out.println("A product with ID " + id + " already exists. Product not added.");
            return;
        }
        String flavor = InputHelper.readFlavor("Flavor: ");
        char size = InputHelper.readServingSize("Serving size (S/M/L): ");
        String container = InputHelper.readContainerType("Container type (cone/cup): ");
        Product same = inventory.findSameProduct(flavor, size, container, null);
        if (same != null) {
            System.out.println(flavor + " (" + size + ", " + container + ") already exists as " + same.getProductId()
                    + ". Update " + same.getProductId() + " instead. Product not added.");
            return;
        }
        int quantity = InputHelper.readInt("Quantity in stock (" + Inventory.MIN_NEW_QUANTITY + "-"
                + Inventory.MAX_STOCK + "): ", Inventory.MIN_NEW_QUANTITY, Inventory.MAX_STOCK);
        double price = InputHelper.readPrice(String.format("Price (P%.2f-P%.2f): P", Product.MIN_PRICE,
                Product.MAX_PRICE));

        if (inventory.addProduct(new Product(id, flavor, size, quantity, price, container))) {
            System.out.println("Product " + id + " added successfully.");
        } else {
            System.out.println("Product could not be added.");
        }
    }

    /** Asks for an ID and returns the product, or null (with a message) if it does not exist. */
    private Product askForExistingProduct() {
        if (inventory.isEmpty()) {
            System.out.println("The inventory is empty.");
            return null;
        }
        String id = InputHelper.readProductId("Enter product ID: ");
        Product product = inventory.findById(id);
        if (product == null) {
            System.out.println("No product found with ID " + id + ".");
        }
        return product;
    }

    /**
     * Changes one field of a product. While a product is in a pending order:
     * - its flavor, serving size and container cannot change, because the customer ordered
     *   exactly that item and the Ice Cream Maker must make it from this product's stock
     * - its price can change; pending orders keep the price they were taken at
     * - its stock can change, but going below what the pending orders need asks first,
     *   because those orders then cannot be processed until the product is restocked
     */
    private void updateProduct() {
        System.out.println();
        System.out.println("--- Update Product Details (enter C at any prompt to cancel) ---");
        Product product = askForExistingProduct();
        if (product == null) {
            return;
        }
        String id = product.getProductId();
        String pendingOrders = iceCreamMaker.getPendingOrderIdsFor(id);
        boolean locked = !pendingOrders.isEmpty();

        // Say straight away what cannot change, before the Manager picks a field
        System.out.println("Current: " + product);
        if (locked) {
            System.out.println(id + " is part of " + describeOrders(pendingOrders) + ", so its flavor, serving size and"
                    + " container type cannot change until " + (isPlural(pendingOrders) ? "they are" : "it is")
                    + " processed (the customer gets what they ordered).");
            System.out.println("Its quantity and price can still be updated.");
        }
        String lockedMark = locked ? " (locked)" : "";
        System.out.println("[1] Flavor" + lockedMark);
        System.out.println("[2] Serving size" + lockedMark);
        System.out.println("[3] Quantity in stock");
        System.out.println("[4] Price");
        System.out.println("[5] Container type" + lockedMark);
        int field = InputHelper.readInt("Field to update [1-5]: ", 1, 5);
        while (locked && field != 3 && field != 4) {
            String fieldName = field == 1 ? "Flavor" : field == 2 ? "Serving size" : "Container type";
            System.out.println(fieldName + " is locked while " + describeOrders(pendingOrders)
                    + (isPlural(pendingOrders) ? " wait" : " waits") + ". Choose 3 (quantity) or 4 (price).");
            field = InputHelper.readInt("Field to update [1-5]: ", 1, 5);
        }

        switch (field) {
            case 1:
                String flavor = InputHelper.readFlavor("New flavor: ");
                if (isDuplicate(product, flavor, product.getServingSize(), product.getContainerType())) {
                    return;
                }
                product.setFlavor(flavor);
                break;
            case 2:
                char size = InputHelper.readServingSize("New serving size (S/M/L): ");
                if (isDuplicate(product, product.getFlavor(), size, product.getContainerType())) {
                    return;
                }
                product.setServingSize(size);
                break;
            case 3:
                int quantity = InputHelper.readInt("New quantity (0-" + Inventory.MAX_STOCK + "): ", 0,
                        Inventory.MAX_STOCK);
                if (!confirmStockBelowReserved(product, quantity, pendingOrders)) {
                    return;
                }
                inventory.updateQuantity(id, quantity);
                break;
            case 4:
                inventory.updatePrice(id, InputHelper.readPrice(String.format(
                        "New price (P%.2f-P%.2f): P", Product.MIN_PRICE, Product.MAX_PRICE)));
                break;
            default:
                String container = InputHelper.readContainerType("New container type (cone/cup): ");
                if (isDuplicate(product, product.getFlavor(), product.getServingSize(), container)) {
                    return;
                }
                product.setContainerType(container);
                break;
        }
        System.out.println("Product updated: " + product);
        if (field == 4 && !pendingOrders.isEmpty()) {
            System.out.println("Note: " + describeOrders(pendingOrders)
                    + (isPlural(pendingOrders) ? " keep the price they were" : " keeps the price it was")
                    + " ordered at. The new price applies to new orders.");
        }
    }

    // Two IDs for the same item would split its stock, so a change that creates one is refused.
    private boolean isDuplicate(Product product, String flavor, char size, String container) {
        Product same = inventory.findSameProduct(flavor, size, container, product.getProductId());
        if (same == null) {
            return false;
        }
        System.out.println("That would make " + product.getProductId() + " the same item as " + same.getProductId()
                + " (" + flavor + ", " + size + ", " + container + "). Product not updated.");
        return true;
    }

    // Returns true if the new stock covers the pending orders, or the Manager confirms it anyway.
    private boolean confirmStockBelowReserved(Product product, int newQuantity, String pendingOrders) {
        int reserved = iceCreamMaker.getPendingQuantity(product);
        if (newQuantity >= reserved) {
            return true;
        }
        System.out.println("The " + describeOrders(pendingOrders) + (isPlural(pendingOrders) ? " need " : " needs ")
                + reserved + " of " + product.getProductId() + ". With only " + newQuantity
                + " in stock, the Ice Cream Maker cannot process " + itOrThem(pendingOrders)
                + " until the product is restocked.");
        if (InputHelper.readYesNo("Set the stock to " + newQuantity + " anyway?", false)) {
            return true;
        }
        System.out.println("Quantity not changed.");
        return false;
    }

    // "pending order ORD-001" or "pending orders ORD-001, ORD-002"
    private static String describeOrders(String orderIds) {
        return (isPlural(orderIds) ? "pending orders " : "pending order ") + orderIds;
    }

    private static boolean isPlural(String orderIds) {
        return orderIds.contains(",");
    }

    private static String itOrThem(String orderIds) {
        return isPlural(orderIds) ? "them" : "it";
    }

    private void removeProduct() {
        System.out.println();
        System.out.println("--- Remove Product (enter C at any prompt to cancel) ---");
        Product product = askForExistingProduct();
        if (product == null) {
            return;
        }
        String pendingOrders = iceCreamMaker.getPendingOrderIdsFor(product.getProductId());
        if (!pendingOrders.isEmpty()) {
            System.out.println("Cannot remove " + product.getProductId() + ": it is part of "
                    + describeOrders(pendingOrders) + ". Process " + itOrThem(pendingOrders) + " first.");
            return;
        }
        System.out.println("Product: " + product);
        // Removing cannot be undone, so pressing Enter alone answers No (C still cancels)
        if (InputHelper.readYesNo("Remove this product?", false)) {
            inventory.removeProduct(product.getProductId());
            System.out.println("Product " + product.getProductId() + " removed.");
        } else {
            System.out.println("Product was not removed.");
        }
    }
}
