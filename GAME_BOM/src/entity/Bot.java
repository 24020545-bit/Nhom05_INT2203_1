package entity;

import utils.Direction;
import manager.BombManager;
import map.GameMap;

import java.util.Random;

public class Bot extends Mod {
    private final Random random = new Random();
    private Direction currentDirection = Direction.DOWN;
    private int changeDirCounter = 0;

    public Bot(double x, double y, double speed, int lives, int bombLimit, int power) {
        super(x, y, speed, lives, bombLimit, power);
    }

    @Override
    public void move(Direction direction) {
        // Giữ nguyên
    }

    /**
     * Hàm di chuyển mượt mà của Bot có tích hợp kiểm tra bom để Bot không tự bước vào bom của mình
     */
    public void move(Direction direction, GameMap gameMap, BombManager bombManager) {
        if (!isAlive() || direction == null || gameMap == null) return;

        double remainingMove = this.speed;
        double stepSize = 0.1;

        while (remainingMove > 0) {
            double currentStep = Math.min(remainingMove, stepSize);

            double nextX = x + direction.getDx() * currentStep;
            double nextY = y + direction.getDy() * currentStep;

            if (direction.getDx() != 0) {
                nextY = Math.round(y);
            } else if (direction.getDy() != 0) {
                nextX = Math.round(x);
            }

            double margin = 0.3;
            double checkX = nextX + direction.getDx() * margin;
            double checkY = nextY + direction.getDy() * margin;

            int targetTileX = (int) Math.round(checkX);
            int targetTileY = (int) Math.round(checkY);

            // 1. Kiểm tra tường
            if (!gameMap.isWalkable(targetTileX, targetTileY)) {
                break;
            }

            // 2. Kiểm tra bom
            if (bombManager != null) {
                var hitBomb = bombManager.getBombAt(targetTileX, targetTileY);
                if (hitBomb != null) {
                    int currentTileX = (int) Math.round(this.x);
                    int currentTileY = (int) Math.round(this.y);

                    if (currentTileX != targetTileX || currentTileY != targetTileY) {
                        break; // Bot bị chặn bởi bom
                    }
                }
            }

            x = nextX;
            y = nextY;

            remainingMove -= currentStep;
        }
    }

    /**
     * Cập nhật AI cho Bot trong mỗi nhịp game
     */
    public void update(GameMap gameMap, BombManager bombManager) {
        if (!isAlive()) return;

        int currentTileX = (int) Math.round(getX());
        int currentTileY = (int) Math.round(getY());

        // 1. Né bom khẩn cấp nếu đang đứng ở vùng nguy hiểm
        if (isDangerZone(gameMap, bombManager, currentTileX, currentTileY)) {
            escapeDanger(gameMap, bombManager);
            return;
        }

        // 2. ĐẶT BOM AN TOÀN
        if (getActiveBombCount() == 0 && random.nextInt(100) < 5) {
            if (bombManager != null) {
                Direction escapeCheck = findStrictEscapeDirection(gameMap, bombManager);
                if (escapeCheck != null) {
                    boolean success = bombManager.placeBomb(currentTileX, currentTileY, getPower(), this);
                    if (success) {
                        setActiveBombCount(getActiveBombCount() + 1);

                        // Lập tức né ngay hướng an toàn để chạy ra xa
                        currentDirection = escapeCheck;
                        move(currentDirection, gameMap, bombManager);
                        return;
                    }
                }
            }
        }

        // 3. Đổi hướng linh hoạt
        changeDirCounter++;
        if (changeDirCounter > 20 || currentDirection == null || !canContinueMoving(gameMap, bombManager)) {
            currentDirection = chooseBestDirection(gameMap, bombManager);
            changeDirCounter = 0;
        }

        // 4. Di chuyển theo hướng hiện tại
        if (currentDirection != null) {
            move(currentDirection, gameMap, bombManager);
        }
    }

    private boolean canContinueMoving(GameMap gameMap, BombManager bombManager) {
        if (currentDirection == null) return false;
        double nextX = x + currentDirection.getDx() * 0.4;
        double nextY = y + currentDirection.getDy() * 0.4;
        int tX = (int) Math.round(nextX);
        int tY = (int) Math.round(nextY);

        if (!gameMap.isWalkable(tX, tY)) return false;
        if (bombManager != null && bombManager.getBombAt(tX, tY) != null) return false;

        return true;
    }

    private Direction chooseBestDirection(GameMap gameMap, BombManager bombManager) {
        Direction[] directions = {Direction.UP, Direction.DOWN, Direction.LEFT, Direction.RIGHT};

        // [ĐÃ SỬA LỖI CÚ PHÁP] Thuật toán trộn mảng ngẫu nhiên (Fisher-Yates)
        for (int i = directions.length - 1; i > 0; i--) {
            int index = random.nextInt(i + 1);
            Direction temp = directions[index];
            directions[index] = directions[i];
            directions[i] = temp;
        }

        int currentTileX = (int) Math.round(getX());
        int currentTileY = (int) Math.round(getY());

        for (Direction d : directions) {
            int checkX = currentTileX + d.getDx();
            int checkY = currentTileY + d.getDy();
            if (gameMap.isWalkable(checkX, checkY) &&
                    (bombManager == null || bombManager.getBombAt(checkX, checkY) == null) &&
                    !isDangerZone(gameMap, bombManager, checkX, checkY)) {
                return d;
            }
        }

        for (Direction d : directions) {
            int checkX = currentTileX + d.getDx();
            int checkY = currentTileY + d.getDy();
            if (gameMap.isWalkable(checkX, checkY) &&
                    (bombManager == null || bombManager.getBombAt(checkX, checkY) == null)) {
                return d;
            }
        }

        return currentDirection;
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
        Direction[] directions = {Direction.UP, Direction.DOWN, Direction.LEFT, Direction.RIGHT};
        int currentTileX = (int) Math.round(getX());
        int currentTileY = (int) Math.round(getY());

        Direction bestDirection = null;
        int maxDistance = -1;

        for (Direction d : directions) {
            int escapeX = currentTileX + d.getDx();
            int escapeY = currentTileY + d.getDy();

            if (gameMap.isWalkable(escapeX, escapeY) &&
                    (bombManager == null || bombManager.getBombAt(escapeX, escapeY) == null) &&
                    !isDangerZone(gameMap, bombManager, escapeX, escapeY)) {
                int distanceToNearestBomb = getMinDistanceToBombs(escapeX, escapeY, bombManager);

                if (distanceToNearestBomb > maxDistance) {
                    maxDistance = distanceToNearestBomb;
                    bestDirection = d;
                }
            }
        }

        if (bestDirection != null) {
            currentDirection = bestDirection;
            move(currentDirection, gameMap, bombManager);
        } else {
            for (Direction d : directions) {
                int deepX = currentTileX + (d.getDx() * 2);
                int deepY = currentTileY + (d.getDy() * 2);
                int midX = currentTileX + d.getDx();
                int midY = currentTileY + d.getDy();

                if (gameMap.isWalkable(midX, midY) &&
                        (bombManager == null || bombManager.getBombAt(midX, midY) == null) &&
                        gameMap.isWalkable(deepX, deepY) &&
                        (bombManager == null || bombManager.getBombAt(deepX, deepY) == null) &&
                        !isDangerZone(gameMap, bombManager, deepX, deepY)) {
                    currentDirection = d;
                    move(currentDirection, gameMap, bombManager);
                    return;
                }
            }
        }
    }

    private Direction findStrictEscapeDirection(GameMap gameMap, BombManager bombManager) {
        Direction[] directions = {Direction.UP, Direction.DOWN, Direction.LEFT, Direction.RIGHT};
        int currentTileX = (int) Math.round(getX());
        int currentTileY = (int) Math.round(getY());

        Direction bestDir = null;
        int maxDist = -1;

        for (Direction d : directions) {
            int targetX = currentTileX + d.getDx();
            int targetY = currentTileY + d.getDy();

            if (gameMap.isWalkable(targetX, targetY) &&
                    (bombManager == null || bombManager.getBombAt(targetX, targetY) == null) &&
                    !isDangerZone(gameMap, bombManager, targetX, targetY)) {
                int dist = getMinDistanceToBombs(targetX, targetY, bombManager);
                if (dist > maxDist) {
                    maxDist = dist;
                    bestDir = d;
                }
            }
        }
        return bestDir;
    }

    private int getMinDistanceToBombs(int x, int y, BombManager bombManager) {
        if (bombManager == null || bombManager.getActiveBombs() == null || bombManager.getActiveBombs().isEmpty()) {
            return 999;
        }

        int minDst = 999;
        for (var bomb : bombManager.getActiveBombs()) {
            int dist = Math.abs(x - bomb.getX()) + Math.abs(y - bomb.getY());
            if (dist < minDst) {
                minDst = dist;
            }
        }
        return minDst;
    }
}