package Player;

import Bomb.BombManager;
import Map.GameMap;

import java.util.Random;

public class Bot extends PlayerOb {
    private final Random random = new Random();
    private int moveCooldown = 0;

    public Bot(double x, double y, double speed, int lives, int bombLimit, int power) {
        super(x, y, speed, lives, bombLimit, power);
    }

    @Override
    public void move(Direction direction) {
        // Cài đặt logic di chuyển theo Direction enum nếu cần
        // Hoặc xử lý trực tiếp thay đổi x, y dựa trên speed
    }

    /**
     * Hàm tự động cập nhật AI cho Bot trong mỗi nhịp game
     */
    public void update(GameMap gameMap, BombManager bombManager) {
        if (!isAlive()) return;

        // 1. Né bom nếu đang ở vùng nguy hiểm
        if (isDangerZone(gameMap, bombManager, (int) getX(), (int) getY())) {
            escapeDanger(gameMap, bombManager);
            return;
        }

        // 2. Cooldown di chuyển
        if (moveCooldown > 0) {
            moveCooldown--;
            return;
        }
        moveCooldown = 8;

        // 3. Đặt bom ngẫu nhiên (15% xác suất)
        int currentX = (int) getX();
        int currentY = (int) getY();
        if (getActiveBombCount() < getBombLimit() && random.nextInt(100) < 15) {
            if (bombManager != null) {
                boolean success = bombManager.placeBomb(currentX, currentY, getPower(), this);
                if (success) {
                    setActiveBombCount(getActiveBombCount() + 1);
                }
            }
        }

        // 4. Di chuyển ngẫu nhiên trên lưới (dùng int cho tọa độ ô bản đồ)
        int[][] directions = {
                {0, -1}, {0, 1}, {-1, 0}, {1, 0}
        };

        int dirIndex = random.nextInt(directions.length);
        int nextX = currentX + directions[dirIndex][0];
        int nextY = currentY + directions[dirIndex][1];

        if (isValidMove(gameMap, nextX, nextY)) {
            setX(nextX);
            setY(nextY);
        }
    }

    private boolean isValidMove(GameMap gameMap, int targetX, int targetY) {
        if (gameMap == null) return false;
        int mapWidth = gameMap.getWidth();
        int mapHeight = gameMap.getHeight();

        // Kiểm tra biên động theo gameMap thay vì GameMap.SIZE cứng
        if (targetX < 0 || targetX >= mapWidth || targetY < 0 || targetY >= mapHeight) {
            return false;
        }
        char[][] grid = gameMap.getGrid();
        return grid[targetY][targetX] == GameMap.EMPTY;
    }

    private boolean isDangerZone(GameMap gameMap, BombManager bombManager, int checkX, int checkY) {
        if (gameMap == null || bombManager == null || bombManager.getActiveBombs() == null) return false;

        int mapWidth = gameMap.getWidth();
        int mapHeight = gameMap.getHeight();
        char[][] grid = gameMap.getGrid();

        for (var bomb : bombManager.getActiveBombs()) {
            int bX = bomb.getX();
            int bY = bomb.getY();
            int power = bomb.getPower();

            if (checkX == bX && checkY == bY) return true;

            int[][] dirs = {{0, -1}, {0, 1}, {-1, 0}, {1, 0}};
            for (int[] d : dirs) {
                for (int i = 1; i <= power; i++) {
                    int fireX = bX + (d[0] * i);
                    int fireY = bY + (d[1] * i);

                    // Kiểm tra biên động
                    if (fireX < 0 || fireX >= mapWidth || fireY < 0 || fireY >= mapHeight) break;

                    char tile = grid[fireY][fireX];
                    if (tile == GameMap.WALL_SOLID || tile == GameMap.WALL_BRICK) {
                        break;
                    }

                    if (checkX == fireX && checkY == fireY) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void escapeDanger(GameMap gameMap, BombManager bombManager) {
        int[][] directions = {{0, -1}, {0, 1}, {-1, 0}, {1, 0}};
        int currentX = (int) getX();
        int currentY = (int) getY();

        for (int[] d : directions) {
            int escapeX = currentX + d[0];
            int escapeY = currentY + d[1];

            if (isValidMove(gameMap, escapeX, escapeY) && !isDangerZone(gameMap, bombManager, escapeX, escapeY)) {
                setX(escapeX);
                setY(escapeY);
                break;
            }
        }
    }
}