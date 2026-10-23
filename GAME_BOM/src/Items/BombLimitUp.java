package Items;

import Player.PlayerOb;

public class BombLimitUp extends Items {
    public BombLimitUp(int x, int y) { super(x, y); }

    @Override
    public void applyEffect(PlayerOb target) {
        if (target != null && target.isAlive()) {
            target.setBombLimit(target.getBombLimit() + 1);
            this.isCollected = true;
        }
    }
}