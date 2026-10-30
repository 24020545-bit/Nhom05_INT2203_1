package ui;

import ui.screen.*;

import javax.swing.*;
import java.awt.*;

public class GameFrame extends JFrame {
    private final CardLayout cardLayout;
    private final JPanel containerPanel;

    private HomeScreen homeScreen;
    private GameSettingScreen gameSettingScreen;
    private KeyBindScreen keyBindScreen;
    private LevelSelectScreen levelSelectScreen;

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
        cardLayout.show(containerPanel, name);
    }

    public void startLevel(int levelIndex) {
        // Tạo màn chơi GamePanel dựa theo level được chọn
        GamePanel gamePanel = new GamePanel(this, levelIndex);
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