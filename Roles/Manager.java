package Roles;

import Utilities.DataValidation;
import IceCream.IceCreamType;

public class Manager extends User {
    public Manager(String role) {
        super(role);
    }

    @Override
    public void landingPage() {
        boolean isRunning = true;
        while (isRunning) {
            System.out.println("\n--- Ice Cream Maker MENU ---");
            System.out.println("[1] View All Ice Cream Products");
            System.out.println("[2] Sort Ice Cream Products");
            System.out.println("[3] Find Specific Ice Cream Product");
            System.out.println("[4] Add an Ice Cream Product");
            System.out.println("[5] Remove an Ice Cream Product");
            System.out.println("[6] Update an Ice Cream Product");
            System.out.println("[7] Log Out");
            int choice = DataValidation.validateNumberChoiceInput("Choice(1-7)", 1, 7);

            switch (choice) {
                case 1 -> viewAllIceCream();
                case 2 -> sortIceCream();
                case 3 -> searchIceCream();
                case 4 -> addIceCream();// TODO METHOD adding new Ice Cream
                case 5 -> removeIceCream();
                case 6 -> updateIceCream();
                default -> isRunning = false;
            }
        }
    }

    public void addIceCream() {
        String prompt = "Choose which type of Ice Cream\n[1] In Cone\n[2] In Cup\nChoice (1-2): ";
        boolean isCone = DataValidation.validateNumberChoiceInput(prompt, 1, 2) == 1;
        String flavor = DataValidation.validateFlavorInput("Enter Ice Cream Flavor: ");
        prompt = "Choose Ice Cream Size\n[1] Small\n[2] Medium\n[3] Large\nChoice (1-3): ";
        int sizeChoice = DataValidation.validateNumberChoiceInput(prompt, 1, 3);
        int quantity = DataValidation.validateNumberChoiceInput("Enter Quantity (1-50): ", 1, 50);
        double price = DataValidation.validatePriceInput("Enter Price of Ice Cream(Php 30.00 - 150.00): ");
        char size = switch (sizeChoice) {
            case 1 -> 'S';
            case 2 -> 'M';
            default -> 'L';
        };
        IceCreamType iceCream = icm.addIceCream(isCone, flavor, size, quantity, price);
        System.out.println("Ice cream added successfully with ID " + iceCream.getIcecreamID() + ".");
    }

    public void removeIceCream() {
        if (icm.isEmpty()) {
            System.out.println("There are no ice creams to remove.");
            return;
        }
        String iceCreamID = DataValidation.validateStringInput("Enter Ice Cream ID to remove: ");
        IceCreamType removedIceCream = icm.removeIceCream(iceCreamID);
        if (removedIceCream == null) {
            System.out.println("Ice cream not found.");
            return;
        }
        System.out.println("Removed ice cream " + removedIceCream.getIcecreamID() + ".");
    }

    public void updateIceCream() {
        if (icm.isEmpty()) {
            System.out.println("There are no ice creams to update.");
            return;
        }
        String iceCreamID = DataValidation.validateStringInput("Enter Ice Cream ID to update: ");
        IceCreamType iceCream = icm.findByID(iceCreamID);
        if (iceCream == null) {
            System.out.println("Ice cream not found.");
            return;
        }

        String prompt = "Choose what to update\n[1] Quantity\n[2] Price\nChoice (1-2): ";
        int updateChoice = DataValidation.validateNumberChoiceInput(prompt, 1, 2);
        if (updateChoice == 1) {
            int oldQuantity = iceCream.getQuantity();
            int quantity;
            do {
                quantity = DataValidation.validateNumberChoiceInput("Enter new quantity (0-50): ", 0, 50);
                if (quantity == oldQuantity) {
                    System.out.println("Enter a quantity different from the current quantity.");
                }
            } while (quantity == oldQuantity);
            if (icm.updateQuantity(iceCreamID, quantity)) {
                System.out.printf("Quantity updated successfully from '%d' to '%d'.%n", oldQuantity, quantity);
            } else {
                System.out.println("Could not update the quantity.");
            }
            return;
        }

        double oldPrice = iceCream.getPrice();
        double price;
        do {
            price = DataValidation.validatePriceInput("Enter new price (Php 30.00 - 150.00): ");
            if (Double.compare(price, oldPrice) == 0) {
                System.out.println("Enter a price different from the current price.");
            }
        } while (Double.compare(price, oldPrice) == 0);
        if (icm.updatePrice(iceCreamID, price)) {
            System.out.printf("Price updated successfully from '%.2f' to '%.2f'.%n", oldPrice, price);
        } else {
            System.out.println("Could not update the price.");
        }
    }
}
