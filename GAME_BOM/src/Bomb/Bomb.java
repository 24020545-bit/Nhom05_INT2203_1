package Bomb;

import Player.Explosion;
import Player.PlayerOb;
import Map.GameMap;

import java.util.Timer;
import java.util.TimerTask;

public class Bomb {
    private int x;
    private int y;
    private int power;
    private PlayerOb owner;
    private GameMap gameMap;
    private BombManager bombManager; // Tham chiếu đến BombManager để báo cáo vụ nổ
    private boolean isExploded;
    private Timer timer;

    public Bomb(int x, int y, int power, PlayerOb owner, GameMap gameMap, BombManager bombManager) {
        this.x = x;
        this.y = y;
        this.power = power;
        this.owner = owner;
        this.gameMap = gameMap;
        this.bombManager = bombManager;
        this.isExploded = false;

        startTimer();
    }

    public void startTimer() {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                explode();
            }
        }, 2000); // 3 giây tự nổ
    }

    public void explode() {
        synchronized (this) {
            if (isExploded) return;
            this.isExploded = true;
            if (timer != null) timer.cancel();
        }

        // Nhờ BombManager kích hoạt hiệu ứng và quét sát thương
        if (bombManager != null) {
            bombManager.triggerExplosion(this.x, this.y, this.power);
        }

        if (owner != null) {
            int currentActive = owner.getActiveBombCount();
            if (currentActive > 0) {
                owner.setActiveBombCount(currentActive - 1);
            }
        }
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getPower() { return power; }
    public PlayerOb getOwner() { return owner; }
    public boolean isExploded() { return isExploded; }
}