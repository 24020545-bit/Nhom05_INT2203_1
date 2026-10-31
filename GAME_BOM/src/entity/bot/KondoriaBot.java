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

    // Bán kính phát hiện Player (tính bằng số ô/tile). Ví dụ: 5 ô
    private static final int CHASE_RADIUS = 4;

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

        // Tính toán tọa độ ô (tile) hiện tại của bot và player
        int j = (int) ((x + width / 2.0) / TILE_SIZE);
        int i = (int) ((y + height / 2.0) / TILE_SIZE);
        int jp = (int) ((targetPlayer.getX() + targetPlayer.getWidth() / 2.0) / TILE_SIZE);
        int ip = (int) ((targetPlayer.getY() + targetPlayer.getHeight() / 2.0) / TILE_SIZE);

        // Kiểm tra phạm vi (CHASE_RADIUS)
        int distanceX = Math.abs(jp - j);
        int distanceY = Math.abs(ip - i);
        boolean isPlayerInRange = (distanceX <= CHASE_RADIUS && distanceY <= CHASE_RADIUS);

        if (isPlayerInRange) {
            // NẾU VỪA LOAD SAVE HOẶC VỪA VÀO TẦM (move đang bằng 0), KÍCH HOẠT HƯỚNG NGAY LẬP TỨC
            if (moveHori == 0 && moveVerti == 0) {
                if (jp != j) {
                    moveHori = (jp < j) ? -speed : speed;
                    currentDirection = (jp < j) ? Direction.LEFT : Direction.RIGHT;
                } else if (ip != i) {
                    moveVerti = (ip < i) ? -speed : speed;
                    currentDirection = (ip < i) ? Direction.UP : Direction.DOWN;
                }
            }

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

                // Nếu di chuyển cả ngang và dọc, ưu tiên hướng có khoảng cách lớn hơn
                if (moveVerti != 0 && moveHori != 0) {
                    if (Math.abs(ip - i) > Math.abs(jp - j)) {
                        moveHori = 0;
                    } else {
                        moveVerti = 0;
                    }
                }
            }
        } else {
            // Nếu người chơi ở ngoài phạm vi, bot dừng lại và reset vận tốc
            moveHori = 0;
            moveVerti = 0;
        }

        double nextX = x + moveHori;
        double nextY = y + moveVerti;

        // Kiểm tra vị trí hợp lệ: Không được đi ra ngoài biên map
        if (isValidPosition(nextX, nextY)) {
            x = nextX;
            y = nextY;
        } else {
            // Nếu đụng biên map, dừng hướng di chuyển đó lại
            moveHori = 0;
            moveVerti = 0;
        }

        // Làm tròn tọa độ giúp di chuyển mượt mà, tránh lệch pixel
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
            // Kiểm tra xem có vượt ra ngoài biên mảng map không
            if (topTile < 0 || bottomTile >= mapHash.length || leftTile < 0 || rightTile >= mapHash[0].length) {
                return false; // Chặn không cho ra khỏi map
            }

            // Kondoria đi xuyên qua mọi vật thể/tường trong map, chỉ chặn khi ra ngoài biên map
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