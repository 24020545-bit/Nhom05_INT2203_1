package bomb;

import manager.BombManager;
import manager.ItemManager;
import map.GameMap;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class Explosion {
    private int x;
    private int y;
    private int power;
    private boolean isFinished;
    private GameMap gameMap;
    private ItemManager itemManager;
    private BombManager bombManager;

    // [MỚI] Lưu lại danh sách tọa độ các ô có ngọn lửa để vẽ chính xác tuyệt đối
    private List<Point> flameTiles;

    public Explosion(int x, int y, int power, GameMap gameMap, ItemManager itemManager, BombManager bombManager) {
        this.x = x;
        this.y = y;
        this.power = power;
        this.gameMap = gameMap;
        this.itemManager = itemManager;
        this.bombManager = bombManager;
        this.isFinished = false;
        this.flameTiles = new ArrayList<>();

        processExplosion();
        startTimer();
    }

    private void processExplosion() {
        if (gameMap == null) return;

        char[][] grid = gameMap.getGrid();
        int mapWidth = gameMap.getWidth();
        int mapHeight = gameMap.getHeight();

        // 1. Xử lý và thêm tâm vụ nổ vào danh sách vẽ lửa
        flameTiles.add(new Point(x, y));
        checkTileContent(x, y);

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

                // Gặp tường đá cố định: Dừng lại hoàn toàn (không thêm lửa vào ô tường đá)
                if (tile == GameMap.WALL_SOLID) {
                    break;
                }

                // [MỚI] Thêm ô này vào danh sách hiển thị lửa trước khi phá hủy/tác động
                flameTiles.add(new Point(targetX, targetY));

                // Kiểm tra nội dung ô (Bom, Item, Tường gạch)
                boolean blocked = checkTileContent(targetX, targetY);
                if (blocked) {
                    break; // Gặp vật cản làm dừng sóng lửa lan tiếp
                }
            }
        }
    }

    // Kiểm tra và xử lý nội dung tại ô lửa lan tới
    private boolean checkTileContent(int targetX, int targetY) {
        char[][] grid = gameMap.getGrid();

        // 1. Kiểm tra xem có quả bom nào ở đây không -> Kích nổ dây chuyền
        if (bombManager != null) {
            Bomb hitBomb = bombManager.getBombAt(targetX, targetY);
            if (hitBomb != null) {
                hitBomb.forceExplode();
                return true; // Chặn sóng lửa sau khi kích nổ bom
            }
        }

        // 2. Kiểm tra xem có Item ở đây không -> Xóa item
        if (itemManager != null) {
            itemManager.removeItem(targetX, targetY);
        }

        // 3. Kiểm tra tường gạch -> Phá hủy gạch và sinh item
        if (grid[targetY][targetX] == GameMap.WALL_BRICK) {
            grid[targetY][targetX] = GameMap.EMPTY;

            if (itemManager != null) {
                itemManager.spawnItem(targetX, targetY);
            }
            return true; // Chặn sóng lửa lại
        }

        return false;
    }

    /**
     * Hàm tự vẽ vụ nổ dựa trên danh sách flameTiles đã tính toán sẵn -> Khắc phục hoàn toàn lỗi vẽ tràn lan
     */
    public void draw(Graphics2D g2d, int tileSize) {
        if (isFinished || gameMap == null) return;

        for (Point p : flameTiles) {
            drawFlameTile(g2d, p.x, p.y, tileSize);
        }
    }

    private void drawFlameTile(Graphics2D g2d, int tileX, int tileY, int tileSize) {
        int drawX = tileX * tileSize;
        int drawY = tileY * tileSize;

        // Vỏ ngoài ngọn lửa màu cam sáng
        g2d.setColor(new Color(249, 115, 22));
        g2d.fillRect(drawX + 2, drawY + 2, tileSize - 4, tileSize - 4);

        // Lõi lửa bên trong màu vàng
        g2d.setColor(new Color(252, 211, 77));
        g2d.fillOval(drawX + tileSize / 4, drawY + tileSize / 4, tileSize / 2, tileSize / 2);
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