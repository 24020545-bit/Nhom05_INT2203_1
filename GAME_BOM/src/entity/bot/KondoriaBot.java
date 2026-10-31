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
    private Player[] players; // Mảng chứa các người chơi (Player 1, Player 2,...)
    private int[][] mapHash;
    private final int TILE_SIZE = 32;
    private double moveHori = 0;
    private double moveVerti = 0;

    private static final int CHASE_RADIUS = 4;
    private static final long HIT_STUN_DURATION = 500;

    private RandomMovement randomMovement;
    private Player currentTarget = null; // Người chơi đang bị đuổi theo hiện tại

    // Tài nguyên hình ảnh
    private Image leftImg;
    private Image rightImg;
    private Image dameImg;
    private final Image[] deadImgs = new Image[2];

    private int animIndex = 0;
    private int animTimer = 0;

    // Constructor hỗ trợ nhiều người chơi
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
            System.err.println("Lỗi khi tải ảnh KondoriaBot: " + e.getMessage());
        }
    }

    @Override
    public void update() {
        if (!survival) return;

        // Kiểm tra va chạm và tấn công bất kỳ Player nào đang sống
        if (players != null) {
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

        moveEnemy();

        animTimer++;
        if (animTimer > 10) {
            animIndex++;
            animTimer = 0;
        }
    }

    private void moveEnemy() {
        if (players == null) return;

        long currentTime = System.currentTimeMillis();

        if (currentTime - lastAttackTime < HIT_STUN_DURATION) {
            moveHori = 0;
            moveVerti = 0;
            return;
        }

        int j = (int) ((x + width / 2.0) / TILE_SIZE);
        int i = (int) ((y + height / 2.0) / TILE_SIZE);

        // --- TÌM XEM CÓ PLAYER NÀO TRONG PHẠM VI KHÔNG ---
        currentTarget = null;
        for (Player p : players) {
            if (p != null && p.isSurvival()) {
                int jp = (int) ((p.getX() + p.getWidth() / 2.0) / TILE_SIZE);
                int ip = (int) ((p.getY() + p.getHeight() / 2.0) / TILE_SIZE);

                int distanceX = Math.abs(jp - j);
                int distanceY = Math.abs(ip - i);

                if (distanceX <= CHASE_RADIUS && distanceY <= CHASE_RADIUS) {
                    currentTarget = p; // Phát hiện ra player này, gán làm mục tiêu và đuổi theo
                    break;
                }
            }
        }

        if (currentTarget != null) {
            // --- TRẠNG THÁI ĐUỔI THEO & ĐI XUYÊN TƯỜNG (PHÁT HIỆN PLAYER BẤT KỲ) ---
            int jp = (int) ((currentTarget.getX() + currentTarget.getWidth() / 2.0) / TILE_SIZE);
            int ip = (int) ((currentTarget.getY() + currentTarget.getHeight() / 2.0) / TILE_SIZE);

            if (moveHori == 0 && moveVerti == 0) {
                if (jp != j) {
                    moveHori = (jp < j) ? -speed : speed;
                    currentDirection = (jp < j) ? Direction.LEFT : Direction.RIGHT;
                } else if (ip != i) {
                    moveVerti = (ip < i) ? -speed : speed;
                    currentDirection = (ip < i) ? Direction.UP : Direction.DOWN;
                }
            }

            if (Math.abs(j * TILE_SIZE - x) < speed && Math.abs(i * TILE_SIZE - y) < speed) {
                x = j * TILE_SIZE;
                y = i * TILE_SIZE;

                moveHori = 0;
                moveVerti = 0;

                if (jp != j) {
                    moveHori = (jp < j) ? -speed : speed;
                    currentDirection = (jp < j) ? Direction.LEFT : Direction.RIGHT;
                }
                if (ip != i) {
                    moveVerti = (ip < i) ? -speed : speed;
                    currentDirection = (ip < i) ? Direction.UP : Direction.DOWN;
                }

                if (moveVerti != 0 && moveHori != 0) {
                    if (Math.abs(ip - i) > Math.abs(jp - j)) {
                        moveHori = 0;
                    } else {
                        moveVerti = 0;
                    }
                }
            }

            // Đi xuyên tường khi đuổi theo mục tiêu
            x += moveHori;
            y += moveVerti;

        } else {
            // --- TRẠNG THÁI DI CHUYỂN NGẪU NHIÊN (KHÔNG PHÁT HIỆN AI, CÓ CHECK TƯỜNG) ---
            randomMovement.update(x, y);
            this.x = randomMovement.getX();
            this.y = randomMovement.getY();
            this.currentDirection = randomMovement.getCurrentDirection();
            moveHori = 0;
            moveVerti = 0;
        }
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

        // Chọn ảnh theo hướng di chuyển (trái/phải)
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