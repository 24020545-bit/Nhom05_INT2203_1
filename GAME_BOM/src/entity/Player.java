package entity;

import bomb.Bomb;
import entity.mob.Mob;
import utils.Direction;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ImageIcon;

public class Player extends Mob {
    private boolean goUp, goDown, goLeft, goRight;
    private int[][] mapHash;
    private final int TILE_SIZE = 32;

    // Đặt kích thước nhỏ hơn TILE_SIZE (ví dụ 24x24) để khớp với ảnh và di chuyển mượt mà không bị vướng khoảng trống
    private static final int PLAYER_WIDTH = 24;
    private static final int PLAYER_HEIGHT = 32;

    private List<Bomb> activeBombs = new ArrayList<>();
    private int bombLimit = 1;

    private Image[] downImgs = new Image[4];
    private Image[] upImgs = new Image[4];
    private Image[] leftImgs = new Image[4];
    private Image[] rightImgs = new Image[4];

    private int animIndex = 0;
    private int animTimer = 0;

    public Player(double startX, double startY, int[][] mapHash) {
        // Truyền width và height là PLAYER_WIDTH và PLAYER_HEIGHT vào constructor của Mob
        super(startX, startY, PLAYER_WIDTH, PLAYER_HEIGHT, 2, 1, 1, 3, 1);
        this.mapHash = mapHash;
        loadImages();
    }

    private void loadImages() {
        try {
            for (int i = 0; i < 4; i++) {
                downImgs[i]  = new ImageIcon(getClass().getResource("/PlayerImage/d" + (i + 1) + ".png")).getImage();
                upImgs[i]    = new ImageIcon(getClass().getResource("/PlayerImage/u" + (i + 1) + ".png")).getImage();
                leftImgs[i]  = new ImageIcon(getClass().getResource("/PlayerImage/l" + (i + 1) + ".png")).getImage();
                rightImgs[i] = new ImageIcon(getClass().getResource("/PlayerImage/r" + (i + 1) + ".png")).getImage();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void draw(Graphics2D g2d) {
        Image currentImg = null;
        switch (currentDirection) {
            case UP: currentImg = upImgs[animIndex]; break;
            case DOWN: currentImg = downImgs[animIndex]; break;
            case LEFT: currentImg = leftImgs[animIndex]; break;
            case RIGHT: currentImg = rightImgs[animIndex]; break;
            default: currentImg = downImgs[animIndex]; break;
        }

        if (currentImg != null) {
            // Vẽ ảnh co dãn theo kích thước width, height của player (có thể căn chỉnh thêm offset nếu muốn ảnh nằm chính giữa ô)
            g2d.drawImage(currentImg, (int) x, (int) y, width, height, null);
        } else {
            g2d.setColor(java.awt.Color.RED);
            g2d.fillRect((int) x, (int) y, width, height);
        }
    }

    public void draw(Graphics g) {
        if (g instanceof Graphics2D) {
            draw((Graphics2D) g);
        }
    }

    public void handleKeyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_W || key == KeyEvent.VK_UP)    { goUp = true; currentDirection = Direction.UP; }
        if (key == KeyEvent.VK_S || key == KeyEvent.VK_DOWN)  { goDown = true; currentDirection = Direction.DOWN; }
        if (key == KeyEvent.VK_A || key == KeyEvent.VK_LEFT)  { goLeft = true; currentDirection = Direction.LEFT; }
        if (key == KeyEvent.VK_D || key == KeyEvent.VK_RIGHT) { goRight = true; currentDirection = Direction.RIGHT; }
        if (key == KeyEvent.VK_SPACE) {
            dropBomb(mapHash, activeBombs);
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

        // Sử dụng width và height thay vì cố định TILE_SIZE để kiểm tra chính xác vùng va chạm nhỏ hơn
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
        // Cho phép đi qua đường đi (0) và các ô trống khác phù hợp
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

        // --- Cải tiến phần kiểm tra và trượt góc (Corner Sliding) ---
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
        activeBombs.removeIf(b -> !b.isActive());
        if (activeBombs.size() < bombLimit) {
            Bomb newBomb = new Bomb(this.x, this.y, mapHash, this.power, this.attackPower);
            activeBombs.add(newBomb);
            globalBombList.add(newBomb);
        }
    }

    @Override
    public void move(Direction direction) {
        this.currentDirection = direction;
    }
}