package utils;

import java.util.Random;

public class RandomMovement {
    private double x, y;
    private double speed;
    private int width, height;
    private final int TILE_SIZE = 32;
    private int[][] mapHash;

    private Direction currentDirection;
    private double moveHori = 0;
    private double moveVerti = 0;
    private int changeDirCounter = 0;
    private Random random;

    public RandomMovement(double x, double y, int width, int height, double speed, int[][] mapHash) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.speed = speed;
        this.mapHash = mapHash;
        this.random = new Random();
        this.currentDirection = getRandomDirection();
        updateVelocity(this.currentDirection);
    }

    public void update(double currentX, double currentY) {
        // Đồng bộ vị trí từ bên ngoài vào nếu có
        this.x = currentX;
        this.y = currentY;

        changeDirCounter++;
        if (changeDirCounter >= 60) { // Thời gian đổi hướng để entity đi sâu vào ô hơn
            Direction newDir = getRandomDirection();
            // Chỉ đổi hướng ngay nếu hướng mới hợp lệ hoặc ngược lại
            if (canChangeDirection(newDir)) {
                this.currentDirection = newDir;
                updateVelocity(this.currentDirection);
            }
            changeDirCounter = 0;
        }

        // Tính tọa độ dự kiến tiếp theo
        double nextX = this.x + moveHori;
        double nextY = this.y + moveVerti;

        // Kiểm tra nếu vị trí tiếp theo đi được
        if (isValidPosition(nextX, nextY)) {
            this.x = nextX;
            this.y = nextY;
        } else {
            // Va chạm tường: Snap (bám) vào vị trí tâm/lưới gần nhất hợp lệ và đổi hướng ngẫu nhiên khác
            snapToGrid();
            this.currentDirection = getRandomDirection();
            updateVelocity(this.currentDirection);
        }

        // Làm tròn số để tránh sai số dấu phẩy động (floating-point precision errors)
        this.x = Math.round(this.x * 10.0) / 10.0;
        this.y = Math.round(this.y * 10.0) / 10.0;
    }

    /**
     * Kiểm tra xem có thể đổi sang hướng mới tại vị trí hiện tại hay không (bám tâm lưới)
     */
    private boolean canChangeDirection(Direction newDir) {
        // Đổi sang hướng ngược lại thì luôn cho phép
        if (isOppositeDirection(this.currentDirection, newDir)) return true;

        // Nếu đang di chuyển ngang muốn đổi sang dọc (hoặc ngược lại),
        // ta kiểm tra xem entity có đang ở gần tâm đường kẻ lưới theo trục vuông góc không để rẽ mượt.
        double centerX = Math.round(this.x / TILE_SIZE) * TILE_SIZE;
        double centerY = Math.round(this.y / TILE_SIZE) * TILE_SIZE;

        // Sai số cho phép khi căn gióng vào tâm lưới để rẽ hướng
        double threshold = speed * 1.5;

        if (moveHori != 0 && (newDir == Direction.UP || newDir == Direction.DOWN)) {
            return Math.abs(this.y - centerY) <= threshold;
        }
        if (moveVerti != 0 && (newDir == Direction.LEFT || newDir == Direction.RIGHT)) {
            return Math.abs(this.x - centerX) <= threshold;
        }

        return false;
    }

    private boolean isOppositeDirection(Direction d1, Direction d2) {
        return (d1 == Direction.UP && d2 == Direction.DOWN) ||
                (d1 == Direction.DOWN && d2 == Direction.UP) ||
                (d1 == Direction.LEFT && d2 == Direction.RIGHT) ||
                (d1 == Direction.RIGHT && d2 == Direction.LEFT);
    }

    /**
     * Căn chỉnh (Snap) vị trí hiện tại về ranh giới/tâm tile gần nhất khi gặp vật cản
     */
    private void snapToGrid() {
        int tileX = (int) Math.round(this.x / TILE_SIZE);
        int tileY = (int) Math.round(this.y / TILE_SIZE);

        // Chỉ snap nếu vị trí snap hợp lệ
        double snapX = tileX * TILE_SIZE;
        double snapY = tileY * TILE_SIZE;
        if (isValidPosition(snapX, snapY)) {
            this.x = snapX;
            this.y = snapY;
        }
    }

    public boolean isValidPosition(double nextX, double nextY) {
        if (mapHash == null) return true;

        int leftTile = (int) Math.floor(nextX / TILE_SIZE);
        int rightTile = (int) Math.floor((nextX + width - 1) / TILE_SIZE);
        int topTile = (int) Math.floor(nextY / TILE_SIZE);
        int bottomTile = (int) Math.floor((nextY + height - 1) / TILE_SIZE);

        try {
            if (topTile < 0 || bottomTile >= mapHash.length || leftTile < 0 || rightTile >= mapHash[0].length) {
                return false;
            }
            int t1 = mapHash[topTile][leftTile];
            int t2 = mapHash[topTile][rightTile];
            int t3 = mapHash[bottomTile][leftTile];
            int t4 = mapHash[bottomTile][rightTile];

            // Ô chứa giá trị 4 hoặc khác 0 là vật cản (tùy theo logic map của bạn)
            if (t1 == 4 || t2 == 4 || t3 == 4 || t4 == 4) return false;

            return t1 == 0 && t2 == 0 && t3 == 0 && t4 == 0;
        } catch (Exception e) {
            return false;
        }
    }

    private void updateVelocity(Direction dir) {
        moveHori = 0;
        moveVerti = 0;
        switch (dir) {
            case UP -> moveVerti = -speed;
            case DOWN -> moveVerti = speed;
            case LEFT -> moveHori = -speed;
            case RIGHT -> moveHori = speed;
        }
    }

    private Direction getRandomDirection() {
        Direction[] dirs = Direction.values();
        return dirs[random.nextInt(dirs.length)];
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public Direction getCurrentDirection() { return currentDirection; }
}