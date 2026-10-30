package ui.screen;

import ui.GameFrame;
import utils.SaveManager;

import javax.swing.*;
import java.awt.*;

public class LevelSelectScreen extends JPanel {
    private static final int TOTAL_LEVELS = 6; // Tổng số cửa ải trong game
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

        // Chia lưới 2 hàng, 3 cột để hiển thị các cửa ải gọn gàng
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

    // Hàm xây dựng và vẽ lại trạng thái các nút cửa ải dựa vào unlockedLevel hiện tại
    public void buildLevelButtons() {
        gridPanel.removeAll();

        for (int i = 1; i <= TOTAL_LEVELS; i++) {
            final int levelId = i;
            JButton levelBtn = new JButton("Cửa ải " + i);
            levelBtn.setFont(new Font("Arial", Font.BOLD, 16));
            levelBtn.setFocusPainted(false);

            // Kiểm tra trạng thái mở khóa
            if (i <= unlockedLevel) {
                levelBtn.setBackground(new Color(60, 179, 113)); // Xanh lá: Đã mở
                levelBtn.setForeground(Color.WHITE);
                levelBtn.setEnabled(true);
                levelBtn.addActionListener(e -> parentFrame.startLevel(levelId));
            } else {
                levelBtn.setBackground(new Color(128, 128, 128)); // Xám: Bị khóa
                levelBtn.setForeground(Color.LIGHT_GRAY);
                levelBtn.setEnabled(false);
                levelBtn.setText("🔒 Cửa ải " + i);
            }

            gridPanel.add(levelBtn);
        }

        gridPanel.revalidate();
        gridPanel.repaint();
    }

    public static void setUnlockedLevel(int level) {
        unlockedLevel = level;
    }

    public static void unlockNextLevel(int completedLevel) {
        if (completedLevel >= unlockedLevel && completedLevel < TOTAL_LEVELS) {
            unlockedLevel = completedLevel + 1;
            // Tự động lưu game ngay khi mở khóa màn mới vào file save.properties
            SaveManager.saveGame(unlockedLevel);
        }
    }
}