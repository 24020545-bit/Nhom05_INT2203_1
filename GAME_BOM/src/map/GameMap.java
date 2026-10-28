package map;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class GameMap {
    public static final char EMPTY = ' ';      // Đường đi (ô trống)
    public static final char WALL_SOLID = '#';  // Tường cố định (không phá được)
    public static final char WALL_BRICK = 'X'; // Tường gạch (phá được bằng bom)

    private List<char[][]> mapList;
    private List<String[]> rawMapTemplates; // Lưu lại bản thiết kế gốc để reset map
    private int currentMapIndex;

    public GameMap() {
        mapList = new ArrayList<>();
        rawMapTemplates = new ArrayList<>();

        // Đọc danh sách map từ file txt (ví dụ: "map.txt")
        loadMapsFromFile("map.txt");

        // Nếu file trống hoặc lỗi, tạo sẵn 1 map mặc định để tránh crash game
        if (mapList.isEmpty()) {
            String[] defaultMap = {
                    "#########",
                    "#.......#",
                    "#.###.#.#",
                    "#.#...#.#",
                    "#########"
            };
            rawMapTemplates.add(defaultMap);
            mapList.add(parseStringToMap(defaultMap));
        }

        currentMapIndex = 0;
    }

    /**
     * Đọc file txt chứa nhiều map được ngăn cách bởi dòng "---"
     */
    private void loadMapsFromFile(String filePath) {
        List<String> currentMapLines = new ArrayList<>();

        try (InputStream is = getClass().getClassLoader().getResourceAsStream(filePath);
             BufferedReader reader = is != null ? new BufferedReader(new InputStreamReader(is))
                     : new BufferedReader(new FileReader(filePath))) {

            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                // Nếu gặp dòng phân cách hoặc dòng trống kết thúc 1 map
                if (line.equals("---")) {
                    if (!currentMapLines.isEmpty()) {
                        String[] template = currentMapLines.toArray(new String[0]);
                        rawMapTemplates.add(template);
                        mapList.add(parseStringToMap(template));
                        currentMapLines.clear();
                    }
                } else if (!line.isEmpty()) {
                    currentMapLines.add(line);
                }
            }

            // Thêm map cuối cùng nếu file không kết thúc bằng "---"
            if (!currentMapLines.isEmpty()) {
                String[] template = currentMapLines.toArray(new String[0]);
                rawMapTemplates.add(template);
                mapList.add(parseStringToMap(template));
            }

        } catch (IOException e) {
            System.err.println("Không thể đọc file bản đồ: " + filePath + " (" + e.getMessage() + ")");
        }
    }

    /**
     * Khôi phục lại toàn bộ bản đồ về trạng thái ban đầu (hồi phục tường gạch)
     */
    public void resetMap() {
        mapList.clear();
        for (String[] template : rawMapTemplates) {
            mapList.add(parseStringToMap(template));
        }
    }

    private char[][] parseStringToMap(String[] mapTemplate) {
        int height = mapTemplate.length;
        int width = 0;
        for (String row : mapTemplate) {
            if (row.length() > width) {
                width = row.length();
            }
        }

        char[][] grid = new char[height][width];

        for (int y = 0; y < height; y++) {
            String rowStr = mapTemplate[y];
            for (int x = 0; x < width; x++) {
                if (x < rowStr.length()) {
                    char c = rowStr.charAt(x);
                    if (c == '.') {
                        grid[y][x] = EMPTY;
                    } else {
                        grid[y][x] = c;
                    }
                } else {
                    grid[y][x] = EMPTY;
                }
            }
        }
        return grid;
    }

    public char[][] getGrid() {
        if (mapList.isEmpty()) {
            return new char[0][0];
        }
        return mapList.get(currentMapIndex);
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
        char[][] grid = getGrid();
        return (grid.length > 0) ? grid[0].length : 0;
    }

    public int getHeight() {
        return getGrid().length;
    }

    public char getTile(int x, int y) {
        char[][] currentGrid = getGrid();
        if (x < 0 || x >= getWidth() || y < 0 || y >= getHeight()) return WALL_SOLID;
        return currentGrid[y][x];
    }

    public boolean isWalkable(int x, int y) {
        if (x < 0 || x >= getWidth() || y < 0 || y >= getHeight()) {
            return false;
        }
        return getTile(x, y) == EMPTY;
    }

    /**
     * [ĐÃ THÊM] Kiểm tra va chạm theo tọa độ pixel của Mob/Bot với tường cứng hoặc tường gạch.
     */
    public boolean checkCollision(double nextX, double nextY, int width, int height) {
        int tileSize = 32; // Kích thước 1 ô tile trong game (32 pixel)

        int leftTile = (int) (nextX / tileSize);
        int rightTile = (int) ((nextX + width - 1) / tileSize);
        int topTile = (int) (nextY / tileSize);
        int bottomTile = (int) ((nextY + height - 1) / tileSize);

        return isSolid(leftTile, topTile) || isSolid(rightTile, topTile) ||
                isSolid(leftTile, bottomTile) || isSolid(rightTile, bottomTile);
    }

    // Hàm phụ trợ kiểm tra xem ô tile có phải vật cản không
    private boolean isSolid(int tileX, int tileY) {
        char tile = getTile(tileX, tileY);
        return tile == WALL_SOLID || tile == WALL_BRICK;
    }
}