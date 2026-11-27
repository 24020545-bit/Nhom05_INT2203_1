package entity;

import bomb.Bomb;
import entity.mob.Mob;
import utils.Direction;
import utils.SoundManager;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ImageIcon;

public class Player extends Mob {
    private boolean goUp, goDown, goLeft, goRight;
    private final int[][] mapHash;
    private final int TILE_SIZE = 32;

    private static final int PLAYER_WIDTH = 20;
    private static final int PLAYER_HEIGHT = 28;

    // QUẢN LÝ BOM RIÊNG CHO TỪNG PLAYER
    private final List<Bomb> myBombs = new ArrayList<>();

    // Mảng ảnh cho các hướng và trạng thái chết
    private final Image[] downImgs = new Image[4];
    private final Image[] upImgs = new Image[4];
    private final Image[] leftImgs = new Image[4];
    private final Image[] rightImgs = new Image[4];
    private final Image[] deads = new Image[3];

    private int animIndex = 0;
    private int animTimer = 0;

    private String playerName = "Player";

    public Player(double startX, double startY, int[][] mapHash) {
        super(startX, startY, PLAYER_WIDTH, PLAYER_HEIGHT, 3, 1, 2, 20, 1);
        this.mapHash = mapHash;
        loadImages();
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public String getPlayerName() {
        return playerName;
    }

    public int getActiveBombCount() {
        myBombs.removeIf(b -> !b.isActive());
        return myBombs.size();
    }

    // ==================== LOAD IMAGE ====================
    private void loadImages() {
        String basePath = "/PlayerImage/";

        for (int i = 0; i < 4; i++) {
            downImgs[i]  = loadImage(basePath + "d" + (i + 1) + ".png");
            upImgs[i]    = loadImage(basePath + "u" + (i + 1) + ".png");
            leftImgs[i]  = loadImage(basePath + "l" + (i + 1) + ".png");
            rightImgs[i] = loadImage(basePath + "r" + (i + 1) + ".png");
        }

        for (int i = 0; i < 3; i++) {
            deads[i] = loadImage(basePath + "dead" + (i + 1) + ".png");
        }
    }

    private Image loadImage(String path) {
        var url = getClass().getResource(path);
        if (url == null) {
            System.err.println("Không tìm thấy ảnh player: " + path);
            return null;
        }
        return new ImageIcon(url).getImage();
    }

    // ==================== DRAW ====================
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

        // Vẽ tên của Player lên trên đầu
        g2d.setFont(new Font("Arial", Font.BOLD, 13));
        g2d.setColor(Color.YELLOW);
        g2d.drawString(playerName, (int) x - 2, (int) y - 12);

        // Gọi hàm vẽ thanh máu từ class cha Mob (độ dài bằng width của player, chiều cao là 4)
        drawHpBar(g2d, width, 4);
    }

    // ==================== INPUT HANDLING ====================
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

    // ==================== COLLISION & TILE CHECK ====================
    private boolean isValidPosition(double nextX, double nextY) {
        if (mapHash == null) return true;

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
        return tileValue == 0 || tileValue == 3 || (tileValue >= 5 && tileValue <= 9);
    }

    // ==================== UPDATE ====================
    @Override
    public void update() {
        if (!survival) return;

        myBombs.removeIf(b -> !b.isActive());

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

    // ==================== MOVEMENT & SLIDING ====================
    private void moveWithSlide(int dx, int dy) {
        int steps = Math.max(Math.abs(dx), Math.abs(dy));
        int stepX = Integer.compare(dx, 0);
        int stepY = Integer.compare(dy, 0);

        for (int i = 0; i < steps; i++) {
            if (isValidPosition(x + stepX, y + stepY)) {
                x += stepX;
                y += stepY;
            } else {
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

    // ==================== BOMB ACTION ====================
    public void dropBomb(int[][] mapHash, List<Bomb> globalBombList) {
        myBombs.removeIf(b -> !b.isActive());

        if (myBombs.size() < getBombLimit()) {
            int bombX = (int) ((x + width / 2.0) / TILE_SIZE) * TILE_SIZE;
            int bombY = (int) ((y + height / 2.0) / TILE_SIZE) * TILE_SIZE;

            boolean alreadyHasBomb = globalBombList.stream()
                    .anyMatch(b -> b.isActive() && (int) b.getBounds().getX() == bombX && (int) b.getBounds().getY() == bombY);

            if (!alreadyHasBomb) {
                Bomb newBomb = new Bomb(bombX, bombY, mapHash, globalBombList, this.power, this.attackPower);
                globalBombList.add(newBomb);
                myBombs.add(newBomb);

                SoundManager.playSoundEffect("/audio/putbomb.wav");
            }
        }
    }

    @Override
    public void move(Direction direction) {
        this.currentDirection = direction;
    }
}