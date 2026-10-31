package entity.bot;

import entity.Player;
import entity.mob.Mob;
import utils.Direction;
import utils.RandomMovement;

import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Image;
import javax.swing.ImageIcon;

public class DollBot extends Mob {
    private int[][] mapHash;
    private final int TILE_SIZE = 32;
    private Player targetPlayer;
    private RandomMovement randomMovement;

    private Image leftImg;
    private Image rightImg;
    private Image dameImg;
    private final Image[] deadImgs = new Image[2];

    private int animIndex = 0;
    private int animTimer = 0;

    public DollBot(double x, double y, int width, int height, int speed, int hp, int attackPower, long attackCooldown, Player targetPlayer, int[][] mapHash) {
        super(x, y, width, height, 1, speed, 0, hp, attackPower);
        this.targetPlayer = targetPlayer;
        this.mapHash = mapHash;
        this.attackCooldown = attackCooldown;
        this.randomMovement = new RandomMovement(x, y, width, height, speed, mapHash);
        this.currentDirection = randomMovement.getCurrentDirection();
        loadImages();
    }

    private void loadImages() {
        try {
            var l = getClass().getResource("/Bot/DollBot/left.png");
            var r = getClass().getResource("/Bot/DollBot/right.png");
            var d = getClass().getResource("/Bot/DollBot/dame.png");
            var dead1 = getClass().getResource("/Bot/DollBot/dead1.png");
            var dead2 = getClass().getResource("/Bot/DollBot/dead2.png");

            leftImg = (l != null) ? new ImageIcon(l).getImage() : null;
            rightImg = (r != null) ? new ImageIcon(r).getImage() : null;
            dameImg = (d != null) ? new ImageIcon(d).getImage() : null;
            deadImgs[0] = (dead1 != null) ? new ImageIcon(dead1).getImage() : null;
            deadImgs[1] = (dead2 != null) ? new ImageIcon(dead2).getImage() : null;
        } catch (Exception e) {
            System.err.println("Lỗi khi tải ảnh DollBot: " + e.getMessage());
        }
    }

    @Override
    public void update() {
        if (!survival) return;

        if (targetPlayer != null && targetPlayer.isSurvival()) {
            if (this.getBounds().intersects(targetPlayer.getBounds())) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastAttackTime >= attackCooldown) {
                    targetPlayer.takeDamage(this.attackPower);
                    lastAttackTime = currentTime;
                }
            }
        }

        randomMovement.update(x, y);
        this.x = randomMovement.getX();
        this.y = randomMovement.getY();
        this.currentDirection = randomMovement.getCurrentDirection();

        animTimer++;
        if (animTimer > 10) {
            animIndex++;
            animTimer = 0;
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
        Image currentImg = rightImg;
        if (currentDirection == Direction.LEFT) {
            currentImg = (leftImg != null) ? leftImg : rightImg;
        } else {
            currentImg = (rightImg != null) ? rightImg : leftImg;
        }

        if (currentImg != null) {
            g2d.drawImage(currentImg, (int) x, (int) y, width, height, null);
        } else {
            // Fallback vẽ hình chữ nhật nếu chưa có ảnh
            g2d.setColor(new Color(219, 112, 147));
            g2d.fillRect((int) x, (int) y, width, height);
            g2d.setColor(Color.BLACK);
            g2d.drawRect((int) x, (int) y, width, height);
        }
    }
}