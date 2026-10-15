package Utilities;

import java.util.Scanner;

public class DataValidation {
    public static final Scanner input = new Scanner(System.in);

    public static int validateIntInput(String prompt) {
        boolean isRunning = true;
        int number = 0;
        String inputNumber = "";
        while (isRunning) {
            System.out.print(prompt);
            inputNumber = input.nextLine().trim();
            if (!inputNumber.matches("-?(0|[1-9]\\d*)")) {
                System.out.println("Invalid Input. Only input positive integers without leading zeros. Try Again.");
                continue;
            }
            try {
                number = Integer.parseInt(inputNumber);
            } catch (NumberFormatException e) {
                System.out.println("Invalid Input. Number is too large. Try Again.");
                continue;
            }
            isRunning = false;
        }
        return number;
    }

    public static int validateNumberChoiceInput(String prompt, int min, int max) {
        boolean isRunning = true;
        int number = 0;
        while (isRunning) {
            number = validateIntInput(prompt);
            if (number >= min & number <= max) {
                isRunning = false;
            } else {
                System.out.printf("Invalid Input. Only input %d-%d without leading zeros. Try Again.\n", min, max);
            }
        }
        return number;
    }

    public static String validateStringInput(String prompt) {
        boolean isRunning = true;
        String stringInput = "";
        while (isRunning) {
            System.out.print(prompt);
            stringInput = input.nextLine().trim();
            if (stringInput.isBlank()) {
                System.out.println("Invalid Input. Input cannot be blank. Try Again.");
            } else {
                isRunning = false;
            }
        }
        return stringInput;
    }

    public static String validateFlavorInput(String prompt) {
        boolean isRunning = true;
        String name = "";
        while (isRunning) {
            name = DataValidation.validateStringInput(prompt);
            if (!name.matches("^[a-zA-Z ]+$")) {
                System.out.println(
                        "Invalid Ice Cream Flavor Input. Flavor must only contain letters(e.g. Chocolate, Vanilla). Try Again.");
                continue;
            } else {
                isRunning = false;
            }
        }
        return name;
    }

    public static double validatePriceInput(String prompt) {
        boolean isRunning = true;
        double number = 0;
        String inputNumber = "";

        while (isRunning) {
            System.out.print(prompt);
            inputNumber = input.nextLine().trim();

            if (!inputNumber.matches("-?(0|[1-9]\\d*)(\\.\\d+)?")) {
                System.out.println("Invalid Input. Only input numbers without leading zeroes. Try Again.");
                continue;
            }
            number = Double.parseDouble(inputNumber);
            if (number < 30 || number > 150) {
                System.out.println("Invalid Price Input. Price must be between P30.00 - P150.00. Try Again.");
                continue;
            }
            isRunning = false;
        }
        return number;
    }
}