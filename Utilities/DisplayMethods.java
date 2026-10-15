package Utilities;

import java.util.ArrayList;
import IceCream.IceCreamType;

public class DisplayMethods {
    public static final IceCreamManager icm = IceCreamManager.getInstance();
    public static final String tableFormat = "%-20s | %-20s | %-20s | %-20s | %-20s\n";

    public static void displayIceCreamHeader(String headerText) {
        System.out.println(headerText);
        System.out.println("=".repeat(100));
        System.out.printf(tableFormat, "ICE CREAM ID", "FLAVOR", "SIZE", "QUANTITY", "PRICE");
        System.out.println("=".repeat(100));
    }

    public static void displayIceCream(ArrayList<IceCreamType> iceCreams) {
        displayIceCreamHeader("ALL ICE CREAMS");
        for (IceCreamType iceCream : iceCreams) {
            System.out.printf(tableFormat, iceCream.getIcecreamID(), iceCream.getFlavor(), iceCream.getSize(),
                    iceCream.getQuantity(), iceCream.getPrice());
        }
    }
}
