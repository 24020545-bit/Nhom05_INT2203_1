package Bomb;

import Items.ItemManager; // Import ItemManager
import Map.GameMap;
import Player.Explosion;
import Player.PlayerOb;

import java.util.ArrayList;
import java.util.List;

public class BombManager {
    private List<Bomb> activeBombs;
    private List<Explosion> activeExplosions;
    private GameMap gameMap;
    private List<PlayerOb> players;
    private ItemManager itemManager; // Thêm biến ItemManager vào đây

    // Cập nhật constructor để nhận thêm tham số itemManager
    public BombManager(GameMap gameMap, List<PlayerOb> players, ItemManager itemManager) {
        this.gameMap = gameMap;
        this.players = players;
        this.itemManager = itemManager;
        this.activeBombs = new ArrayList<>();
        this.activeExplosions = new ArrayList<>();
    }

    public boolean placeBomb(int x, int y, int power, PlayerOb owner) {
        for (Bomb b : activeBombs) {
            if (b.getX() == x && b.getY() == y) {
                return false;
            }
        }

        Bomb newBomb = new Bomb(x, y, power, owner, gameMap, this);
        activeBombs.add(newBomb);
        return true;
    }

    // Khi bom nổ, BombManager sẽ đồng thời kiểm tra sát thương cho toàn bộ players luôn
    public void triggerExplosion(int bombX, int bombY, int power) {
        // Truyền thêm itemManager vào constructor của Explosion để tự động sinh item khi vỡ gạch
        Explosion explosion = new Explosion(bombX, bombY, power, gameMap, itemManager);
        activeExplosions.add(explosion);

        // Kiểm tra xem ngọn lửa có trúng player hay bot nào không
        checkExplosionDamage(explosion);
    }

    private void checkExplosionDamage(Explosion exp) {
        if (players == null) return;

        int bX = exp.getX();
        int bY = exp.getY();
        int power = exp.getPower();

        for (PlayerOb p : players) {
            if (!p.isAlive()) continue;

            int pX = (int) Math.round(p.getX());
            int pY = (int) Math.round(p.getY());

            // Kiểm tra xem nhân vật có nằm trong vùng ảnh hưởng của vụ nổ không
            if (isInExplosionArea(pX, pY, bX, bY, power)) {
                p.takeDamage(); // Trừ máu nhân vật/bot an toàn
            }
        }
    }

    private boolean isInExplosionArea(int pX, int pY, int bX, int bY, int power) {
        if (pX == bX && pY == bY) return true;

        int[][] directions = {{0, -1}, {0, 1}, {-1, 0}, {1, 0}};
        for (int[] d : directions) {
            for (int i = 1; i <= power; i++) {
                int fireX = bX + (d[0] * i);
                int fireY = bY + (d[1] * i);

                // Gặp tường thì sóng lửa dừng lại
                if (!gameMap.isWalkable(fireX, fireY) && gameMap.getTile(fireX, fireY) != GameMap.EMPTY) {
                    // Nếu là gạch thì vẫn tính là dính lửa ở ô đó trước khi gạch bị phá
                    if (gameMap.getTile(fireX, fireY) == GameMap.WALL_BRICK) {
                        if (pX == fireX && pY == fireY) return true;
                    }
                    break;
                }

                if (pX == fireX && pY == fireY) {
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