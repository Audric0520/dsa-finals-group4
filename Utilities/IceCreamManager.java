package Utilities;

import java.util.ArrayList;

import IceCream.IceCreamType;

public class IceCreamManager {
    private final ArrayList<IceCreamType> iceCreams = new ArrayList<>();

    public ArrayList<IceCreamType> getAllIceCreams() {
        return new ArrayList<>(iceCreams);
    }
}
