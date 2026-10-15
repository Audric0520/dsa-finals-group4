package Roles;

import java.util.ArrayList;

import IceCream.IceCreamType;
import Utilities.DataValidation;
import Utilities.IceCreamManager;
import Utilities.Order;
import Utilities.OrderQueue;

public class IceCreamMaker extends User {
    private final IceCreamManager inventory;
    private final OrderQueue orderQueue;

    public IceCreamMaker(String role, IceCreamManager inventory, OrderQueue orderQueue) {
        super(role);
        this.inventory = inventory;
        this.orderQueue = orderQueue;
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
                case 4 -> takeOrder();
                default -> isRunning = false;
            }
        }
    }

    //1. View all ice cream products (displayed in a table format) 

    @Override
    public void viewAllIceCream() {
        ArrayList<IceCreamType> iceCreams = inventory.getAllIceCreams();
        if (iceCreams.isEmpty()) {
            System.out.println("No ice cream products available.");
            return;
        }
        printIceCreams(iceCreams);
    }



    //2. Sort ice cream products by price or quantity (ascending or descending order)
    @Override
    public void sortIceCream() {
        ArrayList<IceCreamType> iceCreams = inventory.getAllIceCreams();
        if (iceCreams.isEmpty()) {
            System.out.println("No ice cream products available to sort.");
            return;
        }

        System.out.println("Sort by: [1] Price  [2] Quantity");
        int sortChoice = DataValidation.validateNumberChoiceInput("Choice(1-2): ", 1, 2);
        System.out.println("Order: [1] Lowest to Highest  [2] Highest to Lowest");
        int orderChoice = DataValidation.validateNumberChoiceInput("Choice(1-2): ", 1, 2);

        boolean byPrice = (sortChoice == 1);
        boolean ascending = (orderChoice == 1);

        insertionSort(iceCreams, byPrice, ascending);
        printIceCreams(iceCreams);
    }

    private void insertionSort(ArrayList<IceCreamType> list, boolean byPrice, boolean ascending) {
        for (int i = 1; i < list.size(); i++) {
            IceCreamType key = list.get(i);
            int j = i - 1;
            // Shift items that belong AFTER the key one step to the right
            while (j >= 0 && isOutOfOrder(list.get(j), key, byPrice, ascending)) {
                list.set(j + 1, list.get(j));
                j--;
            }
            list.set(j + 1, key);
        }
    }

    // Returns true if 'left' should come after 'right' in the chosen order
    private boolean isOutOfOrder(IceCreamType left, IceCreamType right, boolean byPrice, boolean ascending) {
        int comparison;
        if (byPrice) {
            comparison = Double.compare(left.getPrice(), right.getPrice());
        } else {
            comparison = Integer.compare(left.getQuantity(), right.getQuantity());
        }
        return ascending ? comparison > 0 : comparison < 0;
    }

    //3. Find a specific ice cream product by ID or flavor (case-insensitive)
    @Override
    public void searchIceCream() {
        ArrayList<IceCreamType> iceCreams = inventory.getAllIceCreams();
        if (iceCreams.isEmpty()) {
            System.out.println("No ice cream products available to search.");
            return;
        }

        System.out.println("Search by: [1] Ice Cream ID  [2] Flavor");
        int searchChoice = DataValidation.validateNumberChoiceInput("Choice(1-2): ", 1, 2);

        ArrayList<IceCreamType> results;
        if (searchChoice == 1) {
            String id = DataValidation.validateStringInput("Enter Ice Cream ID: ");
            results = linearSearch(iceCreams, id, true);
        } else {
            String flavor = DataValidation.validateFlavorInput("Enter Flavor: ");
            results = linearSearch(iceCreams, flavor, false);
        }

        if (results.isEmpty()) {
            System.out.println("No matching ice cream product found.");
        } else {
            printIceCreams(results);
        }
    }

    // Checks every product from the first to the last and collects the matches
    private ArrayList<IceCreamType> linearSearch(ArrayList<IceCreamType> list, String keyword, boolean byID) {
        ArrayList<IceCreamType> matches = new ArrayList<>();
        String target = keyword.toLowerCase();
        for (int i = 0; i < list.size(); i++) {
            IceCreamType item = list.get(i);
            if (byID) {
                if (item.getIcecreamID().equalsIgnoreCase(keyword)) {
                    matches.add(item);
                }
            } else {
                if (item.getFlavor().toLowerCase().contains(target)) {
                    matches.add(item);
                }
            }
        }
        return matches;
    }

    //4. Take an order from the queue (oldest order first) and mark it as completed
    private void takeOrder() {
        if (orderQueue.isEmpty()) {
            System.out.println("No pending orders.");
            return;
        }

        Order nextOrder = orderQueue.peek(); // oldest order, not removed yet
        IceCreamType item = nextOrder.getIceCream();

        System.out.println("\n--- NEXT ORDER (oldest first) ---");
        System.out.println("Pending orders in queue: " + orderQueue.size());
        System.out.println("Order ID    : " + nextOrder.getOrderID());
        System.out.printf("Item        : %s - %s (%c)%n",
                item.getClass().getSimpleName(), item.getFlavor(), item.getSize());
        System.out.println("Quantity    : " + nextOrder.getQuantity());
        System.out.printf("Unit Price  : P%.2f%n", item.getPrice());
        System.out.printf("Total Price : P%.2f%n", nextOrder.getTotalPrice());
        System.out.println("Status      : " + nextOrder.getStatus());

        System.out.println("Process this order? [1] Yes  [2] No");
        int confirm = DataValidation.validateNumberChoiceInput("Choice(1-2): ", 1, 2);

        if (confirm == 1) {
            Order finished = orderQueue.dequeue(); // removes the same order we peeked
            finished.setStatus(Order.COMPLETED);
            System.out.println("Order " + finished.getOrderID() + " is now " + finished.getStatus() + ".");
        } else {
            System.out.println("Order left in the queue.");
        }
    }

    // Helper method to print ice cream products in a table format
    
    private void printIceCreams(ArrayList<IceCreamType> list) {
        System.out.printf("%n%-8s %-8s %-15s %-5s %-5s %s%n", "ID", "Type", "Flavor", "Size", "Qty", "Price");
        for (int i = 0; i < list.size(); i++) {
            IceCreamType item = list.get(i);
            System.out.printf("%-8s %-8s %-15s %-5c %-5d P%.2f%n",
                    item.getIcecreamID(), item.getClass().getSimpleName(), item.getFlavor(),
                    item.getSize(), item.getQuantity(), item.getPrice());
        }
    }
}