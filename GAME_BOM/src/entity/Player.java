package entity;

import bomb.Bomb;
import entity.mob.Mob;
import utils.Direction;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.util.List;
import javax.swing.ImageIcon;

public class Player extends Mob {
    private boolean goUp, goDown, goLeft, goRight;
    private final int[][] mapHash;
    private final int TILE_SIZE = 32;

    private static final int PLAYER_WIDTH = 24;
    private static final int PLAYER_HEIGHT = 32;

    private final Image[] downImgs = new Image[4];
    private final Image[] upImgs = new Image[4];
    private final Image[] leftImgs = new Image[4];
    private final Image[] rightImgs = new Image[4];
    private final Image[] deads = new Image[3];

    private int animIndex = 0;
    private int animTimer = 0;

    // Thêm tên hiển thị cho người chơi (VD: "P1", "P2")
    private String playerName = "Player";

    public Player(double startX, double startY, int[][] mapHash) {
        super(startX, startY, PLAYER_WIDTH, PLAYER_HEIGHT, 2, 1, 3, 3, 1);
        this.mapHash = mapHash;
        loadImages();
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public String getPlayerName() {
        return playerName;
    }

    private void loadImages() {
        try {
            var d1 = getClass().getResource("/PlayerImage/d1.png");
            var d2 = getClass().getResource("/PlayerImage/d2.png");
            var d3 = getClass().getResource("/PlayerImage/d3.png");
            var d4 = getClass().getResource("/PlayerImage/d4.png");
            downImgs[0] = (d1 != null) ? new ImageIcon(d1).getImage() : null;
            downImgs[1] = (d2 != null) ? new ImageIcon(d2).getImage() : null;
            downImgs[2] = (d3 != null) ? new ImageIcon(d3).getImage() : null;
            downImgs[3] = (d4 != null) ? new ImageIcon(d4).getImage() : null;

            var u1 = getClass().getResource("/PlayerImage/u1.png");
            var u2 = getClass().getResource("/PlayerImage/u2.png");
            var u3 = getClass().getResource("/PlayerImage/u3.png");
            var u4 = getClass().getResource("/PlayerImage/u4.png");
            upImgs[0] = (u1 != null) ? new ImageIcon(u1).getImage() : null;
            upImgs[1] = (u2 != null) ? new ImageIcon(u2).getImage() : null;
            upImgs[2] = (u3 != null) ? new ImageIcon(u3).getImage() : null;
            upImgs[3] = (u4 != null) ? new ImageIcon(u4).getImage() : null;

            var l1 = getClass().getResource("/PlayerImage/l1.png");
            var l2 = getClass().getResource("/PlayerImage/l2.png");
            var l3 = getClass().getResource("/PlayerImage/l3.png");
            var l4 = getClass().getResource("/PlayerImage/l4.png");
            leftImgs[0] = (l1 != null) ? new ImageIcon(l1).getImage() : null;
            leftImgs[1] = (l2 != null) ? new ImageIcon(l2).getImage() : null;
            leftImgs[2] = (l3 != null) ? new ImageIcon(l3).getImage() : null;
            leftImgs[3] = (l4 != null) ? new ImageIcon(l4).getImage() : null;

            var r1 = getClass().getResource("/PlayerImage/r1.png");
            var r2 = getClass().getResource("/PlayerImage/r2.png");
            var r3 = getClass().getResource("/PlayerImage/r3.png");
            var r4 = getClass().getResource("/PlayerImage/r4.png");
            rightImgs[0] = (r1 != null) ? new ImageIcon(r1).getImage() : null;
            rightImgs[1] = (r2 != null) ? new ImageIcon(r2).getImage() : null;
            rightImgs[2] = (r3 != null) ? new ImageIcon(r3).getImage() : null;
            rightImgs[3] = (r4 != null) ? new ImageIcon(r4).getImage() : null;

            var dead1 = getClass().getResource("/PlayerImage/dead1.png");
            var dead2 = getClass().getResource("/PlayerImage/dead2.png");
            var dead3 = getClass().getResource("/PlayerImage/dead3.png");
            deads[0] = (dead1 != null) ? new ImageIcon(dead1).getImage() : null;
            deads[1] = (dead2 != null) ? new ImageIcon(dead2).getImage() : null; // Giữ nguyên an toàn
            deads[2] = (dead3 != null) ? new ImageIcon(dead3).getImage() : null;

        } catch (Exception e) {
            System.err.println("Lỗi khi tải ảnh player: " + e.getMessage());
        }
    }

    @Override
    public void draw(Graphics2D g2d) {
        if (!survival) {
            Image deadImg = (animIndex < deads.length) ? deads[animIndex] : deads[deads.length - 1];
            if (deadImg != null) {
                g2d.drawImage(deadImg, (int) x, (int) y, width, height, null);
            }
            return;
        }

        Image currentImg = switch (currentDirection) {
            case UP -> upImgs[animIndex % upImgs.length];
            case LEFT -> leftImgs[animIndex % leftImgs.length];
            case RIGHT -> rightImgs[animIndex % rightImgs.length];
            default -> downImgs[animIndex % downImgs.length];
        };

        if (currentImg != null) {
            g2d.drawImage(currentImg, (int) x, (int) y, width, height, null);
        } else {
            g2d.setColor(Color.RED);
            g2d.fillRect((int) x, (int) y, width, height);
        }

        // --- VẼ TÊN VÀ THANH HP TRÊN ĐẦU PLAYER ---
        g2d.setFont(new Font("Arial", Font.BOLD, 10));

        // 1. Vẽ tên Player
        g2d.setColor(Color.YELLOW);
        g2d.drawString(playerName, (int) x - 2, (int) y - 12);

        // 2. Vẽ thanh HP (thanh máu nhỏ)
        int barWidth = width;
        int barHeight = 4;
        int barX = (int) x;
        int barY = (int) y - 8;

        // Nền thanh HP (màu đỏ tối)
        g2d.setColor(new Color(100, 0, 0));
        g2d.fillRect(barX, barY, barWidth, barHeight);

        // Máu hiện tại (màu xanh lá)
        // Giả sử HP tối đa là 3, tính tỷ lệ máu
        int maxHp = 3;
        double hpPercent = (double) Math.max(0, hp) / maxHp;
        int currentHpWidth = (int) (barWidth * hpPercent);

        g2d.setColor(Color.GREEN);
        g2d.fillRect(barX, barY, currentHpWidth, barHeight);

        // Viền đen xung quanh thanh HP
        g2d.setColor(Color.BLACK);
        g2d.drawRect(barX, barY, barWidth, barHeight);
    }

    public void handleKeyPressed(KeyEvent e, List<Bomb> globalBombs) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_W || key == KeyEvent.VK_UP)    { goUp = true; currentDirection = Direction.UP; }
        if (key == KeyEvent.VK_S || key == KeyEvent.VK_DOWN)  { goDown = true; currentDirection = Direction.DOWN; }
        if (key == KeyEvent.VK_A || key == KeyEvent.VK_LEFT)  { goLeft = true; currentDirection = Direction.LEFT; }
        if (key == KeyEvent.VK_D || key == KeyEvent.VK_RIGHT) { goRight = true; currentDirection = Direction.RIGHT; }
        if (key == KeyEvent.VK_SPACE) {
            dropBomb(mapHash, globalBombs);
        }
    }

    public void handleKeyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_W || key == KeyEvent.VK_UP)    goUp = false;
        if (key == KeyEvent.VK_S || key == KeyEvent.VK_DOWN)  goDown = false;
        if (key == KeyEvent.VK_A || key == KeyEvent.VK_LEFT)  goLeft = false;
        if (key == KeyEvent.VK_D || key == KeyEvent.VK_RIGHT) goRight = false;

        if (!goUp && !goDown && !goLeft && !goRight) {
            animIndex = 0;
        }
    }

    private boolean isValidPosition(double nextX, double nextY) {
        if (mapHash == null) return true;

        int leftTile = (int) (nextX / TILE_SIZE);
        int rightTile = (int) ((nextX + width - 1) / TILE_SIZE);
        int topTile = (int) (nextY / TILE_SIZE);
        int bottomTile = (int) ((nextY + height - 1) / TILE_SIZE);

        try {
            return isWalkable(mapHash[topTile][leftTile]) &&
                    isWalkable(mapHash[topTile][rightTile]) &&
                    isWalkable(mapHash[bottomTile][leftTile]) &&
                    isWalkable(mapHash[bottomTile][rightTile]);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isWalkable(int tileValue) {
        return tileValue != 1 && tileValue != 2 && tileValue != 4;
    }

    @Override
    public void update() {
        if (!survival) return;
        double nextX = x;
        double nextY = y;
        boolean isMoving = false;

        if (goUp)    { nextY -= speed; isMoving = true; currentDirection = Direction.UP; }
        if (goDown)  { nextY += speed; isMoving = true; currentDirection = Direction.DOWN; }
        if (goLeft)  { nextX -= speed; isMoving = true; currentDirection = Direction.LEFT; }
        if (goRight) { nextX += speed; isMoving = true; currentDirection = Direction.RIGHT; }

        if (goLeft || goRight) {
            if (isValidPosition(nextX, y)) {
                x = nextX;
            } else {
                double alignSpeed = speed;
                if (isValidPosition(nextX, y - alignSpeed)) {
                    y -= alignSpeed;
                } else if (isValidPosition(nextX, y + alignSpeed)) {
                    y += alignSpeed;
                }
            }
        }

        if (goUp || goDown) {
            if (isValidPosition(x, nextY)) {
                y = nextY;
            } else {
                double alignSpeed = speed;
                if (isValidPosition(x - alignSpeed, nextY)) {
                    x -= alignSpeed;
                } else if (isValidPosition(x + alignSpeed, nextY)) {
                    x += alignSpeed;
                }
            }
        }

        if (isMoving) {
            animTimer++;
            if (animTimer > 8) {
                animIndex = (animIndex + 1) % 4;
                animTimer = 0;
            }
        }
    }

    public void dropBomb(int[][] mapHash, List<Bomb> globalBombList) {
        long activeCount = globalBombList.stream()
                .filter(Bomb::isActive)
                .count();

        if (activeCount < getBombLimit()) {
            int bombX = (int) ((x + width / 2.0) / TILE_SIZE) * TILE_SIZE;
            int bombY = (int) ((y + height / 2.0) / TILE_SIZE) * TILE_SIZE;

            boolean alreadyHasBomb = globalBombList.stream()
                    .anyMatch(b -> b.isActive() && (int) b.getBounds().getX() == bombX && (int) b.getBounds().getY() == bombY);

            if (!alreadyHasBomb) {
                Bomb newBomb = new Bomb(bombX, bombY, mapHash, this.power, this.attackPower);
                globalBombList.add(newBomb);
            }
        }
    }

    @Override
    public void move(Direction direction) {
        this.currentDirection = direction;
    }
}