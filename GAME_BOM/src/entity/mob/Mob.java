package entity.mob;

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

    protected int speed;
    protected int power;
    protected int bombLimit;
    protected int activeBombCount;

    protected int hp;
    protected int maxHp;
    protected int attackPower;

    protected Direction currentDirection;

    // Đã loại bỏ 'lives' khỏi tham số constructor
    protected Mob(double x, double y, int width, int height,
                  int speed, int power, int bombLimit, int hp, int attackPower) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.speed = speed;
        this.power = power;
        this.bombLimit = bombLimit;

        this.hp = hp;
        this.maxHp = hp;
        this.attackPower = attackPower;

        this.activeBombCount = 0;
        this.survival = true;
        this.deading = false;
        this.currentDirection = Direction.DOWN;
    }

    public abstract void update();

    public abstract void draw(Graphics2D g2d);

    public abstract void move(Direction direction);

    public void takeDamage(int damage) {
        if (!survival) return;
        hp -= damage;
        if (hp <= 0) {
            hp = 0;
            survival = false; // Hết máu thì chết
        }
    }

    public void heal(int amount) {
        hp += amount;
        if (hp > maxHp) {
            hp = maxHp;
        }
    }

    public int getHp() { return hp; }
    public void setHp(int hp) { this.hp = hp; }
    public int getMaxHp() { return maxHp; }
    public int getAttackPower() { return attackPower; }
    public void setAttackPower(int attackPower) { this.attackPower = attackPower; }
    public Rectangle getBounds() {
        return new Rectangle((int) x, (int) y, width, height);
    }
    public double getX() { return x; }
    public void setX(double x) { this.x = x; } // <-- Thêm hàm này
    public double getY() { return y; }
    public void setY(double y) { this.y = y; } // <-- Thêm hàm này
    public void setLocation(double x, double y) { this.x = x; this.y = y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public boolean isSurvival() { return survival; }
    public void setSurvival(boolean survival) { this.survival = survival; }
    public int getSpeed() { return speed; }
    public void setSpeed(int speed) { this.speed = speed; }
    public int getPower() { return power; }
    public void setPower(int power) { this.power = power; }
    public int getBombLimit() {
        return bombLimit;
    }
    public void setBombLimit(int bombLimit) {
        this.bombLimit = bombLimit;
    }
    public Direction getCurrentDirection() { return currentDirection; }
    public void setCurrentDirection(Direction currentDirection) { this.currentDirection = currentDirection; }
}