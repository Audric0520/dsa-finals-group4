import Roles.Cashier;
import Roles.IceCreamMaker;
import Roles.Manager;
import Utilities.DataValidation;

public class Main {
    private static final String CASHIER_PASSWORD = "123ABC";// TODO CHANGE PASSWORD
    private static final String ICM_PASSWORD = "123ABC";// TODO CHANGE PASSWORD
    private static final String MANAGER_PASSWORD = "123ABC";// TODO CHANGE PASSWORD

    public static void main(String[] args) {
        boolean isRunning = true;
        while (isRunning) {
            System.out.println("=== ICE CREAM MANAGEMENT SYSTEM ===");// TODO CHANGE NAME
            System.out.println("[1] Cashier");
            System.out.println("[2] Ice Cream Maker");
            System.out.println("[3] Manager");
            System.out.println("[4] Exit Program");
            int choice = DataValidation.validateNumberChoiceInput("Choice(1-4): ", 1, 4);

            switch (choice) {
                case 1 -> cashierFunction();
                case 2 -> iceCreamMakerFunction();
                case 3 -> managerFunction();
                default -> isRunning = false;
            }
        }
        System.out.println("Thank you for using the program. Exiting.");
    }

    public static void cashierFunction() {
        String password = DataValidation.validateStringInput("Enter Cashier Password: ");
        if (!password.equals(CASHIER_PASSWORD)) {
            System.out.println("Access Denied. Wrong Password.");
            return;
        }
        System.out.println("Access Granted");
        Cashier cashier = new Cashier("Cashier");
        cashier.landingPage();
    }

    public static void iceCreamMakerFunction() {
        String password = DataValidation.validateStringInput("Enter Ice Cream Maker Password: ");
        if (!password.equals(ICM_PASSWORD)) {
            System.out.println("Access Denied. Wrong Password.");
            return;
        }
        System.out.println("Access Granted");
        IceCreamMaker iceCreamMaker = new IceCreamMaker("Ice Cream Maker");
        iceCreamMaker.landingPage();
    }

    public static void managerFunction() {
        String password = DataValidation.validateStringInput("Enter Manager Password: ");
        if (!password.equals(MANAGER_PASSWORD)) {
            System.out.println("Access Denied. Wrong Password.");
            return;
        }
        System.out.println("Access Granted");
        Manager manager = new Manager("Manager");
        manager.landingPage();
    }
}