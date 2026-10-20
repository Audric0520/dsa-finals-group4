import java.util.ArrayList;
import java.util.Comparator;

/**
 * Hand-written sorting algorithms. Each method sorts the given list in ascending
 * order according to the comparator.
 *
 * Where each one is used:
 *   Insertion Sort - "Sort products" menu option (sorts by price, lowest first)
 *   Quick Sort     - "View inventory" (lists products by product ID)
 *   Merge Sort     - Binary Search (sorts a copy by product ID before searching)
 */
public class SortAlgorithms {

    public static final Comparator<Product> BY_ID = Comparator.comparing(Product::getProductId);
    public static final Comparator<Product> BY_PRICE = Comparator.comparingDouble(Product::getPrice);

    // ---------------- Insertion Sort: O(n^2), simple, stable, good for small lists ----------------

    public static void insertionSort(ArrayList<Product> list, Comparator<Product> comparator) {
        for (int i = 1; i < list.size(); i++) {
            Product current = list.get(i);
            int j = i - 1;
            // Shift bigger items one place to the right to make room for "current"
            while (j >= 0 && comparator.compare(list.get(j), current) > 0) {
                list.set(j + 1, list.get(j));
                j--;
            }
            list.set(j + 1, current);
        }
    }

    // ---------------- Merge Sort: O(n log n), stable ----------------

    public static void mergeSort(ArrayList<Product> list, Comparator<Product> comparator) {
        if (list.size() <= 1) {
            return;
        }
        int middle = list.size() / 2;
        ArrayList<Product> left = new ArrayList<>(list.subList(0, middle));
        ArrayList<Product> right = new ArrayList<>(list.subList(middle, list.size()));

        mergeSort(left, comparator);
        mergeSort(right, comparator);
        merge(list, left, right, comparator);
    }

    // Combines two sorted halves back into the original list.
    private static void merge(ArrayList<Product> list, ArrayList<Product> left, ArrayList<Product> right,
            Comparator<Product> comparator) {
        int i = 0;
        int j = 0;
        int k = 0;
        while (i < left.size() && j < right.size()) {
            // "<= 0" takes from the left half on ties, which keeps the sort stable
            if (comparator.compare(left.get(i), right.get(j)) <= 0) {
                list.set(k++, left.get(i++));
            } else {
                list.set(k++, right.get(j++));
            }
        }
        while (i < left.size()) {
            list.set(k++, left.get(i++));
        }
        while (j < right.size()) {
            list.set(k++, right.get(j++));
        }
    }

    // ---------------- Quick Sort: O(n log n) average, in place ----------------

    public static void quickSort(ArrayList<Product> list, Comparator<Product> comparator) {
        quickSort(list, comparator, 0, list.size() - 1);
    }

    private static void quickSort(ArrayList<Product> list, Comparator<Product> comparator, int low, int high) {
        if (low < high) {
            int pivotIndex = partition(list, comparator, low, high);
            quickSort(list, comparator, low, pivotIndex - 1);
            quickSort(list, comparator, pivotIndex + 1, high);
        }
    }

    // Lomuto partition: the last element is the pivot. Smaller items move to its left.
    private static int partition(ArrayList<Product> list, Comparator<Product> comparator, int low, int high) {
        Product pivot = list.get(high);
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

    private static void swap(ArrayList<Product> list, int a, int b) {
        Product temp = list.get(a);
        list.set(a, list.get(b));
        list.set(b, temp);
    }
}
