package utils;

import ui.screen.LevelSelectScreen;

import java.io.*;
import java.util.Properties;

public class SaveManager {
    private static final String SAVE_FILE = "save.properties";

    // Lưu dữ liệu tiến trình (ví dụ: màn chơi cao nhất đã mở khóa)
    public static void saveGame(int unlockedLevel) {
        Properties props = new Properties();
        props.setProperty("unlockedLevel", String.valueOf(unlockedLevel));

        try (OutputStream output = new FileOutputStream(SAVE_FILE)) {
            props.store(output, "Bomberman Game Save Data");
            System.out.println("Đã lưu game thành công!");
        } catch (IOException e) {
            System.err.println("Lỗi khi lưu game: " + e.getMessage());
        }
    }

    // Tải dữ liệu khi khởi động game
    public static void loadGame() {
        File file = new File(SAVE_FILE);
        if (!file.exists()) {
            System.out.println("Không tìm thấy file save cũ, khởi tạo mặc định.");
            return; // Chưa có file save thì giữ nguyên mặc định (Mở màn 1)
        }

        Properties props = new Properties();
        try (InputStream input = new FileInputStream(SAVE_FILE)) {
            props.load(input);

            // Đọc dữ liệu màn chơi đã mở khóa
            String levelStr = props.getProperty("unlockedLevel");
            if (levelStr != null) {
                int level = Integer.parseInt(levelStr);
                LevelSelectScreen.setUnlockedLevel(level); // Cập nhật vào màn hình chọn cửa ải
                System.out.println("Đã tải dữ liệu game thành công! Mở khóa đến màn: " + level);
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("Lỗi khi đọc file save: " + e.getMessage());
        }
    }
}