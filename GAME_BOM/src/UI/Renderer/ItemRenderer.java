package UI.Renderer;

import Items.Items;
import Items.BombLimitUp;
import Items.LiveUp;
import Items.PowerUp;
import Items.SpeedUp;

import java.awt.*;
import java.util.List;

public class ItemRenderer {
    public static void draw(Graphics2D g2d, List<Items> itemsList, int tileSize) {
        if (itemsList == null) return;

        for (Items item : itemsList) {
            // Chỉ vẽ khi item chưa bị người chơi ăn mất
            if (!item.isCollected()) {
                int px = item.getX() * tileSize;
                int py = item.getY() * tileSize;

                // Vẽ nền hình tròn cho item
                g2d.setColor(Color.WHITE);
                g2d.fillOval(px + 2, py + 2, tileSize - 4, tileSize - 4);

                // Phân loại và vẽ ký hiệu tương ứng cho từng loại item
                if (item instanceof SpeedUp) {
                    g2d.setColor(Color.BLUE);
                    g2d.drawOval(px + 2, py + 2, tileSize - 4, tileSize - 4);
                    g2d.drawString("S", px + tileSize / 3, py + (tileSize * 3) / 4); // S = Speed
                } else if (item instanceof BombLimitUp) {
                    g2d.setColor(Color.BLACK);
                    g2d.drawOval(px + 2, py + 2, tileSize - 4, tileSize - 4);
                    g2d.drawString("B", px + tileSize / 3, py + (tileSize * 3) / 4); // B = Bomb
                } else if (item instanceof LiveUp) {
                    g2d.setColor(Color.RED);
                    g2d.drawOval(px + 2, py + 2, tileSize - 4, tileSize - 4);
                    g2d.drawString("♥", px + tileSize / 4, py + (tileSize * 3) / 4); // ♥ = Live
                } else if (item instanceof PowerUp) {
                    g2d.setColor(Color.ORANGE);
                    g2d.drawOval(px + 2, py + 2, tileSize - 4, tileSize - 4);
                    g2d.drawString("P", px + tileSize / 3, py + (tileSize * 3) / 4); // P = Power (Phạm vi lửa)
                }
            }
        }
    }
}