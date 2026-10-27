package UI.Screen;

import javax.swing.*;
import java.awt.*;

public class HomeScreenPanel extends JPanel {
    private JButton startButton;
    private JButton settingsButton; // [MỚI]

    public HomeScreenPanel(Runnable onStartClicked, Runnable onSettingsClicked) { // [CẬP NHẬT] Nhận thêm callback
        setLayout(new GridBagLayout());
        setBackground(new Color(30, 30, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(15, 15, 15, 15);

        // Tiêu đề game
        JLabel titleLabel = new JLabel("BOMBERMAN JAVA");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 40));
        titleLabel.setForeground(Color.YELLOW);
        add(titleLabel, gbc);

        // Nút Bắt đầu
        gbc.gridy = 1;
        startButton = new JButton("BẮT ĐẦU");
        styleButton(startButton, new Color(46, 204, 113));
        startButton.addActionListener(e -> onStartClicked.run());
        add(startButton, gbc);

        // [MỚI] Nút Cài đặt phím
        gbc.gridy = 2;
        settingsButton = new JButton("CÀI ĐẶT PHÍM");
        styleButton(settingsButton, new Color(52, 152, 219));
        settingsButton.addActionListener(e -> onSettingsClicked.run());
        add(settingsButton, gbc);
    }

    private void styleButton(JButton btn, Color bgColor) {
        btn.setFont(new Font("Arial", Font.BOLD, 20));
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(220, 50));
    }
}