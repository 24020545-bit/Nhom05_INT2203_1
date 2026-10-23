package Items;

import Player.PlayerOb;

public class PowerUp extends Items {
    public PowerUp(int x, int y) { super(x, y); }

    @Override
    public void applyEffect(PlayerOb target) {
        if (target != null && target.isAlive()) {
            target.setPower(target.getPower() + 1);
            this.isCollected = true;
        }
    }
}