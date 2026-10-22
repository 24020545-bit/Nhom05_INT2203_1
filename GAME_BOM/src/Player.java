public class Player extends PlayerOb {
    public Player(int x, int y, double speed, int lives, int bombLimit, int power) {
        super(x, y, speed, lives, bombLimit, power);
    }

    @Override
    public void move(Direction direction) {
        if (!isAlive) return;

        // Tự động tính toán vị trí dựa trên hướng và tốc độ
        x += (int) (direction.getDx() * speed);
        y += (int) (direction.getDy() * speed);
    }
}