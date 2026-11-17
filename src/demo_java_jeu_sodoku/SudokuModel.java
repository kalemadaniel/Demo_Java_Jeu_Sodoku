package demo_java_jeu_sodoku;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CancellationException;

/** Logique du jeu, indépendante de Swing. Compatible Java 8. */
public final class SudokuModel {
    public final int size;
    public final int boxRows;
    public final int boxCols;
    public final int[][] puzzle;
    public final int[][] solution;

    private SudokuModel(int size, int boxRows, int boxCols,
                        int[][] puzzle, int[][] solution) {
        this.size = size;
        this.boxRows = boxRows;
        this.boxCols = boxCols;
        this.puzzle = puzzle;
        this.solution = solution;
    }

    /** Retire seulement les cases qui conservent une solution unique. */
    public static SudokuModel generate(int size, int difficulty, Random random) {
        int br, bc;
        switch (size) {
            case 4: br = 2; bc = 2; break;
            case 6: br = 2; bc = 3; break;
            case 9: br = 3; bc = 3; break;
            case 12: br = 3; bc = 4; break;
            case 16: br = 4; bc = 4; break;
            default: throw new IllegalArgumentException("Taille non prise en charge");
        }
        int[] rows = groupedOrder(size, br, random);
        int[] cols = groupedOrder(size, bc, random);
        List<Integer> symbols = new ArrayList<Integer>();
        for (int i = 1; i <= size; i++) symbols.add(i);
        Collections.shuffle(symbols, random);
        int[][] solved = new int[size][size];
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                int index = (rows[r] * bc + rows[r] / br + cols[c]) % size;
                solved[r][c] = symbols.get(index);
            }
        }
        int[][] puzzle = copy(solved);
        List<Integer> positions = new ArrayList<Integer>();
        for (int i = 0; i < size * size; i++) positions.add(i);
        Collections.shuffle(positions, random);
        // La difficulté représente une densité visée, pas une cotation humaine.
        double fraction = new double[] {0.40, 0.52, 0.62}[difficulty];
        int target = (int) Math.round(size * size * fraction);
        int removed = 0;
        long deadline = System.nanoTime() + 5000000000L;
        for (int position : positions) {
            checkCancelled();
            if (removed >= target || System.nanoTime() >= deadline) break;
            int r = position / size, c = position % size;
            int previous = puzzle[r][c];
            puzzle[r][c] = 0;
            Search search = new Search(br, bc, 25000, deadline);
            if (search.count(puzzle, 2) == 1 && !search.exhausted) removed++;
            else puzzle[r][c] = previous;
        }
        return new SudokuModel(size, br, bc, puzzle, solved);
    }

    private static int[] groupedOrder(int size, int groupSize, Random random) {
        List<Integer> groups = new ArrayList<Integer>();
        for (int i = 0; i < size / groupSize; i++) groups.add(i);
        Collections.shuffle(groups, random);
        int[] result = new int[size];
        int index = 0;
        for (int group : groups) {
            List<Integer> items = new ArrayList<Integer>();
            for (int j = 0; j < groupSize; j++) items.add(group * groupSize + j);
            Collections.shuffle(items, random);
            for (int item : items) result[index++] = item;
        }
        return result;
    }

    public static int[][] copy(int[][] source) {
        int[][] result = new int[source.length][];
        for (int r = 0; r < source.length; r++) result[r] = source[r].clone();
        return result;
    }

    public static boolean canPlace(int[][] board, int br, int bc,
                                   int row, int col, int value) {
        for (int i = 0; i < board.length; i++) {
            if (i != col && board[row][i] == value) return false;
            if (i != row && board[i][col] == value) return false;
        }
        int startR = row / br * br, startC = col / bc * bc;
        for (int r = startR; r < startR + br; r++) {
            for (int c = startC; c < startC + bc; c++) {
                if ((r != row || c != col) && board[r][c] == value) return false;
            }
        }
        return true;
    }

    public static int countSolutions(int[][] board, int br, int bc) {
        int n = board.length;
        if (br * bc != n) return 0;
        for (int r = 0; r < n; r++) {
            if (board[r].length != n) return 0;
            for (int c = 0; c < n; c++) {
                int value = board[r][c];
                if (value < 0 || value > n ||
                        (value != 0 && !canPlace(board, br, bc, r, c, value))) return 0;
            }
        }
        Search search = new Search(br, bc, Integer.MAX_VALUE, Long.MAX_VALUE);
        return search.count(copy(board), 2);
    }

    private static void checkCancelled() {
        if (Thread.currentThread().isInterrupted()) throw new CancellationException();
    }

    private static final class Search {
        final int br, bc;
        final long deadline;
        int remaining;
        boolean exhausted;

        Search(int br, int bc, int budget, long deadline) {
            this.br = br;
            this.bc = bc;
            this.remaining = budget;
            this.deadline = deadline;
        }

        int count(int[][] board, int limit) {
            checkCancelled();
            if (--remaining < 0 || System.nanoTime() >= deadline) {
                exhausted = true;
                return limit;
            }
            int n = board.length, bestR = -1, bestC = -1, bestMask = 0;
            int bestCount = n + 1;
            for (int r = 0; r < n; r++) {
                for (int c = 0; c < n; c++) {
                    if (board[r][c] != 0) continue;
                    int mask = 0;
                    for (int v = 1; v <= n; v++) {
                        if (canPlace(board, br, bc, r, c, v)) mask |= 1 << (v - 1);
                    }
                    int count = Integer.bitCount(mask);
                    if (count == 0) return 0;
                    if (count < bestCount) {
                        bestR = r; bestC = c; bestMask = mask; bestCount = count;
                    }
                }
            }
            if (bestR < 0) return 1;
            int total = 0;
            for (int v = 1; v <= n; v++) {
                if ((bestMask & (1 << (v - 1))) == 0) continue;
                board[bestR][bestC] = v;
                total += count(board, limit - total);
                board[bestR][bestC] = 0;
                if (total >= limit) return total;
            }
            return total;
        }
    }
}
