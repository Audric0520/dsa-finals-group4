package Roles;

import java.util.ArrayList;

import IceCream.IceCreamType;
import Utilities.DataValidation;
import Utilities.DisplayMethods;
import Utilities.IceCreamManager;

public abstract class User {
    public static final IceCreamManager icm = new IceCreamManager();
    private String role;

    public User(String role) {
        this.role = role;
    }

    public String getRole() {
        return role;
    }

    public void viewAllIceCream() {
        DisplayMethods.displayAllIceCream();
    }

    public void sortIceCream() {
        ArrayList<IceCreamType> allIceCreams = icm.getAllIceCreams();
        if (allIceCreams.isEmpty()) {
            System.out.println("There are no Ice Cream Products to sort");
            return;
        }
        String sortField = DataValidation
                .validateFieldChoice("Sort by Quantity or Price?\n1. Quantity\n2. Price\nChoice(1 or 2): ");
        boolean isAscending = DataValidation.validateSortOrderChoice(
                "Sort by Ascending or Descending\n1. Ascending\n2. Descending\nChoice(1 or 2): ");

        ArrayList<IceCreamType> sorted = icm.getSortedIceCream(sortField, isAscending);
        String order = isAscending ? "ASCENDING" : "DESCENDING";
        DisplayMethods.displayIceCreamHeader(
                String.format("ALL ICE CREAMS SORTED BY %s (%s)", sortField.toUpperCase(), order));
        DisplayMethods.displayIceCream(allIceCreams);

    }

    public void searchIceCream() {

    }

    public abstract void landingPage();
}
