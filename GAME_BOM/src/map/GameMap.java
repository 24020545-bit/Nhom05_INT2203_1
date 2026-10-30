package map;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class GameMap {

    public static final int EMPTY = 0;
    public static final int WALL_BRICK = 1;
    public static final int WALL_SOLID = 2;

    public enum ItemType {
        DAMAGE_UP(6),
        SPEED_UP(7),
        RANGE_UP(8),
        HEAL(9);

        private final int mapValue;

        ItemType(int mapValue) {
            this.mapValue = mapValue;
        }

        public int getMapValue() {
            return mapValue;
        }
    }

    private List<int[][]> mapList;
    private List<String[]> rawMapTemplates;
    private int currentMapIndex;

    public GameMap() {
        mapList = new ArrayList<>();
        rawMapTemplates = new ArrayList<>();

        loadMapsFromFile("map.txt");

        if (mapList.isEmpty()) {
            String[] defaultMap = {
                    "222222222",
                    "200000002",
                    "202121202",
                    "200000002",
                    "222222222"
            };

            rawMapTemplates.add(defaultMap);
            mapList.add(parseStringToMap(defaultMap));
        }

        currentMapIndex = 0;
    }

    private void loadMapsFromFile(String filePath) {

        List<String> currentMapLines = new ArrayList<>();

        try {
            InputStream is = getClass()
                    .getClassLoader()
                    .getResourceAsStream(filePath);

            BufferedReader reader;

            if (is != null) {
                reader = new BufferedReader(
                        new InputStreamReader(is)
                );
            } else {
                reader = new BufferedReader(
                        new FileReader(filePath)
                );
            }

            try (reader) {
                String line;

                while ((line = reader.readLine()) != null) {
                    line = line.trim();

                    if (line.equals("---")) {
                        if (!currentMapLines.isEmpty()) {
                            String[] template =
                                    currentMapLines.toArray(new String[0]);

                            rawMapTemplates.add(template);
                            mapList.add(parseStringToMap(template));
                            currentMapLines.clear();
                        }
                    } else if (!line.isEmpty()) {
                        currentMapLines.add(line);
                    }
                }

                if (!currentMapLines.isEmpty()) {
                    String[] template =
                            currentMapLines.toArray(new String[0]);

                    rawMapTemplates.add(template);
                    mapList.add(parseStringToMap(template));
                }
            }

        } catch (IOException e) {
            System.err.println(
                    "Không thể đọc bản đồ: " + e.getMessage()
            );
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

        return mapList.get(currentMapIndex);
    }

    public void resetMap() {
        mapList.clear();

        for (String[] template : rawMapTemplates) {
            mapList.add(parseStringToMap(template));
        }
    }

    public void setMapIndex(int index) {
        if (index >= 0 && index < mapList.size()) {
            currentMapIndex = index;
        }
    }

    public int getCurrentMapIndex() {
        return currentMapIndex;
    }

    public int getMapCount() {
        return mapList.size();
    }

    public int getWidth() {
        int[][] grid = getGrid();
        return grid.length > 0 ? grid[0].length : 0;
    }

    public int getHeight() {
        return getGrid().length;
    }

    public int getTile(int x, int y) {
        if (x < 0 || x >= getWidth()
                || y < 0 || y >= getHeight()) {
            return WALL_SOLID;
        }

        return getGrid()[y][x];
    }

    public void setTile(int x, int y, int value) {
        if (x >= 0 && x < getWidth()
                && y >= 0 && y < getHeight()) {
            getGrid()[y][x] = value;
        }
    }

    public boolean isWalkable(int x, int y) {
        if (x < 0 || x >= getWidth()
                || y < 0 || y >= getHeight()) {
            return false;
        }

        int tile = getTile(x, y);

        return tile == EMPTY || (tile >= 6 && tile <= 9);
    }

    public boolean checkCollision(
            double nextX,
            double nextY,
            int width,
            int height
    ) {
        int tileSize = 32;

        int leftTile = (int) (nextX / tileSize);
        int rightTile = (int) ((nextX + width - 1) / tileSize);
        int topTile = (int) (nextY / tileSize);
        int bottomTile = (int) ((nextY + height - 1) / tileSize);

        return isSolid(leftTile, topTile)
                || isSolid(rightTile, topTile)
                || isSolid(leftTile, bottomTile)
                || isSolid(rightTile, bottomTile);
    }

    private boolean isSolid(int tileX, int tileY) {
        int tile = getTile(tileX, tileY);

        return tile == WALL_SOLID || tile == WALL_BRICK;
    }
}
