package console;

import models.Product;

/**
 * Reads and validates console input. Every method keeps asking until the input is valid,
 * so a typo never crashes the program. The actual reading is done by Terminal.
 *
 * Except for readMenuChoice, readLine and readPassword, every method lets the user type C
 * to cancel. Cancelling throws OperationCancelledException, which the role menu catches.
 */
public class InputHelper {
    public static final String CANCEL = "C";

    /** Reads one trimmed line with no validation and no cancel handling. */
    public static String readLine(String prompt) {
        return Terminal.readLine(prompt).trim();
    }

    /** Reads a line without showing what is typed. */
    public static String readPassword(String prompt) {
        return Terminal.readPassword(prompt).trim();
    }

    /**
     * Menu choices do not support C, because [0] already means log out or exit.
     * Like a shell, pressing Enter on an empty line just shows the prompt again,
     * and q, quit or exit work the same as [0].
     */
    public static int readMenuChoice(int max) {
        Terminal.showNotice();
        while (true) {
            String text = readLine("Choice [0-" + max + "]: ");
            if (text.isEmpty()) {
                continue;
            }
            if (text.equalsIgnoreCase("q") || text.equalsIgnoreCase("quit") || text.equalsIgnoreCase("exit")) {
                return 0;
            }
            Integer number = parseIntInRange(text, 0, max);
            if (number != null) {
                return number;
            }
            Terminal.error(String.format("Invalid choice \"%s\". Please enter a number from 0 to %d.", text, max));
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
            Terminal.error("Input cannot be empty. Try again.");
        }
    }

    public static int readInt(String prompt, int min, int max) {
        while (true) {
            Integer number = parseIntInRange(readText(prompt), min, max);
            if (number != null) {
                return number;
            }
            Terminal.error(String.format("Please enter a whole number from %d to %d.", min, max));
        }
    }

    /** Returns the number, or null if the text is not a whole number in the range. */
    public static Integer parseIntInRange(String text, int min, int max) {
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
            Double price = parsePrice(readText(prompt));
            if (price != null) {
                return price;
            }
            Terminal.error(String.format("Invalid price. Enter an amount from P%.2f to P%.2f with at most"
                    + " 2 decimal places, e.g. 45 or 45.50.", Product.MIN_PRICE, Product.MAX_PRICE));
        }
    }

    /**
     * Returns the price, or null if the text is not a valid price. A leading P is allowed,
     * because the prompt shows one. Only plain amounts with at most 2 decimal places are
     * accepted, so "1e2" or "45.999" are rejected instead of being read or rounded silently.
     */
    public static Double parsePrice(String text) {
        String amount = text.startsWith("P") || text.startsWith("p") ? text.substring(1).trim() : text;
        if (!amount.matches("\\d{1,3}(\\.\\d{1,2})?")) {
            return null;
        }
        double price = Double.parseDouble(amount);
        return Product.isValidPrice(price) ? price : null;
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
            Terminal.error("Product ID must look like IC001 (IC + 3 digits).");
        }
    }

    public static String readFlavor(String prompt) {
        while (true) {
            String flavor = formatFlavor(readText(prompt));
            if (flavor.matches("[a-zA-Z ]+") && flavor.length() <= 18) {
                return flavor;
            }
            Terminal.error("Flavor must contain only letters and spaces (max 18 characters).");
        }
    }

    /**
     * Writes every flavor the same way, however it was typed: single spaces, and each word
     * capitalised except small joining words, e.g. "cookies  AND cream" -> "Cookies and Cream".
     */
    public static String formatFlavor(String text) {
        StringBuilder flavor = new StringBuilder();
        for (String word : text.trim().split("\\s+")) {
            String lower = word.toLowerCase();
            boolean joiningWord = lower.equals("and") || lower.equals("or") || lower.equals("of")
                    || lower.equals("with");
            if (flavor.length() > 0) {
                flavor.append(' ');
            }
            if (joiningWord && flavor.length() > 0) {
                flavor.append(lower);
            } else {
                flavor.append(Character.toUpperCase(lower.charAt(0))).append(lower.substring(1));
            }
        }
        return flavor.toString();
    }

    public static char readServingSize(String prompt) {
        while (true) {
            String size = readText(prompt).toUpperCase();
            if (size.equals("S") || size.equals("M") || size.equals("L")) {
                return size.charAt(0);
            }
            Terminal.error("Serving size must be S, M or L.");
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
            Terminal.error("Container type must be cone or cup.");
        }
    }

    /**
     * Asks a yes/no question. As in most command-line tools, the capital letter in [Y/n] or
     * [y/N] is the default answer, used when the user just presses Enter.
     */
    public static boolean readYesNo(String question, boolean defaultAnswer) {
        String choices = defaultAnswer ? "[Y/n]" : "[y/N]";
        while (true) {
            String answer = readLine(question + " " + choices + " (or C to cancel): ").toUpperCase();
            if (answer.equals(CANCEL)) {
                throw new OperationCancelledException();
            }
            if (answer.isEmpty()) {
                return defaultAnswer;
            }
            if (answer.equals("Y") || answer.equals("YES")) {
                return true;
            }
            if (answer.equals("N") || answer.equals("NO")) {
                return false;
            }
            Terminal.error("Please enter Y or N, press Enter for " + (defaultAnswer ? "Y" : "N")
                    + ", or type C to cancel.");
        }
    }
}
