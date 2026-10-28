package entity;


import entity.Mob;
import map.GameMap;
import manager.BombManager;
import utils.Direction;

import java.awt.Graphics2D;
import java.awt.Color;
import java.util.Random;

public class DollBot extends Mob {
    private Random random;
    private int changeDirCounter = 0;

    public DollBot(double x, double y, int width, int height, int speed) {
        // Gọi constructor của Mob: x, y, width, height, lives, speed, power, bombLimit
        super(x, y, width, height, 1, speed, 0, 0);
        this.random = new Random();
        this.currentDirection = getRandomDirection();
    }

    @Override
    public void update() {
        if (!survival) return;

        // Định kỳ đổi hướng ngẫu nhiên sau một khoảng thời gian
        changeDirCounter++;
        if (changeDirCounter >= 50) {
            this.currentDirection = getRandomDirection();
            changeDirCounter = 0;
        }

        // Thực hiện di chuyển với hướng hiện tại (GameMap và BombManager sẽ được truyền từ vòng lặp game hoặc quản lý chung)
        move(currentDirection, null, null);
    }

    @Override
    public void move(Direction direction, GameMap gameMap, BombManager bombManager) {
        this.currentDirection = direction;
        double nextX = x;
        double nextY = y;

        switch (direction) {
            case UP:
                nextY -= speed;
                break;
            case DOWN:
                nextY += speed;
                break;
            case LEFT:
                nextX -= speed;
                break;
            case RIGHT:
                nextX += speed;
                break;
        }

        // Kiểm tra va chạm với bản đồ bằng hàm checkCollision
        if (gameMap != null && !gameMap.checkCollision(nextX, nextY, width, height)) {
            this.x = nextX;
            this.y = nextY;
        } else {
            // Nếu đâm vào tường, lập tức đổi hướng ngẫu nhiên khác để lách qua đường khác
            this.currentDirection = getRandomDirection();
        }
    }

    private Direction getRandomDirection() {
        Direction[] dirs = Direction.values();
        return dirs[random.nextInt(dirs.length)];
    }

    @Override
    public void draw(Graphics2D g2d) {
        if (!survival && !deading) return;

        // Tạm thời vẽ hình chữ nhật màu hồng/tím đại diện cho Doll (sau này bạn có thể thay bằng Sprite Sheet)
        g2d.setColor(new Color(219, 112, 147));
        g2d.fillRect((int) x, (int) y, width, height);

        g2d.setColor(Color.BLACK);
        g2d.drawRect((int) x, (int) y, width, height);
    }
}