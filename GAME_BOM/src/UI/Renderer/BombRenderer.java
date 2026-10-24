package UI.Renderer;

import Bomb.Bomb;
import Bomb.BombManager;

import java.awt.*;

public class BombRenderer {

    // Hàm chuyên vẽ danh sách bom
    public static void draw(Graphics2D g2d, BombManager bombManager, int tileSize) {
        if (bombManager == null) return;

        for (Bomb bomb : bombManager.getActiveBombs()) {
            if (!bomb.isExploded()) {
                int drawX = bomb.getX() * tileSize;
                int drawY = bomb.getY() * tileSize;

                // Thân quả bom (Hình tròn xám đậm)
                g2d.setColor(new Color(30, 30, 30));
                g2d.fillOval(drawX + 4, drawY + 4, tileSize - 8, tileSize - 8);

                // Ngòi nổ (Chấm đỏ nhỏ ở giữa)
                g2d.setColor(Color.RED);
                g2d.fillOval(drawX + tileSize / 2 - 3, drawY + 3, 6, 6);
            }
        }
    }
}