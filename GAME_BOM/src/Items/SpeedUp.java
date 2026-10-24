package Items;

import Player.PlayerOb;

public class SpeedUp extends Items {
    public SpeedUp(int x, int y) { super(x, y); }

    @Override
    public void applyEffect(PlayerOb target) {
        if (target != null && target.isAlive()) {
            target.setSpeed(target.getSpeed() + 0.02);
            this.isCollected = true;
        }
    }
}