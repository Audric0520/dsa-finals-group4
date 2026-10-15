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

<<<<<<< HEAD
    public static String validateFieldChoice(String prompt) {
        int choice = validateNumberChoiceInput(prompt, 1, 2);
        return (choice == 1) ? "quantity" : "price";
    }

    public static boolean validateSortOrderChoice(String prompt) {
        int choice = validateNumberChoiceInput(prompt, 1, 2);
        return choice == 1;
    }
=======
    public static int validatePositiveIntInput(String prompt) {
        boolean isRunning = true;
        int number = 0;
        while (isRunning) {
            number = validateIntInput(prompt);
            if (number <= 0) {
                System.out.println("Invalid Input. Number must be greater than 0. Try Again.");
            } else {
                isRunning = false;
            }
        }
        return number;
    }

    public static int validateQuantityInput(String prompt, int min, int max) {
        boolean isRunning = true;
        int number = 0;
        while (isRunning) {
            number = validateIntInput(prompt);
            if (number < min || number > max) {
                System.out.printf("Invalid Quantity Input. Quantity must be between %d-%d. Try Again.\n", min, max);
            } else {
                isRunning = false;
            }
        }
        return number;
    }

    public static char validateSizeInput(String prompt) {
        boolean isRunning = true;
        String sizeInput = "";
        while (isRunning) {
            sizeInput = validateStringInput(prompt).toUpperCase();
            if (!sizeInput.matches("^[SML]$")) {
                System.out.println("Invalid Size Input. Only input S(Small), M(Medium) or L(Large). Try Again.");
            } else {
                isRunning = false;
            }
        }
        return sizeInput.charAt(0);
    }

    public static String validateIceCreamIDInput(String prompt) {
        boolean isRunning = true;
        String id = "";
        while (isRunning) {
            id = validateStringInput(prompt).toUpperCase();
            if (!id.matches("^IC\\d{3}$")) {
                System.out.println("Invalid Ice Cream ID Input. ID must follow the format IC### (e.g. IC001). Try Again.");
            } else {
                isRunning = false;
            }
        }
        return id;
    }

    public static boolean validateYesNoInput(String prompt) {
        boolean isRunning = true;
        String answer = "";
        while (isRunning) {
            answer = validateStringInput(prompt).toUpperCase();
            if (!answer.matches("^(Y|N|YES|NO)$")) {
                System.out.println("Invalid Input. Only input Y(Yes) or N(No). Try Again.");
            } else {
                isRunning = false;
            }
        }
        return answer.startsWith("Y");
    }

    public static String validateSearchKeywordInput(String prompt) {
        boolean isRunning = true;
        String keyword = "";
        while (isRunning) {
            keyword = validateStringInput(prompt);
            if (!keyword.matches("^[a-zA-Z0-9 ]+$")) {
                System.out.println("Invalid Search Input. Only letters, numbers and spaces are allowed. Try Again.");
            } else {
                isRunning = false;
            }
        }
        return keyword;
    }

>>>>>>> 76ae9e9a59d115e6e5ccbf10f5362ca6e9dc215a
}