package UI.Screen;

import map.GameMap;
import javax.swing.*;
import java.awt.*;

public class GameSetupPanel extends JPanel {
    private JComboBox<String> modeComboBox;
    private JComboBox<String> mapComboBox;
    private JButton playButton;

    public GameSetupPanel(GameMap gameMap, OnGameStartListener listener) {
        setLayout(new GridBagLayout());
        setBackground(new Color(40, 40, 55));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        // Tiêu đề cấu hình
        JLabel title = new JLabel("THIẾT LẬP TRẬN ĐẤU");
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setForeground(Color.WHITE);

        GridBagConstraints titleGbc = new GridBagConstraints();
        titleGbc.gridx = 0; titleGbc.gridy = 0;
        titleGbc.gridwidth = 2;
        titleGbc.insets = new Insets(0, 0, 30, 0);
        titleGbc.anchor = GridBagConstraints.CENTER;
        add(title, titleGbc);

        // 1. Chọn chế độ chơi
        gbc.gridy = 1; gbc.gridx = 0;
        JLabel modeLabel = new JLabel("Chế độ chơi:");
        modeLabel.setForeground(Color.WHITE);
        modeLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        add(modeLabel, gbc);

        gbc.gridx = 1;
        String[] modes = {
                "1 Người chơi + 3 Bot",
                "2 Người chơi + 2 Bot"
        };
        modeComboBox = new JComboBox<>(modes);
        modeComboBox.setFont(new Font("Arial", Font.PLAIN, 14));
        add(modeComboBox, gbc);

        // 2. Chọn Map
        gbc.gridy = 2; gbc.gridx = 0;
        JLabel mapLabel = new JLabel("Chọn Bản đồ:");
        mapLabel.setForeground(Color.WHITE);
        mapLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        add(mapLabel, gbc);

        gbc.gridx = 1;
        int mapCount = gameMap.getMapCount();
        String[] mapNames = new String[mapCount];
        for (int i = 0; i < mapCount; i++) {
            mapNames[i] = "Bản đồ " + (i + 1);
        }
        mapComboBox = new JComboBox<>(mapNames);
        mapComboBox.setFont(new Font("Arial", Font.PLAIN, 14));
        add(mapComboBox, gbc);

        // 3. Nút Vào Chơi
        gbc.gridy = 3; gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(30, 10, 10, 10);

        playButton = new JButton("VÀO CHƠI");
        playButton.setFont(new Font("Arial", Font.BOLD, 18));
        playButton.setBackground(new Color(52, 152, 219));
        playButton.setForeground(Color.WHITE);
        playButton.setFocusPainted(false);
        playButton.setPreferredSize(new Dimension(180, 40));

        playButton.addActionListener(e -> {
            int selectedMode = modeComboBox.getSelectedIndex(); // 0: 1P+3Bot, 1: 2P+2Bot
            int selectedMapIndex = mapComboBox.getSelectedIndex();
            listener.onStart(selectedMode, selectedMapIndex);
        });

        add(playButton, gbc);
    }

    public interface OnGameStartListener {
        void onStart(int gameMode, int mapIndex);
    }
}