package Player;

import Map.GameMap;

import java.util.Timer;
import java.util.TimerTask;

public class Explosion {
    private int x;              // Tọa độ X trên bản đồ
    private int y;              // Tọa độ Y trên bản đồ
    private int power;          // Bán kính ngọn lửa lan rộng
    private boolean isFinished; // Trạng thái báo hiệu vụ nổ đã kết thúc
    private GameMap gameMap;    // Tham chiếu đến bản đồ

    /**
     * Constructor khởi tạo vụ nổ
     */
    public Explosion(int x, int y, int power, GameMap gameMap) {
        this.x = x;
        this.y = y;
        this.power = power;
        this.gameMap = gameMap;
        this.isFinished = false;

        // 1. Thực hiện phá tường và lan rộng lửa ngay khi bom nổ
        processExplosion();

        // 2. Hẹn giờ tự động tắt lửa sau 500ms
        startTimer();
    }

    /**
     * Logic lan rộng ngọn lửa ra 4 hướng và phá hủy tường gạch
     */
    private void processExplosion() {
        if (gameMap == null) return;

        char[][] grid = gameMap.getGrid();
        int mapWidth = gameMap.getWidth();
        int mapHeight = gameMap.getHeight();

        // 1. Xử lý phá hủy ngay tại ô tâm quả bom (nếu là gạch)
        destroyTile(grid, x, y, mapWidth, mapHeight);

        // 2. Các hướng lan: Lên, Xuống, Trái, Phải
        int[][] directions = {
                {0, -1}, // Lên
                {0, 1},  // Xuống
                {-1, 0}, // Trái
                {1, 0}   // Phải
        };

        for (int[] dir : directions) {
            int dx = dir[0];
            int dy = dir[1];

            // Lan rộng từ 1 ô đến hết bán kính power
            for (int i = 1; i <= power; i++) {
                int targetX = x + (dx * i);
                int targetY = y + (dy * i);

                // Kiểm tra nếu vượt quá biên bản đồ động thì dừng hướng này lại
                if (targetX < 0 || targetX >= mapWidth || targetY < 0 || targetY >= mapHeight) {
                    break;
                }

                char tile = grid[targetY][targetX];

                // Gặp tường đá cố định (#): Chặn hoàn toàn sóng nổ, không lan tiếp, không phá được
                if (tile == GameMap.WALL_SOLID) {
                    break;
                }

                // Gặp tường gạch (X): Phá hủy thành đường đi (EMPTY), đồng thời dừng sóng nổ hướng này
                if (tile == GameMap.WALL_BRICK) {
                    grid[targetY][targetX] = GameMap.EMPTY;
                    break;
                }

                // Nếu là ô trống (EMPTY): Lửa tiếp tục lan qua
            }
        }
    }

    /**
     * Hàm phụ trợ kiểm tra và phá hủy ô gạch tại tọa độ chỉ định
     */
    private void destroyTile(char[][] grid, int tileX, int tileY, int mapWidth, int mapHeight) {
        if (tileX >= 0 && tileX < mapWidth && tileY >= 0 && tileY < mapHeight) {
            if (grid[tileY][tileX] == GameMap.WALL_BRICK) {
                grid[tileY][tileX] = GameMap.EMPTY;
            }
        }
    }

    /**
     * Hẹn giờ tồn tại của vụ nổ (0.5 giây)
     */
    public void startTimer() {
        Timer timer = new Timer();

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                finish();
                timer.cancel();
            }
        }, 500); // 500 mili-giây
    }

    /**
     * Xử lý khi vụ nổ kết thúc
     */
    public void finish() {
        synchronized (this) {
            if (isFinished) {
                return;
            }
            this.isFinished = true;
        }

        if (gameMap != null) {
            // gameMap.removeExplosion(this);
        }
    }

    // ==========================================
    // GETTERS & SETTERS
    // ==========================================

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getPower() {
        return power;
    }

    public boolean isFinished() {
        return isFinished;
    }
}