
public class NormalBot extends Bot {

    public NormalBot(int x, int y) {
        super(x, y, 1.0, 1, 1);
    }

    @Override
    public void updateAI() {
        if (!isAlive) {
            return;
        }

        Direction[] directions = Direction.values();
        Direction nextDirection;

        do {
            nextDirection =
                    directions[(int) (Math.random() * directions.length)];
        } while (nextDirection == direction.getOpposite());

        move(nextDirection);
    }
}

