# SC Ice Cream Management System

A Java console application for managing the ice cream inventory and the pending orders of a small ice cream shop.
It is written in plain Java with no external libraries.

## 1. Requirement Analysis

**Roles**

| Role | Can do |
|------|--------|
| Manager | View, add, update, and remove products; undo the last removal; adjust stock; search; sort |
| Cashier | View, search, and sort the inventory only |
| IceCreamMaker | View, search, and sort; add orders to the queue; view the queue; process the next order (FIFO); cancel an order |

**Product fields:** product ID, flavor, serving size (S/M/L), quantity in stock, price, and container type (Cone/Cup).

**Design decisions and ambiguities resolved**

- **Role selection has no password.** The specification asks for separate menus, not authentication, so a simple role menu is used.
- **One `IceCream` class** stores the container type as a field instead of using `InCone`/`InCup` subclasses. The subclasses had no behaviour of their own, so the hierarchy added complexity without purpose.
- **Product IDs** use the format `IC###` (for example `IC001`). The Manager enters the ID, and duplicates are rejected.
- **Validation:** price must be greater than 0 (max 9999.99), and stock must be between 0 and 999. All menu and number inputs are re-asked until valid.
- **Who creates orders:** the specification gives the order queue to the IceCreamMaker and limits the Cashier to view/search/sort. Orders are therefore added from the IceCreamMaker menu.
- **Stock and orders:** stock is *reserved* (subtracted) when an order enters the queue, so two orders can never use the same stock. Processing removes the order from the front of the queue. Cancelling removes it and returns the stock.
- **ArrayDeque as a stack:** the specification does not say where stack behaviour belongs. The simplest useful place is **undo last removal**. Each removed product is pushed onto an `ArrayDeque`, and undo pops the most recent one (LIFO).
- **Sorting** always works on a copy, so the stored inventory order never changes. The user chooses both the key (ID, flavor, price, quantity) and the algorithm (Merge, Quick, Insertion), which makes every algorithm demonstrable.
- **Binary Search** searches by product ID. It first sorts a copy of the list **by ID** with Merge Sort, so it never runs on a list sorted by a different key.
- **HashMap** maps ID to product. It gives O(1) lookups for update, remove, stock adjustment, and orders, and it detects duplicate IDs.

## 2. Project Structure

```
dsa-finals-group4/
├── README.md
├── src/
│   ├── Main.java              entry point, role menu, sample products
│   ├── IceCream.java          product model
│   ├── Order.java             pending order model
│   ├── Inventory.java         ArrayList + HashMap + ArrayDeque (undo stack)
│   ├── SortAlgorithms.java    Merge Sort, Quick Sort, Insertion Sort
│   ├── SearchAlgorithms.java  Linear Search, Binary Search
│   ├── InputHelper.java       validated console input
│   ├── User.java              shared view / search / sort for all roles
│   ├── Manager.java
│   ├── Cashier.java
│   └── IceCreamMaker.java     Queue of pending orders (FIFO)
└── test/
    ├── AlgorithmTest.java     automated checks (plain Java, no JUnit)
    ├── demo-input.txt         scripted session covering every feature
    └── demo-output.txt        output produced by that session
```

## 3. Source Code

All source code is in `src/` (see the structure above). Comments explain each algorithm.

## 4. Compile and Run (JDK 11 or newer)

From the project folder:

```bash
javac -d out src/*.java
java -cp out Main
```

Run the automated tests:

```bash
javac -d out src/*.java test/AlgorithmTest.java
java -cp out AlgorithmTest
```

Replay the scripted demo session:

```bash
java -cp out Main < test/demo-input.txt
```

## 5. Testing and Sample Output

`AlgorithmTest` runs 41 checks, and all of them pass. They cover:

- Merge Sort, Quick Sort, and Insertion Sort by ID, flavor, price, and quantity, all producing the expected order
- All three sorts on empty and single-item lists
- Linear Search: partial match, case-insensitive match, no match, and an empty list
- Binary Search: finds every ID in an *unsorted* inventory, returns null for a missing ID or an empty list, and does not reorder the inventory
- Inventory: add, duplicate-ID rejection, HashMap lookup, stock limits (no negatives, max 999), and removing a missing ID
- ArrayDeque undo stack: LIFO order, empty stack, and refusal when the ID was reused
- Queue: FIFO order, and polling an empty queue

`test/demo-input.txt` drives every menu. `test/demo-output.txt` holds the actual output of that session. Some excerpts:

Search with Binary Search (ID) and Linear Search (flavor):
```
Enter product ID: IC999 -> No product found with ID IC999.
Enter flavor keyword: co -> 2 match(es) found:
IC003  | Chocolate          | M    | Cup  |    40 | P   65.00
IC004  | Cookies and Cream  | L    | Cup  |    30 | P   90.00
```

Sort by price with Quick Sort:
```
IC001  | Vanilla            | S    | Cone |    50 | P   45.00
IC002  | Mango              | M    | Cone |    25 | P   60.00
IC003  | Chocolate          | M    | Cup  |    40 | P   65.00
IC006  | Ube                | L    | Cone |    20 | P   80.00
IC005  | Strawberry         | L    | Cup  |    25 | P   85.00
IC004  | Cookies and Cream  | L    | Cup  |    30 | P   90.00
```

FIFO order queue (orders 1, 2, 3 added; order 2 cancelled):
```
Order # to cancel: 2 -> 3 returned to stock of IC005. Canceled: Order #2
Next order: Order #1 | IC001 Vanilla (S, Cone) x5 | Total: P225.00
Process this order? (Y/N): N -> Order left in the queue.
Process this order? (Y/N): Y -> Processed: Order #1 | IC001 Vanilla (S, Cone) x5 | Total: P225.00
Processed: Order #3 | IC002 Mango (M, Cone) x2 | Total: P120.00
No pending orders to process.
```

Input validation (the user is asked again and the program never crashes):
```
Flavor: Ube 1        -> Flavor must contain only letters and spaces (max 18 characters).
Serving size: X      -> Serving size must be S, M or L.
Quantity: -5         -> Please enter a whole number from 0 to 999.
Price: -10 / abc     -> Price must be a number greater than 0 and at most 9999.99.
Container: bowl      -> Container type must be cone or cup.
Product ID: IC003    -> A product with ID IC003 already exists. Product not added.
```

## 6. Requirements Compliance Checklist

| Requirement | Implemented in | Done |
|---|---|---|
| Product has ID, flavor, serving size, quantity, price, container | `IceCream` | ✅ |
| Add product (no duplicate IDs, no negative price or stock) | `Manager.addProduct`, `Inventory.addProduct`, `InputHelper` | ✅ |
| Remove product (checks it exists, asks for confirmation) | `Manager.removeProduct`, `Inventory.removeProduct` | ✅ |
| Update product details (checks it exists) | `Manager.updateProduct` | ✅ |
| Display inventory | `User.viewInventory`, `Inventory.displayProducts` | ✅ |
| Search by product ID | `SearchAlgorithms.binarySearchById` via `User.searchProducts` | ✅ |
| Sort products | `User.sortProducts`, `SortAlgorithms` | ✅ |
| Adjust inventory quantities | `Manager.adjustStock`, `Inventory.adjustStock`; order reserve and return in `IceCreamMaker` | ✅ |
| Input validation and graceful errors | `InputHelper` (re-asks, handles end of input); missing ID and empty checks in each role | ✅ |
| Manager / Cashier / IceCreamMaker menus with correct permissions | `Manager.showMenu`, `Cashier.showMenu`, `IceCreamMaker.showMenu` | ✅ |
| Order queue in IceCreamMaker, FIFO processing | `IceCreamMaker.pendingOrders` (`Queue`), `processNextOrder` (`poll`) | ✅ |
| Order removed only when processed or cancelled | `processNextOrder`, `cancelOrder` | ✅ |
| Empty queue handled | `processNextOrder`, `viewPendingOrders`, `cancelOrder` | ✅ |
| `ArrayList` for products | `Inventory.products` | ✅ |
| `HashMap` for lookup | `Inventory.productMap` | ✅ |
| `Queue` for pending orders | `IceCreamMaker.pendingOrders` | ✅ |
| `ArrayDeque` as a stack | `Inventory.removedProducts` (undo last removal) | ✅ |
| Merge Sort | `SortAlgorithms.mergeSort` (also used before Binary Search) | ✅ |
| Quick Sort | `SortAlgorithms.quickSort` | ✅ |
| Insertion Sort | `SortAlgorithms.insertionSort` | ✅ |
| Linear Search | `SearchAlgorithms.linearSearchByFlavor` | ✅ |
| Binary Search on a list sorted by the same key | `SearchAlgorithms.binarySearchById` (sorts by ID first) | ✅ |
| No built-in sort or search replacing the required algorithms | Only hand-written algorithms are used | ✅ |
| Standard Java, console only, no external libraries | All files | ✅ |
