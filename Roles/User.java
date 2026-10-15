package Roles;

import java.util.ArrayList;

import IceCream.IceCreamType;
import Utilities.DataValidation;
import Utilities.IceCreamManager;

public abstract class User {
    public static final IceCreamManager icm = IceCreamManager.getInstance();
    private String role;

    public User(String role) {
        this.role = role;
    }

    public String getRole() {
        return role;
    }

    public void viewAllIceCream() {
        icm.displayAllIceCreams();
    }

    public void sortIceCream() {
        icm.sortFromInput();
    }

    public void searchIceCream() {
        if (icm.isEmpty()) {
            System.out.println("No ice cream products available to search.");
            return;
        }

        String iceCreamID = DataValidation.validateStringInput("Enter Ice Cream ID to search: ");
        IceCreamType iceCream = icm.findByID(iceCreamID);
        if (iceCream == null) {
            System.out.println("Ice cream not found.");
            return;
        }

        ArrayList<IceCreamType> results = new ArrayList<>();
        results.add(iceCream);
        icm.displayIceCreams(results);
    }

    public abstract void landingPage();
}
