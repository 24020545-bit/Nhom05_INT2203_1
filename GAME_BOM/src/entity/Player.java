package entity;

import entity.Mob.Mob;
import map.GameMap;
import manager.BombManager;
import utils.Direction;
import java.awt.Graphics2D;

public class Player extends Mob {
    private String name;

    public Player(double x, double y, double speed, int lives,
                  int bombLimit, int power, String name) {
        // Gọi constructor của lớp cha Mob: (x, y, width, height, lives, speed, power, bombLimit)
        super(x, y, 32, 32, lives, (int) speed, power, bombLimit);
        this.name = name;
        this.survival = true;
    }

    @Override
    public void update() {
        // Xử lý logic cập nhật của player nếu cần
    }

    @Override
    public void draw(Graphics2D g2d) {
        // Code vẽ player (nếu bạn vẽ trực tiếp ở đây)
    }

    @Override
    public void move(Direction direction, GameMap gameMap, BombManager bombManager) {
        if (!survival || direction == null || gameMap == null) {
            return;
        }

        double remainingMove = this.speed;
        double stepSize = 0.1;

        while (remainingMove > 0) {
            double currentStep = Math.min(remainingMove, stepSize);

            double nextX = x + direction.getDx() * currentStep;
            double nextY = y + direction.getDy() * currentStep;

            if (direction.getDx() != 0) {
                nextY = Math.round(y);
            } else if (direction.getDy() != 0) {
                nextX = Math.round(x);
            }

            double margin = 0.3;
            double checkX = nextX + direction.getDx() * margin;
            double checkY = nextY + direction.getDy() * margin;

            int targetTileX = (int) Math.round(checkX);
            int targetTileY = (int) Math.round(checkY);

            // 1. Kiểm tra bản đồ có đi được không (tường đá, tường gạch)
            if (!gameMap.isWalkable(targetTileX, targetTileY)) {
                break;
            }

            // 2. Kiểm tra xem ô tiếp theo có bom hay không
            if (bombManager != null) {
                var hitBomb = bombManager.getBombAt(targetTileX, targetTileY);
                if (hitBomb != null) {
                    int currentTileX = (int) Math.round(this.x);
                    int currentTileY = (int) Math.round(this.y);

                    if (currentTileX != targetTileX || currentTileY != targetTileY) {
                        break; // Bị chặn lại bởi quả bom!
                    }
                }
            }

            // Cập nhật tọa độ tạm thời
            x = nextX;
            y = nextY;

            remainingMove -= currentStep;
        }
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}