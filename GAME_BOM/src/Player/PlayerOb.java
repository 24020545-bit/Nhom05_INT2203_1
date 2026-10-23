package Player;

import Bomb.Bomb;

import java.util.Timer;
import java.util.TimerTask;

public abstract class PlayerOb {
    protected int x;
    protected int y;
    protected double speed;
    protected int lives;
    protected int bombLimit;
    protected int activeBombCount;
    protected boolean isAlive;
    protected int power;

    // Thêm biến trạng thái kiểm tra có đang trong thời gian bất tử hay không
    protected boolean isInvulnerable = false;

    public PlayerOb(int x, int y, double speed, int lives, int bombLimit, int power) {
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

//    public void placeBomb(Map.GameMap gameMap) {
//        if (!isAlive) return;
//
//        if (activeBombCount < bombLimit) {
//            Bomb newBomb = new Bomb(this.x, this.y, this.power, this, gameMap);
//            if (gameMap != null) {
//                gameMap.addBomb(newBomb);
//            }
//            activeBombCount++;
//        }
//    }

    /**
     * Phương thức nhận sát thương sử dụng Timer để đếm thời gian bất tử đúng 1 giây
     */
    public void takeDamage() {
        if (!isAlive || isInvulnerable) {
            return; // Nếu đã chết hoặc đang bất tử thì bỏ qua
        }

        lives--;
        if (lives <= 0) {
            die();
        } else {
            // Kích hoạt trạng thái bất tử tạm thời và dùng Timer đếm đúng 1 giây (1000ms)
            isInvulnerable = true;
            Timer invulnerableTimer = new Timer();
            invulnerableTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    isInvulnerable = false; // Hết 1 giây thì tắt trạng thái bất tử
                    invulnerableTimer.cancel();
                }
            }, 1000); // Đếm chính xác 1000ms = 1 giây
        }
    }

    public void die() {
        this.isAlive = false;
    }

    // ==========================================
    // GETTERS & SETTERS
    // ==========================================

    public int getX() { return x; }
    public void setX(int x) { this.x = x; }

    public int getY() { return y; }
    public void setY(int y) { this.y = y; }

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