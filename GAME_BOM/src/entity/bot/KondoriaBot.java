package entity.bot;

import entity.Player;
import entity.mob.Mob;
import utils.Direction;
import utils.RandomMovement;

import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Image;
import javax.swing.ImageIcon;

public class KondoriaBot extends Mob {
    private Player[] players;
    private int[][] mapHash;
    private final int TILE_SIZE = 32;

    private double moveHori = 0;
    private double moveVerti = 0;

    // Biến hỗ trợ trượt mượt khi thoát khỏi tường
    private double targetX = -1, targetY = -1;
    private boolean isEscapingWall = false;

    private static final int CHASE_RADIUS = 5;
    private static final long HIT_STUN_DURATION = 500;

    private RandomMovement randomMovement;
    private Player currentTarget = null;

    // Hình ảnh
    private Image leftImg;
    private Image rightImg;
    private Image dameImg;
    private final Image[] deadImgs = new Image[2];

    private int animIndex = 0;
    private int animTimer = 0;

    public KondoriaBot(double x, double y, int width, int height, int speed, int hp, int attackPower, long attackCooldown, Player[] players, int[][] mapHash) {
        super(x, y, width, height, speed, 0, 0, hp, attackPower);
        this.players = players;
        this.mapHash = mapHash;
        this.attackCooldown = attackCooldown;
        this.currentDirection = Direction.LEFT;
        this.randomMovement = new RandomMovement(x, y, width, height, speed, mapHash);
        loadImages();
    }

    private void loadImages() {
        try {
            var l = getClass().getResource("/Bot/KondoriaBot/left.png");
            var r = getClass().getResource("/Bot/KondoriaBot/right.png");
            var d = getClass().getResource("/Bot/KondoriaBot/dame.png");
            var dead1 = getClass().getResource("/Bot/KondoriaBot/dead1.png");
            var dead2 = getClass().getResource("/Bot/KondoriaBot/dead2.png");

            leftImg = (l != null) ? new ImageIcon(l).getImage() : null;
            rightImg = (r != null) ? new ImageIcon(r).getImage() : null;
            dameImg = (d != null) ? new ImageIcon(d).getImage() : null;
            deadImgs[0] = (dead1 != null) ? new ImageIcon(dead1).getImage() : null;
            deadImgs[1] = (dead2 != null) ? new ImageIcon(dead2).getImage() : null;
        } catch (Exception e) {
            System.err.println("Lỗi tải ảnh KondoriaBot: " + e.getMessage());
        }
    }

    @Override
    public void update() {
        if (!survival) return;

        checkPlayerCollision();
        moveEnemy();
        clampToBounds(); // Luôn chặn biên để bot không bao giờ ra ngoài map

        animTimer++;
        if (animTimer > 10) {
            animIndex++;
            animTimer = 0;
        }
    }

    // 1. Kiểm tra va chạm với người chơi để gây sát thương
    private void checkPlayerCollision() {
        if (players == null) return;
        long currentTime = System.currentTimeMillis();
        for (Player player : players) {
            if (player != null && player.isSurvival()) {
                if (this.getBounds().intersects(player.getBounds())) {
                    if (currentTime - lastAttackTime >= attackCooldown) {
                        player.takeDamage(this.attackPower);
                        lastAttackTime = currentTime;
                    }
                }
            }
        }
    }

    // 2. Hàm cố định tọa độ bot không được vượt quá kích thước bản đồ
    private void clampToBounds() {
        if (mapHash != null && mapHash.length > 0 && mapHash[0].length > 0) {
            double minX = 0;
            double minY = 0;
            double maxX = (mapHash[0].length * TILE_SIZE) - width;
            double maxY = (mapHash.length * TILE_SIZE) - height;

            if (x < minX) x = minX;
            if (x > maxX) x = maxX;
            if (y < minY) y = minY;
            if (y > maxY) y = maxY;
        }
    }

    // 3. Logic di chuyển chính của bot
    private void moveEnemy() {
        if (players == null) return;

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastAttackTime < HIT_STUN_DURATION) {
            moveHori = 0;
            moveVerti = 0;
            return;
        }

        int currentTileX = (int) ((x + width / 2.0) / TILE_SIZE);
        int currentTileY = (int) ((y + height / 2.0) / TILE_SIZE);

        // Tìm người chơi gần nhất trong phạm vi phát hiện
        findClosestTarget(currentTileX, currentTileY);

        if (currentTarget != null) {
            // --- TRẠNG THÁI: ĐUỔI THEO NGƯỜI CHƠI (ĐI XUYÊN TƯỜNG) ---
            isEscapingWall = false;

            int targetTileX = (int) ((currentTarget.getX() + currentTarget.getWidth() / 2.0) / TILE_SIZE);
            int targetTileY = (int) ((currentTarget.getY() + currentTarget.getHeight() / 2.0) / TILE_SIZE);

            if (moveHori == 0 && moveVerti == 0) {
                updateDirectionAndSpeed(targetTileX, targetTileY, currentTileX, currentTileY);
            }

            if (Math.abs(currentTileX * TILE_SIZE - x) < speed && Math.abs(currentTileY * TILE_SIZE - y) < speed) {
                x = currentTileX * TILE_SIZE;
                y = currentTileY * TILE_SIZE;
                moveHori = 0;
                moveVerti = 0;
                updateDirectionAndSpeed(targetTileX, targetTileY, currentTileX, currentTileY);
            }

            x += moveHori;
            y += moveVerti;

        } else {
            // --- TRẠNG THÁI: MẤT MỤC TIÊU / ĐI NGẪU NHIÊN ---
            handleRandomOrEscapeMovement(currentTileX, currentTileY);
        }
    }

    // Tìm kiếm Player trong phạm vi CHASE_RADIUS
    private void findClosestTarget(int j, int i) {
        currentTarget = null;
        for (Player p : players) {
            if (p != null && p.isSurvival()) {
                int jp = (int) ((p.getX() + p.getWidth() / 2.0) / TILE_SIZE);
                int ip = (int) ((p.getY() + p.getHeight() / 2.0) / TILE_SIZE);

                if (Math.abs(jp - j) <= CHASE_RADIUS && Math.abs(ip - i) <= CHASE_RADIUS) {
                    currentTarget = p;
                    break;
                }
            }
        }
    }

    // Cập nhật hướng và vận tốc bám theo mục tiêu
    private void updateDirectionAndSpeed(int targetX, int targetY, int curX, int curY) {
        if (targetX != curX) {
            moveHori = (targetX < curX) ? -speed : speed;
            currentDirection = (targetX < curX) ? Direction.LEFT : Direction.RIGHT;
        }
        if (targetY != curY) {
            moveVerti = (targetY < curY) ? -speed : speed;
            currentDirection = (targetY < curY) ? Direction.UP : Direction.DOWN;
        }

        if (moveVerti != 0 && moveHori != 0) {
            if (Math.abs(targetY - curY) > Math.abs(targetX - curX)) {
                moveHori = 0;
            } else {
                moveVerti = 0;
            }
        }
    }

    // Xử lý khi đi ngẫu nhiên hoặc trượt thoát khỏi tường
    private void handleRandomOrEscapeMovement(int j, int i) {
        if (!randomMovement.isValidPosition(x, y) && !isEscapingWall) {
            for (int r = 1; r <= 3; r++) {
                double[][] candidates = {
                        { (j + r) * TILE_SIZE, i * TILE_SIZE },
                        { (j - r) * TILE_SIZE, i * TILE_SIZE },
                        { j * TILE_SIZE, (i + r) * TILE_SIZE },
                        { j * TILE_SIZE, (i - r) * TILE_SIZE }
                };
                for (double[] coord : candidates) {
                    if (randomMovement.isValidPosition(coord[0], coord[1])) {
                        targetX = coord[0];
                        targetY = coord[1];
                        isEscapingWall = true;
                        break;
                    }
                }
                if (isEscapingWall) break;
            }
        }

        if (isEscapingWall) {
            double dx = targetX - x;
            double dy = targetY - y;
            double dist = Math.sqrt(dx * dx + dy * dy);

            if (dist <= speed) {
                x = targetX;
                y = targetY;
                isEscapingWall = false;
            } else {
                x += (dx / dist) * speed;
                y += (dy / dist) * speed;
            }
            moveHori = 0;
            moveVerti = 0;
            return;
        }

        randomMovement.update(x, y);
        this.x = randomMovement.getX();
        this.y = randomMovement.getY();
        this.currentDirection = randomMovement.getCurrentDirection();
        moveHori = 0;
        moveVerti = 0;
    }

    @Override
    public void move(Direction direction) {
        this.currentDirection = direction;
    }

    @Override
    public void draw(Graphics2D g2d) {
        if (!survival && !deading) return;

        if (!survival) {
            Image deadImg = deadImgs[animIndex % deadImgs.length];
            if (deadImg != null) {
                g2d.drawImage(deadImg, (int) x, (int) y, width, height, null);
            }
            return;
        }

        Image currentImg = (currentDirection == Direction.LEFT) ? leftImg : rightImg;
        if (currentImg == null) {
            currentImg = (leftImg != null) ? leftImg : rightImg;
        }

        if (currentImg != null) {
            g2d.drawImage(currentImg, (int) x, (int) y, width, height, null);
        } else {
            g2d.setColor(new Color(255, 140, 0));
            g2d.fillRect((int) x, (int) y, width, height);
            g2d.setColor(Color.BLACK);
            g2d.drawRect((int) x, (int) y, width, height);
        }
    }
}