package item;

import entity.Mod;
import java.awt.*;

public abstract class Items {
    protected int x;
    protected int y;
    protected boolean isCollected;

    public Items(int x, int y) {
        this.x = x;
        this.y = y;
        this.isCollected = false;
    }

    public abstract void applyEffect(Mod target);

    // Thêm hàm draw trừu tượng để các item con tự định nghĩa cách vẽ
    public abstract void draw(Graphics2D g2d, int tileSize);

    public int getX() { return x; }
    public int getY() { return y; }
    public boolean isCollected() { return isCollected; }
    public void setCollected(boolean collected) { isCollected = collected; }
}