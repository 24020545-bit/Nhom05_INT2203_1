package Items;

import Player.PlayerOb;

public abstract class Items {
    protected int x;
    protected int y;
    protected boolean isCollected;

    public Items(int x, int y) {
        this.x = x;
        this.y = y;
        this.isCollected = false;
    }

    public abstract void applyEffect(PlayerOb target);

    public int getX() { return x; }
    public int getY() { return y; }
    public boolean isCollected() { return isCollected; }
    public void setCollected(boolean collected) { isCollected = collected; }
}