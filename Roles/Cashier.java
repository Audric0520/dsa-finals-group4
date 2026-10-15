
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

            int choice = DataValidation.validateNumberChoiceInput(
                    "Choice(1-5): ", 1, 5);

            switch (choice) {
                case 1 -> viewAllIceCream();
                case 2 -> sortIceCream();
                case 3 -> searchIceCream();
                case 4 -> inputCustomerOrder();
                case 5 -> isRunning = false;
            }
        }

        System.out.println("Logging out.");
    }

    private void inputCustomerOrder() {
        String[] flavors = {
            "Chocolate",
            "Vanilla",
            "Strawberry",
            "Cookies and Cream",
            "Mango"
        };

        double[] prices = {
            50.00,
            45.00,
            55.00,
            60.00,
            50.00
        };

        System.out.println("\n--- INPUT CUSTOMER ORDER ---");
        System.out.println("Available Ice Cream Products:");

        for (int i = 0; i < flavors.length; i++) {
            System.out.printf(
                    "[%d] %-20s PHP %.2f%n",
                    i + 1,
                    flavors[i],
                    prices[i]
            );
        }

        int choice = DataValidation.validateNumberChoiceInput(
                "Select Ice Cream (1-5): ", 1, 5);

        System.out.println("\n--- ORDER DETAILS ---");
        System.out.println(
                "Selected Ice Cream: " + flavors[choice - 1]
        );
        System.out.printf(
                "Price: PHP %.2f%n",
                prices[choice - 1]
        );
        System.out.println("Order selected successfully!");
    }
}
