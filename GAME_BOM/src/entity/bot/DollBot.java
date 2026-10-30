package entity.bot;

import entity.mob.Mob;
import utils.Direction;

import java.awt.Graphics2D;
import java.awt.Color;
import java.util.Random;

public class DollBot extends Mob {
    private Random random;
    private int changeDirCounter = 0;
    private int[][] mapHash; // Mảng bản đồ để check tường và bom
    private final int TILE_SIZE = 32;
    private double moveHori = 0;
    private double moveVerti = 0;

    public DollBot(double x, double y, int width, int height, int speed, int[][] mapHash) {
        super(x, y, width, height, 1, speed, 0, 3, 1);
        this.mapHash = mapHash;
        this.random = new Random();
        this.currentDirection = getRandomDirection();
        updateVelocity(this.currentDirection);
    }

    @Override
    public void update() {
        if (!survival) return;

        // Định kỳ đổi hướng ngẫu nhiên sau một khoảng thời gian
        changeDirCounter++;
        if (changeDirCounter >= 50) {
            this.currentDirection = getRandomDirection();
            updateVelocity(this.currentDirection);
            changeDirCounter = 0;
        }

        moveEnemy();
    }

    private void moveEnemy() {
        // Tính toán tọa độ ô (tile) hiện tại của bot (giống hệt KondoriaBot)
        int j = (int) ((x + width / 2.0) / TILE_SIZE);
        int i = (int) ((y + height / 2.0) / TILE_SIZE);

        // Khi bot dịch chuyển đến đúng tâm ô lưới, tiến hành căn chỉnh và check hướng đi
        if (Math.abs(j * TILE_SIZE - x) < speed && Math.abs(i * TILE_SIZE - y) < speed) {
            x = j * TILE_SIZE;
            y = i * TILE_SIZE;

            // Kiểm tra xem hướng hiện tại có đi tiếp được không, nếu vướng thì đổi hướng ngẫu nhiên khác ngay tại tâm
            if (!isValidPosition(x + moveHori, y + moveVerti)) {
                this.currentDirection = getRandomDirection();
                updateVelocity(this.currentDirection);
            }
        }

        double nextX = x + moveHori;
        double nextY = y + moveVerti;

        // Kiểm tra vị trí hợp lệ trên đường di chuyển
        if (isValidPosition(nextX, nextY)) {
            x = nextX;
            y = nextY;
        } else {
            // Nếu đụng bom/tường giữa đường, lập tức snap về tâm ô và đổi hướng ngẫu nhiên để không bị kẹt cứng
            x = j * TILE_SIZE;
            y = i * TILE_SIZE;
            this.currentDirection = getRandomDirection();
            updateVelocity(this.currentDirection);
        }

        // Làm tròn tọa độ giúp di chuyển mượt mà, tránh lệch pixel (học hỏi từ hàm lamtron trong KondoriaBot)
        x = lamtron(x * 10) / 10.0;
        y = lamtron(y * 10) / 10.0;
    }

    private void updateVelocity(Direction dir) {
        moveHori = 0;
        moveVerti = 0;
        switch (dir) {
            case UP:
                moveVerti = -speed;
                break;
            case DOWN:
                moveVerti = speed;
                break;
            case LEFT:
                moveHori = -speed;
                break;
            case RIGHT:
                moveHori = speed;
                break;
        }
    }

    private boolean isValidPosition(double nextX, double nextY) {
        if (mapHash == null) return true;

        int leftTile = (int) (nextX / TILE_SIZE);
        int rightTile = (int) ((nextX + width - 1) / TILE_SIZE);
        int topTile = (int) (nextY / TILE_SIZE);
        int bottomTile = (int) ((nextY + height - 1) / TILE_SIZE);

        try {
            int t1 = mapHash[topTile][leftTile];
            int t2 = mapHash[topTile][rightTile];
            int t3 = mapHash[bottomTile][leftTile];
            int t4 = mapHash[bottomTile][rightTile];

            // Kiểm tra va chạm với bom hoặc tường (tuỳ theo quy ước giá trị số trên map của bạn, ví dụ: 0 là đường đi, 4/5 là bom)
            if (t1 == 4 || t2 == 4 || t3 == 4 || t4 == 4) return false;

            return t1 == 0 && t2 == 0 && t3 == 0 && t4 == 0;
        } catch (Exception e) {
            return false; // Chạm biên ngoài map
        }
    }

    private double lamtron(double a) {
        double num = (int) (a);
        if (num + 0.5 <= a) {
            num += 1;
        }
        return num;
    }

    private Direction getRandomDirection() {
        Direction[] dirs = Direction.values();
        return dirs[random.nextInt(dirs.length)];
    }

    @Override
    public void move(Direction direction) {
        this.currentDirection = direction;
        updateVelocity(direction);
    }

    @Override
    public void draw(Graphics2D g2d) {
        if (!survival && !deading) return;

        g2d.setColor(new Color(219, 112, 147)); // Màu hồng DollBot
        g2d.fillRect((int) x, (int) y, width, height);
        g2d.setColor(Color.BLACK);
        g2d.drawRect((int) x, (int) y, width, height);
    }
}