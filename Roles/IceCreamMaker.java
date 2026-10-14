package Roles;

import Utilities.DataValidation;

public class IceCreamMaker extends User {
    public IceCreamMaker(String role) {
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
            System.out.println("[4] Take an Order");
            System.out.println("[5] Log Out");
            int choice = DataValidation.validateNumberChoiceInput("Choice(1-5)", 1, 5);

            switch (choice) {
                case 1 -> viewAllIceCream();
                case 2 -> sortIceCream();
                case 3 -> searchIceCream();
                // case 4 -> //TODO METHOD FOR TAKING ORDER
                default -> isRunning = false;
            }
        }
    }
}
