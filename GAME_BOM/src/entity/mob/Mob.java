package entity.mob;

import utils.Direction;

import java.awt.Color;
import java.awt.Font;
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

    protected long lastAttackTime = 0;
    protected long attackCooldown = 1000;

    protected Mob(double x, double y, int width, int height,
                  int speed, int power, int bombLimit, int hp, int attackPower) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.speed = speed;
        this.power = power;
        this.bombLimit = bombLimit;

        this.hp = Math.max(0, hp);
        this.maxHp = Math.max(0, hp);
        this.attackPower = attackPower;

        this.activeBombCount = 0;
        this.survival = hp > 0;
        this.deading = false;
        this.currentDirection = Direction.DOWN;
    }

    public abstract void update();

    public abstract void draw(Graphics2D g2d);

    public abstract void move(Direction direction);

    /**
     * Hàm vẽ thanh máu chung cho mọi Mob (Player, Bot, v.v.) ở class cha.
     * @param g2d Đối tượng đồ họa Graphics2D
     * @param barWidth Độ dài (chiều ngang) của thanh máu
     * @param barHeight Độ rộng (chiều cao) của thanh máu
     */
    protected void drawHpBar(Graphics2D g2d, int barWidth, int barHeight) {
        // Đặt vị trí thanh máu nằm ngay phía trên đầu của Mob
        int barX = (int) x + (width - barWidth) / 2; // Canh giữa theo chiều ngang của mob
        int barY = (int) y - barHeight - 4;          // Nằm cách đỉnh mob lên trên một khoảng

        // Vẽ màu nền (máu đã mất / nền tối)
        g2d.setColor(new Color(100, 0, 0));
        g2d.fillRect(barX, barY, barWidth, barHeight);

        int currentMaxHp = getMaxHp();
        if (currentMaxHp <= 0) currentMaxHp = 1;

        // Tính toán phần trăm máu hiện tại
        double hpPercent = (double) Math.max(0, hp) / currentMaxHp;
        int currentHpWidth = (int) (barWidth * hpPercent);

        // Vẽ phần máu hiện tại (màu xanh hoặc đỏ tùy ý)
        g2d.setColor(Color.GREEN);
        g2d.fillRect(barX, barY, currentHpWidth, barHeight);

        // Viền đen bao quanh thanh máu
        g2d.setColor(Color.BLACK);
        g2d.drawRect(barX, barY, barWidth, barHeight);
    }

    // Nhận sát thương
    public void takeDamage(int damage) {
        if (!survival || damage <= 0) return;

        hp = Math.max(0, hp - damage);

        if (hp == 0) {
            survival = false;
            deading = true;
        }
    }

    // Hồi máu
    public void heal(int amount) {
        if (!survival || amount <= 0) return;
        hp = Math.min(maxHp, hp + amount);
    }

    public int getHp() {
        return hp;
    }

    public void setHp(int hp) {
        this.hp = Math.max(0, Math.min(maxHp, hp));

        if (this.hp == 0) {
            survival = false;
            deading = true;
        }
    }

    public int getMaxHp() {
        return maxHp;
    }

    public int getAttackPower() {
        return attackPower;
    }

    public void setAttackPower(int attackPower) {
        this.attackPower = attackPower;
    }

    public Rectangle getBounds() {
        return new Rectangle((int) x, (int) y, width, height);
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public void setLocation(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public boolean isSurvival() {
        return survival;
    }

    public void setSurvival(boolean survival) {
        this.survival = survival;
        if (survival && hp <= 0) {
            hp = 1;
        }
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

    public Direction getCurrentDirection() {
        return currentDirection;
    }

    public void setCurrentDirection(Direction currentDirection) {
        this.currentDirection = currentDirection;
    }

    public long getAttackCooldown() {
        return attackCooldown;
    }

    public void setAttackCooldown(long attackCooldown) {
        this.attackCooldown = attackCooldown;
    }

    public long getLastAttackTime() {
        return lastAttackTime;
    }

    public void setLastAttackTime(long lastAttackTime) {
        this.lastAttackTime = lastAttackTime;
    }
}