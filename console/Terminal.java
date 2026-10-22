package console;

import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * The only class that reads from or controls the terminal. Like real command-line tools,
 * it checks what it is connected to and behaves accordingly.
 *
 * Interactive mode - a person is typing in a real terminal window:
 *   every menu starts on a cleared screen, and results stay up until Enter is pressed.
 *
 * Plain mode - there is no real terminal (input comes from a file or a pipe, or the program
 * runs in an IDE's console), or the --plain option was given:
 *   - no screen clearing and no pauses, so the output can be saved and compared
 *   - answers read from a file or pipe are printed after their prompt (passwords excepted),
 *     so the saved output reads like a real terminal session
 */
public class Terminal {
    private static final String ESC = "\u001B[";

    // Used when there is no real terminal
    private static final BufferedReader input = new BufferedReader(new InputStreamReader(System.in));

    private static Console console;  // the real terminal, or null if there is none
    private static boolean screens;  // clear the screen for each menu and pause after results
    private static boolean ansi;     // the terminal understands escape codes (used to clear the screen)

    // A message that must still be visible after the next screen is cleared
    private static String notice;

    /**
     * Detects the terminal. Without this call everything runs in plain mode.
     * plain turns off screen clearing and pauses.
     */
    public static void init(boolean plain) {
        console = System.console();
        if (console != null && !isRealTerminal(console)) {
            console = null;
        }
        screens = console != null && !plain;
        ansi = screens && supportsAnsi();
    }

    // Java 22+ returns a Console even when input is redirected; isTerminal() tells them apart.
    // Older versions only return a Console for a real terminal, so it is real if the method is missing.
    private static boolean isRealTerminal(Console console) {
        try {
            return (Boolean) Console.class.getMethod("isTerminal").invoke(console);
        } catch (ReflectiveOperationException e) {
            return true;
        }
    }

    // The classic Windows console shows escape codes as garbage unless its host turns them on.
    // Windows Terminal, VS Code, ConEmu and Git Bash do; macOS and Linux terminals always do.
    private static boolean supportsAnsi() {
        String term = System.getenv("TERM");
        if ("dumb".equals(term)) {
            return false;
        }
        if (!isWindows()) {
            return true;
        }
        return System.getenv("WT_SESSION") != null || System.getenv("TERM_PROGRAM") != null
                || "ON".equalsIgnoreCase(System.getenv("ConEmuANSI")) || term != null;
    }

    private static boolean isWindows() {
        return System.getProperty("os.name").startsWith("Windows");
    }

    // ===================== Input =====================

    /** Shows the prompt and reads one line. Exits the program if there is no more input. */
    public static String readLine(String prompt) {
        System.out.print(prompt);
        System.out.flush();
        if (console != null) {
            return endOfInputIfNull(console.readLine());
        }
        boolean fromFile = isInputWaiting();
        String line = readInputLine();
        if (fromFile) {
            System.out.println(line); // nobody saw it being typed, so show it like a terminal would
        }
        return line;
    }

    /**
     * Reads a password. It stays visible while it is typed, like any other answer, but one
     * read from a file is not copied into the output, so the program never prints a password.
     */
    public static String readPassword(String prompt) {
        System.out.print(prompt);
        System.out.flush();
        if (console != null) {
            return endOfInputIfNull(console.readLine());
        }
        boolean fromFile = isInputWaiting();
        String line = readInputLine();
        if (fromFile) {
            System.out.println(); // finish the prompt line; the password itself is never printed
        }
        return line;
    }

    // An answer that is already waiting before its prompt is shown comes from a file or a pipe.
    // When a person types in an IDE console nothing is waiting yet, and the IDE already shows
    // what they type, so printing it again would show every answer twice.
    private static boolean isInputWaiting() {
        try {
            return input.ready();
        } catch (IOException e) {
            return false;
        }
    }

    private static String readInputLine() {
        try {
            return endOfInputIfNull(input.readLine());
        } catch (IOException e) {
            return endOfInputIfNull(null);
        }
    }

    // End of input: the end of an input file, or in a terminal Ctrl+D (macOS/Linux),
    // Ctrl+Z then Enter (Windows) or Ctrl+C (which Windows reports the same way)
    private static String endOfInputIfNull(String line) {
        if (line == null) {
            System.out.println();
            System.out.println(console != null ? "Exiting program." : "No more input. Exiting program.");
            System.exit(0);
        }
        return line;
    }

    // ===================== Screen =====================

    /** Starts a new screen: clears it in interactive mode, or prints a blank line in plain mode. */
    public static void newScreen() {
        if (!screens) {
            System.out.println();
        } else if (ansi) {
            System.out.print(ESC + "H" + ESC + "2J"); // cursor to the top left, then clear
            System.out.flush();
        } else if (isWindows()) {
            try {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } catch (IOException | InterruptedException e) {
                System.out.println(); // clearing is only cosmetic, so carry on without it
            }
        } else {
            System.out.println();
        }
    }

    /** Interactive mode: keeps the result on screen until Enter is pressed. */
    public static void pause() {
        if (screens) {
            readLine("\nPress Enter to continue...");
        }
    }

    /**
     * Shows a short message that must survive the next screen clear, such as "Cashier logged out.".
     * Interactive mode shows it just above the next menu prompt; plain mode prints it right away.
     */
    public static void notice(String message) {
        if (screens) {
            notice = message;
        } else {
            System.out.println(message);
        }
    }

    /** Prints the waiting notice, if any. Called just before a menu prompt. */
    public static void showNotice() {
        if (notice != null) {
            System.out.println(notice);
            notice = null;
        }
    }
}
