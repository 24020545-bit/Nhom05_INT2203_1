package entity.bot;

import entity.Player;
import entity.mob.Mob;
import utils.Direction;
import utils.RandomMovement;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import javax.swing.ImageIcon;

/**
 * Kondoria: xuyên được cả gạch mềm lẫn tường cứng,
 * chỉ bị giới hạn bởi viền ngoài bản đồ.
 */
public class KondoriaBot extends Mob {

    private static final int TILE_SIZE = 32;
    private static final int CHASE_RADIUS = 4;
    private static final double EPSILON = 0.001;

    private final Player[] players;
    private final int[][] mapHash;
    private Player currentTarget;

    // Sử dụng RandomMovement cho việc đi lang thang
    private RandomMovement randomMovement;

    // Biến hỗ trợ di chuyển bám lưới khi chase
    private double moveHori = 0;
    private double moveVerti = 0;

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

        // Khởi tạo đối tượng RandomMovement
        this.randomMovement = new RandomMovement(x, y, width, height, speed, mapHash);

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

    private void chasePlayer() {
        if (currentTarget == null) return;

        int j = (int) ((x + width / 2.0) / TILE_SIZE);
        int i = (int) ((y + height / 2.0) / TILE_SIZE);

        int jp = (int) (currentTarget.getX() + currentTarget.getWidth() / 2.0) / TILE_SIZE;
        int ip = (int) (currentTarget.getY() + currentTarget.getHeight() / 2.0) / TILE_SIZE;

        double targetCenterX = j * TILE_SIZE;
        double targetCenterY = i * TILE_SIZE;

        // Chỉ căn gióng về tâm khi khoảng cách thực tế nhỏ hơn tốc độ di chuyển để triệt tiêu hiện tượng giật/teleport
        if ((moveHori == 0 && moveVerti == 0) ||
                (Math.abs(targetCenterX - x) <= speed && Math.abs(targetCenterY - y) <= speed)) {

            x = targetCenterX;
            y = targetCenterY;

            moveHori = 0;
            moveVerti = 0;

            if (jp != j) {
                moveHori = (jp < j) ? -speed : speed;
            }
            if (ip != i) {
                moveVerti = (ip < i) ? -speed : speed;
            }

            // Nếu đi được cả 2 trục, ưu tiên trục có khoảng cách lớn hơn
            if (moveVerti != 0 && moveHori != 0) {
                if (Math.abs(ip - i) > Math.abs(jp - j)) {
                    moveHori = 0;
                } else {
                    moveVerti = 0;
                }
            }
        }

        applyMovement();
    }

    private void randomMove() {
        // Sử dụng hàm update của RandomMovement để đi lang thang (ưu tiên đường trống nhưng vẫn xuyên tường được)
        randomMovement.update(x, y);

        // Đồng bộ tọa độ và hướng từ RandomMovement sang KondoriaBot
        this.x = randomMovement.getX();
        this.y = randomMovement.getY();
        this.currentDirection = randomMovement.getCurrentDirection();

        // Reset lại vận tốc chase để khi mất mục tiêu/quay lại chase sẽ không bị xung đột
        moveHori = 0;
        moveVerti = 0;
    }

    private void applyMovement() {
        double nextX = x + moveHori;
        double nextY = y + moveVerti;

        if (canMove(nextX, nextY)) {
            x = nextX;
            y = nextY;

            if (Math.abs(moveHori) > EPSILON) {
                currentDirection = moveHori < 0 ? Direction.LEFT : Direction.RIGHT;
            } else if (Math.abs(moveVerti) > EPSILON) {
                currentDirection = moveVerti < 0 ? Direction.UP : Direction.DOWN;
            }
        }

        // Làm tròn tọa độ tránh sai số dấu phẩy động
        x = roundValue(x * 10) / 10.0;
        y = roundValue(y * 10) / 10.0;
    }

    private double roundValue(double a) {
        double num = (int) a;
        if (num + 0.5 <= a) {
            num += 1;
        }
        return num;
    }

    private boolean canMove(double nextX, double nextY) {
        if (mapHash == null || mapHash.length == 0) return false;

        int left = (int) Math.floor(nextX / TILE_SIZE);
        int right = (int) Math.floor((nextX + width - EPSILON) / TILE_SIZE);
        int top = (int) Math.floor(nextY / TILE_SIZE);
        int bottom = (int) Math.floor((nextY + height - EPSILON) / TILE_SIZE);

        int maxRow = mapHash.length;
        int maxCol = mapHash[0].length;

        // Kondoria chỉ bị chặn bởi viền ngoài bản đồ (hàng/cột đầu và cuối)
        return left >= 1 && top >= 1 && right < maxCol - 1 && bottom < maxRow - 1;
    }

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