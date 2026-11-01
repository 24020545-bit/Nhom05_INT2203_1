package map;

import java.awt.Graphics2D;
import java.awt.Image;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ImageIcon;

public class GameMap {

    public static final int EMPTY = 0;
    public static final int WALL_BRICK = 1;
    public static final int WALL_SOLID = 2;

    private static final int TILE_SIZE = 32;

    private final List<int[][]> mapList;
    private int[][] currentGrid;

    private Image wall0Img;
    private Image wall1Img;
    private Image wall2Img;

    public GameMap() {
        mapList = new ArrayList<>();
        loadMapImages();
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

        if (!mapList.isEmpty()) {
            this.currentGrid = mapList.get(0);
        }
    }

    /**
     * Tải bản đồ theo Cửa ải (Level):
     * - Cửa ải 1, 2 dùng Map 1 (Index 0)
     * - Cửa ải 3, 4 dùng Map 2 (Index 1)
     * - Cửa ải 5, 6... dùng Map 3 (Index 2)
     */
    public void loadLevelMap(int levelIndex) {
        if (mapList.isEmpty()) return;

        int mapIndex = switch (levelIndex) {
            case 1, 2 -> 0; // Map 1
            case 3, 4 -> 1; // Map 2
            default -> 2;   // Map 3 (Level 5, 6...)
        };

        // Đảm bảo không bị vượt quá số lượng map đọc từ file map.txt
        if (mapIndex >= mapList.size()) {
            mapIndex = mapList.size() - 1;
        }

        this.currentGrid = mapList.get(mapIndex);
    }

    private void loadMapImages() {
        try {
            var w0 = getClass().getResource("/EnvironmentImgae/wall0.png");
            var w1 = getClass().getResource("/EnvironmentImgae/wall1.png");
            var w2 = getClass().getResource("/EnvironmentImgae/wall2.png");

            wall0Img = (w0 != null) ? new ImageIcon(w0).getImage() : null;
            wall1Img = (w1 != null) ? new ImageIcon(w1).getImage() : null;
            wall2Img = (w2 != null) ? new ImageIcon(w2).getImage() : null;
        } catch (Exception e) {
            System.err.println("Lỗi khi tải ảnh môi trường bản đồ: " + e.getMessage());
        }
    }

    private void loadMapsFromFile() {
        List<String> currentMapLines = new ArrayList<>();

        try {
            InputStream is = getClass()
                    .getClassLoader()
                    .getResourceAsStream("map.txt");

            if (is == null) {
                return;
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
        if (currentGrid != null) {
            return currentGrid;
        }
        if (mapList.isEmpty()) {
            return new int[0][0];
        }
        return mapList.getFirst();
    }

    public int getWidth() {
        int[][] grid = getGrid();
        return grid.length > 0 ? grid[0].length : 0;
    }

    public int getHeight() {
        return getGrid().length;
    }

    public void setGrid(int[][] loadedGrid) {
        this.currentGrid = loadedGrid;
    }

    /** Phương thức vẽ bản đồ bằng hình ảnh */
    public void draw(Graphics2D g2d) {
        int[][] grid = getGrid();
        for (int y = 0; y < getHeight(); y++) {
            for (int x = 0; x < getWidth(); x++) {
                int tile = grid[y][x];
                Image tileImg = null;

                if (tile >= 5 && tile <= 9) {
                    tileImg = wall0Img;
                } else {
                    switch (tile) {
                        case WALL_BRICK -> tileImg = wall1Img;
                        case WALL_SOLID -> tileImg = wall2Img;
                        default -> tileImg = wall0Img;
                    }
                }

                if (tileImg != null) {
                    g2d.drawImage(tileImg, x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE, null);
                }
            }
        }
    }
}