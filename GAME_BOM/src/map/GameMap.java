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

    private static final int TILE_SIZE = 32; // Kích thước chuẩn cho mỗi ô

    private final List<int[][]> mapList;
    private int[][] currentGrid; // Lưới bản đồ đang được sử dụng cho màn chơi hiện tại

    // Các biến chứa ảnh môi trường
    private Image wall0Img; // Đường trống (EMPTY)
    private Image wall1Img; // Tường gạch phá hủy được (WALL_BRICK)
    private Image wall2Img; // Tường cố định (WALL_SOLID)

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

        // Mặc định gán grid đầu tiên cho bản đồ hiện tại
        if (!mapList.isEmpty()) {
            this.currentGrid = mapList.get(0);
        }
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

    // Phương thức gán trực tiếp mảng grid khi load game save dở
    public void setGrid(int[][] loadedGrid) {
        this.currentGrid = loadedGrid;
    }

    /**
     * Phương thức vẽ bản đồ bằng hình ảnh theo đúng đường dẫn thư mục
     */
    public void draw(Graphics2D g2d) {
        int[][] grid = getGrid();
        for (int y = 0; y < getHeight(); y++) {
            for (int x = 0; x < getWidth(); x++) {
                int tile = grid[y][x];
                Image tileImg = null;

                // Các ô chứa item (5 -> 9) hiển thị nền đất trống (dùng wall0)
                if (tile >= 5 && tile <= 9) {
                    tileImg = wall0Img;
                } else {
                    switch (tile) {
                        case WALL_BRICK -> tileImg = wall1Img; // Tường gạch
                        case WALL_SOLID -> tileImg = wall2Img; // Tường cố định
                        default -> tileImg = wall0Img;         // Đường trống (EMPTY)
                    }
                }

                if (tileImg != null) {
                    g2d.drawImage(tileImg, x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE, null);
                }
            }
        }
    }
}