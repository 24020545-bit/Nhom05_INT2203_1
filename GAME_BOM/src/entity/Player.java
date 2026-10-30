package entity;

import bomb.Bomb;
import entity.mob.Mob;
import utils.Direction;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.net.URL;
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

    private int animIndex = 0;
    private int animTimer = 0;

    public Player(double startX, double startY, int[][] mapHash) {
        super(startX, startY, PLAYER_WIDTH, PLAYER_HEIGHT, 2, 1, 3, 3, 1);
        this.mapHash = mapHash;
        loadImages();
    }

    private void loadImages() {
        for (int i = 0; i < 4; i++) {
            try {
                URL dUrl = getClass().getResource("/PlayerImage/d" + (i + 1) + ".png");
                URL uUrl = getClass().getResource("/PlayerImage/u" + (i + 1) + ".png");
                URL lUrl = getClass().getResource("/PlayerImage/l" + (i + 1) + ".png");
                URL rUrl = getClass().getResource("/PlayerImage/r" + (i + 1) + ".png");

                downImgs[i]  = (dUrl != null) ? new ImageIcon(dUrl).getImage() : null;
                upImgs[i]    = (uUrl != null) ? new ImageIcon(uUrl).getImage() : null;
                leftImgs[i]  = (lUrl != null) ? new ImageIcon(lUrl).getImage() : null;
                rightImgs[i] = (rUrl != null) ? new ImageIcon(rUrl).getImage() : null;
            } catch (Exception e) {
                System.err.println("Lỗi khi tải ảnh player: " + e.getMessage());
            }
        }
    }

    @Override
    public void draw(Graphics2D g2d) {
        Image currentImg = switch (currentDirection) {
            case UP -> upImgs[animIndex];
            case LEFT -> leftImgs[animIndex];
            case RIGHT -> rightImgs[animIndex];
            default -> downImgs[animIndex];
        };

        if (currentImg != null) {
            g2d.drawImage(currentImg, (int) x, (int) y, width, height, null);
        } else {
            g2d.setColor(Color.RED);
            g2d.fillRect((int) x, (int) y, width, height);
        }
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