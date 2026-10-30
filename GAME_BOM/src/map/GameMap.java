package map;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class GameMap {

    public static final int EMPTY = 0;
    public static final int WALL_BRICK = 1;
    public static final int WALL_SOLID = 2;

    private final List<int[][]> mapList;

    public GameMap() {
        mapList = new ArrayList<>();

        loadMapsFromFile();

        if (mapList.isEmpty()) {
            String[] defaultMap = {
                    "222222222",
                    "200000002",
                    "202121202",
                    "200000002",
                    "222222222"
            };

            mapList.add(parseStringToMap(defaultMap));
        }
    }

    private void loadMapsFromFile() {
        List<String> currentMapLines = new ArrayList<>();

        try {
            InputStream is = getClass()
                    .getClassLoader()
                    .getResourceAsStream("map.txt");

            if (is == null) {
                return; // Không tìm thấy file map.txt thì bỏ qua
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                String line;

                while ((line = reader.readLine()) != null) {
                    line = line.trim();

                    if (line.equals("---")) {
                        if (!currentMapLines.isEmpty()) {
                            String[] template = currentMapLines.toArray(new String[0]);
                            mapList.add(parseStringToMap(template));
                            currentMapLines.clear();
                        }
                    } else if (!line.isEmpty()) {
                        currentMapLines.add(line);
                    }
                }

                if (!currentMapLines.isEmpty()) {
                    String[] template = currentMapLines.toArray(new String[0]);
                    mapList.add(parseStringToMap(template));
                }
            }

        } catch (IOException e) {
            System.err.println("Không thể đọc bản đồ: " + e.getMessage());
        }
    }

    private int[][] parseStringToMap(String[] mapTemplate) {
        int height = mapTemplate.length;
        int width = 0;

        for (String row : mapTemplate) {
            width = Math.max(width, row.length());
        }

        int[][] grid = new int[height][width];

        for (int y = 0; y < height; y++) {
            String row = mapTemplate[y];

            for (int x = 0; x < width; x++) {
                if (x < row.length()) {
                    char c = row.charAt(x);

                    if (c >= '0' && c <= '9') {
                        grid[y][x] = c - '0';
                    } else {
                        grid[y][x] = EMPTY;
                    }
                } else {
                    grid[y][x] = EMPTY;
                }
            }
        }

        return grid;
    }

    public int[][] getGrid() {
        if (mapList.isEmpty()) {
            return new int[0][0];
        }

        // Mặc định trả về bản đồ đầu tiên trong danh sách
        return mapList.getFirst();
    }

    public int getWidth() {
        int[][] grid = getGrid();
        return grid.length > 0 ? grid[0].length : 0;
    }

    public int getHeight() {
        return getGrid().length;
    }
}