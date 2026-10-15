package Utilities;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

import IceCream.IceCreamType;
import IceCream.InCone;
import IceCream.InCup;

public class IceCreamManager {
    private static final int MAX_STOCK = 999;
    private static IceCreamManager instance;

    // ArrayList keeps the insertion order, HashMap gives O(1) lookup by ID
    private final ArrayList<IceCreamType> iceCreams = new ArrayList<>();
    private final HashMap<String, IceCreamType> iceCreamIndex = new HashMap<>();
    // Queue for orders waiting to be made (FIFO)
    private final Queue<Order> pendingOrders = new LinkedList<>();
    // Stack of removed ice creams so the last removal can be undone (LIFO)
    private final ArrayDeque<IceCreamType> removedIceCreams = new ArrayDeque<>();
    private final ArrayList<Order> completedOrders = new ArrayList<>();
    private int nextIceCreamNumber = 1;
    private int nextOrderNumber = 1;

    public static IceCreamManager getInstance() {
        if (instance == null) {
            instance = new IceCreamManager();
        }
        return instance;
    }

    public ArrayList<IceCreamType> getAllIceCreams() {
        return new ArrayList<>(iceCreams);
    }

    public int getIceCreamCount() {
        return iceCreams.size();
    }

    public boolean isEmpty() {
        return iceCreams.isEmpty();
    }

    // ===================== ADD / UPDATE / REMOVE =====================

    public String generateIceCreamID() {
        String id;
        do {
            id = String.format("IC%03d", nextIceCreamNumber++);
        } while (iceCreamIndex.containsKey(id));
        return id;
    }

    public boolean addIceCream(IceCreamType iceCream) {
        if (iceCream == null || iceCreamIndex.containsKey(iceCream.getIcecreamID())) {
            return false;
        }
        iceCreams.add(iceCream);
        iceCreamIndex.put(iceCream.getIcecreamID(), iceCream);
        return true;
    }

    public IceCreamType addIceCream(boolean isCone, String flavor, char size, int quantity, double price) {
        String id = generateIceCreamID();
        IceCreamType iceCream = isCone
                ? new InCone(id, flavor, size, quantity, price)
                : new InCup(id, flavor, size, quantity, price);
        addIceCream(iceCream);
        return iceCream;
    }

    public IceCreamType removeIceCream(String id) {
        IceCreamType iceCream = findByID(id);
        if (iceCream != null) {
            iceCreamIndex.remove(iceCream.getIcecreamID());
            iceCreams.remove(iceCream);
            removedIceCreams.push(iceCream);
        }
        return iceCream;
    }

    public IceCreamType undoLastRemoval() {
        while (!removedIceCreams.isEmpty()) {
            IceCreamType iceCream = removedIceCreams.pop();
            if (addIceCream(iceCream)) {
                return iceCream;
            }
        }
        return null;
    }

    public boolean updateFlavor(String id, String flavor) {
        IceCreamType iceCream = findByID(id);
        if (iceCream == null) {
            return false;
        }
        iceCream.setFlavor(flavor);
        return true;
    }

    public boolean updateSize(String id, char size) {
        IceCreamType iceCream = findByID(id);
        if (iceCream == null) {
            return false;
        }
        iceCream.setSize(size);
        return true;
    }

    public boolean updatePrice(String id, double price) {
        IceCreamType iceCream = findByID(id);
        if (iceCream == null) {
            return false;
        }
        iceCream.setPrice(price);
        return true;
    }

    public boolean restock(String id, int amount) {
        IceCreamType iceCream = findByID(id);
        if (iceCream == null || amount <= 0 || iceCream.getQuantity() + amount > MAX_STOCK) {
            return false;
        }
        iceCream.setQuantity(iceCream.getQuantity() + amount);
        return true;
    }

    // ===================== SEARCHING =====================

    // Hash lookup - O(1)
    public IceCreamType findByID(String id) {
        if (id == null) {
            return null;
        }
        return iceCreamIndex.get(id.toUpperCase());
    }

    // Linear search - O(n), matches part of the flavor (case-insensitive)
    public ArrayList<IceCreamType> linearSearchByFlavor(String keyword) {
        ArrayList<IceCreamType> results = new ArrayList<>();
        String lowerKeyword = keyword.toLowerCase();
        for (IceCreamType iceCream : iceCreams) {
            if (iceCream.getFlavor().toLowerCase().contains(lowerKeyword)) {
                results.add(iceCream);
            }
        }
        return results;
    }

    // Linear search - O(n)
    public ArrayList<IceCreamType> linearSearchBySize(char size) {
        ArrayList<IceCreamType> results = new ArrayList<>();
        for (IceCreamType iceCream : iceCreams) {
            if (Character.toUpperCase(iceCream.getSize()) == Character.toUpperCase(size)) {
                results.add(iceCream);
            }
        }
        return results;
    }

    // Linear search - O(n)
    public ArrayList<IceCreamType> linearSearchByPriceRange(double minPrice, double maxPrice) {
        ArrayList<IceCreamType> results = new ArrayList<>();
        for (IceCreamType iceCream : iceCreams) {
            if (iceCream.getPrice() >= minPrice && iceCream.getPrice() <= maxPrice) {
                results.add(iceCream);
            }
        }
        return results;
    }

    // Binary search - O(log n), list is sorted by ID first
    public IceCreamType binarySearchByID(String id) {
        ArrayList<IceCreamType> sorted = mergeSort(getAllIceCreams(), byID());
        String target = id.toUpperCase();
        int low = 0;
        int high = sorted.size() - 1;
        while (low <= high) {
            int mid = low + (high - low) / 2;
            int comparison = sorted.get(mid).getIcecreamID().compareTo(target);
            if (comparison == 0) {
                return sorted.get(mid);
            } else if (comparison < 0) {
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }
        return null;
    }

    public ArrayList<IceCreamType> getLowStock(int threshold) {
        ArrayList<IceCreamType> results = new ArrayList<>();
        for (IceCreamType iceCream : iceCreams) {
            if (iceCream.getQuantity() <= threshold) {
                results.add(iceCream);
            }
        }
        return results;
    }

    // ===================== SORTING =====================

    public static Comparator<IceCreamType> byID() {
        return Comparator.comparing(IceCreamType::getIcecreamID);
    }

    public static Comparator<IceCreamType> byFlavor() {
        return Comparator.comparing(iceCream -> iceCream.getFlavor().toLowerCase());
    }

    public static Comparator<IceCreamType> bySize() {
        return Comparator.comparingInt(iceCream -> sizeRank(iceCream.getSize()));
    }

    public static Comparator<IceCreamType> byQuantity() {
        return Comparator.comparingInt(IceCreamType::getQuantity);
    }

    public static Comparator<IceCreamType> byPrice() {
        return Comparator.comparingDouble(IceCreamType::getPrice);
    }

    private static int sizeRank(char size) {
        return switch (Character.toUpperCase(size)) {
            case 'S' -> 1;
            case 'M' -> 2;
            case 'L' -> 3;
            default -> 4;
        };
    }

    // Merge sort - O(n log n), stable
    public ArrayList<IceCreamType> mergeSort(ArrayList<IceCreamType> list, Comparator<IceCreamType> comparator) {
        if (list.size() <= 1) {
            return list;
        }
        int mid = list.size() / 2;
        ArrayList<IceCreamType> left = mergeSort(new ArrayList<>(list.subList(0, mid)), comparator);
        ArrayList<IceCreamType> right = mergeSort(new ArrayList<>(list.subList(mid, list.size())), comparator);

        ArrayList<IceCreamType> merged = new ArrayList<>(list.size());
        int i = 0;
        int j = 0;
        while (i < left.size() && j < right.size()) {
            if (comparator.compare(left.get(i), right.get(j)) <= 0) {
                merged.add(left.get(i++));
            } else {
                merged.add(right.get(j++));
            }
        }
        while (i < left.size()) {
            merged.add(left.get(i++));
        }
        while (j < right.size()) {
            merged.add(right.get(j++));
        }
        return merged;
    }

    // Quick sort - O(n log n) average, sorts the list in place
    public void quickSort(ArrayList<IceCreamType> list, Comparator<IceCreamType> comparator) {
        quickSort(list, comparator, 0, list.size() - 1);
    }

    private void quickSort(ArrayList<IceCreamType> list, Comparator<IceCreamType> comparator, int low, int high) {
        if (low < high) {
            int pivotIndex = partition(list, comparator, low, high);
            quickSort(list, comparator, low, pivotIndex - 1);
            quickSort(list, comparator, pivotIndex + 1, high);
        }
    }

    private int partition(ArrayList<IceCreamType> list, Comparator<IceCreamType> comparator, int low, int high) {
        IceCreamType pivot = list.get(high);
        int i = low - 1;
        for (int j = low; j < high; j++) {
            if (comparator.compare(list.get(j), pivot) <= 0) {
                i++;
                swap(list, i, j);
            }
        }
        swap(list, i + 1, high);
        return i + 1;
    }

    // Insertion sort - O(n^2), good for small or nearly sorted lists
    public void insertionSort(ArrayList<IceCreamType> list, Comparator<IceCreamType> comparator) {
        for (int i = 1; i < list.size(); i++) {
            IceCreamType key = list.get(i);
            int j = i - 1;
            while (j >= 0 && comparator.compare(list.get(j), key) > 0) {
                list.set(j + 1, list.get(j));
                j--;
            }
            list.set(j + 1, key);
        }
    }

    private void swap(ArrayList<IceCreamType> list, int i, int j) {
        IceCreamType temp = list.get(i);
        list.set(i, list.get(j));
        list.set(j, temp);
    }

    // Returns a sorted copy, the original list stays the same
    public ArrayList<IceCreamType> getSortedIceCreams(Comparator<IceCreamType> comparator, boolean ascending) {
        Comparator<IceCreamType> order = ascending ? comparator : comparator.reversed();
        return mergeSort(getAllIceCreams(), order);
    }

    // ===================== ORDERS =====================

    public static class Order {
        private final String orderID;
        private final IceCreamType iceCream;
        private final int quantity;
        private final double total;

        public Order(String orderID, IceCreamType iceCream, int quantity) {
            this.orderID = orderID;
            this.iceCream = iceCream;
            this.quantity = quantity;
            this.total = iceCream.getPrice() * quantity;
        }

        public String getOrderID() {
            return orderID;
        }

        public IceCreamType getIceCream() {
            return iceCream;
        }

        public int getQuantity() {
            return quantity;
        }

        public double getTotal() {
            return total;
        }

        @Override
        public String toString() {
            return String.format("%-6s | %-6s | %-20s | x%-3d | P%.2f", orderID, iceCream.getIcecreamID(),
                    iceCream.getFlavor(), quantity, total);
        }
    }

    // Reserves the stock and adds the order to the queue
    public Order placeOrder(String iceCreamID, int quantity) {
        IceCreamType iceCream = findByID(iceCreamID);
        if (iceCream == null || quantity <= 0 || quantity > iceCream.getQuantity()) {
            return null;
        }
        iceCream.setQuantity(iceCream.getQuantity() - quantity);
        Order order = new Order(String.format("OR%03d", nextOrderNumber++), iceCream, quantity);
        pendingOrders.offer(order);
        return order;
    }

    public Order peekNextOrder() {
        return pendingOrders.peek();
    }

    // Takes the oldest order from the queue and marks it as completed
    public Order takeNextOrder() {
        Order order = pendingOrders.poll();
        if (order != null) {
            completedOrders.add(order);
        }
        return order;
    }

    public ArrayList<Order> getPendingOrders() {
        return new ArrayList<>(pendingOrders);
    }

    public ArrayList<Order> getCompletedOrders() {
        return new ArrayList<>(completedOrders);
    }

    public double getTotalSales() {
        double total = 0;
        for (Order order : completedOrders) {
            total += order.getTotal();
        }
        return total;
    }

    // ===================== DISPLAY =====================

    public static String getContainerType(IceCreamType iceCream) {
        return iceCream instanceof InCone ? "Cone" : "Cup";
    }

    public void displayIceCreams(ArrayList<IceCreamType> list) {
        if (list.isEmpty()) {
            System.out.println("No ice cream products found.");
            return;
        }
        System.out.println("---------------------------------------------------------------");
        System.out.printf("%-6s | %-20s | %-4s | %-4s | %-5s | %s\n", "ID", "Flavor", "Type", "Size", "Qty",
                "Price");
        System.out.println("---------------------------------------------------------------");
        for (IceCreamType iceCream : list) {
            System.out.printf("%-6s | %-20s | %-4s | %-4c | %-5d | P%.2f\n", iceCream.getIcecreamID(),
                    iceCream.getFlavor(), getContainerType(iceCream), iceCream.getSize(), iceCream.getQuantity(),
                    iceCream.getPrice());
        }
        System.out.println("---------------------------------------------------------------");
    }

    public void displayAllIceCreams() {
        displayIceCreams(iceCreams);
    }

    public void displayOrders(ArrayList<Order> orders) {
        if (orders.isEmpty()) {
            System.out.println("No orders found.");
            return;
        }
        for (Order order : orders) {
            System.out.println(order);
        }
    }

    // ===================== INPUT HELPERS =====================

    public IceCreamType addIceCreamFromInput() {
        System.out.println("[1] In Cone");
        System.out.println("[2] In Cup");
        boolean isCone = DataValidation.validateNumberChoiceInput("Container(1-2): ", 1, 2) == 1;
        String flavor = DataValidation.validateFlavorInput("Enter Flavor: ");
        char size = DataValidation.validateSizeInput("Enter Size(S/M/L): ");
        int quantity = DataValidation.validateQuantityInput("Enter Quantity: ", 0, MAX_STOCK);
        double price = DataValidation.validatePriceInput("Enter Price: ");
        IceCreamType iceCream = addIceCream(isCone, flavor, size, quantity, price);
        System.out.println("Ice cream added with ID " + iceCream.getIcecreamID() + ".");
        return iceCream;
    }

    public void sortFromInput() {
        if (isEmpty()) {
            System.out.println("No ice cream products to sort.");
            return;
        }
        System.out.println("Sort by:");
        System.out.println("[1] ID");
        System.out.println("[2] Flavor");
        System.out.println("[3] Size");
        System.out.println("[4] Quantity");
        System.out.println("[5] Price");
        int choice = DataValidation.validateNumberChoiceInput("Choice(1-5): ", 1, 5);
        Comparator<IceCreamType> comparator = switch (choice) {
            case 1 -> byID();
            case 2 -> byFlavor();
            case 3 -> bySize();
            case 4 -> byQuantity();
            default -> byPrice();
        };
        System.out.println("[1] Ascending");
        System.out.println("[2] Descending");
        boolean ascending = DataValidation.validateNumberChoiceInput("Order(1-2): ", 1, 2) == 1;
        displayIceCreams(getSortedIceCreams(comparator, ascending));
    }

    public void searchFromInput() {
        if (isEmpty()) {
            System.out.println("No ice cream products to search.");
            return;
        }
        System.out.println("Search by:");
        System.out.println("[1] ID");
        System.out.println("[2] Flavor");
        System.out.println("[3] Size");
        int choice = DataValidation.validateNumberChoiceInput("Choice(1-3): ", 1, 3);
        ArrayList<IceCreamType> results = new ArrayList<>();
        switch (choice) {
            case 1 -> {
                IceCreamType iceCream = binarySearchByID(DataValidation.validateIceCreamIDInput("Enter ID: "));
                if (iceCream != null) {
                    results.add(iceCream);
                }
            }
            case 2 -> results = linearSearchByFlavor(DataValidation.validateSearchKeywordInput("Enter Flavor: "));
            default -> results = linearSearchBySize(DataValidation.validateSizeInput("Enter Size(S/M/L): "));
        }
        displayIceCreams(results);
    }

    public Order placeOrderFromInput() {
        if (isEmpty()) {
            System.out.println("No ice cream products available.");
            return null;
        }
        displayAllIceCreams();
        IceCreamType iceCream = findByID(DataValidation.validateIceCreamIDInput("Enter Ice Cream ID: "));
        if (iceCream == null) {
            System.out.println("Ice cream not found.");
            return null;
        }
        if (iceCream.getQuantity() == 0) {
            System.out.println("Ice cream is out of stock.");
            return null;
        }
        int quantity = DataValidation.validateQuantityInput("Enter Quantity: ", 1, iceCream.getQuantity());
        Order order = placeOrder(iceCream.getIcecreamID(), quantity);
        System.out.printf("Order %s placed. Total: P%.2f\n", order.getOrderID(), order.getTotal());
        return order;
    }
}
