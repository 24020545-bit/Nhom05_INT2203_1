package utils;

import java.util.Random;

public class RandomMovement {
    private double x, y;
    private final double speed;
    private final int width, height;
    private static final int TILE_SIZE = 32;
    private final int[][] mapHash;
    private final Random random = new Random();
    private Direction currentDirection;
    private double moveHori, moveVerti;
    private int changeDirCounter;

    public RandomMovement(double x, double y, int width, int height,
                          double speed, int[][] mapHash) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.speed = speed;
        this.mapHash = mapHash;
        currentDirection = getRandomDirection();
        updateVelocity(currentDirection);
    }

    public void update(double currentX, double currentY) {
        x = currentX;
        y = currentY;
        if (++changeDirCounter >= 60) {
            Direction newDir = getRandomDirection();
            if (canChangeDirection(newDir)) {
                currentDirection = newDir;
                updateVelocity(newDir);
            }
            changeDirCounter = 0;
        }
        double nextX = x + moveHori;
        double nextY = y + moveVerti;
        if (isValidPosition(nextX, nextY)) {
            x = nextX;
            y = nextY;
        } else {
            snapToGrid();
            currentDirection = getRandomDirection();
            updateVelocity(currentDirection);
        }
        x = Math.round(x * 10.0) / 10.0;
        y = Math.round(y * 10.0) / 10.0;
    }

    private boolean canChangeDirection(Direction newDir) {
        if (isOppositeDirection(currentDirection, newDir)) return true;
        double centerX = Math.round(x / TILE_SIZE) * TILE_SIZE;
        double centerY = Math.round(y / TILE_SIZE) * TILE_SIZE;
        double threshold = speed * 1.5;
        if (moveHori != 0 && (newDir == Direction.UP || newDir == Direction.DOWN)) {
            return Math.abs(y - centerY) <= threshold;
        }
        if (moveVerti != 0 && (newDir == Direction.LEFT || newDir == Direction.RIGHT)) {
            return Math.abs(x - centerX) <= threshold;
        }
        return false;
    }

    private boolean isOppositeDirection(Direction d1, Direction d2) {
        return (d1 == Direction.UP && d2 == Direction.DOWN)
                || (d1 == Direction.DOWN && d2 == Direction.UP)
                || (d1 == Direction.LEFT && d2 == Direction.RIGHT)
                || (d1 == Direction.RIGHT && d2 == Direction.LEFT);
    }

    private void snapToGrid() {
        double snapX = Math.round(x / TILE_SIZE) * TILE_SIZE;
        double snapY = Math.round(y / TILE_SIZE) * TILE_SIZE;
        if (isValidPosition(snapX, snapY)) {
            x = snapX;
            y = snapY;
        }
    }

    public boolean isValidPosition(double nextX, double nextY) {
        if (mapHash == null) return true;
        int leftTile = (int) Math.floor(nextX / TILE_SIZE);
        int rightTile = (int) Math.floor((nextX + width - 1) / TILE_SIZE);
        int topTile = (int) Math.floor(nextY / TILE_SIZE);
        int bottomTile = (int) Math.floor((nextY + height - 1) / TILE_SIZE);
        try {
            if (topTile < 0 || bottomTile >= mapHash.length
                    || leftTile < 0 || rightTile >= mapHash[0].length) return false;
            return mapHash[topTile][leftTile] == 0
                    && mapHash[topTile][rightTile] == 0
                    && mapHash[bottomTile][leftTile] == 0
                    && mapHash[bottomTile][rightTile] == 0;
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
