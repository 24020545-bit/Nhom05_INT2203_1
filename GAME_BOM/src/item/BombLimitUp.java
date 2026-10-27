package item;

import entity.PlayerOb;

import java.awt.*;

public class BombLimitUp extends Items {
    public BombLimitUp(int x, int y) { super(x, y); }

    @Override
    public void applyEffect(PlayerOb target) {
        if (target != null && target.isAlive()) {
            target.setBombLimit(target.getBombLimit() + 1);
            this.isCollected = true;
        }
    }
    @Override
    public void draw(Graphics2D g2d, int tileSize) {
        if (isCollected) return;
        int px = x * tileSize;
        int py = y * tileSize;

        g2d.setColor(Color.WHITE);
        g2d.fillOval(px + 2, py + 2, tileSize - 4, tileSize - 4);
        g2d.setColor(Color.BLACK);
        g2d.drawOval(px + 2, py + 2, tileSize - 4, tileSize - 4);
        g2d.drawString("B", px + tileSize / 3, py + (tileSize * 3) / 4);
    }
}