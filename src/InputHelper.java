import java.util.Scanner;

/**
 * Reads and validates console input. Every method keeps asking until the input is valid,
 * so a typo never crashes the program.
 *
 * Except for readMenuChoice and readLine, every method lets the user type C to cancel.
 * Cancelling throws OperationCancelledException, which the role menu catches.
 */
public class InputHelper {
    public static final String CANCEL = "C";

    private static final Scanner scanner = new Scanner(System.in);

    /** Reads one trimmed line with no validation and no cancel handling. */
    public static String readLine(String prompt) {
        System.out.print(prompt);
        if (!scanner.hasNextLine()) {
            // Input stream closed (e.g. end of a test input file): exit cleanly
            System.out.println();
            System.out.println("No more input. Exiting program.");
            System.exit(0);
        }
        return scanner.nextLine().trim();
    }

    /** Menu choices do not support C, because [0] already means log out or exit. */
    public static int readMenuChoice(int max) {
        while (true) {
            String text = readLine("Choice: ");
            Integer number = parseIntInRange(text, 0, max);
            if (number != null) {
                return number;
            }
            System.out.printf("Invalid choice. Please enter a number from 0 to %d.%n", max);
        }
    }

    /** Reads a non-empty line. Throws OperationCancelledException if the user types C. */
    public static String readText(String prompt) {
        while (true) {
            String text = readLine(prompt);
            if (text.equalsIgnoreCase(CANCEL)) {
                throw new OperationCancelledException();
            }
            if (!text.isEmpty()) {
                return text;
            }
            System.out.println("Input cannot be empty. Try again.");
        }
    }

    public static int readInt(String prompt, int min, int max) {
        while (true) {
            Integer number = parseIntInRange(readText(prompt), min, max);
            if (number != null) {
                return number;
            }
            System.out.printf("Please enter a whole number from %d to %d.%n", min, max);
        }
    }

    // Returns the number, or null if the text is not a whole number in the range.
    private static Integer parseIntInRange(String text, int min, int max) {
        try {
            int number = Integer.parseInt(text);
            if (number >= min && number <= max) {
                return number;
            }
        } catch (NumberFormatException e) {
            // not a number, handled by the caller
        }
        return null;
    }

    /** Price must be from Product.MIN_PRICE to Product.MAX_PRICE (inclusive). */
    public static double readPrice(String prompt) {
        while (true) {
            String text = readText(prompt);
            try {
                double price = Double.parseDouble(text);
                if (Product.isValidPrice(price)) {
                    return Math.round(price * 100) / 100.0;
                }
            } catch (NumberFormatException e) {
                // falls through to the error message below
            }
            System.out.printf("Invalid price. Price must be from P%.2f to P%.2f.%n", Product.MIN_PRICE,
                    Product.MAX_PRICE);
        }
    }

    /** Checks the format IC followed by 3 digits, e.g. IC001. */
    public static boolean isValidProductId(String id) {
        return id.matches("IC\\d{3}");
    }

    public static String readProductId(String prompt) {
        while (true) {
            String id = readText(prompt).toUpperCase();
            if (isValidProductId(id)) {
                return id;
            }
            System.out.println("Product ID must look like IC001 (IC + 3 digits).");
        }
    }

    public static String readFlavor(String prompt) {
        while (true) {
            String flavor = readText(prompt);
            if (flavor.matches("[a-zA-Z ]+") && flavor.length() <= 18) {
                return flavor;
            }
            System.out.println("Flavor must contain only letters and spaces (max 18 characters).");
        }
    }

    public static char readServingSize(String prompt) {
        while (true) {
            String size = readText(prompt).toUpperCase();
            if (size.equals("S") || size.equals("M") || size.equals("L")) {
                return size.charAt(0);
            }
            System.out.println("Serving size must be S, M or L.");
        }
    }

    public static String readContainerType(String prompt) {
        while (true) {
            String type = readText(prompt).toLowerCase();
            if (type.equals("cone")) {
                return "Cone";
            }
            if (type.equals("cup")) {
                return "Cup";
            }
            System.out.println("Container type must be cone or cup.");
        }
    }

    public static boolean readYesNo(String prompt) {
        while (true) {
            String answer = readText(prompt).toUpperCase();
            if (answer.equals("Y") || answer.equals("YES")) {
                return true;
            }
            if (answer.equals("N") || answer.equals("NO")) {
                return false;
            }
            System.out.println("Please enter Y or N (or C to cancel).");
        }
    }
}
