package manager;

import map.GameMap;
import bomb.Bomb;
import bomb.Explosion;
import entity.Mob; // Đảm bảo đúng package chứa lớp Mob của bạn

import java.util.ArrayList;
import java.util.List;

public class BombManager {
    private List<Bomb> activeBombs;
    private List<Explosion> activeExplosions;
    private GameMap gameMap;
    private List<Mob> mobs; // Đổi từ players thành danh sách tổng quát các Mob (bao gồm Player và Bot)
    private ItemManager itemManager;

    public BombManager(GameMap gameMap, List<Mob> mobs, ItemManager itemManager) {
        this.gameMap = gameMap;
        this.mobs = mobs;
        this.itemManager = itemManager;
        this.activeBombs = new ArrayList<>();
        this.activeExplosions = new ArrayList<>();
    }

    public boolean placeBomb(int x, int y, int power, Mob owner) {
        for (Bomb b : activeBombs) {
            if (b.getX() == x && b.getY() == y) {
                return false;
            }
        }

        Bomb newBomb = new Bomb(x, y, power, owner, gameMap, this);
        activeBombs.add(newBomb);
        return true;
    }

    // Tìm xem tại tọa độ (x, y) có quả bom nào đang hoạt động không
    public Bomb getBombAt(int x, int y) {
        for (Bomb b : activeBombs) {
            if (!b.isExploded() && b.getX() == x && b.getY() == y) {
                return b;
            }
        }
        return null;
    }

    public void triggerExplosion(int bombX, int bombY, int power) {
        Explosion explosion = new Explosion(bombX, bombY, power, gameMap, itemManager, this);
        activeExplosions.add(explosion);

        checkExplosionDamage(explosion);
    }

    private void checkExplosionDamage(Explosion exp) {
        if (mobs == null) return;

        int bX = exp.getX();
        int bY = exp.getY();
        int power = exp.getPower();

        for (Mob mob : mobs) {
            // Kiểm tra nếu mob còn sống mới xét sát thương
            if (!mob.isSurvival()) continue;

            // Chuyển đổi tọa độ pixel của mob sang tọa độ ô lưới (tile) tương ứng để so sánh với lửa
            int tileSize = 32; // Thay đổi kích thước tile cho khớp với game của bạn (ví dụ: 32 hoặc 48)
            int mobX = (int) Math.round(mob.getX() / tileSize);
            int mobY = (int) Math.round(mob.getY() / tileSize);

            if (isInExplosionArea(mobX, mobY, bX, bY, power)) {
                mob.die(); // Gọi hàm die() có sẵn trong lớp Mob để tiêu diệt Player hoặc Bot
            }
        }
    }

    private boolean isInExplosionArea(int mobX, int mobY, int bX, int bY, int power) {
        if (mobX == bX && mobY == bY) return true;

        int[][] directions = {{0, -1}, {0, 1}, {-1, 0}, {1, 0}};
        for (int[] d : directions) {
            for (int i = 1; i <= power; i++) {
                int fireX = bX + (d[0] * i);
                int fireY = bY + (d[1] * i);

                if (!gameMap.isWalkable(fireX, fireY) && gameMap.getTile(fireX, fireY) != GameMap.EMPTY) {
                    if (gameMap.getTile(fireX, fireY) == GameMap.WALL_BRICK) {
                        if (mobX == fireX && mobY == fireY) return true;
                    }
                    break;
                }

                if (mobX == fireX && mobY == fireY) {
                    return true;
                }
            }
        }
        return false;
    }

    public void addExplosion(Explosion explosion) {
        activeExplosions.add(explosion);
    }

    public void update() {
        activeBombs.removeIf(Bomb::isExploded);
        activeExplosions.removeIf(Explosion::isFinished);
    }

    public List<Bomb> getActiveBombs() { return activeBombs; }
    public List<Explosion> getActiveExplosions() { return activeExplosions; }
}