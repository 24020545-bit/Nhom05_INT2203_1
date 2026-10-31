package ui.screen;

import ui.GameFrame;

import javax.swing.*;
import java.awt.*;

public class KeyBindScreen extends JPanel {
    public KeyBindScreen(GameFrame frame) {
        setLayout(new BorderLayout());
        setBackground(Color.DARK_GRAY);

        JLabel title = new JLabel("THIẾT LẬP PHÍM ĐIỀU KHIỂN", JLabel.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setForeground(Color.WHITE);
        title.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));
        add(title, BorderLayout.NORTH);

        JPanel content = new JPanel(new GridLayout(6, 1, 5, 5));
        content.setOpaque(false);
        content.setBorder(BorderFactory.createEmptyBorder(10, 100, 10, 100));

        // Người chơi 1
        content.add(createSectionHeader("--- NGƯỜI CHƠI 1 ---", Color.CYAN));
        content.add(createKeyRow("Di chuyển:", "W, A, S, D"));
        content.add(createKeyRow("Đặt bom:", "Phím SPACE"));

        // Người chơi 2
        content.add(createSectionHeader("--- NGƯỜI CHƠI 2 ---", Color.ORANGE));
        content.add(createKeyRow("Di chuyển:", "Các phím Mũi Tên"));
        content.add(createKeyRow("Đặt bom:", "Phím ENTER"));

        add(content, BorderLayout.CENTER);

        JPanel bottom = new JPanel();
        bottom.setOpaque(false);
        bottom.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        JButton backBtn = new JButton("Quay lại");
        backBtn.setPreferredSize(new Dimension(120, 35));
        backBtn.addActionListener(e -> frame.showScreen("HOME"));
        bottom.add(backBtn);

        add(bottom, BorderLayout.SOUTH);
    }

    private JLabel createSectionHeader(String text, Color color) {
        JLabel lbl = new JLabel(text);
        lbl.setForeground(color);
        lbl.setFont(new Font("Arial", Font.BOLD, 15));
        return lbl;
    }

    private JPanel createKeyRow(String action, String key) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        JLabel lblAction = new JLabel(action);
        lblAction.setForeground(Color.WHITE);
        lblAction.setFont(new Font("Arial", Font.PLAIN, 14));

        JLabel lblKey = new JLabel(key);
        lblKey.setForeground(Color.YELLOW);
        lblKey.setFont(new Font("Arial", Font.BOLD, 14));

        row.add(lblAction, BorderLayout.WEST);
        row.add(lblKey, BorderLayout.EAST);
        return row;
    }
}