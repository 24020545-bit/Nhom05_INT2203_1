package Util;

public class Constants {
    public static int TILE_SIZE = 22; // Kích thước mặc định
    public static final int MIN_TILE_SIZE = 14; // Giới hạn nhỏ nhất
    public static final int MAX_TILE_SIZE = 40; // Giới hạn lớn nhất

    // Hàm phóng to
    public static void zoomIn() {
        if (TILE_SIZE < MAX_TILE_SIZE) {
            TILE_SIZE += 2; // Tăng 2 pixel mỗi lần zoom
        }
    }

    // Hàm thu nhỏ
    public static void zoomOut() {
        if (TILE_SIZE > MIN_TILE_SIZE) {
            TILE_SIZE -= 2; // Giảm 2 pixel mỗi lần zoom
        }
    }
}