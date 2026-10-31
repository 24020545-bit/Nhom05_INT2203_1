package ui.screen;

import ui.GameFrame;
import utils.SaveManager;

import javax.swing.*;
import java.awt.*;
import java.util.Properties;

public class LevelSelectScreen extends JPanel {
    private static final int TOTAL_LEVELS = 6;
    private static int unlockedLevel = 1;

    private final JPanel gridPanel;
    private final GameFrame parentFrame;

    public LevelSelectScreen(GameFrame frame) {
        this.parentFrame = frame;
        setLayout(new BorderLayout());
        setBackground(Color.DARK_GRAY);

        JLabel title = new JLabel("CHỌN CỬA ẢI", JLabel.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setForeground(Color.WHITE);
        title.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));
        add(title, BorderLayout.NORTH);

        gridPanel = new JPanel(new GridLayout(2, 3, 20, 20));
        gridPanel.setOpaque(false);
        gridPanel.setBorder(BorderFactory.createEmptyBorder(30, 80, 30, 80));

        buildLevelButtons();
        add(gridPanel, BorderLayout.CENTER);

        JPanel bottom = new JPanel();
        bottom.setOpaque(false);
        bottom.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        JButton backBtn = new JButton("Quay lại Cài đặt");
        backBtn.setFont(new Font("Arial", Font.PLAIN, 14));
        backBtn.addActionListener(e -> frame.showScreen("GAME_SETTING"));
        bottom.add(backBtn);

        add(bottom, BorderLayout.SOUTH);
    }

    public void buildLevelButtons() {
        gridPanel.removeAll();

        for (int i = 1; i <= TOTAL_LEVELS; i++) {
            final int levelId = i;
            JButton levelBtn = new JButton("Cửa ải " + i);
            levelBtn.setFont(new Font("Arial", Font.BOLD, 16));
            levelBtn.setFocusPainted(false);

            if (i <= unlockedLevel) {
                levelBtn.setBackground(new Color(60, 179, 113));
                levelBtn.setForeground(Color.WHITE);
                levelBtn.setEnabled(true);
                levelBtn.addActionListener(e -> handleLevelClick(levelId));
            } else {
                levelBtn.setBackground(new Color(128, 128, 128));
                levelBtn.setForeground(Color.LIGHT_GRAY);
                levelBtn.setEnabled(false);
                levelBtn.setText("🔒 Cửa ải " + i);
            }

            gridPanel.add(levelBtn);
        }

        gridPanel.revalidate();
        gridPanel.repaint();
    }

    private void handleLevelClick(int levelId) {
        Properties saveProps = SaveManager.loadGameProgress();

        if (saveProps != null) {
            int savedLevel = Integer.parseInt(saveProps.getProperty("currentLevel", "0"));

            if (savedLevel == levelId) {
                String[] options = {"Chơi tiếp (Dở trận)", "Chơi lại từ đầu", "Hủy"};
                int choice = JOptionPane.showOptionDialog(
                        this,
                        "Bạn đang có tiến trình chơi dở ở Cửa ải " + levelId + ". Bạn muốn tiếp tục hay chơi lại từ đầu?",
                        "Tùy chọn chơi",
                        JOptionPane.DEFAULT_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        options,
                        options[0]
                );

                if (choice == 0) {
                    parentFrame.startLevelWithSave(levelId, saveProps);
                    return;
                } else if (choice == 1) {
                    SaveManager.clearSaveFile();
                    parentFrame.startLevel(levelId);
                    return;
                } else {
                    return;
                }
            }
        }

        parentFrame.startLevel(levelId);
    }

    public static void setUnlockedLevel(int level) {
        unlockedLevel = level;
    }

    public static void unlockNextLevel(int completedLevel) {
        if (completedLevel >= unlockedLevel && completedLevel < TOTAL_LEVELS) {
            unlockedLevel = completedLevel + 1;
            SaveManager.saveGame(unlockedLevel);
        }
    }
}