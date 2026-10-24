package UI;

import Player.Explosion;
import Map.GameMap;

import java.awt.*;
import java.util.List;

public class ExplosionRenderer {

    public static void draw(Graphics2D g2d, List<Explosion> activeExplosions, GameMap gameMap, int tileSize) {
        if (activeExplosions == null || activeExplosions.isEmpty() || gameMap == null) return;

        char[][] grid = gameMap.getGrid();

        for (Explosion exp : activeExplosions) {
            if (exp.isFinished()) continue;

            int x = exp.getX();
            int y = exp.getY();
            int power = exp.getPower();

            // 1. Vẽ tâm vụ nổ (ô đặt bom)
            drawFlameTile(g2d, x, y, tileSize);

            // 2. Lan ra 4 hướng (Lên, Xuống, Trái, Phải)
            int[][] directions = {
                    {0, -1}, // Lên
                    {0, 1},  // Xuống
                    {-1, 0}, // Trái
                    {1, 0}   // Phải
            };

            for (int[] dir : directions) {
                int dx = dir[0];
                int dy = dir[1];

                for (int i = 1; i <= power; i++) {
                    int targetX = x + (dx * i);
                    int targetY = y + (dy * i);

                    // Kiểm tra biên bản đồ
                    if (targetX < 0 || targetX >= GameMap.SIZE || targetY < 0 || targetY >= GameMap.SIZE) {
                        break; // Ra khỏi biên -> dừng hướng này
                    }

                    char tile = grid[targetY][targetX];

                    // Gặp Tường đá (#): Sóng lửa bị chặn lại hoàn toàn, KHÔNG vẽ qua ô này và dừng hướng này
                    if (tile == GameMap.WALL_SOLID) {
                        break;
                    }

                    // Gặp Tường gạch (X): Lửa vẫn vẽ đè lên viên gạch đó (vì gạch đang nổ),
                    // nhưng sau ô này thì sóng lửa bị chặn lại, không lan tiếp nữa
                    if (tile == GameMap.WALL_BRICK) {
                        drawFlameTile(g2d, targetX, targetY, tileSize);
                        break;
                    }

                    // Nếu là ô trống (EMPTY): Vẽ lửa bình thường và tiếp tục lan ra xa
                    drawFlameTile(g2d, targetX, targetY, tileSize);
                }
            }
        }
    }

    private static void drawFlameTile(Graphics2D g2d, int tileX, int tileY, int tileSize) {
        int drawX = tileX * tileSize;
        int drawY = tileY * tileSize;

        // Vỏ ngoài ngọn lửa màu cam sáng
        g2d.setColor(new Color(249, 115, 22));
        g2d.fillRect(drawX + 2, drawY + 2, tileSize - 4, tileSize - 4);

        // Lõi lửa bên trong màu vàng
        g2d.setColor(new Color(252, 211, 77));
        g2d.fillOval(drawX + tileSize / 4, drawY + tileSize / 4, tileSize / 2, tileSize / 2);
    }
}