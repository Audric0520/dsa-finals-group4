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
        Terminal.title("========== MANAGER MENU ==========");
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

    /** Every value is collected first. The product is saved only after the last input. */
    private void addProduct() {
        System.out.println();
        Terminal.title("--- Add Product (enter C at any prompt to cancel) ---");
        String id = InputHelper.readProductId("Enter product ID, e.g. IC006 (or C to cancel): ");
        if (inventory.containsId(id)) {
            Terminal.error("A product with ID " + id + " already exists. Product not added.");
            return;
        }
        String flavor = InputHelper.readFlavor("Flavor: ");
        char size = InputHelper.readServingSize("Serving size (S/M/L): ");
        int quantity = InputHelper.readInt("Quantity in stock (" + Inventory.MIN_NEW_QUANTITY + "-"
                + Inventory.MAX_STOCK + "): ", Inventory.MIN_NEW_QUANTITY, Inventory.MAX_STOCK);
        double price = InputHelper.readPrice(String.format("Price (P%.2f-P%.2f): P", Product.MIN_PRICE,
                Product.MAX_PRICE));
        String container = InputHelper.readContainerType("Container type (cone/cup): ");

        if (inventory.addProduct(new Product(id, flavor, size, quantity, price, container))) {
            Terminal.success("Product " + id + " added successfully.");
        } else {
            Terminal.error("Product could not be added.");
        }
    }

    /** Asks for an ID and returns the product, or null (with a message) if it does not exist. */
    private Product askForExistingProduct() {
        if (inventory.isEmpty()) {
            Terminal.warning("The inventory is empty.");
            return null;
        }
        String id = InputHelper.readProductId("Enter product ID (or C to cancel): ");
        Product product = inventory.findById(id);
        if (product == null) {
            Terminal.error("No product found with ID " + id + ".");
        }
        return product;
    }

    private void updateProduct() {
        System.out.println();
        Terminal.title("--- Update Product Details ---");
        Product product = askForExistingProduct();
        if (product == null) {
            return;
        }
        System.out.println("Current: " + product);
        System.out.println("[1] Flavor");
        System.out.println("[2] Serving size");
        System.out.println("[3] Quantity in stock");
        System.out.println("[4] Price");
        System.out.println("[5] Container type");
        int field = InputHelper.readInt("Field to update [1-5] (or C to cancel): ", 1, 5);
        String id = product.getProductId();

        switch (field) {
            case 1:
                product.setFlavor(InputHelper.readFlavor("New flavor (or C to cancel): "));
                break;
            case 2:
                product.setServingSize(InputHelper.readServingSize("New serving size S/M/L (or C to cancel): "));
                break;
            case 3:
                inventory.updateQuantity(id, InputHelper.readInt(
                        "New quantity 0-" + Inventory.MAX_STOCK + " (or C to cancel): ", 0, Inventory.MAX_STOCK));
                break;
            case 4:
                inventory.updatePrice(id, InputHelper.readPrice(String.format(
                        "New price P%.2f-P%.2f (or C to cancel): P", Product.MIN_PRICE, Product.MAX_PRICE)));
                break;
            default:
                product.setContainerType(InputHelper.readContainerType("New container cone/cup (or C to cancel): "));
                break;
        }
        Terminal.success("Product updated: " + product);
    }

    private void removeProduct() {
        System.out.println();
        Terminal.title("--- Remove Product ---");
        Product product = askForExistingProduct();
        if (product == null) {
            return;
        }
        if (iceCreamMaker.hasPendingOrderFor(product.getProductId())) {
            Terminal.error("Cannot remove " + product.getProductId()
                    + ": it is part of a pending order. Process that order first.");
            return;
        }
        System.out.println("Product: " + product);
        // Removing cannot be undone, so pressing Enter alone answers No
        if (InputHelper.readYesNo("Remove this product?", false)) {
            inventory.removeProduct(product.getProductId());
            Terminal.success("Product " + product.getProductId() + " removed.");
        } else {
            Terminal.warning("Product was not removed.");
        }
    }
}
