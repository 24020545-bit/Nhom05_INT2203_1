package entity.bot;

import entity.Player;
import entity.mob.Mob;
import utils.Direction;

import java.awt.Graphics2D;
import java.awt.Color;

public class KondoriaBot extends Mob {
    private Player targetPlayer;
    private int[][] mapHash;
    private final int TILE_SIZE = 32;
    private double moveHori = 0;
    private double moveVerti = 0;

    public KondoriaBot(double x, double y, int width, int height, int speed, Player targetPlayer, int[][] mapHash) {
        super(x, y, width, height, speed, 0, 0, 10, 1);
        this.targetPlayer = targetPlayer;
        this.mapHash = mapHash;
        this.currentDirection = Direction.LEFT;
    }

    @Override
    public void update() {
        if (!survival) return;
        moveEnemy();
    }

    private void moveEnemy() {
        if (targetPlayer == null || !targetPlayer.isSurvival()) return;

        // Tính toán tọa độ ô (tile) hiện tại của bot và player (tham khảo cách tính tâm giống file Kondoria mẫu)
        int j = (int) ((x + width / 2.0) / TILE_SIZE);
        int i = (int) ((y + height / 2.0) / TILE_SIZE);
        int jp = (int) ((targetPlayer.getX() + targetPlayer.getWidth() / 2.0) / TILE_SIZE);
        int ip = (int) ((targetPlayer.getY() + targetPlayer.getHeight() / 2.0) / TILE_SIZE);

        // Khi bot dịch chuyển đến đúng tâm ô lưới, tiến hành tính toán hướng di chuyển trực diện về phía Player
        if (Math.abs(j * TILE_SIZE - x) < speed && Math.abs(i * TILE_SIZE - y) < speed) {
            x = j * TILE_SIZE;
            y = i * TILE_SIZE;

            moveHori = 0;
            moveVerti = 0;

            if (jp == j) {
                moveHori = 0;
            } else if (jp < j) {
                moveHori = -speed;
                currentDirection = Direction.LEFT;
            } else {
                moveHori = speed;
                currentDirection = Direction.RIGHT;
            }

            if (ip == i) {
                moveVerti = 0;
            } else if (ip < i) {
                moveVerti = -speed;
                currentDirection = Direction.UP;
            } else {
                moveVerti = speed;
                currentDirection = Direction.DOWN;
            }

            // Nếu di chuyển cả ngang và dọc, ưu tiên hướng có khoảng cách lớn hơn (giống logic file mẫu)
            if (moveVerti != 0 && moveHori != 0) {
                if (Math.abs(ip - i) > Math.abs(jp - j)) {
                    moveHori = 0;
                } else {
                    moveVerti = 0;
                }
            }
        }

        double nextX = x + moveHori;
        double nextY = y + moveVerti;

        // Kiểm tra vị trí hợp lệ: Đi xuyên tường thoải mái nhưng bị chặn bởi bom (4, 5)
        if (isValidPosition(nextX, nextY)) {
            x = nextX;
            y = nextY;
        } else {
            // Nếu đụng bom, dừng hướng di chuyển đó lại để không đi xuyên qua bom
            moveHori = 0;
            moveVerti = 0;
        }

        // Làm tròn tọa độ giúp di chuyển mượt mà, tránh lệch pixel (học hỏi từ hàm lamtron trong Kondoria mẫu)
        x = lamtron(x * 10) / 10.0;
        y = lamtron(y * 10) / 10.0;
    }

    private double lamtron(double a) {
        double num = (int) (a);
        if (num + 0.5 <= a) {
            num += 1;
        }
        return num;
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

            // Nếu bất kỳ góc nào chạm vào ô bom đã khóa (4) -> Chặn lại
            if (t1 == 4 || t2 == 4 || t3 == 4 || t4 == 4) return false;

            // Còn lại (đường đi, tường xuyên qua, hay ô đang đặt bom mã 5) thì cho qua thoải mái
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void move(Direction direction) {
        this.currentDirection = direction;
    }

    @Override
    public void draw(Graphics2D g2d) {
        if (!survival) return;

        g2d.setColor(new Color(255, 140, 0));
        g2d.fillRect((int) x, (int) y, width, height);
        g2d.setColor(Color.BLACK);
        g2d.drawRect((int) x, (int) y, width, height);
    }
}