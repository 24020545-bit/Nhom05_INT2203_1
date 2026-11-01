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

    private int playerCount = 1;

    public GameFrame() {
        utils.SaveManager.loadGame();
        setTitle("Bomberman Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setSize(800, 600);
        setResizable(true);

        cardLayout = new CardLayout();
        containerPanel = new JPanel(cardLayout);

        homeScreen = new HomeScreen(this);
        gameSettingScreen = new GameSettingScreen(this);
        keyBindScreen = new KeyBindScreen(this);
        levelSelectScreen = new LevelSelectScreen(this);

        containerPanel.add(homeScreen, "HOME");
        containerPanel.add(gameSettingScreen, "GAME_SETTING");
        containerPanel.add(keyBindScreen, "KEY_BIND");
        containerPanel.add(levelSelectScreen, "LEVEL_SELECT");

        add(containerPanel);
        cardLayout.show(containerPanel, "HOME");

        setLocationRelativeTo(null);
    }

    public void showScreen(String name) {
        if (name.equals("LEVEL_SELECT") || name.equals("HOME") || name.equals("GAME_SETTING") || name.equals("KEY_BIND")) {
            setSize(800, 600);
            setLocationRelativeTo(null);
        }

        if (name.equals("LEVEL_SELECT")) {
            levelSelectScreen.buildLevelButtons();
        }
        cardLayout.show(containerPanel, name);
    }

    public int getPlayerCount() {
        return playerCount;
    }

    public void setPlayerCount(int playerCount) {
        this.playerCount = Math.max(1, Math.min(2, playerCount));
    }

    public void startLevel(int levelIndex) {
        GamePanel gamePanel = new GamePanel(this, levelIndex, playerCount);
        containerPanel.add(gamePanel, "GAME_PLAY_" + levelIndex);
        cardLayout.show(containerPanel, "GAME_PLAY_" + levelIndex);

        gamePanel.adjustWindowSize();
        gamePanel.requestFocusInWindow();
    }

    public void startLevelWithSave(int levelIndex, Properties saveProps) {
        GamePanel gamePanel = new GamePanel(this, levelIndex, playerCount, saveProps);
        containerPanel.add(gamePanel, "GAME_PLAY_" + levelIndex);
        cardLayout.show(containerPanel, "GAME_PLAY_" + levelIndex);

        gamePanel.adjustWindowSize();
        gamePanel.requestFocusInWindow();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new GameFrame().setVisible(true);
        });
    }
}