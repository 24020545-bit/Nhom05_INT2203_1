
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

        setTitle("Bomberman Map Selector - 30x30");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLayout(new BorderLayout());

        // --- Panel Bản đồ ---
        mapPanel = new MapPanel();
        add(mapPanel, BorderLayout.CENTER);

        // --- Panel Control Chọn Map ---
        JPanel controlPanel = createControlPanel();
        add(controlPanel, BorderLayout.NORTH);

        // --- Panel Chú Thích ---
        JPanel legendPanel = createLegendPanel();
        add(legendPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
    }

    private class MapPanel extends JPanel {
        public MapPanel() {
            int mapDimension = GameMap.SIZE * TILE_SIZE;
            setPreferredSize(new Dimension(mapDimension, mapDimension));
            setBackground(new Color(34, 139, 34));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            char[][] grid = gameMap.getGrid();

            for (int y = 0; y < GameMap.SIZE; y++) {
                for (int x = 0; x < GameMap.SIZE; x++) {
                    int drawX = x * TILE_SIZE;
                    int drawY = y * TILE_SIZE;
                    char tile = grid[y][x];

                    switch (tile) {
                        case GameMap.WALL_SOLID:
                            g2d.setColor(new Color(75, 85, 99));
                            g2d.fillRect(drawX, drawY, TILE_SIZE, TILE_SIZE);
                            g2d.setColor(new Color(107, 114, 128));
                            g2d.fillRect(drawX + 2, drawY + 2, TILE_SIZE - 4, TILE_SIZE - 4);
                            break;

                        case GameMap.WALL_BRICK:
                            g2d.setColor(new Color(180, 83, 9));
                            g2d.fillRect(drawX, drawY, TILE_SIZE, TILE_SIZE);
                            g2d.setColor(new Color(217, 119, 6));
                            g2d.drawRect(drawX + 2, drawY + 2, TILE_SIZE - 5, TILE_SIZE - 5);
                            break;

                        case GameMap.EMPTY:
                            g2d.setColor(new Color(134, 239, 172));
                            g2d.fillRect(drawX, drawY, TILE_SIZE, TILE_SIZE);
                            if (x <= 3 && y <= 3) {
                                g2d.setColor(new Color(187, 247, 208));
                                g2d.fillRect(drawX, drawY, TILE_SIZE, TILE_SIZE);
                            }
                            break;
                    }

                    if (x == 1 && y == 1) {
                        g2d.setColor(new Color(239, 68, 68));
                        g2d.fillOval(drawX + 3, drawY + 3, TILE_SIZE - 6, TILE_SIZE - 6);
                        g2d.setColor(Color.WHITE);
                        g2d.drawOval(drawX + 3, drawY + 3, TILE_SIZE - 6, TILE_SIZE - 6);
                    }

                    if (showGridCheckBox != null && showGridCheckBox.isSelected()) {
                        g2d.setColor(new Color(0, 0, 0, 40));
                        g2d.drawRect(drawX, drawY, TILE_SIZE, TILE_SIZE);
                    }
                }
            }
        }
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        panel.setBackground(new Color(30, 41, 59));

        // Đúng 4 map cố định cho người chơi lựa chọn
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
            gameMap.setMapIndex(index);
            mapPanel.repaint(); // Cập nhật lại giao diện khi đổi map
        });

        showGridCheckBox = new JCheckBox("Hiện lưới Grid", true);
        showGridCheckBox.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        showGridCheckBox.setForeground(Color.WHITE);
        showGridCheckBox.setOpaque(false);
        showGridCheckBox.addActionListener(e -> mapPanel.repaint());

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
        panel.add(createLegendItem("Vị trí Spawn", new Color(239, 68, 68)));

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