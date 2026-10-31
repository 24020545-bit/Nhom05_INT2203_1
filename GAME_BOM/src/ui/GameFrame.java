package ui;

import ui.screen.*;

import javax.swing.*;
import java.awt.*;
import java.util.Properties;

public class GameFrame extends JFrame {
    private final CardLayout cardLayout;
    private final JPanel containerPanel;

    private HomeScreen homeScreen;
    private GameSettingScreen gameSettingScreen;
    private KeyBindScreen keyBindScreen;
    private LevelSelectScreen levelSelectScreen;

    // Biến lưu trữ số lượng người chơi được chọn từ Cài đặt (Mặc định là 1 người chơi)
    private int playerCount = 1;

    public GameFrame() {
        utils.SaveManager.loadGame();
        setTitle("Bomberman Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        setResizable(false);

        cardLayout = new CardLayout();
        containerPanel = new JPanel(cardLayout);

        // Khởi tạo các màn hình
        homeScreen = new HomeScreen(this);
        gameSettingScreen = new GameSettingScreen(this);
        keyBindScreen = new KeyBindScreen(this);
        levelSelectScreen = new LevelSelectScreen(this);

        // Đưa các màn hình vào CardLayout
        containerPanel.add(homeScreen, "HOME");
        containerPanel.add(gameSettingScreen, "GAME_SETTING");
        containerPanel.add(keyBindScreen, "KEY_BIND");
        containerPanel.add(levelSelectScreen, "LEVEL_SELECT");

        add(containerPanel);
        cardLayout.show(containerPanel, "HOME");
    }

    public void showScreen(String name) {
        // Nếu quay trở về màn hình chọn cửa ải, tiến hành vẽ lại (refresh) trạng thái các nút mở khóa
        if (name.equals("LEVEL_SELECT")) {
            levelSelectScreen.buildLevelButtons();
        }
        cardLayout.show(containerPanel, name);
    }

    // --- CÁC HÀM GETTER/SETTER CHO SỐ LƯỢNG NGƯỜI CHƠI ---
    public int getPlayerCount() {
        return playerCount;
    }

    public void setPlayerCount(int playerCount) {
        this.playerCount = Math.max(1, Math.min(2, playerCount)); // Giới hạn từ 1 đến 2 người
    }

    public void startLevel(int levelIndex) {
        // Tạo màn chơi GamePanel mới từ đầu, truyền kèm theo số lượng người chơi đã chọn
        GamePanel gamePanel = new GamePanel(this, levelIndex, playerCount);
        containerPanel.add(gamePanel, "GAME_PLAY_" + levelIndex);
        cardLayout.show(containerPanel, "GAME_PLAY_" + levelIndex);
        gamePanel.requestFocusInWindow();
    }

    // Khởi tạo màn chơi khôi phục từ file save dở (truyền kèm properties và số lượng người chơi tương ứng)
    public void startLevelWithSave(int levelIndex, Properties saveProps) {
        GamePanel gamePanel = new GamePanel(this, levelIndex, playerCount, saveProps);
        containerPanel.add(gamePanel, "GAME_PLAY_" + levelIndex);
        cardLayout.show(containerPanel, "GAME_PLAY_" + levelIndex);
        gamePanel.requestFocusInWindow();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new GameFrame().setVisible(true);
        });
    }
}