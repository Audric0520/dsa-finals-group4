package Roles;

import Utilities.DataValidation;

public class Cashier extends User {
    public Cashier(String role) {
        super(role);
    }

    @Override
    public void landingPage() {
        boolean isRunning = true;
        while (isRunning) {
            System.out.println("\n--- CASHIER MENU ---");
            System.out.println("[1] View All Ice Cream Products");
            System.out.println("[2] Sort Ice Cream Products");
            System.out.println("[3] Find Specific Ice Cream Product");
            System.out.println("[4] Input Customer Order");
            System.out.println("[5] Log Out");
            int choice = DataValidation.validateNumberChoiceInput("Choice(1-5)", 1, 5);

            switch (choice) {
                case 1 -> viewAllIceCream();
                case 2 -> sortIceCream();
                case 3 -> searchIceCream();
                // case 4 -> //TODO METHOD FOR INPUT ORDER
                default -> isRunning = false;
            }
        }
        System.out.println("Logging out.");
    }
}
