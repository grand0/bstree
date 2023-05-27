package ru.kpfu.itis.ponomarev.btree;

import java.io.*;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class Main {
    private static final Random RNG = new Random();
    private static final int GEN_COUNT = 10000;
    private static final int SEARCH_COUNT = 100;
    private static final int REMOVE_COUNT = 1000;
    private static final int TREE_ORDER = 3;

    public static void main(String[] args) {
        int[] arr = generateRandomArray(GEN_COUNT);
        BTree t = new BTree(TREE_ORDER);

        Metrics[] addMetrics = new Metrics[GEN_COUNT];
        Metrics[] searchMetrics = new Metrics[SEARCH_COUNT];
        Metrics[] removeMetrics = new Metrics[REMOVE_COUNT];

        for (int i = 0; i < GEN_COUNT; i++) {
            long timeStart = System.nanoTime();
            t.add(arr[i]);
            long timeNano = System.nanoTime() - timeStart;
            addMetrics[i] = new Metrics(t.getAndClearOperations(), timeNano);
        }

        for (int i = 0; i < SEARCH_COUNT; i++) {
            int k = arr[RNG.nextInt(GEN_COUNT)];
            long timeStart = System.nanoTime();
            t.contains(k);
            long timeNano = System.nanoTime() - timeStart;
            searchMetrics[i] = new Metrics(t.getAndClearOperations(), timeNano);
        }

        Set<Integer> removed = new HashSet<>();
        for (int i = 0; i < REMOVE_COUNT; i++) {
            int index;
            do {
                index = RNG.nextInt(GEN_COUNT);
            } while (removed.contains(index));
            removed.add(index);

            int k = arr[index];
            long timeStart = System.nanoTime();
            t.remove(k);
            long timeNano = System.nanoTime() - timeStart;
            removeMetrics[i] = new Metrics(t.getAndClearOperations(), timeNano);
        }

        writeMetrics(addMetrics, "add.csv");
        writeMetrics(searchMetrics, "search.csv");
        writeMetrics(removeMetrics, "remove.csv");
    }

    private static int[] generateRandomArray(int size) {
        int[] arr = new int[size];
        for (int i = 0; i < size; i++) {
            arr[i] = RNG.nextInt();
        }
        return arr;
    }

    private static void writeMetrics(Metrics[] arr, String fileName) {
        try (BufferedWriter fos = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName)))) {
            for (Metrics metrics : arr) {
                fos.write(metrics.toString() + "\n");
            }
            fos.flush();
        } catch (IOException e) {
            System.err.println("Couldn't write: " + e.getMessage());
        }
    }
}
