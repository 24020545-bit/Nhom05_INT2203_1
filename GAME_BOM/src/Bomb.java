import java.util.Timer;
import java.util.TimerTask;

public class Bomb {
    private int x, y, power;
    private PlayerOb owner;
    private GameMap gameMap;
    private boolean isExploded = false;
    private Timer timer;

    public Bomb(int x, int y, int power, PlayerOb owner, GameMap gameMap) {
        this.x = x;
        this.y = y;
        this.power = power;
        this.owner = owner;
        this.gameMap = gameMap;
        startTimer();
    }

    public void startTimer() {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() { explode(); }
        }, 3000);
    }

    public void explode() {
        synchronized (this) {
            if (isExploded) return;
            isExploded = true;
            if (timer != null) timer.cancel();
        }

        if (gameMap != null) {
            gameMap.addExplosion(new Explosion(this.x, this.y, this.power, this.gameMap));
            gameMap.removeBomb(this);
        }

        if (owner != null && owner.getActiveBombCount() > 0) {
            owner.setActiveBombCount(owner.getActiveBombCount() - 1);
        }
    }

    public int getX() { return x; }
    public void setX(int x) { this.x = x; }
    public int getY() { return y; }
    public void setY(int y) { this.y = y; }
    public int getPower() { return power; }
    public void setPower(int power) { this.power = power; }
    public PlayerOb getOwner() { return owner; }
    public void setOwner(PlayerOb owner) { this.owner = owner; }
    public boolean isExploded() { return isExploded; }
}