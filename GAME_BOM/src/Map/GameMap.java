package Map;

import java.util.ArrayList;
import java.util.List;

public class GameMap {
    public static final int SIZE = 30;

    // Các ký hiệu trên bản đồ
    public static final char EMPTY = ' ';      // Đường đi (ô trống)
    public static final char WALL_SOLID = '#';  // Tường cố định (không phá được)
    public static final char WALL_BRICK = 'X'; // Tường gạch (phá được bằng bom)

    // Danh sách lưu trữ chính xác 4 map cố định
    private List<char[][]> mapList;
    private int currentMapIndex;

    public GameMap() {
        mapList = new ArrayList<>();

        // Khởi tạo và nạp sẵn 4 map vào danh sách
        mapList.add(createMap1_Classic());


        currentMapIndex = 0; // Mặc định chọn Map 1
    }

    // ==========================================
    // THIẾT KẾ 4 MAP CỐ ĐỊNH
    // ==========================================

    // Hàm chuyển đổi cải tiến: Dùng dấu chấm '.' làm đường đi để dễ nhìn và không sợ lệch cột
    private char[][] parseStringToMap(String[] mapTemplate) {
        char[][] grid = new char[SIZE][SIZE];

        for (int y = 0; y < SIZE; y++) {
            String rowStr = (y < mapTemplate.length) ? mapTemplate[y] : "";

            for (int x = 0; x < SIZE; x++) {
                if (x < rowStr.length()) {
                    char c = rowStr.charAt(x);
                    // Quy ước ký tự trong chuỗi sang ký tự của game:
                    if (c == '.') {
                        grid[y][x] = EMPTY;     // Dấu chấm là đường đi
                    } else {
                        grid[y][x] = c;         // Giữ nguyên # hoặc X
                    }
                } else {
                    grid[y][x] = EMPTY;         // Mặc định nếu thiếu ký tự
                }
            }
        }
        return grid;
    }

    // Bản đồ 30x30
    // Map 1: Cổ điển (Classic Bomberman style)
    private char[][] createMap1_Classic() {
        String[] customDesign = {
                "#################",
                "#...#XXXXX#XXX..#",
                "#.#.#X#####X###.#",
                "#X#.XXXXXXXXX#XX#",
                "#X#.#X#X####X#X##",
                "#X#X#X#XXXXXXXXX#",
                "#X#X#X#X#######X#",
                "#X#X#X###XXXXXXX#",
                "#XXX#XXXXX##X####",
                "#X###X###X#XXXXX#",
                "#XXX#XXX#XXX#####",
                "#X#X#X#X#X#X#XXX#",
                "#X#XXX#X#X#X###X#",
                "#X#X###X#X#X#XXX#",
                "#X#XXX#X#X#XXX#X#",
                "#X#X#X#X#####X#X#",
                "#XXX#X#XXXXXXX#X#",
                "###X#X#########X#",
                "#XXX#XXXXXX#XXXX#",
                "#.###X####X#X##.#",
                "#..XXXXXX#XXXX..#",
                "#################"
        };

        return parseStringToMap(customDesign);
    }

    // Phương thức để bạn có thể thêm map tùy chỉnh thủ công bên ngoài nếu muốn
    public void addCustomMap(char[][] customGrid) {
        if (customGrid.length == SIZE && customGrid[0].length == SIZE) {
            mapList.add(customGrid);
        }
    }

    public char[][] getGrid() {
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

    public char getTile(int x, int y) {
        char[][] currentGrid = getGrid();
        if (x < 0 || x >= SIZE || y < 0 || y >= SIZE) return WALL_SOLID;
        return currentGrid[y][x];
    }

    public boolean isWalkable(int x, int y) {
        if (x < 0 || x >= SIZE || y < 0 || y >= SIZE) {
            return false;
        }

        return getTile(x, y) == EMPTY;
    }


}