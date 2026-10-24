package UI.Screen;

import Map.GameMap;
import UI.MainFrame;
import UI.MapPanel;

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

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        controlPanel.setBackground(new Color(30, 41, 59));

        JLabel mapLabel = new JLabel("Chọn bản đồ:");
        mapLabel.setForeground(Color.WHITE);

        JCheckBox gridCheckBox = new JCheckBox("Hiện lưới Grid", true);
        gridCheckBox.setForeground(Color.WHITE);
        gridCheckBox.setOpaque(false);

        // Khởi tạo nút Zoom + và Zoom -
        JButton zoomInBtn = new JButton("Zoom +");
        JButton zoomOutBtn = new JButton("Zoom -");

        JButton startButton = new JButton("Bắt đầu chơi (1 Player + 3 Bots)");
        JButton backButton = new JButton("Quay lại");

        mapSelectorCombo.addActionListener(e -> {
            gameMap.setMapIndex(mapSelectorCombo.getSelectedIndex());
            mapPanel.repaint();
        });

        gridCheckBox.addActionListener(e ->
                mapPanel.setShowGrid(gridCheckBox.isSelected())
        );

        // Sự kiện phóng to bản đồ
        zoomInBtn.addActionListener(e -> {
            mapPanel.zoomMap(true);
        });

        // Sự kiện thu nhỏ bản đồ
        zoomOutBtn.addActionListener(e -> {
            mapPanel.zoomMap(false);
        });

        // Khi bấm bắt đầu, chỉ cần truyền mapIndex (mặc định luôn 1 Player và 3 Bot)
        startButton.addActionListener(e -> {
            int selectedMap = mapSelectorCombo.getSelectedIndex();
            frame.startGame(selectedMap);
        });

        backButton.addActionListener(e ->
                frame.showScreen("HOME")
        );

        // Thêm các thành phần vào controlPanel
        controlPanel.add(mapLabel);
        controlPanel.add(mapSelectorCombo);
        controlPanel.add(gridCheckBox);
        controlPanel.add(zoomInBtn);  // Thêm nút Zoom +
        controlPanel.add(zoomOutBtn); // Thêm nút Zoom -
        controlPanel.add(startButton);
        controlPanel.add(backButton);

        add(controlPanel, BorderLayout.NORTH);
        add(mapPanel, BorderLayout.CENTER);
    }
}