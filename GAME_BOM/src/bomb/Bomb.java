package bomb;

import map.GameMap;
import entity.Mod;
import manager.BombManager;

import java.awt.*;
import java.util.Timer;
import java.util.TimerTask;

public class Bomb {
    private int x;
    private int y;
    private int power;
    private Mod owner;
    private GameMap gameMap;
    private BombManager bombManager; // Tham chiếu đến BombManager để báo cáo vụ nổ
    private boolean isExploded;
    private Timer timer;

    public Bomb(int x, int y, int power, Mod owner, GameMap gameMap, BombManager bombManager) {
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
        }, 2000); // 2 giây tự nổ
    }

    // Hàm kích hoạt nổ ngay lập tức (dùng cho bom dây chuyền)
    public void forceExplode() {
        synchronized (this) {
            if (isExploded) return;
            if (timer != null) {
                timer.cancel();
            }
        }
        explode();
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

    // ==========================================
    // HÀM VẼ QUẢ BOM LÊN MÀN HÌNH
    // ==========================================
    public void draw(Graphics2D g2d, int tileSize) {
        if (isExploded) return;

        int drawX = x * tileSize;
        int drawY = y * tileSize;

        // Thân quả bom (Hình tròn xám đậm)
        g2d.setColor(new Color(30, 30, 30));
        g2d.fillOval(drawX + 4, drawY + 4, tileSize - 8, tileSize - 8);

        // Ngòi nổ (Chấm đỏ nhỏ ở giữa)
        g2d.setColor(Color.RED);
        g2d.fillOval(drawX + tileSize / 2 - 3, drawY + 3, 6, 6);
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getPower() { return power; }
    public Mod getOwner() { return owner; }
    public boolean isExploded() { return isExploded; }
}