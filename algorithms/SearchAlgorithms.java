package algorithms;

import java.util.ArrayList;

import models.Product;

/**
 * Hand-written searching algorithms.
 */
public class SearchAlgorithms {

    /**
     * Linear Search: checks every product one by one and returns all products
     * whose flavor contains the keyword (case-insensitive). O(n).
     */
    public static ArrayList<Product> linearSearchByFlavor(ArrayList<Product> list, String keyword) {
        ArrayList<Product> matches = new ArrayList<>();
        String lowerKeyword = keyword.toLowerCase();
        for (Product product : list) {
            if (product.getFlavor().toLowerCase().contains(lowerKeyword)) {
                matches.add(product);
            }
        }
        return matches;
    }

    /**
     * Binary Search by product ID. O(log n).
     * Binary search only works on a list sorted by the same key, so this method
     * first sorts a copy of the list by ID (using Merge Sort) before searching.
     * Returns null if the ID is not found.
     */
    public static Product binarySearchById(ArrayList<Product> list, String productId) {
        ArrayList<Product> sorted = new ArrayList<>(list);
        SortAlgorithms.mergeSort(sorted, SortAlgorithms.BY_ID);

        int low = 0;
        int high = sorted.size() - 1;
        while (low <= high) {
            int middle = low + (high - low) / 2;
            int result = sorted.get(middle).getProductId().compareTo(productId);
            if (result == 0) {
                return sorted.get(middle);
            } else if (result < 0) {
                low = middle + 1;   // target is in the right half
            } else {
                high = middle - 1;  // target is in the left half
            }
        }
        return null;
    }
}
