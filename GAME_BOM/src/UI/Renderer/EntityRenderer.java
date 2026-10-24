package UI.Renderer;

import Player.PlayerOb;
import java.awt.*;

public class EntityRenderer {

    // Hàm vẽ chung cho mọi đối tượng kế thừa PlayerOb (Player, Bot,...)
    public static void draw(Graphics2D g2d, PlayerOb entity, int tileSize, Color bodyColor) {
        if (!entity.isAlive()) return;

        // 1. Tính toán tọa độ pixel thực tế trên màn hình
        double pixelX = entity.getX() * tileSize;
        double pixelY = entity.getY() * tileSize;

        // 2. Kích thước nhân vật (nhỏ hơn ô tile một chút để có khoảng cách)
        int charSize = tileSize - 8;

        // 3. Căn giữa nhân vật vào ô lưới
        int renderX = (int) Math.round(pixelX + (double)(tileSize - charSize) / 2.0);
        int renderY = (int) Math.round(pixelY + (double)(tileSize - charSize) / 2.0);

        // 4. Vẽ thân đối tượng (Màu sắc truyền vào tùy ý: Player đỏ, Bot xanh,...)
        g2d.setColor(bodyColor);
        g2d.fillOval(renderX, renderY, charSize, charSize);

        // 5. Vẽ viền trắng xung quanh
        g2d.setColor(Color.WHITE);
        g2d.drawOval(renderX, renderY, charSize, charSize);
    }
}