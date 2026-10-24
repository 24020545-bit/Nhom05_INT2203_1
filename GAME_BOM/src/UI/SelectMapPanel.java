package UI;

import Map.GameMap;

import javax.swing.*;
import java.awt.*;

public class SelectMapPanel extends JPanel {
    private final GameMap gameMap;
    private final MapPanel mapPanel;
    private final JComboBox<String> mapSelectorCombo;

    public SelectMapPanel(MainFrame frame, GameMap gameMap) {
        this.gameMap = gameMap;

        setLayout(new BorderLayout());

        mapPanel = new MapPanel(gameMap);

        String[] maps = {"Map 1", "Map 2", "Map 3", "Map 4"};
        mapSelectorCombo = new JComboBox<>(maps);

        JPanel controlPanel = new JPanel(
                new FlowLayout(FlowLayout.CENTER, 15, 10)
        );
        controlPanel.setBackground(new Color(30, 41, 59));

        JLabel label = new JLabel("Chọn bản đồ:");
        label.setForeground(Color.WHITE);

        JCheckBox gridCheckBox = new JCheckBox("Hiện lưới Grid", true);
        gridCheckBox.setForeground(Color.WHITE);
        gridCheckBox.setOpaque(false);

        JButton startButton = new JButton("Bắt đầu chơi");
        JButton backButton = new JButton("Quay lại");

        mapSelectorCombo.addActionListener(e -> {
            gameMap.setMapIndex(mapSelectorCombo.getSelectedIndex());
            mapPanel.repaint();
        });

        gridCheckBox.addActionListener(e ->
                mapPanel.setShowGrid(gridCheckBox.isSelected())
        );

        startButton.addActionListener(e ->
                frame.startGame(mapSelectorCombo.getSelectedIndex())
        );

        backButton.addActionListener(e ->
                frame.showScreen("HOME")
        );

        controlPanel.add(label);
        controlPanel.add(mapSelectorCombo);
        controlPanel.add(gridCheckBox);
        controlPanel.add(startButton);
        controlPanel.add(backButton);

        add(controlPanel, BorderLayout.NORTH);
        add(mapPanel, BorderLayout.CENTER);
        add(createLegendPanel(), BorderLayout.SOUTH);
    }

    private JPanel createLegendPanel() {
        JPanel panel = new JPanel(
                new FlowLayout(FlowLayout.CENTER, 20, 8)
        );
        panel.setBackground(new Color(15, 23, 42));

        panel.add(createLegendItem(
                "Tường đá", new Color(75, 85, 99)
        ));
        panel.add(createLegendItem(
                "Gạch nâu", new Color(180, 83, 9)
        ));
        panel.add(createLegendItem(
                "Đường đi", new Color(134, 239, 172)
        ));
        panel.add(createLegendItem(
                "Vị trí Spawn", new Color(239, 68, 68)
        ));

        return panel;
    }

    private JPanel createLegendItem(String text, Color color) {
        JPanel item = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 5, 0)
        );
        item.setOpaque(false);

        JPanel colorBox = new JPanel();
        colorBox.setPreferredSize(new Dimension(14, 14));
        colorBox.setBackground(color);
        colorBox.setBorder(
                BorderFactory.createLineBorder(Color.WHITE)
        );

        JLabel label = new JLabel(text);
        label.setForeground(Color.WHITE);

        item.add(colorBox);
        item.add(label);

        return item;
    }
}