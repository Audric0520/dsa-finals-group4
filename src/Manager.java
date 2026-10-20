/**
 * Manager: adds, updates and removes product records and adjusts stock.
 */
public class Manager extends User {

    public Manager(Inventory inventory) {
        super(inventory);
    }

    @Override
    public void showMenu() {
        boolean running = true;
        while (running) {
            System.out.println("\n===== MANAGER MENU =====");
            System.out.println("[1] View inventory");
            System.out.println("[2] Add product");
            System.out.println("[3] Update product details");
            System.out.println("[4] Remove product");
            System.out.println("[5] Undo last removal");
            System.out.println("[6] Adjust stock quantity");
            System.out.println("[7] Search products");
            System.out.println("[8] Sort products");
            System.out.println("[0] Log out");
            int choice = InputHelper.readInt("Choice: ", 0, 8);

            switch (choice) {
                case 1:
                    viewInventory();
                    break;
                case 2:
                    addProduct();
                    break;
                case 3:
                    updateProduct();
                    break;
                case 4:
                    removeProduct();
                    break;
                case 5:
                    undoRemoval();
                    break;
                case 6:
                    adjustStock();
                    break;
                case 7:
                    searchProducts();
                    break;
                case 8:
                    sortProducts();
                    break;
                default:
                    running = false;
            }
        }
        System.out.println("Manager logged out.");
    }

    private void addProduct() {
        System.out.println("\n--- Add Product ---");
        String id = InputHelper.readProductId("Product ID (e.g. IC006): ");
        if (inventory.containsId(id)) {
            System.out.println("A product with ID " + id + " already exists. Product not added.");
            return;
        }
        String flavor = InputHelper.readFlavor("Flavor: ");
        char size = InputHelper.readServingSize("Serving size (S/M/L): ");
        int quantity = InputHelper.readInt("Quantity in stock (0-" + Inventory.MAX_STOCK + "): ", 0,
                Inventory.MAX_STOCK);
        double price = InputHelper.readPrice("Price: P");
        String container = InputHelper.readContainerType("Container type (cone/cup): ");

        inventory.addProduct(new IceCream(id, flavor, size, quantity, price, container));
        System.out.println("Product " + id + " added successfully.");
    }

    /** Asks for an ID and returns the product, or null (with a message) if it does not exist. */
    private IceCream askForExistingProduct() {
        if (inventory.isEmpty()) {
            System.out.println("The inventory is empty.");
            return null;
        }
        String id = InputHelper.readProductId("Product ID: ");
        IceCream product = inventory.findById(id);
        if (product == null) {
            System.out.println("No product found with ID " + id + ".");
        }
        return product;
    }

    private void updateProduct() {
        System.out.println("\n--- Update Product ---");
        IceCream product = askForExistingProduct();
        if (product == null) {
            return;
        }
        System.out.println("Current: " + product);
        System.out.println("[1] Flavor");
        System.out.println("[2] Serving size");
        System.out.println("[3] Price");
        System.out.println("[4] Container type");
        System.out.println("[0] Cancel");
        int choice = InputHelper.readInt("Field to update: ", 0, 4);

        switch (choice) {
            case 1:
                product.setFlavor(InputHelper.readFlavor("New flavor: "));
                break;
            case 2:
                product.setServingSize(InputHelper.readServingSize("New serving size (S/M/L): "));
                break;
            case 3:
                product.setPrice(InputHelper.readPrice("New price: P"));
                break;
            case 4:
                product.setContainerType(InputHelper.readContainerType("New container type (cone/cup): "));
                break;
            default:
                System.out.println("Update cancelled.");
                return;
        }
        System.out.println("Updated: " + product);
    }

    private void removeProduct() {
        System.out.println("\n--- Remove Product ---");
        IceCream product = askForExistingProduct();
        if (product == null) {
            return;
        }
        System.out.println("Product: " + product);
        if (InputHelper.readYesNo("Remove this product? (Y/N): ")) {
            inventory.removeProduct(product.getProductId());
            System.out.println("Product " + product.getProductId() + " removed. (Use 'Undo last removal' to restore.)");
        } else {
            System.out.println("Removal cancelled.");
        }
    }

    private void undoRemoval() {
        if (!inventory.hasRemovedProducts()) {
            System.out.println("There is no removal to undo.");
            return;
        }
        IceCream restored = inventory.undoLastRemoval();
        if (restored == null) {
            System.out.println("Could not restore: a new product already uses that ID.");
        } else {
            System.out.println("Restored product: " + restored);
        }
    }

    private void adjustStock() {
        System.out.println("\n--- Adjust Stock ---");
        IceCream product = askForExistingProduct();
        if (product == null) {
            return;
        }
        System.out.println("Current stock of " + product.getProductId() + ": " + product.getQuantity());
        int change = InputHelper.readInt("Amount to add (use a negative number to subtract): ",
                -Inventory.MAX_STOCK, Inventory.MAX_STOCK);
        if (inventory.adjustStock(product.getProductId(), change)) {
            System.out.println("New stock: " + product.getQuantity());
        } else {
            System.out.println("Invalid adjustment. Stock must stay between 0 and " + Inventory.MAX_STOCK + ".");
        }
    }
}
