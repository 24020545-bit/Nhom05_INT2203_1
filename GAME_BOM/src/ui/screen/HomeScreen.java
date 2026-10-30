package ui.screen;

import ui.GameFrame;

import javax.swing.*;
import java.awt.*;

public class HomeScreen extends JPanel {
    public HomeScreen(GameFrame frame) {
        setLayout(new GridBagLayout());
        setBackground(Color.DARK_GRAY);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = GridBagConstraints.RELATIVE;
        gbc.insets = new Insets(15, 0, 15, 0);

        JLabel titleLabel = new JLabel("BOMBERMAN");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 40));
        titleLabel.setForeground(Color.WHITE);
        add(titleLabel, gbc);

        JButton startBtn = createButton("Bắt đầu");
        JButton keyBindBtn = createButton("Thiết lập phím");

        startBtn.addActionListener(e -> frame.showScreen("GAME_SETTING"));
        keyBindBtn.addActionListener(e -> frame.showScreen("KEY_BIND"));

        add(startBtn, gbc);
        add(keyBindBtn, gbc);
    }

    private JButton createButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.BOLD, 18));
        btn.setPreferredSize(new Dimension(220, 45));
        btn.setFocusPainted(false);
        return btn;
    }
}