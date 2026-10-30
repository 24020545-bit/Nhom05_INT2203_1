package ui.screen;

import ui.GameFrame;

import javax.swing.*;
import java.awt.*;

public class GameSettingScreen extends JPanel {
    private JComboBox<String> playerChoiceBox;

    public GameSettingScreen(GameFrame frame) {
        setLayout(new GridBagLayout());
        setBackground(Color.DARK_GRAY);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = GridBagConstraints.RELATIVE;
        gbc.insets = new Insets(10, 0, 10, 0);

        JLabel title = new JLabel("THIẾT LẬP TRÒ CHƠI");
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setForeground(Color.WHITE);
        add(title, gbc);

        JPanel panelOptions = new JPanel(new FlowLayout());
        panelOptions.setOpaque(false);
        JLabel lblPlayers = new JLabel("Số người chơi: ");
        lblPlayers.setForeground(Color.WHITE);
        lblPlayers.setFont(new Font("Arial", Font.PLAIN, 16));

        playerChoiceBox = new JComboBox<>(new String[]{"1 Người chơi", "2 Người chơi (Chưa hỗ trợ)"});
        panelOptions.add(lblPlayers);
        panelOptions.add(playerChoiceBox);
        add(panelOptions, gbc);

        JButton nextBtn = new JButton("Tiếp tục");
        nextBtn.setFont(new Font("Arial", Font.BOLD, 16));
        nextBtn.setPreferredSize(new Dimension(150, 40));
        nextBtn.addActionListener(e -> frame.showScreen("LEVEL_SELECT"));

        JButton backBtn = new JButton("Quay lại");
        backBtn.setFont(new Font("Arial", Font.PLAIN, 14));
        backBtn.addActionListener(e -> frame.showScreen("HOME"));

        add(nextBtn, gbc);
        add(backBtn, gbc);
    }
}