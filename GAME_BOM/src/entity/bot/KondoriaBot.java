package entity.bot;

import entity.Player;
import entity.mob.Mob;
import utils.Direction;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.util.Random;
import javax.swing.ImageIcon;

/**
 * Kondoria: xuyên được cả gạch mềm lẫn tường cứng,
 * chỉ bị giới hạn bởi viền ngoài bản đồ.
 */
public class KondoriaBot extends Mob {

    private static final int TILE_SIZE = 32;
    private static final int CHASE_RADIUS = 3;
    private static final double EPSILON = 0.001;

    private final Player[] players;
    private final int[][] mapHash;
    private Player currentTarget;

    // Wander (đi lang thang khi không thấy player)
    private final Random rnd = new Random();
    private Direction wanderDir = Direction.LEFT;
    private int wanderTicks = 0;

    // Hình ảnh + animation
    private Image leftImg, rightImg;
    private final Image[] deadImgs = new Image[2];
    private int animIndex = 0;
    private int animTimer = 0;

    public KondoriaBot(double x, double y, int width, int height,
                       int speed, int hp, int attackPower,
                       long attackCooldown, Player[] players,
                       int[][] mapHash) {

        super(x, y, width, height, speed, 0, 0, hp, attackPower);

        this.players = players;
        this.mapHash = mapHash;
        this.attackCooldown = attackCooldown;
        this.currentDirection = Direction.LEFT;

        loadImages();
    }

    // ===================== Ảnh =====================

    private void loadImages() {
        String path = "/Bot/KondoriaBot/";

        leftImg = loadImage(path + "left.png");
        rightImg = loadImage(path + "right.png");
        deadImgs[0] = loadImage(path + "dead1.png");
        deadImgs[1] = loadImage(path + "dead2.png");
    }

    private Image loadImage(String path) {
        var url = getClass().getResource(path);
        if (url == null) {
            System.err.println("Không tìm thấy ảnh KondoriaBot: " + path);
            return null;
        }
        return new ImageIcon(url).getImage();
    }

    // ===================== Update =====================

    @Override
    public void update() {
        if (!survival) return;

        attackPlayer();
        findClosestTarget();

        if (currentTarget != null) {
            chasePlayer();
        } else {
            randomMove();
        }

        clampToBounds();
        updateAnimation();
    }

    private void updateAnimation() {
        if (++animTimer > 10) {
            animIndex = (animIndex + 1) % 2;
            animTimer = 0;
        }
    }

    // ===================== Tấn công =====================

    // Tấn công Player khi va chạm
    private void attackPlayer() {
        if (players == null) return;

        long now = System.currentTimeMillis();

        for (Player player : players) {
            if (player != null && player.isSurvival()
                    && getBounds().intersects(player.getBounds())
                    && now - lastAttackTime >= attackCooldown) {

                player.takeDamage(attackPower);
                lastAttackTime = now;
                break;
            }
        }
    }

    // ===================== Tìm mục tiêu =====================

    // Tìm Player gần nhất trong phạm vi phát hiện
    private void findClosestTarget() {
        currentTarget = null;
        if (players == null) return;

        double minDistance = Double.MAX_VALUE;

        int botX = (int) ((x + width / 2.0) / TILE_SIZE);
        int botY = (int) ((y + height / 2.0) / TILE_SIZE);

        for (Player player : players) {
            if (player == null || !player.isSurvival()) continue;

            int px = (int) ((player.getX() + player.getWidth() / 2.0) / TILE_SIZE);
            int py = (int) ((player.getY() + player.getHeight() / 2.0) / TILE_SIZE);

            int dx = px - botX;
            int dy = py - botY;

            if (Math.abs(dx) <= CHASE_RADIUS && Math.abs(dy) <= CHASE_RADIUS) {
                double distance = dx * dx + dy * dy;

                if (distance < minDistance) {
                    minDistance = distance;
                    currentTarget = player;
                }
            }
        }
    }

    // ===================== Di chuyển =====================

    // Đuổi theo Player (ưu tiên trục có khoảng cách lớn hơn)
    private void chasePlayer() {
        double dx = currentTarget.getX() + currentTarget.getWidth() / 2.0
                - (x + width / 2.0);
        double dy = currentTarget.getY() + currentTarget.getHeight() / 2.0
                - (y + height / 2.0);

        if (Math.abs(dx) >= Math.abs(dy)) {
            if (!moveHorizontal(dx)) {
                moveVertical(dy);
            }
        } else {
            if (!moveVertical(dy)) {
                moveHorizontal(dx);
            }
        }
    }

    // Đi lang thang: chọn hướng ngẫu nhiên, đi một lúc rồi đổi
    private void randomMove() {
        if (--wanderTicks <= 0) {
            Direction[] dirs = {
                    Direction.LEFT, Direction.RIGHT,
                    Direction.UP, Direction.DOWN
            };
            wanderDir = dirs[rnd.nextInt(dirs.length)];
            wanderTicks = 30 + rnd.nextInt(60);
        }

        boolean moved = false;

        switch (wanderDir) {
            case LEFT:
                moved = moveHorizontal(-speed);
                break;
            case RIGHT:
                moved = moveHorizontal(speed);
                break;
            case UP:
                moved = moveVertical(-speed);
                break;
            case DOWN:
                moved = moveVertical(speed);
                break;
            default:
                break;
        }

        // Chạm viền bản đồ -> đổi hướng ngay ở tick sau
        if (!moved) wanderTicks = 0;
    }

    // Di chuyển ngang
    private boolean moveHorizontal(double dx) {
        if (Math.abs(dx) < EPSILON) return false;

        double step = Math.signum(dx) * Math.min(speed, Math.abs(dx));
        double nextX = x + step;

        if (!canMove(nextX, y)) return false;

        x = nextX;
        currentDirection = dx < 0 ? Direction.LEFT : Direction.RIGHT;
        return true;
    }

    // Di chuyển dọc
    private boolean moveVertical(double dy) {
        if (Math.abs(dy) < EPSILON) return false;

        double step = Math.signum(dy) * Math.min(speed, Math.abs(dy));
        double nextY = y + step;

        if (!canMove(x, nextY)) return false;

        y = nextY;
        currentDirection = dy < 0 ? Direction.UP : Direction.DOWN;
        return true;
    }

    // Kondoria xuyên được gạch mềm (1) lẫn tường cứng (2),
    // chỉ bị chặn bởi viền ngoài bản đồ (hàng/cột đầu và cuối)
    private boolean canMove(double nextX, double nextY) {
        if (mapHash == null || mapHash.length == 0) return false;

        int left = (int) Math.floor(nextX / TILE_SIZE);
        int right = (int) Math.floor((nextX + width - EPSILON) / TILE_SIZE);
        int top = (int) Math.floor(nextY / TILE_SIZE);
        int bottom = (int) Math.floor((nextY + height - EPSILON) / TILE_SIZE);

        int maxRow = mapHash.length - 1;
        int maxCol = mapHash[0].length - 1;

        return left >= 1 && top >= 1 && right < maxCol && bottom < maxRow;
    }

    // Giới hạn tọa độ trong bản đồ
    private void clampToBounds() {
        if (mapHash == null || mapHash.length == 0) return;

        double maxX = (mapHash[0].length - 1) * TILE_SIZE - width;
        double maxY = (mapHash.length - 1) * TILE_SIZE - height;

        x = Math.max(TILE_SIZE, Math.min(x, maxX));
        y = Math.max(TILE_SIZE, Math.min(y, maxY));
    }

    @Override
    public void move(Direction direction) {
        currentDirection = direction;
    }

    // ===================== Vẽ =====================

    @Override
    public void draw(Graphics2D g2d) {
        if (!survival && !deading) return;

        if (!survival) {
            Image img = deadImgs[animIndex % deadImgs.length];

            if (img != null) {
                g2d.drawImage(img, (int) x, (int) y, width, height, null);
            }
            return;
        }

        Image img = currentDirection == Direction.LEFT ? leftImg : rightImg;

        if (img == null) {
            img = leftImg != null ? leftImg : rightImg;
        }

        if (img != null) {
            g2d.drawImage(img, (int) x, (int) y, width, height, null);
        } else {
            g2d.setColor(Color.ORANGE);
            g2d.fillRect((int) x, (int) y, width, height);
            g2d.setColor(Color.BLACK);
            g2d.drawRect((int) x, (int) y, width, height);
        }
    }
}