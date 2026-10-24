package UI;

import Map.GameMap;
import java.awt.*;

public class MapRenderer {

    public static void draw(Graphics2D g2d, GameMap gameMap, int tileSize, boolean showGrid) {
        if (gameMap == null) return;

        char[][] grid = gameMap.getGrid();
        int mapWidth = gameMap.getWidth();
        int mapHeight = gameMap.getHeight();

        // Sử dụng kích thước động từ gameMap thay vì GameMap.SIZE cứng
        for (int y = 0; y < mapHeight; y++) {
            for (int x = 0; x < mapWidth; x++) {
                int drawX = x * tileSize;
                int drawY = y * tileSize;
                char tile = grid[y][x];

                // 1. Vẽ ô tương ứng (Tường đá, gạch, đường đi)
                switch (tile) {
                    case GameMap.WALL_SOLID:
                        g2d.setColor(new Color(75, 85, 99));
                        g2d.fillRect(drawX, drawY, tileSize, tileSize);
                        break;
                    case GameMap.WALL_BRICK:
                        g2d.setColor(new Color(180, 83, 9));
                        g2d.fillRect(drawX, drawY, tileSize, tileSize);
                        break;
                    case GameMap.EMPTY:
                        g2d.setColor(new Color(134, 239, 172));
                        g2d.fillRect(drawX, drawY, tileSize, tileSize);
                        break;
                }

                // 2. Vẽ lưới (Grid) nếu được bật
                if (showGrid) {
                    g2d.setColor(new Color(0, 0, 0, 40));
                    g2d.drawRect(drawX, drawY, tileSize, tileSize);
                }
            }
        }
    }
}