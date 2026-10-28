package entity;


import map.GameMap;
import manager.BombManager;
import utils.Direction;

import java.awt.Graphics2D;
import java.awt.Color;

public class KondoriaBot extends Mob {
    private Player targetPlayer; // Người chơi để đuổi theo
    private int updateTargetCounter = 0;

    public KondoriaBot(double x, double y, int width, int height, int speed, Player targetPlayer) {
        // Gọi constructor của Mob (lives = 1, power = 0, bombLimit = 0)
        super(x, y, width, height, 1, speed, 0, 0);
        this.targetPlayer = targetPlayer;
        this.currentDirection = Direction.LEFT;
    }

    @Override
    public void update() {
        if (!survival) return;

        // Cứ mỗi 30 frame, tính toán lại hướng đuổi theo Player
        updateTargetCounter++;
        if (updateTargetCounter >= 30 && targetPlayer != null && targetPlayer.isSurvival()) {
            calculateChasingDirection();
            updateTargetCounter = 0;
        }

        // Thực hiện di chuyển theo hướng hiện tại
        move(currentDirection, null, null);
    }

    private void calculateChasingDirection() {
        double diffX = targetPlayer.getX() - this.x;
        double diffY = targetPlayer.getY() - this.y;

        if (Math.abs(diffX) > Math.abs(diffY)) {
            currentDirection = (diffX > 0) ? Direction.RIGHT : Direction.LEFT;
        } else {
            currentDirection = (diffY > 0) ? Direction.DOWN : Direction.UP;
        }
    }

    @Override
    public void move(Direction direction, GameMap gameMap, BombManager bombManager) {
        this.currentDirection = direction;
        double nextX = x;
        double nextY = y;

        switch (direction) {
            case UP: nextY -= speed; break;
            case DOWN: nextY += speed; break;
            case LEFT: nextX -= speed; break;
            case RIGHT: nextX += speed; break;
        }

        // Kiểm tra va chạm với bản đồ
        if (gameMap != null && !gameMap.checkCollision(nextX, nextY, width, height)) {
            this.x = nextX;
            this.y = nextY;
        }
    }

    @Override
    public void draw(Graphics2D g2d) {
        if (!survival && !deading) return;

        g2d.setColor(new Color(255, 140, 0)); // Màu cam cho Kondoria
        g2d.fillRect((int) x, (int) y, width, height);
        g2d.setColor(Color.BLACK);
        g2d.drawRect((int) x, (int) y, width, height);
    }
}