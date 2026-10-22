# SC Ice Cream Management System

A Java console application for managing the ice cream inventory and the pending customer orders of a small ice cream shop.
It is written in plain Java with no external libraries.

## 1. Summary of the Revision

| Feedback | Correction |
|---|---|
| Role passwords | `Authentication` stores the three password constants. Each role menu needs its own password. As with `sudo`, nothing appears while the password is typed and a wrong password can be retried up to 3 times (the message shows the attempts left). `C` cancels back to the main menu, and passwords are never printed. |
| Price limits | Prices must be from **P30.00 to P120.00**, inclusive. The rule is checked in three places: at input (`InputHelper.readPrice`), in the inventory logic (`Inventory.addProduct` / `updatePrice`), and in the model (`Product.setPrice` throws on an invalid price). This covers both add and update. |
| Sorting | **Sort products** asks for the key (`1` = Quantity, `2` = Price) and the order (`1` = Ascending, `2` = Descending). It then sorts with Insertion Sort and shows the result with a confirmation. There is no algorithm menu, and the behaviour is the same for every role. |
| Quantity limit | A new product must start with **1 to 100** in stock. This is checked at input and in `Inventory.addProduct`. When updating an existing product, 0-100 is allowed, because a product can be sold out. |
| Product tables | View, search and sort all print the same titled table. The header and rows share one format (`Product.TABLE_ROW_FORMAT`), so the columns always line up. |
| Order entry prompts | The cashier sees short instructions first. Each prompt then says what to type: `Enter product ID to add (D = Done, U = Undo last item, C = Cancel order)`. Typing the quantity after the ID (`IC001 2`) adds an item in one step, and a running total is shown after every change. |
| Orders | The **Cashier** enters customer orders (one or more products) and views pending orders. The **Ice Cream Maker** views and processes orders (FIFO). |
| Menus | All three menus match the required layout and order. `[0]` is always Log out. |
| Cancel input | Typing `C` at any prompt inside an operation cancels it and returns to the role menu, without saving partial data. |
| Main menu | Roles are listed as `[1] Cashier`, `[2] Ice Cream Maker`, `[3] Manager`, `[0] Exit`. |
| Order cancellation removed | The Ice Cream Maker's "Cancel an order" option was removed, as requested. Pending orders leave the queue only when they are processed. |
| Terminal experience | The program now behaves like a real terminal application: it detects whether a person is typing in a terminal or input is piped from a file, and adapts (see section 3). The menu layouts are unchanged. |

## 2. Requirement Analysis and Design Decisions

**Roles**

| Role | Menu |
|---|---|
| Manager | View inventory, Sort products, Search products, Add product, Update product details, Remove product, Log out |
| Cashier | View inventory, Search products, Sort products, Add new order to queue, View pending orders, Log out |
| Ice Cream Maker | View inventory, Search products, Sort products, View pending orders, Process next order, Log out |

**Product fields:** product ID (`IC###`), flavor, serving size (S/M/L), quantity in stock, price, and container type (Cone/Cup).

- **Cancel mechanism.** Every input method in `InputHelper` (except menu choices, where `[0]` already means log out) throws `OperationCancelledException` when the user types `C`. `User.showMenu()` catches it in **one** place and prints `Operation cancelled. Returning to <Role> Menu...`. Because products and orders are saved only after the last input, a cancelled operation never leaves partial data behind. The password prompt handles `C` itself and returns to the Main Menu.
- **Shared menu loop.** `User.showMenu()` holds the loop, the `[0] Log out` handling and the cancel handling. Each role only defines `printMenu()` and `handleChoice()`, so none of this is duplicated.
- **Stock policy: deduct on processing.** The inventory changes **only** when the Ice Cream Maker processes an order. Adding an order to the queue does not touch the stock.
  - **Placing an order:** it is accepted only if the *available* stock covers it. Available stock is the current stock minus everything already in pending orders, so the queue never promises more than exists.
  - **Processing an order:** stock is checked again, in case the Manager lowered it in the meantime. If there is not enough, the order stays at the front of the queue and nothing is deducted.
  - **Backing out:** the cashier can still cancel an order with `C` while entering it. The Manager cannot remove a product that is part of a pending order, so a queued order never points to a missing product.
- **Queue location.** The queue lives in `IceCreamMaker`, as the specification requires. The Cashier is given a reference to it and calls `placeOrder()`.
- **Where each algorithm is used.** Each one has a real job:
  - **Insertion Sort**: the *Sort products* option (by quantity or price, ascending or descending, stable).
  - **Quick Sort**: *View inventory* lists products by product ID.
  - **Merge Sort**: *Binary Search* sorts a copy **by product ID** before searching, so it never runs on a list sorted by another key.
  - **Linear Search**: search by flavor keyword.
  - **Binary Search**: search by product ID.
- **ArrayDeque as a stack.** While entering an order, the Cashier's items are pushed onto an `ArrayDeque`. Typing `U` pops the most recently added item (LIFO undo). This replaces the earlier "undo last removal" option, which is not part of the required Manager menu.
- **HashMap** maps ID to product. It gives O(1) lookups for update, remove and orders, and it detects duplicate IDs. The **ArrayList** stores the product records.
- **Order IDs** are readable and unique: `ORD-001`, `ORD-002`, and so on.
- **Product `IceCream` was renamed to `Product`** to match the specification.
- **One class talks to the terminal.** `Terminal` does all reading, screen clearing and colouring. The rest of the program only asks it to read a line, show a message or start a new screen, so the terminal behaviour is decided in one place.

## 3. Terminal Behaviour

When it starts, the program checks what it is connected to (`System.console()`), the same way real command-line tools decide whether to use colours or pause for input.

| | Interactive mode (a real terminal window) | Plain mode (input from a file or pipe, an IDE console, or `--plain`) |
|---|---|---|
| Passwords | Read without echo; nothing appears while typing | Never printed by the program |
| Screens | Each menu starts on a cleared screen; results stay up until **Enter** is pressed | Plain scrolling output, no pauses |
| Answers | Shown by the terminal as they are typed | Answers read from a file or pipe are printed after their prompt, so the saved output reads like a real session. Answers typed in an IDE console are not printed again, because the IDE already shows them |
| Colours | Errors red, successes green, warnings yellow, titles cyan, status lines dimmed, sold-out table rows red | None, so the output can be saved and compared |

The rest works the same in both modes:

- **Prompts show the valid range**, for example `Choice [0-5]:`. Pressing **Enter** on an empty line just shows the prompt again, like a shell, and `q`, `quit` or `exit` work the same as `[0]`.
- **A status line under each role menu** shows live information: products and pending orders for the Cashier, the queue and the next order for the Ice Cream Maker, and products, out-of-stock items and pending orders for the Manager.
- **Yes/no questions have a default**, shown by the capital letter: `Process this order? [Y/n]` (Enter = yes) and `Remove this product? [y/N]` (Enter = no, because removing cannot be undone).
- **Messages that would be cleared away stay visible.** "Access granted", "Cashier logged out" or "Operation cancelled" appear just above the next menu prompt instead of disappearing with the screen.
- **Exiting with pending orders asks first.** Orders are kept only in memory, so `[0] Exit` warns how many would be lost and asks `Exit anyway? [y/N]`.
- **End of input and Ctrl+C exit cleanly.** Ctrl+D (macOS/Linux) or Ctrl+Z then Enter (Windows) ends the program, and Ctrl+C restores the terminal's normal colours before quitting.
- **Colours are used only where they work.** On macOS/Linux and in Windows Terminal, VS Code and Git Bash the program uses colours and escape codes. In the classic Windows console it skips them (they would show up as `←[31m`) and clears the screen with `cls` instead. The common `NO_COLOR` environment variable turns colours off.

## 4. Project Structure

```
dsa-finals-group4/
├── README.md
├── run.bat                               compile and run (Windows)
├── run.sh                                compile and run (macOS / Linux)
├── src/
│   ├── Main.java                         entry point, role selection, sample products
│   ├── Authentication.java               password constants and login (hidden, 3 attempts)
│   ├── OperationCancelledException.java  thrown when the user types C
│   ├── Terminal.java                     terminal detection, screens, hidden input, colours
│   ├── InputHelper.java                  validated console input with C to cancel
│   ├── Product.java                      product model (price range enforced)
│   ├── Inventory.java                    ArrayList + HashMap, product rules
│   ├── OrderItem.java                    one product line in an order
│   ├── Order.java                        a customer order (one or more items)
│   ├── SortAlgorithms.java               Insertion Sort, Merge Sort, Quick Sort
│   ├── SearchAlgorithms.java             Linear Search, Binary Search
│   ├── User.java                         shared menu loop, view / search / sort
│   ├── Manager.java
│   ├── Cashier.java                      order entry (ArrayDeque stack for undo)
│   └── IceCreamMaker.java                Queue of pending orders (FIFO)
└── test/
    ├── AlgorithmTest.java                automated checks (plain Java, no JUnit)
    ├── demo-input.txt                    scripted session covering every feature
    └── demo-output.txt                   output produced by that session
```

## 5. Passwords (demonstration only)

| Role | Password |
|---|---|
| Manager | `ICManager123` |
| Cashier | `ICCashier123` |
| Ice Cream Maker | `ICMaker123` |

## 6. Compile and Run (JDK 11 or newer)

**Quick start:**
- **Windows:** double-click `run.bat`, or run it from a terminal in the project folder.
- **macOS / Linux:** run `sh run.sh`.

Both scripts compile everything into `out/` and start the program. Any options are passed on, for example `run.bat --plain`.

Or compile and run manually from the project folder:

```bash
javac -d out src/*.java
java -cp out Main
```

Options:

| Option | Effect |
|---|---|
| `--plain` | Plain scrolling output: no screen clearing, no pauses, no colours (passwords stay hidden) |
| `--no-color` | Turns colours off only (setting the `NO_COLOR` environment variable does the same) |
| `-h`, `--help` | Shows the options and exits |

An unknown option prints the usage to standard error and exits with code 2.

> IDE run consoles, and Git Bash in its default setup, do not give Java a real console, so the program runs in plain mode there and cannot hide the password while it is typed. For the full interactive mode, use Windows Terminal, PowerShell, `cmd`, or a macOS/Linux terminal. In Git Bash, `winpty java -cp out Main` also works.

Run the automated tests:

```bash
javac -d out src/*.java test/AlgorithmTest.java
java -cp out AlgorithmTest
```

Replay the scripted demo session:

```bash
java -cp out Main < test/demo-input.txt
```

## 7. Testing and Sample Output

`AlgorithmTest` runs 83 checks, and all of them pass. They cover:
- Insertion, Merge and Quick Sort (by price, quantity and ID, stability, empty and single-item lists)
- Insertion Sort by quantity and by price in descending order
- The quantity limits: a new product needs 1-100; an update allows 0-100
- Linear and Binary Search (including unsorted input, missing IDs and empty lists)
- The price limits: 30.00 and 120.00 accepted; 29.99, 120.01, 25, 130 and 150 rejected, both in the constructor and in update
- The inventory rules
- Order placement leaving the inventory unchanged, and rejecting orders that exceed the stock not yet promised to pending orders
- FIFO processing that subtracts stock only when each order is processed, and keeps an order in the queue if the stock dropped below what it needs
- The pending-order list keeping arrival order as orders are processed and added
- The ArrayDeque undo stack
- Number parsing used by menu choices and the one-step `IC001 2` entry, the counts shown in the status lines, and plain mode output containing no colour codes

`test/demo-input.txt` drives every menu of every role. `test/demo-output.txt` is the real output. Because the answers come from a file, each one is printed after its prompt. Excerpts:

**Authentication (passwords are never shown; 3 attempts)**
```
Choice [0-3]: 3
Enter Manager password (hidden, or C to cancel):
Incorrect password for Manager. 2 attempts left.
Enter Manager password (hidden, or C to cancel):
Access granted. Welcome, Manager!
...
Enter Cashier password (hidden, or C to cancel):
Incorrect password for Cashier. 2 attempts left.
Enter Cashier password (hidden, or C to cancel):
Incorrect password for Cashier. 1 attempt left.
Enter Cashier password (hidden, or C to cancel):
3 incorrect password attempts for Cashier. Returning to Main Menu...
...
Enter Cashier password (hidden, or C to cancel):
Login cancelled. Returning to Main Menu...
```

**Menu prompts (Enter alone shows the prompt again; the status line shows live counts)**
```
[0] Exit
Choice [0-3]:
Choice [0-3]: 9
Invalid choice "9". Please enter a number from 0 to 3.
...
[0] Log out

Products: 6 | Pending orders: 3
Choice [0-5]: exit
Cashier logged out.
```

**Price validation**
```
Price (P30.00-P120.00): P25
Invalid price. Price must be from P30.00 to P120.00.
Price (P30.00-P120.00): P130
Invalid price. Price must be from P30.00 to P120.00.
Price (P30.00-P120.00): P30
Container type (cone/cup): cone
Product IC006 added successfully.
```

**Input cancellation**
```
--- Add Product (enter C at any prompt to cancel) ---
Enter product ID, e.g. IC006 (or C to cancel): IC008
Flavor: Pistachio
Serving size (S/M/L): C
Operation cancelled. Returning to Manager Menu...
```

**Quantity limit when adding a product (1-100)**
```
Quantity in stock (1-100): 0
Please enter a whole number from 1 to 100.
Quantity in stock (1-100): 101
Please enter a whole number from 1 to 100.
Quantity in stock (1-100): 20
```

**Confirmation with a safe default (Enter alone answers No)**
```
Product: IC007  | Mocha              | M    | Cup  |    10 |  P120.00
Remove this product? [y/N] (or C to cancel):
Product was not removed.
```

**Sorting (Insertion Sort, user picks the key and the order)**
```
Sort products by:
[1] Quantity
[2] Price
Choice [1-2] (or C to cancel): 1

Sort order:
[1] Ascending (lowest to highest)
[2] Descending (highest to lowest)
Choice [1-2] (or C to cancel): 2

Sorting complete: products sorted by quantity (highest to lowest) using Insertion Sort.

=== SORTED PRODUCTS (quantity, highest to lowest) ===
------------------------------------------------------------
ID     | Flavor             | Size | Type | Stock |    Price
------------------------------------------------------------
IC001  | Vanilla            | S    | Cone |    50 |   P45.00
IC003  | Chocolate          | M    | Cup  |    40 |   P65.00
IC004  | Cookies and Cream  | L    | Cup  |    30 |   P90.00
IC002  | Mango              | M    | Cone |    25 |   P60.00
IC006  | Ube                | L    | Cone |    20 |   P99.50
IC005  | Strawberry         | L    | Cup  |    15 |   P85.00
------------------------------------------------------------
```

**Search results table**
```
Enter flavor keyword (or C to cancel): co

=== SEARCH RESULTS (flavor contains "co") - 2 found ===
------------------------------------------------------------
ID     | Flavor             | Size | Type | Stock |    Price
------------------------------------------------------------
IC003  | Chocolate          | M    | Cup  |    40 |   P65.00
IC004  | Cookies and Cream  | L    | Cup  |    30 |   P90.00
------------------------------------------------------------
```

**Order creation (Cashier, with running total and one-step entry)**
```
--- New Customer Order ---
How to enter an order:
  - Type a product ID (e.g. IC001) to add that product to the order.
    To skip the quantity question, type the quantity after it (e.g. IC001 2).
  - Type D when the order is complete to send it to the queue.
  - Type U to remove the last product you added.
  - Type C to cancel the whole order.
...
Enter product ID to add (D = Done, U = Undo last item, C = Cancel order): IC001
How many Vanilla (IC001)? Enter 1-50 (or C to cancel the order): 2
Added to order: 2x IC001 Vanilla (S, Cone) = P90.00
Items in this order: 1 | Running total: P90.00

Enter product ID to add (D = Done, U = Undo last item, C = Cancel order): IC003
How many Chocolate (IC003)? Enter 1-40 (or C to cancel the order): 1
Added to order: 1x IC003 Chocolate (M, Cup) = P65.00
Items in this order: 2 | Running total: P155.00

Enter product ID to add (D = Done, U = Undo last item, C = Cancel order): U
Removed from order: 1x IC003 Chocolate (M, Cup) = P65.00
Items in this order: 1 | Running total: P90.00

Enter product ID to add (D = Done, U = Undo last item, C = Cancel order): IC002 30
Quantity for Mango (IC002) must be a whole number from 1 to 25. Nothing was added.

Enter product ID to add (D = Done, U = Undo last item, C = Cancel order): IC002 3
Added to order: 3x IC002 Mango (M, Cone) = P180.00
Items in this order: 2 | Running total: P270.00

Enter product ID to add (D = Done, U = Undo last item, C = Cancel order): D
Order added to the queue: ORD-001 | Total: P270.00
    - 2x IC001 Vanilla (S, Cone) = P90.00
    - 3x IC002 Mango (M, Cone) = P180.00
Orders waiting: 1
```

**FIFO processing (Ice Cream Maker)**
```
Pending orders: 3 | Next: ORD-001
Choice [0-5]: 5

Next order: ORD-001 | Total: P270.00
    - 2x IC001 Vanilla (S, Cone) = P90.00
    - 3x IC002 Mango (M, Cone) = P180.00
Process this order? [Y/n] (or C to cancel): N
Order ORD-001 was left in the queue.
...
Process this order? [Y/n] (or C to cancel): Y
Order ORD-001 processed successfully. Inventory updated:
    - IC001 Vanilla: -2, stock now 48
    - IC002 Mango: -3, stock now 22
Orders still waiting: 2
...
Next order: ORD-002 | Total: P1275.00
    - 15x IC005 Strawberry (L, Cup) = P1275.00
Process this order? [Y/n] (or C to cancel):
Order ORD-002 processed successfully. Inventory updated:
    - IC005 Strawberry: -15, stock now 0
Orders still waiting: 1
...
Pending orders: 0 | Next: none
Choice [0-5]: 5
No pending orders to process.
```

**Exiting with an unprocessed order**
```
Choice [0-3]: 0
1 order is still pending and will be lost when the program closes.
Exit anyway? [y/N] (or C to cancel):
...
Choice [0-3]: q
1 order is still pending and will be lost when the program closes.
Exit anyway? [y/N] (or C to cancel): y
Thank you for using the SC Ice Cream Management System. Goodbye!
```

**Stock consistency.** In the demo, the inventory viewed by the cashier *after* placing three orders is still unchanged (Vanilla 50, Mango 25, Strawberry 15, Cookies and Cream 30). The stock only drops when the Ice Cream Maker processes each order:
- Vanilla 50 → 48 and Mango 25 → 22 (ORD-001)
- Strawberry 15 → 0 (ORD-002)
- Cookies and Cream 30 → 28 (ORD-003)
- Ube unchanged at 20 (the cashier cancelled that order with `C` before finishing it)

## 8. Requirements Checklist

| Requirement | Implemented in | Done |
|---|---|---|
| Product with ID, flavor, serving size, quantity, price, container | `Product` | ✅ |
| Add product (unique ID, valid price and stock) | `Manager.addProduct`, `Inventory.addProduct` | ✅ |
| Update product details (checks the product exists) | `Manager.updateProduct`, `Inventory.updatePrice` / `updateQuantity` | ✅ |
| Remove product (checks it exists, confirmation, blocked while in a pending order) | `Manager.removeProduct`, `Inventory.removeProduct` | ✅ |
| Display inventory | `User.viewInventory` | ✅ |
| Search by ID / flavor | `User.searchProducts`, `SearchAlgorithms` | ✅ |
| Adjust inventory quantities | `Inventory.updateQuantity`, `Inventory.adjustStock` (subtracted when an order is processed) | ✅ |
| Password per role, retry (3 attempts), cancel, hidden while typed, passwords never printed, constants in one class | `Authentication`, `Terminal.readPassword` | ✅ |
| Price 30.00–120.00 on add and update, enforced in logic | `Product.isValidPrice` / `setPrice`, `Inventory`, `InputHelper.readPrice` | ✅ |
| Sort products: choose quantity or price, then ascending or descending; Insertion Sort; no algorithm menu; all roles | `User.sortProducts` | ✅ |
| New product quantity must be 1-100 | `Manager.addProduct`, `Inventory.addProduct` | ✅ |
| Search results shown in an aligned, titled table | `Inventory.displayProducts`, `Product.TABLE_ROW_FORMAT` | ✅ |
| Clear order-entry instructions and prompts, one-step `IC001 2` entry, running total | `Cashier.addNewOrder` | ✅ |
| Manager menu (exact layout, `[0]` = Log out) | `Manager.printMenu` | ✅ |
| Cashier menu (exact layout) | `Cashier.printMenu` | ✅ |
| Main menu order: Cashier, Ice Cream Maker, Manager, Exit | `Main.main` | ✅ |
| Ice Cream Maker menu (4 = view pending, 5 = process next, no cancel option) | `IceCreamMaker.printMenu` | ✅ |
| Cashier creates validated orders with multiple items and unique IDs | `Cashier.addNewOrder`, `IceCreamMaker.placeOrder` | ✅ |
| No over-ordering across pending orders | `placeOrder` and the cart compare against stock minus pending orders (`getAvailableQuantity`) | ✅ |
| Cashier and Ice Cream Maker can view pending orders | `IceCreamMaker.displayPendingOrders` | ✅ |
| FIFO processing; order removed only once processed | `IceCreamMaker.processNextOrderFromMenu` (`peek`, confirm, `poll`) | ✅ |
| Inventory updated only when an order is processed, with a stock re-check | `IceCreamMaker.completeNextOrder`, `hasStockToProcess` | ✅ |
| Empty queue handled | process and view options | ✅ |
| `C` cancels any multi-step input without saving partial data | `InputHelper`, `OperationCancelledException`, `User.showMenu` | ✅ |
| Invalid menu choices handled (prompt shows the range; Enter alone re-prompts; `q`, `quit`, `exit` = `[0]`) | `InputHelper.readMenuChoice` | ✅ |
| `ArrayList` | `Inventory.products` | ✅ |
| `HashMap` | `Inventory.productMap` | ✅ |
| `Queue` (in IceCreamMaker) | `IceCreamMaker.pendingOrders` | ✅ |
| `ArrayDeque` as a stack | `Cashier.addNewOrder` cart (`push` / `pop` for undo) | ✅ |
| Insertion Sort | `SortAlgorithms.insertionSort`, used by Sort products | ✅ |
| Quick Sort | `SortAlgorithms.quickSort`, used by View inventory | ✅ |
| Merge Sort | `SortAlgorithms.mergeSort`, used before Binary Search | ✅ |
| Linear Search | `SearchAlgorithms.linearSearchByFlavor` | ✅ |
| Binary Search on a list sorted by the same key | `SearchAlgorithms.binarySearchById` | ✅ |
| No built-in sort or search replacing the required algorithms | Only hand-written algorithms are used | ✅ |
| Standard Java, console only, no external libraries | All files | ✅ |
| Terminal-aware behaviour: cleared screens, pauses, colours and hidden input in a real terminal; plain, replayable output otherwise | `Terminal` | ✅ |
| Status line under each role menu | `User.showMenu`, each role's `getStatusLine` | ✅ |
| `[Y/n]` / `[y/N]` defaults; warning before exiting with pending orders | `InputHelper.readYesNo`, `Main.confirmExit` | ✅ |
