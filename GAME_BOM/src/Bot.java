
import java.util.Random;

public class Bot extends PlayerOb {
    protected Direction direction;
    private final Random random = new Random();

    public Bot(int x, int y, double speed,
               int lives, int bombLimit) {
        super(x, y, speed, lives, bombLimit);
        this.direction = Direction.DOWN;
    }

    @Override
    public void move(Direction direction) {
        if (!isAlive || direction == null) {
            return;
        }

        this.direction = direction;

        x += direction.getDx();
        y += direction.getDy();
    }

    @Override
    public void die() {
        isAlive = false;
    }

    // AI đơn giản: chọn ngẫu nhiên một hướng
    public void updateAI() {
        if (!isAlive) {
            return;
        }

        Direction[] directions = Direction.values();
        Direction nextDirection =
                directions[random.nextInt(directions.length)];

        move(nextDirection);
    }
}

