package entity;

import utils.Direction;
import java.awt.*;

public abstract class PlayerOb {
    protected double x;
    protected double y;
    protected double speed;
    protected int lives;
    protected int bombLimit;
    protected int activeBombCount;
    protected boolean isAlive;
    protected int power;
    protected boolean isInvulnerable = false;

    public PlayerOb(double x, double y, double speed, int lives, int bombLimit, int power) {
        this.x = x;
        this.y = y;
        this.speed = speed;
        this.lives = lives;
        this.bombLimit = bombLimit;
        this.power = power;
        this.activeBombCount = 0;
        this.isAlive = true;
    }

    public abstract void move(Direction direction);

    // ==========================================
    // HÀM VẼ CHUNG CHO NHÂN VẬT / BOT (Đã sửa lỗi nhân`)
    // ==========================================
    public void draw(Graphics2D g2d, int tileSize, Color bodyColor) {
        if (!isAlive) return;

        // 1. Tính tọa độ pixel thực tế (Đã sửa lỗi cú pháp nhân tileSize)
        double pixelX = x * tileSize;
        double pixelY = y * tileSize;

        // 2. Kích thước nhân vật (nhỏ hơn ô tile một chút)
        int charSize = tileSize - 8;

        // 3. Căn giữa ô lưới
        int renderX = (int) Math.round(pixelX + (double)(tileSize - charSize) / 2.0);
        int renderY = (int) Math.round(pixelY + (double)(tileSize - charSize) / 2.0);

        // 4. Vẽ thân đối tượng
        g2d.setColor(bodyColor);
        g2d.fillOval(renderX, renderY, charSize, charSize);

        // 5. Vẽ viền trắng xung quanh
        g2d.setColor(Color.WHITE);
        g2d.drawOval(renderX, renderY, charSize, charSize);
    }

    public void takeDamage() {
        if (!isAlive || isInvulnerable) {
            return;
        }

        lives--;
        if (lives <= 0) {
            die();
        } else {
            isInvulnerable = true;
            java.util.Timer invulnerableTimer = new java.util.Timer();
            invulnerableTimer.schedule(new java.util.TimerTask() {
                @Override
                public void run() {
                    isInvulnerable = false;
                    invulnerableTimer.cancel();
                }
            }, 1000);
        }
    }

    public void die() {
        this.isAlive = false;
    }

    // ==========================================
    // GETTERS & SETTERS
    // ==========================================
    public double getX() { return x; }
    public void setX(double x) { this.x = x; }

    public double getY() { return y; }
    public void setY(double y) { this.y = y; }

    public double getSpeed() { return speed; }
    public void setSpeed(double speed) { this.speed = speed; }

    public int getLives() { return lives; }
    public void setLives(int lives) { this.lives = lives; }

    public int getBombLimit() { return bombLimit; }
    public void setBombLimit(int bombLimit) { this.bombLimit = bombLimit; }

    public int getActiveBombCount() { return activeBombCount; }
    public void setActiveBombCount(int activeBombCount) { this.activeBombCount = activeBombCount; }

    public boolean isAlive() { return isAlive; }
    public void setAlive(boolean alive) { isAlive = alive; }

    public int getPower() { return power; }
    public void setPower(int power) { this.power = power; }
}