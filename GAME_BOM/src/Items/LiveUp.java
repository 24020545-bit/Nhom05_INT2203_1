package Items;

import Player.PlayerOb;

public class LiveUp extends Items {
    public LiveUp(int x, int y) { super(x, y); }

    @Override
    public void applyEffect(PlayerOb target) {
        if (target != null && target.isAlive()) {
            target.setLives(target.getLives() + 1);
            this.isCollected = true;
        }
    }
}