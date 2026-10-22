public abstract class PlayerOb {
    protected int x, y;
    protected double speed;
    protected int lives, bombLimit, activeBombCount, power;
    protected boolean isAlive = true;

    public PlayerOb(int x, int y, double speed, int lives, int bombLimit, int power) {
        this.x = x;
        this.y = y;
        this.speed = speed;
        this.lives = lives;
        this.bombLimit = bombLimit;
        this.power = power;
    }

    public abstract void move(Direction direction);

    public void placeBomb(GameMap gameMap) {
        if (!isAlive || gameMap == null || activeBombCount >= bombLimit) return;

        gameMap.addBomb(new Bomb(this.x, this.y, this.power, this, gameMap));
        activeBombCount++;
    }

    public void takeDamage() {
        if (isAlive && --lives <= 0) die();
    }

    public void die() {
        this.isAlive = false;
    }

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