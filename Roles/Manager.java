package Roles;

import Utilities.DataValidation;

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
                default -> isRunning = false;
            }
        }
    }

    public void addIceCream() {
        String prompt = "Choose which type of Ice Cream\n[1] In Cone\n[2] In Cup\nChoice(1 or 2): ";
        int iceCreamChoice = DataValidation.validateNumberChoiceInput(prompt, 1, 2);
        String iceCreamID = DataValidation.validateStringInput("Enter Ice Cream ID");// TODO CHANGE METHOD
        prompt = "Choose Ice Cream Size\n[1] Small\n[2] Medium\n[3] Large\nChoice (1-3): ";
        int sizeChoice = DataValidation.validateNumberChoiceInput(prompt, 1, 3);
    }
}
