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
        this.x = currentX;
        this.y = currentY;

        changeDirCounter++;
        if (changeDirCounter >= 50) {
            this.currentDirection = getRandomDirection();
            updateVelocity(this.currentDirection);
            changeDirCounter = 0;
        }

        int j = (int) ((x + width / 2.0) / TILE_SIZE);
        int i = (int) ((y + height / 2.0) / TILE_SIZE);

        if (Math.abs(j * TILE_SIZE - x) < speed && Math.abs(i * TILE_SIZE - y) < speed) {
            this.x = j * TILE_SIZE;
            this.y = i * TILE_SIZE;

            if (!isValidPosition(this.x + moveHori, this.y + moveVerti)) {
                this.currentDirection = getRandomDirection();
                updateVelocity(this.currentDirection);
            }
        }

        double nextX = this.x + moveHori;
        double nextY = this.y + moveVerti;

        if (isValidPosition(nextX, nextY)) {
            this.x = nextX;
            this.y = nextY;
        } else {
            this.x = j * TILE_SIZE;
            this.y = i * TILE_SIZE;
            this.currentDirection = getRandomDirection();
            updateVelocity(this.currentDirection);
        }

        this.x = lamtron(this.x * 10) / 10.0;
        this.y = lamtron(this.y * 10) / 10.0;
    }

    public boolean isValidPosition(double nextX, double nextY) {
        if (mapHash == null) return true;

        int leftTile = (int) (nextX / TILE_SIZE);
        int rightTile = (int) ((nextX + width - 1) / TILE_SIZE);
        int topTile = (int) (nextY / TILE_SIZE);
        int bottomTile = (int) ((nextY + height - 1) / TILE_SIZE);

        try {
            if (topTile < 0 || bottomTile >= mapHash.length || leftTile < 0 || rightTile >= mapHash[0].length) {
                return false;
            }
            int t1 = mapHash[topTile][leftTile];
            int t2 = mapHash[topTile][rightTile];
            int t3 = mapHash[bottomTile][leftTile];
            int t4 = mapHash[bottomTile][rightTile];

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

    private double lamtron(double a) {
        double num = (int) (a);
        if (num + 0.5 <= a) {
            num += 1;
        }
        return num;
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public Direction getCurrentDirection() { return currentDirection; }
}