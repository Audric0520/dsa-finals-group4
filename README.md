# SC Ice Cream Management System

A Java console application for managing the ice cream inventory and the pending customer orders of a small ice cream shop.
It is written in plain Java with no external libraries.

## 1. Summary of the Revision

| Feedback | Correction |
|---|---|
| Role passwords | `Authentication` stores the three password constants. Each role menu needs its own password. As with `sudo`, a wrong password can be retried up to 3 times (the message shows the attempts left). `C` cancels back to the main menu. The password is visible while it is typed, but the program itself never prints a password. |
| Price limits | Prices must be from **P30.00 to P120.00**, inclusive, with at most 2 decimal places. The rule is checked in three places: at input (`InputHelper.readPrice`), in the inventory logic (`Inventory.addProduct` / `updatePrice`), and in the model (`Product.setPrice` throws on an invalid price). This covers both add and update. |
| Sorting | **Sort products** asks for the key (`1` = Quantity, `2` = Price) and the order (`1` = Ascending, `2` = Descending). It then sorts with Insertion Sort and shows the result with a confirmation. There is no algorithm menu, and the behaviour is the same for every role. |
| Quantity limit | A new product must start with **1 to 100** in stock. This is checked at input and in `Inventory.addProduct`. When updating an existing product, 0-100 is allowed, because a product can be sold out. |
| Product tables | View, search and sort all print the same titled table. The header and rows share one format (`Product.TABLE_ROW_FORMAT`), so the columns always line up. |
| Order entry prompts | The cashier types at an `order>` prompt, like a small shell. A product ID adds an item (`IC001`, or `IC001 2` with the quantity in one step), and the words `done`, `undo` and `cancel` finish, undo or discard the order (`D`, `U` and `C` also work). Two lines above the prompt explain this, and a running total is shown after every change. |
| Orders | The **Cashier** enters customer orders (one or more products) and views pending orders. The **Ice Cream Maker** views and processes orders (FIFO). |
| Menus | All three menus match the required layout and order. `[0]` is always Log out. |
| Cancel input | Typing `C` at any prompt inside an operation cancels it and returns to the role menu, without saving partial data. |
| Main menu | Roles are listed as `[1] Cashier`, `[2] Ice Cream Maker`, `[3] Manager`, `[0] Exit`. |
| Order cancellation removed | The Ice Cream Maker's "Cancel an order" option was removed, as requested. Pending orders leave the queue only when they are processed. |
| Terminal experience | The program behaves like a real terminal application: it detects whether a person is typing in a terminal or input is piped from a file, and adapts (see section 3). The menu layouts are unchanged. |
| Editing a product that is in a pending order | A customer's order no longer changes when the Manager edits the product afterwards. Each order line keeps the flavor, size, container and price it was ordered with. While the order waits, the product's flavor, size and container cannot be changed; a new price applies only to new orders; and lowering the stock below what pending orders need asks for confirmation first (see section 2). |

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
  - **Viewing the inventory:** because pending orders are not yet subtracted, *View inventory* lists the stock they have reserved under the table (`IC005 Strawberry: 15 reserved, 0 left for new orders`). The table and the cashier's "no stock left" message therefore never contradict each other.
  - **Processing an order:** stock is checked again, in case the Manager lowered it in the meantime. If there is not enough, the order stays at the front of the queue, nothing is deducted, and the message lists each item that is short.
  - **Backing out:** the cashier can still cancel an order while entering it. On an order that already has items, `cancel` (or `C`) first asks `Discard this order (2 items, P155.00)? [y/N]`, so one mistyped letter cannot lose a long order; `C` at the "How many?" question skips only that product. The Manager cannot remove a product that is part of a pending order, so a queued order never points to a missing product.
- **An order line is a record of the sale.** `OrderItem` copies the product's flavor, serving size, container and price when the order is taken, like a printed receipt. A pending order's items and total therefore stay exactly as the customer ordered them, whatever the Manager edits later.
- **Product edits while an order is pending.** `Manager.updateProduct` checks the queue (`IceCreamMaker.getPendingOrderIdsFor`) first:

  | Field | While the product is in a pending order |
  |---|---|
  | Flavor, serving size, container | Blocked, naming the orders (`Cannot change the flavor of IC001: it is part of pending order ORD-001...`). Otherwise the Ice Cream Maker would make a different item from this stock. |
  | Price | Allowed. Pending orders keep the price they were taken at, and a note says so. |
  | Quantity | Allowed (for example after a recount). Going below what the pending orders need shows a warning and asks `Set the stock to 5 anyway? [y/N]`. |

- **No duplicate products.** Two IDs for the same flavor, size and container would split one item's stock. `Inventory.addProduct` refuses them, and the Manager is told right after typing the container (`Vanilla (S, Cone) already exists as IC001. Update IC001 instead.`). An update that would turn a product into a copy of another is refused too. Flavors are compared ignoring upper/lower case.
- **Consistent input.** Flavors are stored the same way however they are typed (`"  mint   CHOCO "` becomes `Mint Choco`). Prices accept a leading `P` (the prompt shows one) but reject anything that is not a plain amount with at most 2 decimals, such as `1e2` or `45.999`, instead of reading or rounding it silently.
- **One line per product.** If the cashier adds the same product twice (`IC001 2`, later `IC001 3`), the order is queued as one line, `5x IC001`. Undo still removes the most recent addition while the order is being entered.
- **Queue location.** The queue lives in `IceCreamMaker`, as the specification requires. The Cashier and Manager are given a reference to it.
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
- **One class talks to the terminal.** `Terminal` does all reading, and screen clearing. The rest of the program only asks it to read a line, show a message or start a new screen, so the terminal behaviour is decided in one place.

## 3. Terminal Behaviour

When it starts, the program checks what it is connected to (`System.console()`), the same way real command-line tools decide whether to clear the screen or pause for input.

| | Interactive mode (a real terminal window) | Plain mode (input from a file or pipe, an IDE console, or `--plain`) |
|---|---|---|
| Passwords | Visible while typed, like any other answer | A password read from a file is not copied into the output, so the saved output never contains one |
| Screens | Each menu starts on a cleared screen; results stay up until **Enter** is pressed | Plain scrolling output, no pauses |
| Answers | Shown by the terminal as they are typed | Answers read from a file or pipe are printed after their prompt, so the saved output reads like a real session. Answers typed in an IDE console are not printed again, because the IDE already shows them |

The rest works the same in both modes:

- **Prompts show the valid range**, for example `Choice [0-5]:`. Pressing **Enter** on an empty line just shows the prompt again, like a shell, and `q`, `quit` or `exit` work the same as `[0]`.
- **A status line under each role menu** shows live information: products and pending orders for the Cashier, the queue and the next order for the Ice Cream Maker, and products, out-of-stock items and pending orders for the Manager.
- **Yes/no questions have a default**, shown by the capital letter: `Process this order? [Y/n]` (Enter = yes) and `Remove this product? [y/N]` (Enter = no, because removing cannot be undone).
- **Messages that would be cleared away stay visible.** "Access granted", "Cashier logged out" or "Operation cancelled" appear just above the next menu prompt instead of disappearing with the screen.
- **Exiting with pending orders asks first.** Orders are kept only in memory, so `[0] Exit` warns how many would be lost and asks `Exit anyway? [y/N]`.
- **End of input and Ctrl+C exit cleanly.** Ctrl+D (macOS/Linux) or Ctrl+Z then Enter (Windows) ends the program and Ctrl+C quits.
- **The screen is cleared in the way the terminal supports.** Modern terminals (macOS/Linux, Windows Terminal, VS Code, Git Bash) get an escape code; the classic Windows console, which would show escape codes as garbage, is cleared with `cls` instead.

## 4. Project Structure

```
dsa-finals-group4/
├── README.md
├── Main.java                            entry point, role selection, sample products
├── roles/                               the people who use the system
│   ├── User.java                        shared menu loop, view / search / sort
│   ├── Cashier.java                     order entry (ArrayDeque stack for undo)
│   ├── IceCreamMaker.java               Queue of pending orders (FIFO)
│   ├── Manager.java                     add, update and remove products
│   └── Authentication.java              password constants and login (3 attempts)
├── models/                              the shop's data
│   ├── Product.java                     product model (price range enforced)
│   ├── Order.java                       a customer order (one or more items)
│   ├── OrderItem.java                   one order line, recorded when the order is taken
│   └── Inventory.java                   ArrayList + HashMap, product rules
├── algorithms/                          hand-written sorting and searching
│   ├── SortAlgorithms.java              Insertion Sort, Merge Sort, Quick Sort
│   └── SearchAlgorithms.java            Linear Search, Binary Search
└── console/                             reading input and controlling the terminal
    ├── Terminal.java                    terminal detection, screens, input
    ├── InputHelper.java                 validated console input with C to cancel
    └── OperationCancelledException.java thrown when the user types C
```

Each folder is a Java package with the same name (`package roles;`, `package models;` and so on), so IDEs such as IntelliJ, Eclipse and VS Code open the project without errors. `Main` stays in the project folder itself.

## 5. Passwords (demonstration only)

| Role | Password |
|---|---|
| Manager | `ICManager123` |
| Cashier | `ICCashier123` |
| Ice Cream Maker | `ICMaker123` |

## 6. Compile and Run (JDK 11 or newer)

From the project folder:

```bash
javac -d out Main.java
java -cp out Main
```

`javac` finds the other classes through their packages. The compiled classes go into `out`, which git ignores and which can be deleted at any time. IDEs can also run `Main` directly.

Options:

| Option | Effect |
|---|---|
| `--plain` | Plain scrolling output: no screen clearing and no pauses |
| `-h`, `--help` | Shows the options and exits |

An unknown option prints the usage to standard error and exits with code 2.

A session can also be replayed from a text file with one answer per line, for example `java -cp out Main < session.txt`. The answers are printed after their prompts, so the output reads like the real session.

> IDE run consoles, and Git Bash in its default setup, do not give Java a real console, so the program runs in plain mode there (no screen clearing or pauses). For the full interactive mode, use Windows Terminal, PowerShell, `cmd`, or a macOS/Linux terminal.

## 7. Scenario Testing and Sample Output

Realistic shop sessions were replayed against the program, one answer per line, and every output was read through. They exposed the problems below, which are fixed in this version.

| Scenario | Problem found | Now |
|---|---|---|
| Customer orders 2x Vanilla (S, Cone) for P90.00; the Manager then changes IC001's price to P60 and its flavor, size and container to Ube, L, Cup | The pending order changed to `2x IC001 Ube (L, Cup) = P120.00`, and the Ice Cream Maker made Ube | The order stays `2x Vanilla (S, Cone) = P90.00`; flavor, size and container changes are blocked while it waits; the new price applies to new orders only |
| Manager renames pending Chocolate (IC003) to Vanilla S Cone | Changed a queued order and created a copy of IC001 | Blocked while pending; renaming another product into a copy of IC001 is refused |
| Order of 10 Mango, then the Manager sets Mango's stock to 5 | No warning; the order was stuck at the front of the queue | Warning and `[y/N]` confirmation; the Ice Cream Maker is told `IC002 Mango: needs 10, only 5 in stock` |
| All 15 Strawberry ordered, then the cashier views the inventory | Table showed 15 in stock, yet a new order was refused | A note under the table shows `15 reserved, 0 left for new orders` |
| Manager adds Vanilla S Cone again as IC008 | Accepted: one item under two IDs | Refused right after the container is entered |
| Prices `1e2`, `45.999`, `119.996`, `120.004`, `P45` | `1e2` read as P100.00; `119.996` rounded up to P120.00 while `120.004` was rejected; `P45` rejected | Only plain amounts with at most 2 decimals; a leading `P` is accepted |
| Flavor typed as `  mint choco  ` | Saved in lower case, unlike the other flavors | Saved as `Mint Choco` |
| Cashier adds IC001 twice in one order | Two separate lines for the same product | One line, `5x IC001` |
| Cashier types C halfway through a long order, or at the "How many?" question | The whole order was discarded at once, with no question | `Discard this order (2 items, P155.00)? [y/N]` first; `C` at "How many?" skips only that product |
| Selling a product with 0 stock | "has no more stock available" message listing zeros | `Cookies and Cream (IC004) is out of stock.` |
| Removing a product in several pending orders | Message did not say which orders | `Cannot remove IC001: it is part of pending orders ORD-001, ORD-002.` |

These sessions behaved correctly before and after the fixes: an empty inventory (every option), sold-out products, wrong and cancelled passwords, 3 failed attempts, menu input such as `01`, `Q` and `exit`, quantities such as `99999999999`, `2.5`, `0`, `-1` and `two`, invalid product IDs, undo, cancelling at every kind of prompt, end of input in the middle of an order, and the exit warning (`C`, an invalid answer, `N`, then `y`).

Sample output from these sessions:

**Editing a product that is in a pending order**
```
Order ORD-001 added to the queue. Total: P90.00
    - 2x IC001 Vanilla (S, Cone) = P90.00
...
Field to update [1-5] (or C to cancel): 4
New price P30.00-P120.00 (or C to cancel): P60
Product updated: IC001  | Vanilla            | S    | Cone |    50 |   P60.00
Note: pending order ORD-001 keeps the price it was ordered at. The new price applies to new orders.
...
Field to update [1-5] (or C to cancel): 1
Cannot change the flavor of IC001: it is part of pending order ORD-001. Process it first, so the customer gets what they ordered.
...
--- Pending Orders (first = next to process) ---
1. ORD-001 | Total: P90.00
    - 2x IC001 Vanilla (S, Cone) = P90.00
```

**Lowering stock below what pending orders need**
```
New quantity 0-100 (or C to cancel): 5
The pending order ORD-001 needs 10 of IC002. With only 5 in stock, the Ice Cream Maker cannot process it until the product is restocked.
Set the stock to 5 anyway? [y/N] (or C to cancel): y
Product updated: IC002  | Mango              | M    | Cone |     5 |   P60.00
...
Next order: ORD-001 | Total: P600.00
    - 10x IC002 Mango (M, Cone) = P600.00
Cannot process ORD-001: not enough stock for this order.
    - IC002 Mango: needs 10, only 5 in stock
The order stays at the front of the queue. Ask the Manager to restock first.
```

**Reserved stock under the inventory table**
```
=== PRODUCT INVENTORY (by product ID) ===
------------------------------------------------------------
ID     | Flavor             | Size | Type | Stock |    Price
------------------------------------------------------------
IC001  | Vanilla            | S    | Cone |    50 |   P45.00
IC002  | Mango              | M    | Cone |    25 |   P60.00
IC003  | Chocolate          | M    | Cup  |    40 |   P65.00
IC004  | Cookies and Cream  | L    | Cup  |    30 |   P90.00
IC005  | Strawberry         | L    | Cup  |    15 |   P85.00
------------------------------------------------------------
Reserved for pending orders (subtracted when each order is processed):
  IC005 Strawberry: 15 reserved, 0 left for new orders
```

**Adding a product (duplicate check, quantity and price limits)**
```
Enter product ID, e.g. IC006 (or C to cancel): IC008
Flavor: vanilla
Serving size (S/M/L): s
Container type (cone/cup): cone
Vanilla (S, Cone) already exists as IC001. Update IC001 instead. Product not added.
...
Enter product ID, e.g. IC006 (or C to cancel): IC006
Flavor: Ube
Serving size (S/M/L): L
Container type (cone/cup): cone
Quantity in stock (1-100): 0
Please enter a whole number from 1 to 100.
Quantity in stock (1-100): 101
Please enter a whole number from 1 to 100.
Quantity in stock (1-100): 20
Price (P30.00-P120.00): P25
Invalid price. Enter an amount from P30.00 to P120.00 with at most 2 decimal places, e.g. 45 or 45.50.
Price (P30.00-P120.00): P30
Product IC006 added successfully.
```

**Authentication (3 attempts; a password read from a file is not copied into the output)**
```
Enter Cashier password (or C to cancel):
Incorrect password for Cashier. 2 attempts left.
Enter Cashier password (or C to cancel):
Incorrect password for Cashier. 1 attempt left.
Enter Cashier password (or C to cancel):
3 incorrect password attempts for Cashier. Returning to Main Menu...
```

**Order entry (`order>` prompt, running total, skip, undo, confirmed cancel, repeated product merged)**
```
--- New Customer Order ---
Type a product ID (and quantity, e.g. IC001 2) to add it,
or one of: done, undo, cancel
order> IC001 2
Added 2x IC001 Vanilla (S, Cone) = P90.00 | Total: P90.00
order> IC003
How many Chocolate (IC003)? [1-40] (or C to skip): C
Chocolate not added.
order> IC003
How many Chocolate (IC003)? [1-40] (or C to skip): 1
Added 1x IC003 Chocolate (M, Cup) = P65.00 | Total: P155.00
order> IC001 3
Added 3x IC001 Vanilla (S, Cone) = P135.00 | Total: P290.00
order> undo
Removed 3x IC001 Vanilla (S, Cone) = P135.00 | Total: P155.00
order> cancel
Discard this order (2 items, P155.00)? [y/N]:
Order kept.
order> IC001 3
Added 3x IC001 Vanilla (S, Cone) = P135.00 | Total: P290.00
order> done
Order ORD-001 added to the queue. Total: P290.00
    - 5x IC001 Vanilla (S, Cone) = P225.00
    - 1x IC003 Chocolate (M, Cup) = P65.00
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
```

**FIFO processing (Ice Cream Maker)**
```
Next order: ORD-001 | Total: P270.00
    - 2x IC001 Vanilla (S, Cone) = P90.00
    - 3x IC002 Mango (M, Cone) = P180.00
Process this order? [Y/n] (or C to cancel): Y
Order ORD-001 processed successfully. Inventory updated:
    - IC001 Vanilla: -2, stock now 48
    - IC002 Mango: -3, stock now 22
Orders still waiting: 2
```

## 8. Requirements Checklist

| Requirement | Implemented in | Done |
|---|---|---|
| Product with ID, flavor, serving size, quantity, price, container | `Product` | ✅ |
| Add product (unique ID, no duplicate flavor/size/container, valid price and stock) | `Manager.addProduct`, `Inventory.addProduct`, `Inventory.findSameProduct` | ✅ |
| Update product details (checks the product exists; protects pending orders; no duplicates) | `Manager.updateProduct`, `Inventory.updatePrice` / `updateQuantity` | ✅ |
| Remove product (checks it exists, confirmation, blocked while in a pending order) | `Manager.removeProduct`, `Inventory.removeProduct` | ✅ |
| Display inventory, with stock reserved by pending orders | `User.viewInventory` | ✅ |
| Search by ID / flavor | `User.searchProducts`, `SearchAlgorithms` | ✅ |
| Adjust inventory quantities | `Inventory.updateQuantity`, `Inventory.adjustStock` (subtracted when an order is processed) | ✅ |
| Password per role, retry (3 attempts), cancel, passwords never printed, constants in one class | `Authentication`, `Terminal.readPassword` | ✅ |
| Price 30.00–120.00 on add and update, enforced in logic | `Product.isValidPrice` / `setPrice`, `Inventory`, `InputHelper.parsePrice` | ✅ |
| Sort products: choose quantity or price, then ascending or descending; Insertion Sort; no algorithm menu; all roles | `User.sortProducts` | ✅ |
| New product quantity must be 1-100 | `Manager.addProduct`, `Inventory.addProduct` | ✅ |
| Search results shown in an aligned, titled table | `Inventory.displayProducts`, `Product.TABLE_ROW_FORMAT` | ✅ |
| Clear order-entry instructions; `order>` prompt with `done`, `undo`, `cancel`; one-step `IC001 2` entry; running total; confirmation before discarding | `Cashier.addNewOrder` | ✅ |
| Manager menu (exact layout, `[0]` = Log out) | `Manager.printMenu` | ✅ |
| Cashier menu (exact layout) | `Cashier.printMenu` | ✅ |
| Main menu order: Cashier, Ice Cream Maker, Manager, Exit | `Main.main` | ✅ |
| Ice Cream Maker menu (4 = view pending, 5 = process next, no cancel option) | `IceCreamMaker.printMenu` | ✅ |
| Cashier creates validated orders with multiple items and unique IDs | `Cashier.addNewOrder`, `IceCreamMaker.placeOrder` | ✅ |
| A pending order keeps its items and total when products are edited | `OrderItem` (recorded at order time), `Manager.updateProduct` | ✅ |
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
| Terminal-aware behaviour: cleared screens and pauses in a real terminal; plain, replayable output otherwise | `Terminal` | ✅ |
| Source files grouped by purpose into packages (`roles`, `models`, `algorithms`, `console`) | project folders | ✅ |
| Status line under each role menu | `User.showMenu`, each role's `getStatusLine` | ✅ |
| `[Y/n]` / `[y/N]` defaults; warning before exiting with pending orders | `InputHelper.readYesNo`, `Main.confirmExit` | ✅ |
