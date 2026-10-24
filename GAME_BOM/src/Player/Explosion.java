package Player;

import Items.ItemManager; // Import ItemManager
import Map.GameMap;

import java.util.Timer;
import java.util.TimerTask;

public class Explosion {
    private int x;
    private int y;
    private int power;
    private boolean isFinished;
    private GameMap gameMap;
    private ItemManager itemManager; // Thêm biến ItemManager

    // Cập nhật constructor nhận thêm itemManager
    public Explosion(int x, int y, int power, GameMap gameMap, ItemManager itemManager) {
        this.x = x;
        this.y = y;
        this.power = power;
        this.gameMap = gameMap;
        this.itemManager = itemManager;
        this.isFinished = false;

        processExplosion();
        startTimer();
    }

    private void processExplosion() {
        if (gameMap == null) return;

        char[][] grid = gameMap.getGrid();
        int mapWidth = gameMap.getWidth();
        int mapHeight = gameMap.getHeight();

        // 1. Phá hủy ngay tại tâm vụ nổ (nếu là gạch)
        destroyTile(grid, x, y, mapWidth, mapHeight);

        int[][] directions = {
                {0, -1}, // Lên
                {0, 1},  // Xuống
                {-1, 0}, // Trái
                {1, 0}   // Phải
        };

        for (int[] dir : directions) {
            int dx = dir[0];
            int dy = dir[1];

            for (int i = 1; i <= power; i++) {
                int targetX = x + (dx * i);
                int targetY = y + (dy * i);

                if (targetX < 0 || targetX >= mapWidth || targetY < 0 || targetY >= mapHeight) {
                    break;
                }

                char tile = grid[targetY][targetX];

                if (tile == GameMap.WALL_SOLID) {
                    break;
                }

                // Gặp tường gạch: Phá hủy và gọi sinh ngẫu nhiên item tại đây
                if (tile == GameMap.WALL_BRICK) {
                    grid[targetY][targetX] = GameMap.EMPTY;

                    if (itemManager != null) {
                        itemManager.spawnItem(targetX, targetY); // Sinh ngẫu nhiên item
                    }
                    break;
                }
            }
        }
    }

    private void destroyTile(char[][] grid, int tileX, int tileY, int mapWidth, int mapHeight) {
        if (tileX >= 0 && tileX < mapWidth && tileY >= 0 && tileY < mapHeight) {
            if (grid[tileY][tileX] == GameMap.WALL_BRICK) {
                grid[tileY][tileX] = GameMap.EMPTY;

                if (itemManager != null) {
                    itemManager.spawnItem(tileX, tileY); // Sinh item ở tâm nếu là gạch
                }
            }
        }
    }

    public void startTimer() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                finish();
                timer.cancel();
            }
        }, 500);
    }

    public void finish() {
        synchronized (this) {
            if (isFinished) {
                return;
            }
            this.isFinished = true;
        }
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getPower() { return power; }
    public boolean isFinished() { return isFinished; }
}