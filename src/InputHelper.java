import java.util.Scanner;

/**
 * Reads and validates console input. Every method keeps asking until the input is valid,
 * so a typo never crashes the program.
 */
public class InputHelper {
    private static final Scanner scanner = new Scanner(System.in);

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

    public static String readNonEmpty(String prompt) {
        while (true) {
            String text = readLine(prompt);
            if (!text.isEmpty()) {
                return text;
            }
            System.out.println("Input cannot be empty. Try again.");
        }
    }

    public static int readInt(String prompt, int min, int max) {
        while (true) {
            String text = readLine(prompt);
            try {
                int number = Integer.parseInt(text);
                if (number >= min && number <= max) {
                    return number;
                }
            } catch (NumberFormatException e) {
                // falls through to the error message below
            }
            System.out.printf("Please enter a whole number from %d to %d.%n", min, max);
        }
    }

    public static double readPrice(String prompt) {
        while (true) {
            String text = readLine(prompt);
            try {
                double price = Double.parseDouble(text);
                if (price > 0 && price <= 9999.99) {
                    return Math.round(price * 100) / 100.0;
                }
            } catch (NumberFormatException e) {
                // falls through to the error message below
            }
            System.out.println("Price must be a number greater than 0 and at most 9999.99.");
        }
    }

    /** Product IDs use the format IC followed by 3 digits, e.g. IC001. */
    public static String readProductId(String prompt) {
        while (true) {
            String id = readLine(prompt).toUpperCase();
            if (id.matches("IC\\d{3}")) {
                return id;
            }
            System.out.println("Product ID must look like IC001 (IC + 3 digits).");
        }
    }

    public static String readFlavor(String prompt) {
        while (true) {
            String flavor = readNonEmpty(prompt);
            if (flavor.matches("[a-zA-Z ]+") && flavor.length() <= 18) {
                return flavor;
            }
            System.out.println("Flavor must contain only letters and spaces (max 18 characters).");
        }
    }

    public static char readServingSize(String prompt) {
        while (true) {
            String size = readLine(prompt).toUpperCase();
            if (size.equals("S") || size.equals("M") || size.equals("L")) {
                return size.charAt(0);
            }
            System.out.println("Serving size must be S, M or L.");
        }
    }

    public static String readContainerType(String prompt) {
        while (true) {
            String type = readLine(prompt).toLowerCase();
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
            String answer = readLine(prompt).toUpperCase();
            if (answer.equals("Y") || answer.equals("YES")) {
                return true;
            }
            if (answer.equals("N") || answer.equals("NO")) {
                return false;
            }
            System.out.println("Please enter Y or N.");
        }
    }
}
