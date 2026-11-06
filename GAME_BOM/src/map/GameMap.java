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
            this.currentGrid = copyGrid(mapList.get(0));
        }
    }

    /**
     * Tải bản đồ theo Cửa ải (Level):
     * Cửa ải 1, 2 dùng Map index 0
     * Cửa ải 3, 4 dùng Map index 1
     * Cửa ải 5, 6 dùng Map index 2...
     * Tự động tạo bản sao (Deep Copy) để phá gạch màn trước không làm hỏng màn sau.
     */
    public void loadLevelMap(int levelIndex) {
        if (mapList.isEmpty()) return;

        // Tính toán index map dựa vào levelIndex (mỗi 2 level đổi 1 map)
        int mapIndex = (levelIndex - 1) / 2;

        // Đảm bảo không bị out of bounds nếu levelIndex quá lớn
        if (mapIndex >= mapList.size()) {
            mapIndex = mapList.size() - 1;
        }

        // Deep copy bản đồ gốc để biến đổi ô gạch độc lập giữa các màn
        this.currentGrid = copyGrid(mapList.get(mapIndex));
    }

    /**
     * Phương thức sao chép mảng 2 chiều để tránh gán trùng tham chiếu (Reference)
     */
    private int[][] copyGrid(int[][] original) {
        if (original == null) return new int[0][0];
        int height = original.length;
        int width = original[0].length;
        int[][] copy = new int[height][width];

        for (int i = 0; i < height; i++) {
            System.arraycopy(original[i], 0, copy[i], 0, width);
        }
        return copy;
    }

    private void loadMapImages() {
        try {
            // Đã kiểm tra fallback cả 2 đường dẫn tránh gõ sai chính tả đường dẫn thư mục
            var w0 = getClass().getResource("/EnvironmentImgae/wall0.png");
            if (w0 == null) w0 = getClass().getResource("/EnvironmentImage/wall0.png");

            var w1 = getClass().getResource("/EnvironmentImgae/wall1.png");
            if (w1 == null) w1 = getClass().getResource("/EnvironmentImage/wall1.png");

            var w2 = getClass().getResource("/EnvironmentImgae/wall2.png");
            if (w2 == null) w2 = getClass().getResource("/EnvironmentImage/wall2.png");

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
                System.err.println("Không tìm thấy file map.txt trong resources!");
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
        this.currentGrid = copyGrid(loadedGrid);
    }

    /** Phương thức vẽ bản đồ bằng hình ảnh */
    public void draw(Graphics2D g2d) {
        int[][] grid = getGrid();
        int height = getHeight();
        int width = getWidth();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int tile = grid[y][x];
                Image tileImg = null;

                // Ẩn các ô chứa item (5-9) bên dưới hình gạch/cỏ
                if (tile >= 5 && tile <= 9) {
                    tileImg = wall0Img; // Ô chứa item khi chưa nổ gạch thì hiển thị nền cỏ hoặc tường tùy game logic
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