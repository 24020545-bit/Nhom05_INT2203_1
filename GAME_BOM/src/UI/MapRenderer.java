package UI;

import map.GameMap;
import java.awt.*;

public class MapRenderer {

    public static void draw(Graphics2D g2d, GameMap gameMap, int tileSize, boolean showGrid) {
        if (gameMap == null) return;

        char[][] grid = gameMap.getGrid();
        int mapWidth = gameMap.getWidth();
        int mapHeight = gameMap.getHeight();

        for (int y = 0; y < mapHeight; y++) {
            for (int x = 0; x < mapWidth; x++) {
                int drawX = x * tileSize;
                int drawY = y * tileSize;
                char tile = grid[y][x];

                // 1. Vẽ ô tương ứng trên bản đồ (Tường đá, tường gạch, ô trống)
                switch (tile) {
                    case GameMap.WALL_SOLID:
                        g2d.setColor(new Color(75, 85, 99)); // Xám đậm (Tường cứng)
                        g2d.fillRect(drawX, drawY, tileSize, tileSize);
                        break;
                    case GameMap.WALL_BRICK:
                        g2d.setColor(new Color(180, 83, 9)); // Cam đất (Tường gạch phá được)
                        g2d.fillRect(drawX, drawY, tileSize, tileSize);
                        break;
                    case GameMap.EMPTY:
                        g2d.setColor(new Color(134, 239, 172)); // Xanh lá nhạt (Đường đi)
                        g2d.fillRect(drawX, drawY, tileSize, tileSize);
                        break;
                }

                // 2. Vẽ khung lưới (Grid) nếu được bật
                if (showGrid) {
                    g2d.setColor(new Color(0, 0, 0, 40));
                    g2d.drawRect(drawX, drawY, tileSize, tileSize);
                }
            }
        }
    }
}