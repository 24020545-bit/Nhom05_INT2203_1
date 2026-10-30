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
        title.setBorder(BorderFactory.createEmptyBorder(30, 0, 20, 0));
        add(title, BorderLayout.NORTH);

        JPanel content = new JPanel(new GridLayout(4, 1, 10, 10));
        content.setOpaque(false);
        content.setBorder(BorderFactory.createEmptyBorder(20, 150, 20, 150));

        content.add(createKeyRow("Di chuyển lên:", "W / Phũ tên Lên"));
        content.add(createKeyRow("Di chuyển xuống:", "S / Mũi tên Xuống"));
        content.add(createKeyRow("Di chuyển trái:", "A / Mũi tên Trái"));
        content.add(createKeyRow("Di chuyển phải:", "D / Mũi tên Phải"));

        add(content, BorderLayout.CENTER);

        JPanel bottom = new JPanel();
        bottom.setOpaque(false);
        bottom.setBorder(BorderFactory.createEmptyBorder(0, 0, 30, 0));
        JButton backBtn = new JButton("Quay lại");
        backBtn.setPreferredSize(new Dimension(120, 35));
        backBtn.addActionListener(e -> frame.showScreen("HOME"));
        bottom.add(backBtn);

        add(bottom, BorderLayout.SOUTH);
    }

    private JPanel createKeyRow(String action, String key) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        JLabel lblAction = new JLabel(action);
        lblAction.setForeground(Color.WHITE);
        lblAction.setFont(new Font("Arial", Font.PLAIN, 16));

        JLabel lblKey = new JLabel(key);
        lblKey.setForeground(Color.YELLOW);
        lblKey.setFont(new Font("Arial", Font.BOLD, 16));

        row.add(lblAction, BorderLayout.WEST);
        row.add(lblKey, BorderLayout.EAST);
        return row;
    }
}