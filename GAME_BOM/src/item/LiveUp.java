package item;

import entity.Mod;

import java.awt.*;

public class LiveUp extends Items {
    public LiveUp(int x, int y) { super(x, y); }

    @Override
    public void applyEffect(Mod target) {
        if (target != null && target.isAlive()) {
            target.setLives(target.getLives() + 1);
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
        g2d.setColor(Color.RED);
        g2d.drawOval(px + 2, py + 2, tileSize - 4, tileSize - 4);
        g2d.drawString("♥", px + tileSize / 4, py + (tileSize * 3) / 4);
    }
}