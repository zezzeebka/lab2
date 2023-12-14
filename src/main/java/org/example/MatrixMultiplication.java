package org.example;


import java.util.concurrent.RecursiveTask;
import java.util.concurrent.ForkJoinPool;

public class MatrixMultiplication {

    public static void main(String[] args) {
        int matrixSize = 600;

        int[][] matrixA = generateRandomMatrix(matrixSize);
        int[][] matrixB = generateRandomMatrix(matrixSize);


        long startTime = System.currentTimeMillis();
        int[][] resultSequential = multiplyMatrixSequential(matrixA, matrixB);
        long endTime = System.currentTimeMillis();
        System.out.println("Sequential multiplication took: " + (endTime - startTime) + " milliseconds");


        ForkJoinPool forkJoinPool = new ForkJoinPool();
        startTime = System.currentTimeMillis();
        int[][] resultParallel = forkJoinPool.invoke(new MatrixMultiplicationTask(matrixA, matrixB));
        endTime = System.currentTimeMillis();
        System.out.println("Parallel multiplication took: " + (endTime - startTime) + " milliseconds");


    }

    public static int[][] generateRandomMatrix(int size) {
        int[][] matrix = new int[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                matrix[i][j] = (int) (Math.random() * 10);
            }
        }
        return matrix;
    }

    public static int[][] multiplyMatrixSequential(int[][] matrixA, int[][] matrixB) {
        int size = matrixA.length;
        int[][] result = new int[size][size];

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                for (int k = 0; k < size; k++) {
                    result[i][j] += matrixA[i][k] * matrixB[k][j];
                }
            }
        }

        return result;
    }

    public static class MatrixMultiplicationTask extends RecursiveTask<int[][]> {
        private int[][] matrixA;
        private int[][] matrixB;

        public MatrixMultiplicationTask(int[][] matrixA, int[][] matrixB) {
            this.matrixA = matrixA;
            this.matrixB = matrixB;
        }

        @Override
        protected int[][] compute() {
            int size = matrixA.length;

            if (size <= 2) {
                return multiplyMatrixSequential(matrixA, matrixB);
            }

            MatrixMultiplicationTask topLeft = new MatrixMultiplicationTask(
                    subMatrix(matrixA, 0, 0),
                    subMatrix(matrixB, 0, 0)
            );
            MatrixMultiplicationTask topRight = new MatrixMultiplicationTask(
                    subMatrix(matrixA, 0, size / 2),
                    subMatrix(matrixB, size / 2, 0)
            );
            MatrixMultiplicationTask bottomLeft = new MatrixMultiplicationTask(
                    subMatrix(matrixA, size / 2, 0),
                    subMatrix(matrixB, 0, size / 2)
            );
            MatrixMultiplicationTask bottomRight = new MatrixMultiplicationTask(
                    subMatrix(matrixA, size / 2, size / 2),
                    subMatrix(matrixB, size / 2, size / 2)
            );

            invokeAll(topLeft, topRight, bottomLeft, bottomRight);

            int[][] result = new int[size][size];

            joinMatrix(result, topLeft.join(), 0, 0);
            joinMatrix(result, topRight.join(), 0, size / 2);
            joinMatrix(result, bottomLeft.join(), size / 2, 0);
            joinMatrix(result, bottomRight.join(), size / 2, size / 2);

            return result;
        }

        private void joinMatrix(int[][] result, int[][] subMatrix, int row, int col) {
            int subSize = subMatrix.length;
            for (int i = 0; i < subSize; i++) {
                for (int j = 0; j < subSize; j++) {
                    result[row + i][col + j] = subMatrix[i][j];
                }
            }
        }

        private int[][] subMatrix(int[][] matrix, int startRow, int startCol) {
            int size = matrix.length;
            int subSize = size / 2;
            int[][] subMatrix = new int[subSize][subSize];

            for (int i = 0; i < subSize; i++) {
                for (int j = 0; j < subSize; j++) {
                    subMatrix[i][j] = matrix[startRow + i][startCol + j];
                }
            }

            return subMatrix;
        }
    }
}