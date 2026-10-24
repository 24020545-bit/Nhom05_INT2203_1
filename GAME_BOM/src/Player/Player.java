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

        // Bước nhích cố định là 0.1 để không bao giờ kẹt tường
        double step = 0.1;

        double nextX = x + direction.getDx() * step;
        double nextY = y + direction.getDy() * step;

        // Khóa trục vuông góc để đi giữa đường
        if (direction.getDx() != 0) {
            nextY = Math.round(y);
        } else if (direction.getDy() != 0) {
            nextX = Math.round(x);
        }

        // Kiểm tra vùng an toàn (margin)
        double margin = 0.3;
        double checkX = nextX + direction.getDx() * margin;
        double checkY = nextY + direction.getDy() * margin;

        if (gameMap.isWalkable((int) Math.round(checkX), (int) Math.round(checkY))) {
            x = nextX;
            y = nextY;
        }
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

}