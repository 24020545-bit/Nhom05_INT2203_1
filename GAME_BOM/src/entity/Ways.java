package entity;


public enum Ways {
    UP,
    DOWN,
    LEFT,
    RIGHT,
    HERE;

    public int getDx() {
        switch (this) {
            case LEFT:
                return -1;
            case RIGHT:
                return 1;
            default:
                return 0;
        }
    }

    public int getDy() {
        switch (this) {
            case UP:
                return -1;
            case DOWN:
                return 1;
            default:
                return 0;
        }
    }
}

