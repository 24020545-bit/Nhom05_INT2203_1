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

    private String playerName = "Player";

    public Player(double startX, double startY, int[][] mapHash) {
        super(startX, startY, PLAYER_WIDTH, PLAYER_HEIGHT, 2, 1, 3, 10, 1);
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
            deads[1] = (dead2 != null) ? new ImageIcon(dead2).getImage() : null;
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

        // Vẽ tên và thanh HP trên đầu Player
        g2d.setFont(new Font("Arial", Font.BOLD, 10));

        g2d.setColor(Color.YELLOW);
        g2d.drawString(playerName, (int) x - 2, (int) y - 12);

        int barWidth = width;
        int barHeight = 4;
        int barX = (int) x;
        int barY = (int) y - 8;

        g2d.setColor(new Color(100, 0, 0));
        g2d.fillRect(barX, barY, barWidth, barHeight);

        int maxHp = getMaxHp();
        if (maxHp <= 0) maxHp = 1;

        double hpPercent = (double) Math.max(0, hp) / maxHp;
        int currentHpWidth = (int) (barWidth * hpPercent);

        g2d.setColor(Color.GREEN);
        g2d.fillRect(barX, barY, currentHpWidth, barHeight);

        g2d.setColor(Color.BLACK);
        g2d.drawRect(barX, barY, barWidth, barHeight);
    }

    public void handleKeyPressed(KeyEvent e, List<Bomb> globalBombs) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_W || key == KeyEvent.VK_UP)    { goUp = true; }
        if (key == KeyEvent.VK_S || key == KeyEvent.VK_DOWN)  { goDown = true; }
        if (key == KeyEvent.VK_A || key == KeyEvent.VK_LEFT)  { goLeft = true; }
        if (key == KeyEvent.VK_D || key == KeyEvent.VK_RIGHT) { goRight = true; }
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

        // Thu nhỏ nhẹ hitbox (padding = 3) để dễ dàng lách qua khe hẹp
        int padding = 3;
        int leftTile = (int) ((nextX + padding) / TILE_SIZE);
        int rightTile = (int) ((nextX + width - padding - 1) / TILE_SIZE);
        int topTile = (int) ((nextY + padding) / TILE_SIZE);
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
        // Chỉ chặn tường gạch (1) và tường cố định (2). Cho phép đi qua đường trống (0) và các loại item (5-9)
        return tileValue != 1 && tileValue != 2;
    }

    @Override
    public void update() {
        if (!survival) return;
        boolean isMoving = false;

        if (goLeft) {
            currentDirection = Direction.LEFT;
            isMoving = true;
            moveWithSlide(-speed, 0);
        } else if (goRight) {
            currentDirection = Direction.RIGHT;
            isMoving = true;
            moveWithSlide(speed, 0);
        }

        if (goUp) {
            currentDirection = Direction.UP;
            isMoving = true;
            moveWithSlide(0, -speed);
        } else if (goDown) {
            currentDirection = Direction.DOWN;
            isMoving = true;
            moveWithSlide(0, speed);
        }

        if (isMoving) {
            animTimer++;
            if (animTimer > 8) {
                animIndex = (animIndex + 1) % 4;
                animTimer = 0;
            }
        }
    }

    /**
     * Chia nhỏ bước đi từng pixel và hỗ trợ trượt góc mượt mà (corner sliding)
     */
    private void moveWithSlide(int dx, int dy) {
        int steps = Math.max(Math.abs(dx), Math.abs(dy));
        int stepX = Integer.compare(dx, 0);
        int stepY = Integer.compare(dy, 0);

        for (int i = 0; i < steps; i++) {
            if (isValidPosition(x + stepX, y + stepY)) {
                x += stepX;
                y += stepY;
            } else {
                // Cơ chế trượt góc thông minh khi vướng tường
                if (stepX != 0) {
                    if (isValidPosition(x + stepX, y - 1)) { y -= 1; x += stepX; }
                    else if (isValidPosition(x + stepX, y + 1)) { y += 1; x += stepX; }
                } else if (stepY != 0) {
                    if (isValidPosition(x - 1, y + stepY)) { x -= 1; y += stepY; }
                    else if (isValidPosition(x + 1, y + stepY)) { x += 1; y += stepY; }
                }
                break;
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