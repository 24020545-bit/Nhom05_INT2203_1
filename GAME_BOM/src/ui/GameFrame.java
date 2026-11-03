package ui;

import ui.screen.*;

import javax.swing.*;
import java.awt.*;
import java.util.Properties;
// Thêm thư viện âm thanh
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.net.URL;

public class GameFrame extends JFrame {
    private final CardLayout cardLayout;
    private final JPanel containerPanel;

    private HomeScreen homeScreen;
    private GameSettingScreen gameSettingScreen;
    private KeyBindScreen keyBindScreen;
    private LevelSelectScreen levelSelectScreen;

    private int playerCount = 1;
    private Clip bgClip; // Thêm biến lưu clip nhạc

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

        // Bắt đầu phát nhạc ngay khi khởi tạo Frame
        playMenuMusic();

        setLocationRelativeTo(null);
    }

    // THÊM: Hàm phát nhạc
    public void playMenuMusic() {
        try {
            if (bgClip != null && bgClip.isRunning()) return;

            URL url = getClass().getResource("/audio/homestart.wav");
            if (url != null) {
                AudioInputStream audioIn = AudioSystem.getAudioInputStream(url);
                bgClip = AudioSystem.getClip();
                bgClip.open(audioIn);
                bgClip.loop(Clip.LOOP_CONTINUOUSLY);
                bgClip.start();
            } else {
                System.err.println("Không tìm thấy file âm thanh: /audio/homestart.wav");
            }
        } catch (Exception e) {
            System.err.println("Lỗi phát nhạc menu: " + e.getMessage());
        }
    }

    // THÊM: Hàm dừng nhạc
    public void stopMenuMusic() {
        if (bgClip != null && bgClip.isRunning()) {
            bgClip.stop();
        }
    }

    public void showScreen(String name) {
        if (name.equals("LEVEL_SELECT") || name.equals("HOME") || name.equals("GAME_SETTING") || name.equals("KEY_BIND")) {
            setSize(800, 600);
            setLocationRelativeTo(null);
        }

        if (name.equals("LEVEL_SELECT")) {
            levelSelectScreen.buildLevelButtons();
        }

        // THÊM: Kiểm tra nếu quay lại các màn hình menu thì tiếp tục bật nhạc (nếu đang tắt)
        if (name.equals("HOME") || name.equals("GAME_SETTING") || name.equals("KEY_BIND") || name.equals("LEVEL_SELECT")) {
            playMenuMusic();
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
        // THÊM: Tắt nhạc khi bắt đầu vào chơi game
        stopMenuMusic();

        GamePanel gamePanel = new GamePanel(this, levelIndex, playerCount);
        containerPanel.add(gamePanel, "GAME_PLAY_" + levelIndex);
        cardLayout.show(containerPanel, "GAME_PLAY_" + levelIndex);

        gamePanel.adjustWindowSize();
        gamePanel.requestFocusInWindow();
    }

    public void startLevelWithSave(int levelIndex, Properties saveProps) {
        // THÊM: Tắt nhạc khi tải lại game dở
        stopMenuMusic();

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