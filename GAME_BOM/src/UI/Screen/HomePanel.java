package UI.Screen;

import UI.MainFrame;

import javax.swing.*;
import java.awt.*;

public class HomePanel extends JPanel {
    public HomePanel(MainFrame frame) {
        setPreferredSize(new Dimension(500, 400));
        setLayout(new GridBagLayout());
        setBackground(new Color(30, 41, 59));

        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("BOMBERMAN");
        title.setFont(new Font("Segoe UI", Font.BOLD, 36));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton playButton = new JButton("Bắt đầu");
        JButton exitButton = new JButton("Thoát");

        playButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        exitButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        playButton.addActionListener(e -> frame.showScreen("SELECT"));
        exitButton.addActionListener(e -> System.exit(0));

        panel.add(title);
        panel.add(Box.createVerticalStrut(35));
        panel.add(playButton);
        panel.add(Box.createVerticalStrut(15));
        panel.add(exitButton);

        add(panel);
    }
}