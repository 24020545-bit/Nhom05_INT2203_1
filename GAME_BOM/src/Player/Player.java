package Player;

import Map.GameMap;

public class Player extends PlayerOb {
    private String name;

    public Player(int x, int y, double speed, int lives, int bombLimit, int power, String name) {
        super(x, y, speed, lives, bombLimit, power);
        this.name = name;
    }

    @Override
    public void move(Direction direction) {
        // Phương thức này có thể nhận thêm tham số Map.GameMap nếu gọi từ GameWindow hoặc quản lý qua Map.GameMap
    }

    /**
     * Phương thức di chuyển có kiểm tra bản đồ để không đi xuyên tường
     */
//    public void move(Direction direction, GameMap gameMap) {
//        if (!isAlive || gameMap == null) return;
//
//        int nextX = x + (int) direction.getDx();
//        int nextY = y + (int) direction.getDy();
//
//        if (gameMap.isWalkable(nextX, nextY)) {
//            x = nextX;
//            y = nextY;
//        }
//    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}