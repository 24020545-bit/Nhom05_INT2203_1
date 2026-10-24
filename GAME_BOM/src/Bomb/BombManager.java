package Bomb;

import Map.GameMap;
import Player.Explosion;
import Player.PlayerOb;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class BombManager {
    private List<Bomb> activeBombs;
    private List<Explosion> activeExplosions; // Danh sách quản lý các vụ nổ đang cháy
    private GameMap gameMap;

    public BombManager(GameMap gameMap) {
        this.gameMap = gameMap;
        this.activeBombs = new ArrayList<>();
        this.activeExplosions = new ArrayList<>();
    }

    public boolean placeBomb(int x, int y, int power, PlayerOb owner) {
        for (Bomb b : activeBombs) {
            if (b.getX() == x && b.getY() == y) {
                return false;
            }
        }

        // Truyền thêm 'this' (BombManager) vào để quả bom có thể thêm vụ nổ khi kích hoạt
        Bomb newBomb = new Bomb(x, y, power, owner, gameMap, this);
        activeBombs.add(newBomb);
        return true;
    }

    public void addExplosion(Explosion explosion) {
        activeExplosions.add(explosion);
    }

    // Cập nhật trạng thái định kỳ: dọn dẹp bom đã nổ và vụ nổ đã hết thời gian (0.5s)
    public void update() {
        // Dọn dẹp bom đã nổ
        Iterator<Bomb> bombIterator = activeBombs.iterator();
        while (bombIterator.hasNext()) {
            Bomb bomb = bombIterator.next();
            if (bomb.isExploded()) {
                bombIterator.remove();
            }
        }

        // Dọn dẹp hiệu ứng vụ nổ sau khi hết giờ
        Iterator<Explosion> explosionIterator = activeExplosions.iterator();
        while (explosionIterator.hasNext()) {
            Explosion explosion = explosionIterator.next();
            if (explosion.isFinished()) {
                explosionIterator.remove();
            }
        }
    }

    public List<Bomb> getActiveBombs() {
        return activeBombs;
    }

    public List<Explosion> getActiveExplosions() {
        return activeExplosions;
    }
}