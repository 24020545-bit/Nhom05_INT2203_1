package Player;
import Map.GameMap;

public abstract class Bot extends PlayerOb {
    protected Direction direction;

    public Bot(int x, int y, double speed, int lives, int bombLimit, int power) {
        super(x, y, speed, lives, bombLimit, power);
        this.direction = Direction.DOWN;
    }

    public abstract void updateAI(GameMap gameMap);

    @Override
    public void move(Direction direction) {
        // Mặc định
    }

//    public void move(Direction direction, GameMap gameMap) {
//        if (!isAlive || gameMap == null) return;
//
//        int nextX = x + (int) direction.getDx();
//        int nextY = y + (int) direction.getDy();
//
//        if (gameMap.isWalkable(nextX, nextY)) {
//            x = nextX;
//            y = nextY;
//        } else {
//            // Đổi hướng ngẫu nhiên nếu gặp tường cản
//            Direction[] dirs = Direction.values();
//            this.direction = dirs[(int) (Math.random() * dirs.length)];
//        }
//    }

    @Override
    public void die() {
        super.die();
        this.x = -100;
        this.y = -100;
    }

    public Direction getDirection() { return direction; }
    public void setDirection(Direction direction) { this.direction = direction; }
}