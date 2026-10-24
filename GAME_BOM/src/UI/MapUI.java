package UI;

import Map.GameMap;

import javax.swing.*;
import java.awt.*;

public class MapUI extends JFrame {

    private GameMap gameMap;
    private MapPanel mapPanel;
    private JCheckBox showGridCheckBox;
    private JComboBox<String> mapSelectorCombo;

    private static final int TILE_SIZE = 22;

    public MapUI() {
        gameMap = new GameMap();

        setTitle("Bomberman Map Selector - Dynamic Size");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 700); // Kích thước khung hình chính
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // --- Panel Control Chọn Map (Phía Trên) ---
        JPanel controlPanel = createControlPanel();
        add(controlPanel, BorderLayout.NORTH);

        // --- Panel Bản đồ (Ở Giữa - Dùng chung MapPanel hiện tại) ---
        mapPanel = new MapPanel(gameMap);

        // Bọc MapPanel vào một container có màu nền đồng bộ để tự động căn giữa bản đồ
        JPanel centerContainer = new JPanel(new GridBagLayout());
        centerContainer.setBackground(new Color(34, 139, 34)); // Màu nền cỏ
        centerContainer.add(mapPanel);

        add(centerContainer, BorderLayout.CENTER);

        // --- Panel Chú Thích (Phía Dưới) ---
        JPanel legendPanel = createLegendPanel();
        add(legendPanel, BorderLayout.SOUTH);
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        panel.setBackground(new Color(30, 41, 59));

        // Các tùy chọn bản đồ
        String[] mapOptions = {
                "Map 1",
                "Map 2",
                "Map 3",
                "Map 4"
        };

        mapSelectorCombo = new JComboBox<>(mapOptions);
        mapSelectorCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        mapSelectorCombo.addActionListener(e -> {
            int index = mapSelectorCombo.getSelectedIndex();
            gameMap.setMapIndex(index); // Đổi map dựa trên lựa chọn

            // Cập nhật lại kích thước ưu tiên cho mapPanel theo kích thước map mới
            int mapWidthPx = gameMap.getWidth() * TILE_SIZE;
            int mapHeightPx = gameMap.getHeight() * TILE_SIZE;
            mapPanel.setPreferredSize(new Dimension(mapWidthPx, mapHeightPx));
            mapPanel.revalidate();
            mapPanel.repaint();
        });

        showGridCheckBox = new JCheckBox("Hiện lưới Grid", true);
        showGridCheckBox.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        showGridCheckBox.setForeground(Color.WHITE);
        showGridCheckBox.setOpaque(false);
        showGridCheckBox.addActionListener(e -> {
            mapPanel.setShowGrid(showGridCheckBox.isSelected());
        });

        JLabel label = new JLabel("Chọn Bản Đồ:");
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));

        panel.add(label);
        panel.add(mapSelectorCombo);
        panel.add(showGridCheckBox);

        return panel;
    }

    private JPanel createLegendPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 8));
        panel.setBackground(new Color(15, 23, 42));

        panel.add(createLegendItem("Tường Đá (#)", new Color(75, 85, 99)));
        panel.add(createLegendItem("Gạch Nâu (X)", new Color(180, 83, 9)));
        panel.add(createLegendItem("Đường Đi ( )", new Color(134, 239, 172)));

        return panel;
    }

    private JPanel createLegendItem(String labelText, Color color) {
        JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        item.setOpaque(false);

        JPanel colorBox = new JPanel();
        colorBox.setPreferredSize(new Dimension(14, 14));
        colorBox.setBackground(color);
        colorBox.setBorder(BorderFactory.createLineBorder(Color.WHITE, 1));

        JLabel label = new JLabel(labelText);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        item.add(colorBox);
        item.add(label);
        return item;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MapUI ui = new MapUI();
            ui.setVisible(true);
        });
    }
}