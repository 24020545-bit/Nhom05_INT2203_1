package Player;

public abstract class PlayerOb {
    protected double x; // Chuyển sang double
    protected double y; // Chuyển sang double
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
    // GETTERS & SETTERS (Kiểu double)
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