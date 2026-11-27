package utils;

import java.util.*;
import java.util.function.BiPredicate;

public final class BFS {
    private BFS() {}

    public static List<int[]> findPath(int[][] map, int startCol, int startRow,
                                       int endCol, int endRow,
                                       BiPredicate<Integer, Integer> canVisit) {
        return findNearest(map, startCol, startRow, canVisit,
                (col, row) -> col == endCol && row == endRow);
    }

    // Path includes the start and destination; empty means no path.
    public static List<int[]> findNearest(int[][] map, int startCol, int startRow,
                                          BiPredicate<Integer, Integer> canVisit,
                                          BiPredicate<Integer, Integer> isGoal) {
        if (map == null || map.length == 0 || map[0].length == 0)
            return Collections.emptyList();
        int rows = map.length, cols = map[0].length;
        if (startCol < 0 || startCol >= cols || startRow < 0 || startRow >= rows)
            return Collections.emptyList();
        boolean[][] visited = new boolean[rows][cols];
        int[][][] parent = new int[rows][cols][];
        Queue<int[]> queue = new ArrayDeque<>();
        queue.add(new int[]{startCol, startRow});
        visited[startRow][startCol] = true;
        int[][] dirs = {{0,-1},{0,1},{-1,0},{1,0}};
        while (!queue.isEmpty()) {
            int[] cell = queue.poll();
            int col = cell[0], row = cell[1];
            if (isGoal.test(col, row)) {
                List<int[]> path = new ArrayList<>();
                for (int[] p = cell; p != null; p = parent[p[1]][p[0]])
                    path.add(p);
                Collections.reverse(path);
                return path;
            }
            for (int[] dir : dirs) {
                int c = col + dir[0], r = row + dir[1];
                if (c < 0 || c >= cols || r < 0 || r >= rows
                        || visited[r][c] || !canVisit.test(c, r)) continue;
                visited[r][c] = true;
                parent[r][c] = cell;
                queue.add(new int[]{c,r});
            }
        }
        return Collections.emptyList();
    }
}
