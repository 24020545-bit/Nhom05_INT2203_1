package entity;

import map.GameMap;
import manager.BombManager;
import utils.Direction;

public class Player extends Mod {
    private String name;

    public Player(double x, double y, double speed, int lives,
                  int bombLimit, int power, String name) {
        super(x, y, speed, lives, bombLimit, power);
        this.name = name;
    }

    @Override
    public void move(Direction direction) {}

    // [CẬP NHẬT] Thêm tham số bombManager để check va chạm với bom
    public void move(Direction direction, GameMap gameMap, BombManager bombManager) {
        if (!isAlive || direction == null || gameMap == null) {
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

            // 2. [MỚI] Kiểm tra xem ô tiếp theo có bom hay không
            if (bombManager != null) {
                var hitBomb = bombManager.getBombAt(targetTileX, targetTileY);
                if (hitBomb != null) {
                    // Nếu người chơi đang đứng NGAY TẠI ô chứa quả bom đó (vừa đặt xong), cho phép đi qua/thoát ra.
                    // Ngược lại, nếu từ ô khác bước vào ô có bom -> Chặn lại!
                    int currentTileX = (int) Math.round(this.x);
                    int currentTileY = (int) Math.round(this.y);

                    if (currentTileX != targetTileX || currentTileY != targetTileY) {
                        break; // Bị chặn lại bởi quả bom!
                    }
                }
            }

            // Nếu mọi thứ an toàn, cập nhật tọa độ tạm thời
            x = nextX;
            y = nextY;

            remainingMove -= currentStep;
        }
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}