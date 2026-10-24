package UI;

import Map.GameMap;
import Player.Player;

import javax.swing.*;
import java.awt.*;

public class MapPanel extends JPanel {
    private final GameMap gameMap;
    private final Player player;
    private boolean showGrid = true;

    private static final int TILE_SIZE = 22;

    public MapPanel(GameMap gameMap) {
        this(gameMap, null);
    }

    public MapPanel(GameMap gameMap, Player player) {
        this.gameMap = gameMap;
        this.player = player;

        int dimension = GameMap.SIZE * TILE_SIZE;
        setPreferredSize(new Dimension(dimension, dimension));
        setBackground(new Color(34, 139, 34));
    }

    public void setShowGrid(boolean showGrid) {
        this.showGrid = showGrid;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );

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
                        g2d.fillRect(
                                drawX + 2, drawY + 2,
                                TILE_SIZE - 4, TILE_SIZE - 4
                        );
                        break;

                    case GameMap.WALL_BRICK:
                        g2d.setColor(new Color(180, 83, 9));
                        g2d.fillRect(drawX, drawY, TILE_SIZE, TILE_SIZE);

                        g2d.setColor(new Color(217, 119, 6));
                        g2d.drawRect(
                                drawX + 2, drawY + 2,
                                TILE_SIZE - 5, TILE_SIZE - 5
                        );
                        break;

                    case GameMap.EMPTY:
                        g2d.setColor(new Color(134, 239, 172));
                        g2d.fillRect(drawX, drawY, TILE_SIZE, TILE_SIZE);

                        if (x <= 3 && y <= 3) {
                            g2d.setColor(new Color(187, 247, 208));
                            g2d.fillRect(
                                    drawX, drawY, TILE_SIZE, TILE_SIZE
                            );
                        }
                        break;
                }

                // [ĐÃ XÓA] Đoạn code vẽ Spawn cố định tại (x == 1 && y == 1) ở đây
                // để tránh bị để lại chấm đỏ mờ khi người chơi di chuyển đi nơi khác.

                // Hiển thị lưới
                if (showGrid) {
                    g2d.setColor(new Color(0, 0, 0, 40));
                    g2d.drawRect(drawX, drawY, TILE_SIZE, TILE_SIZE);
                }
            }
        }

        // Vẽ người chơi căn giữa tuyệt đối cho mọi hướng di chuyển
        if (player != null && player.isAlive()) {
            // 1. Lấy tọa độ pixel thực tế của nhân vật trên màn hình
            double pixelX = player.getX() * TILE_SIZE;
            double pixelY = player.getY() * TILE_SIZE;

            // 2. Kích thước nhân vật (nhỏ hơn TILE_SIZE một chút để có khoảng thở)
            int charSize = TILE_SIZE - 8;

            // 3. Tính toán vị trí vẽ sao cho tâm của hình tròn luôn nằm chính giữa ô lưới
            // Công thức: Tọa độ ô + (Kích thước ô - Kích thước nhân vật) / 2
            int renderX = (int) Math.round(pixelX + (double)(TILE_SIZE - charSize) / 2.0);
            int renderY = (int) Math.round(pixelY + (double)(TILE_SIZE - charSize) / 2.0);

            // Vẽ thân nhân vật
            g2d.setColor(Color.RED);
            g2d.fillOval(renderX, renderY, charSize, charSize);

            // Vẽ viền trắng xung quanh
            g2d.setColor(Color.WHITE);
            g2d.drawOval(renderX, renderY, charSize, charSize);
        }
    }
}