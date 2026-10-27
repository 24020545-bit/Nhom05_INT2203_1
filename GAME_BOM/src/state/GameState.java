package state;

public enum GameState {
    HOME,           // Màn hình trang chủ (có nút Start)
    SETUP,          // Màn hình cấu hình (chọn chế độ, chọn map)
    PLAYING,        // Màn hình chơi game chính
    GAME_OVER       // (Tùy chọn) Màn hình kết thúc game
}