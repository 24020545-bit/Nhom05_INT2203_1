package utils;

import bomb.Bomb;
import java.util.Random;

public class RandomMovement {
    private double x, y;
    private double speed;
    private int width, height;
    private final int TILE_SIZE = 32;
    private int[][] mapHash;

    private Direction currentDirection;
    private int targetCol = -1;
    private int targetRow = -1;
    private boolean moving = false;
    private final Random random;

    private static final double EPSILON = 0.01;

    public RandomMovement(double x, double y, int width, int height, double speed, int[][] mapHash) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.speed = speed;
        this.mapHash = mapHash;
        this.random = new Random();
        this.currentDirection = Direction.LEFT;
    }

    // ==================== GRID & POSITION ====================
    private double getCellX(int col) {
        return col * TILE_SIZE + (TILE_SIZE - width) / 2.0;
    }

    private double getCellY(int row) {
        return row * TILE_SIZE + (TILE_SIZE - height) / 2.0;
    }

    private int getCurrentCol() {
        return (int) Math.round((x + width / 2.0) / TILE_SIZE - 0.5);
    }

    private int getCurrentRow() {
        return (int) Math.round((y + height / 2.0) / TILE_SIZE - 0.5);
    }

    // ==================== FLOOR CHECK ====================
    private boolean isWalkableFloorValue(int val) {
        return val == Bomb.TILE_EMPTY || (val >= 5 && val <= 9);
    }

    private boolean canMoveOnFloor(double nextX, double nextY) {
        if (!isValidMap()) return false;
        int left = (int) Math.floor(nextX / TILE_SIZE);
        int right = (int) Math.floor((nextX + width - EPSILON) / TILE_SIZE);
        int top = (int) Math.floor(nextY / TILE_SIZE);
        int bottom = (int) Math.floor((nextY + height - EPSILON) / TILE_SIZE);
        int maxRow = mapHash.length - 1;
        int maxCol = mapHash[0].length - 1;

        if (left < 1 || top < 1 || right >= maxCol || bottom >= maxRow) {
            return false;
        }

        for (int row = top; row <= bottom; row++) {
            for (int col = left; col <= right; col++) {
                if (!isWalkableFloorValue(mapHash[row][col])) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isValidMap() {
        return mapHash != null
                && mapHash.length > 0
                && mapHash[0] != null
                && mapHash[0].length > 0;
    }

    // ==================== UPDATE (Học theo KondoriaBot) ====================
    public void update(double currentX, double currentY) {
        this.x = currentX;
        this.y = currentY;

        // Nếu đang di chuyển về một ô đích, tiếp tục dịch chuyển tới đó
        if (moving) {
            boolean finished = moveToTargetCell();
            if (finished) {
                moving = false;
            }
            return;
        }

        // Nếu đã đến đích hoặc đứng yên, chọn ngẫu nhiên một hướng đi tiếp theo hợp lệ trên sàn
        randomMove();
    }

    private void randomMove() {
        Direction[] directions = {
                Direction.UP,
                Direction.DOWN,
                Direction.LEFT,
                Direction.RIGHT
        };

        // Xáo trộn mảng hướng để chọn ngẫu nhiên
        for (int i = directions.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            Direction temp = directions[i];
            directions[i] = directions[j];
            directions[j] = temp;
        }

        for (Direction direction : directions) {
            int col = getCurrentCol();
            int row = getCurrentRow();
            switch (direction) {
                case LEFT -> col--;
                case RIGHT -> col++;
                case UP -> row--;
                case DOWN -> row++;
                default -> { continue; }
            }

            double nextX = getCellX(col);
            double nextY = getCellY(row);

            // Kiểm tra xem ô tiếp theo có đi được không
            if (canMoveOnFloor(nextX, nextY)) {
                currentDirection = direction;
                setTargetCell(col, row);
                moveToTargetCell();
                return;
            }
        }
    }

    private void setTargetCell(int col, int row) {
        targetCol = col;
        targetRow = row;
        moving = true;
    }

    private boolean moveToTargetCell() {
        if (targetCol < 0 || targetRow < 0) return true;
        double targetX = getCellX(targetCol);
        double targetY = getCellY(targetRow);

        // Dịch chuyển theo trục X trước
        if (Math.abs(targetX - x) > EPSILON) {
            double dx = targetX - x;
            double step = Math.signum(dx) * Math.min(speed, Math.abs(dx));
            double nextX = x + step;
            if (!canMoveOnFloor(nextX, y)) {
                moving = false;
                return true;
            }
            x = nextX;
            currentDirection = dx < 0 ? Direction.LEFT : Direction.RIGHT;
            return false;
        }

        // Dịch chuyển theo trục Y sau
        if (Math.abs(targetY - y) > EPSILON) {
            double dy = targetY - y;
            double step = Math.signum(dy) * Math.min(speed, Math.abs(dy));
            double nextY = y + step;
            if (!canMoveOnFloor(x, nextY)) {
                moving = false;
                return true;
            }
            y = nextY;
            currentDirection = dy < 0 ? Direction.UP : Direction.DOWN;
            return false;
        }

        // Đã đến đúng tâm ô
        x = targetX;
        y = targetY;
        moving = false;
        return true;
    }

    // ==================== GETTERS ====================
    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public Direction getCurrentDirection() {
        return currentDirection;
    }
}