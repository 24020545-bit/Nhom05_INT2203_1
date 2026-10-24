package Player;

import Map.GameMap;

public class Player extends PlayerOb {
    private String name;

    public Player(double x, double y, double speed, int lives,
                  int bombLimit, int power, String name) {
        super(x, y, speed, lives, bombLimit, power);
        this.name = name;
    }

    @Override
    public void move(Direction direction) {}

    public void move(Direction direction, GameMap gameMap) {
        if (!isAlive || direction == null || gameMap == null) {
            return;
        }

        // --- SỬA Ở ĐÂY: Dùng trực tiếp 'speed' hiện tại của player thay vì số cứng 0.1 ---
        // Nếu speed ban đầu là 1.0, ta có thể nhân thêm một hệ số nhỏ (ví dụ 0.1) để giữ độ mịn,
        // hoặc nếu bạn muốn speed chuẩn theo tốc độ nhân vật, hãy chia nhỏ quãng đường speed ra.
        double remainingMove = this.speed; // Lấy tốc độ linh hoạt (đã bao gồm hiệu ứng item SpeedUp)
        double stepSize = 0.1;             // Kích thước mỗi bước nhỏ để check va chạm mượt

        while (remainingMove > 0) {
            double currentStep = Math.min(remainingMove, stepSize);

            double nextX = x + direction.getDx() * currentStep;
            double nextY = y + direction.getDy() * currentStep;

            // Khóa trục vuông góc để căn giữa đường đi
            if (direction.getDx() != 0) {
                nextY = Math.round(y);
            } else if (direction.getDy() != 0) {
                nextX = Math.round(x);
            }

            // Kiểm tra vùng an toàn (margin) với các ô xung quanh
            double margin = 0.3;
            double checkX = nextX + direction.getDx() * margin;
            double checkY = nextY + direction.getDy() * margin;

            // Nếu đi được thì cập nhật tọa độ tạm thời trong vòng lặp
            if (gameMap.isWalkable((int) Math.round(checkX), (int) Math.round(checkY))) {
                x = nextX;
                y = nextY;
            } else {
                // Đụng tường thì dừng hẳn hướng đó luôn
                break;
            }

            remainingMove -= currentStep;
        }
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}