package entity.Mob;

import map.GameMap;
import manager.BombManager;
import utils.Direction;

import java.awt.Graphics2D;
import java.awt.Rectangle;

public abstract class Mob {
    protected double x;
    protected double y;
    protected int width;
    protected int height;

    protected boolean survival;
    protected boolean deading;

    protected int lives;
    protected int speed;
    protected int power;
    protected int bombLimit;
    protected int activeBombCount;

    protected Direction currentDirection;

    protected Mob(double x, double y, int width, int height, int lives,
                  int speed, int power, int bombLimit) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.lives = lives;
        this.speed = speed;
        this.power = power;
        this.bombLimit = bombLimit;

        this.activeBombCount = 0;
        this.survival = true;
        this.deading = false;
        this.currentDirection = Direction.DOWN; // Hướng mặc định ban đầu
    }

    public abstract void update();

    public abstract void draw(Graphics2D g2d);

    public abstract void move(
            Direction direction,
            GameMap gameMap,
            BombManager bombManager
    );

    // Trả về vùng va chạm (Hitbox) phục vụ kiểm tra va chạm với tường, bom, lửa
    public Rectangle getBounds() {
        return new Rectangle((int) x, (int) y, width, height);
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public void setLocation(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public boolean isDeading() {
        return deading;
    }

    public boolean isSurvival() {
        return survival;
    }

    public void setSurvival(boolean survival) {
        this.survival = survival;
    }

    public Direction getCurrentDirection() {
        return currentDirection;
    }

    public void setCurrentDirection(Direction currentDirection) {
        this.currentDirection = currentDirection;
    }

    public int getLives() {
        return lives;
    }

    public void setLives(int lives) {
        this.lives = lives;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public int getPower() {
        return power;
    }

    public void setPower(int power) {
        this.power = power;
    }

    public int getBombLimit() {
        return bombLimit;
    }

    public void setBombLimit(int bombLimit) {
        this.bombLimit = bombLimit;
    }

    public int getActiveBombCount() {
        return activeBombCount;
    }

    public void setActiveBombCount(int activeBombCount) {
        this.activeBombCount = activeBombCount;
    }

    public void die() {
        survival = false;
        deading = true;
    }
}