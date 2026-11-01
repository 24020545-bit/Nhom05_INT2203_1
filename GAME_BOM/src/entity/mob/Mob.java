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

    protected long lastAttackTime = 0;
    protected long attackCooldown = 1000;

    // Khởi tạo cơ sở cho tất cả các đối tượng Mob với các thuộc tính vị trí, kích thước, tốc độ và máu
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

    // Cập nhật trạng thái của Mob mỗi khung hình
    public abstract void update();

    // Vẽ hình ảnh của Mob lên màn hình
    public abstract void draw(Graphics2D g2d);

    // Điều khiển hướng di chuyển của Mob
    public abstract void move(Direction direction);

    // Trừ lượng máu (HP) của Mob khi nhận sát thương và xử lý trạng thái tử vong nếu máu về 0
    public void takeDamage(int damage) {
        if (!survival) return;
        hp -= damage;
        if (hp <= 0) {
            hp = 0;
            survival = false;
        }
    }

    // Hồi máu cho Mob, đảm bảo lượng máu không vượt quá giới hạn tối đa (maxHp)
    public void heal(int amount) {
        hp += amount;
        if (hp > maxHp) {
            hp = maxHp;
        }
    }

    // Lấy giá trị máu hiện tại của Mob
    public int getHp() { return hp; }

    // Thiết lập giá trị máu mới cho Mob
    public void setHp(int hp) { this.hp = hp; }

    // Lấy giá trị máu tối đa của Mob
    public int getMaxHp() { return maxHp; }

    // Lấy chỉ số sức mạnh tấn công của Mob
    public int getAttackPower() { return attackPower; }

    // Thiết lập chỉ số sức mạnh tấn công cho Mob
    public void setAttackPower(int attackPower) { this.attackPower = attackPower; }

    // Lấy khung chữ nhật va chạm (Bounding Box) của Mob phục vụ kiểm tra va chạm
    public Rectangle getBounds() {
        return new Rectangle((int) x, (int) y, width, height);
    }

    public double getX() { return x; }
    public void setX(double x) { this.x = x; }
    public double getY() { return y; }
    public void setY(double y) { this.y = y; }
    public void setLocation(double x, double y) { this.x = x; this.y = y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public boolean isSurvival() { return survival; }
    public void setSurvival(boolean survival) { this.survival = survival; }
    public int getSpeed() { return speed; }
    public void setSpeed(int speed) { this.speed = speed; }
    public int getPower() { return power; }
    public void setPower(int power) { this.power = power; }
    public int getBombLimit() { return bombLimit; }
    public void setBombLimit(int bombLimit) { this.bombLimit = bombLimit; }
    public Direction getCurrentDirection() { return currentDirection; }
    public void setCurrentDirection(Direction currentDirection) { this.currentDirection = currentDirection; }
    public long getAttackCooldown() { return attackCooldown; }
    public void setAttackCooldown(long attackCooldown) { this.attackCooldown = attackCooldown; }
    public long getLastAttackTime() { return lastAttackTime; }
    public void setLastAttackTime(long lastAttackTime) { this.lastAttackTime = lastAttackTime; }
}