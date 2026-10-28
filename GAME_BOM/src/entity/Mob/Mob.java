
package entity.Mob;

import map.GameMap;
import manager.BombManager;
import utils.Direction;

import java.awt.Graphics2D;

public abstract class Mob {
    protected double x;
    protected double y;

    protected boolean survival;
    protected boolean deading;

    protected int lives;
    protected int speed;
    protected int power;
    protected int bombLimit;
    protected int activeBombCount;

    protected boolean flamepass;

    protected Mob(double x, double y, int lives,
                  int speed, int power, int bombLimit) {
        this.x = x;
        this.y = y;
        this.lives = lives;
        this.speed = speed;
        this.power = power;
        this.bombLimit = bombLimit;

        this.activeBombCount = 0;
        this.survival = true;
        this.deading = false;
        this.flamepass = false;
    }

    public abstract void draw(Graphics2D g2d);

    public abstract void move(
            Direction direction,
            GameMap gameMap,
            BombManager bombManager
    );

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

    public boolean isDeading() {
        return deading;
    }

    public boolean isSurvival() {
        return survival;
    }

    public void setSurvival(boolean survival) {
        this.survival = survival;
    }

    public boolean isFlamepass() {
        return flamepass;
    }

    public void setFlamepass(boolean flamepass) {
        this.flamepass = flamepass;
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
