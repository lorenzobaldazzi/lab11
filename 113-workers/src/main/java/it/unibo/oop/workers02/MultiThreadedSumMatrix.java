package it.unibo.oop.workers02;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of multithreadsummatrix.
 */
public final class MultiThreadedSumMatrix implements SumMatrix {
    private final int nthread;

    /**
     * @param nthread
     *            no. of thread performing the sum.
     */
    public MultiThreadedSumMatrix(final int nthread) {
        this.nthread = nthread;
    }

    @Override
    public double sum(final double[][] matrix) {
        final int rows = matrix.length;
        final int cols = matrix[0].length;
        final int total = rows * cols;
        final int size = (total + nthread - 1) / nthread;
        final List<Worker> workers = new ArrayList<>(nthread);
        for (int start = 0; start < total; start += size) {
            workers.add(new Worker(matrix, start, size, cols, total));
        }

        for (final Worker w: workers) {
            w.start();
        }

        double sum = 0;
        for (final Worker w : workers) { 
            try {
                w.join();
                sum += w.getResult();
            } catch (final InterruptedException e) {
                throw new IllegalStateException(e);
            }
        }
        return sum;
    }

    private static class Worker extends Thread {
        private final double[] values;
        private final int start;
        private final int size;
        private final int total;
        private double res;

        Worker(final double[][] matrix, final int start, final int size, final int cols, final int total) {
            final int stop = Math.min(start + size, total);
            this.values = new double[stop - start];
            int i = 0;
            for (int j = start; j < stop; j++) {
                final int row = j / cols;
                final int col = j % cols;
                this.values[i] = matrix[row][col];
                i++;
            }
            this.start = start;
            this.total = total;
            this.size = size;
        }

        @Override
        @SuppressWarnings("PMD.SystemPrintln")
        public synchronized void run() {
            final int stop = Math.min(start + size, total);
            System.out.println("Working from: " + start + " to: " + (stop - 1));
            double tmp = 0;
            for (final double value: values) {
                tmp += value;
            }
            this.res = tmp;
        }

        public synchronized double getResult() {
            return res;
        }
    }
}
