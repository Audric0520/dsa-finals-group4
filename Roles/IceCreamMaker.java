package Roles;

import java.util.LinkedList;
import java.util.Queue;

import Utilities.DataValidation;

public class IceCreamMaker extends User {
    // Queue of pending orders (FIFO). The Cashier adds orders with:
    // IceCreamMaker.pendingOrders.add("Order #1: 2x Vanilla - P120.00");
    public static final Queue<String> pendingOrders = new LinkedList<>();

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

            int choice = DataValidation.validateNumberChoiceInput(
                "Choice(1-5)", 1, 5
            );

            switch (choice) {
                case 1 -> viewAllIceCream();
                case 2 -> sortIceCream();
                case 3 -> searchIceCream();
                case 4 -> takeOrder();
                default -> isRunning = false;
            }
        }
    }

    private void takeOrder() {
        if (pendingOrders.isEmpty()) {
            System.out.println("No pending orders.");
            return;
        }

        System.out.println("\n--- NEXT ORDER (oldest first) ---");
        System.out.println("Pending orders: " + pendingOrders.size());
        System.out.println(pendingOrders.peek()); // look at the oldest order, do not remove yet

        System.out.println("Process this order? [1] Yes  [2] No");
        int confirm = DataValidation.validateNumberChoiceInput("Choice(1-2)", 1, 2);

        if (confirm == 1) {
            String done = pendingOrders.poll(); // removes the oldest order
            System.out.println("COMPLETED: " + done);
        } else {
            System.out.println("Order left in the queue.");
        }
    }
}