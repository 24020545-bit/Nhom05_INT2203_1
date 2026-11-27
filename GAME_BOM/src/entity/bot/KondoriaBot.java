package entity.bot;

import bomb.Bomb;
import entity.Player;
import entity.mob.Mob;
import utils.Direction;
import utils.BFS;
import utils.RandomMovement;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.util.List;
import javax.swing.ImageIcon;

public class KondoriaBot extends Mob {
    private static final int TILE_SIZE = 32;
    private static final int CHASE_RADIUS = 4;
    private static final double EPSILON = 0.01;

    private final Player[] players;
    private final int[][] mapHash;
    private final RandomMovement randomMovement;
    private Player currentTarget;

    // Vị trí ô đích hiện tại
    private int targetCol = -1;
    private int targetRow = -1;
    private boolean moving = false;
    private boolean movingIgnoreWalls = false;

    // Thời gian tấn công
    private long attackCooldown;

    // Animation
    private Image leftImg;
    private Image rightImg;
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
        this.randomMovement = new RandomMovement(x, y, width, height, speed, mapHash);
        this.attackCooldown = attackCooldown;
        this.currentDirection = Direction.LEFT;
        loadImages();
    }

    // ==================== LOAD IMAGE ====================
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
            System.err.println("Không tìm thấy ảnh: " + path);
            return null;
        }
        return new ImageIcon(url).getImage();
    }

    // ==================== GRID ====================
    private double getCellX(int col) {
        return col * TILE_SIZE + (TILE_SIZE - width) / 2.0;
    }

    private double getCellY(int row) {
        return row * TILE_SIZE + (TILE_SIZE - height) / 2.0;
    }

    private int getCurrentCol() {
        return (int) Math.round((x + width / 2.0) / TILE_SIZE - 0.5);
    }

    private int getCurrentRow() {
        return (int) Math.round((y + height / 2.0) / TILE_SIZE - 0.5);
    }

    // ==================== TILE & BOMB CHECK ====================
    private boolean isBombAtCell(int col, int row) {
        if (!isValidMap() || row < 0 || row >= mapHash.length || col < 0 || col >= mapHash[0].length) {
            return false;
        }
        int val = mapHash[row][col];
        return val == Bomb.TILE_BOMB_PLAYER_ON || val == Bomb.TILE_BOMB_SOLID;
    }

    private boolean isWalkableFloorValue(int val) {
        return val == Bomb.TILE_EMPTY || (val >= 5 && val <= 9);
    }

    private boolean hasBombAt(double nextX, double nextY) {
        int left = (int) Math.floor(nextX / TILE_SIZE);
        int right = (int) Math.floor((nextX + width - EPSILON) / TILE_SIZE);
        int top = (int) Math.floor(nextY / TILE_SIZE);
        int bottom = (int) Math.floor((nextY + height - EPSILON) / TILE_SIZE);

        int curLeft = (int) Math.floor(x / TILE_SIZE);
        int curRight = (int) Math.floor((x + width - EPSILON) / TILE_SIZE);
        int curTop = (int) Math.floor(y / TILE_SIZE);
        int curBottom = (int) Math.floor((y + height - EPSILON) / TILE_SIZE);

        for (int row = top; row <= bottom; row++) {
            for (int col = left; col <= right; col++) {
                boolean alreadyOnCell = (row >= curTop && row <= curBottom
                        && col >= curLeft && col <= curRight);
                if (!alreadyOnCell && isBombAtCell(col, row)) {
                    return true;
                }
            }
        }
        return false;
    }

    // ==================== UPDATE ====================
    @Override
    public void update() {
        if (!survival) {
            updateAnimation();
            return;
        }
        attackPlayer();
        findClosestTarget();

        if (moving) {
            boolean finished = moveToTargetCell(movingIgnoreWalls);
            if (finished) {
                moving = false;
            }
            updateAnimation();
            return;
        }

        if (currentTarget != null) {
            chasePlayer();
        } else if (!canMoveOnFloor(x, y)) {
            escapeToFloor();
        } else {
            randomMove();
        }
        updateAnimation();
        clampToBounds();
    }

    private void updateAnimation() {
        if (++animTimer >= 10) {
            animIndex = (animIndex + 1) % 2;
            animTimer = 0;
        }
    }

    // ==================== ATTACK ====================
    private void attackPlayer() {
        if (players == null) return;
        long now = System.currentTimeMillis();
        for (Player player : players) {
            if (player == null || !player.isSurvival()) continue;
            if (getBounds().intersects(player.getBounds())
                    && now - lastAttackTime >= attackCooldown) {
                player.takeDamage(attackPower);
                lastAttackTime = now;
                break;
            }
        }
    }

    // ==================== FIND PLAYER ====================
    private void findClosestTarget() {
        currentTarget = null;
        if (players == null) return;
        int botCol = getCurrentCol();
        int botRow = getCurrentRow();
        double minDistance = Double.MAX_VALUE;
        for (Player player : players) {
            if (player == null || !player.isSurvival()) continue;
            int playerCol = (int) ((player.getX() + player.getWidth() / 2.0) / TILE_SIZE);
            int playerRow = (int) ((player.getY() + player.getHeight() / 2.0) / TILE_SIZE);
            int dx = playerCol - botCol;
            int dy = playerRow - botRow;
            if (Math.abs(dx) <= CHASE_RADIUS && Math.abs(dy) <= CHASE_RADIUS) {
                double distance = dx * dx + dy * dy;
                if (distance < minDistance) {
                    minDistance = distance;
                    currentTarget = player;
                }
            }
        }
    }

    // ==================== CHASE PLAYER ====================
    private boolean insideMap(int col, int row) {
        return col >= 1 && row >= 1
                && col < mapHash[0].length - 1
                && row < mapHash.length - 1;
    }

    private void chasePlayer() {
        if (currentTarget == null || !isValidMap()) return;
        int endCol = (int) ((currentTarget.getX()
                + currentTarget.getWidth() / 2.0) / TILE_SIZE);
        int endRow = (int) ((currentTarget.getY()
                + currentTarget.getHeight() / 2.0) / TILE_SIZE);
        if (!insideMap(endCol, endRow)) return;
        List<int[]> path = BFS.findPath(mapHash, getCurrentCol(), getCurrentRow(),
                endCol, endRow, this::insideMap);
        followPath(path);
    }

    private void randomMove() {
        randomMovement.update(x, y);
        double nextX = randomMovement.getX();
        double nextY = randomMovement.getY();
        if (canMoveOnFloor(nextX, nextY)) {
            x = nextX;
            y = nextY;
        }
        currentDirection = randomMovement.getCurrentDirection();
    }

    private void followPath(List<int[]> path) {
        if (path.size() < 2) return;
        int[] next = path.get(1);
        setTargetCell(next[0], next[1], true);
        moveToTargetCell(true);
    }

    // ==================== SET GRID TARGET ====================
    private void setTargetCell(int col, int row, boolean ignoreWalls) {
        targetCol = col;
        targetRow = row;
        moving = true;
        movingIgnoreWalls = ignoreWalls;
    }

    private boolean moveToTargetCell(boolean ignoreWalls) {
        if (targetCol < 0 || targetRow < 0) return true;
        double targetX = getCellX(targetCol);
        double targetY = getCellY(targetRow);

        if (Math.abs(targetX - x) > EPSILON) {
            double dx = targetX - x;
            double step = Math.signum(dx) * Math.min(speed, Math.abs(dx));
            double nextX = x + step;
            if (!canMove(nextX, y)) {
                moving = false;
                return true;
            }
            if (!ignoreWalls && !canMoveOnFloor(nextX, y)) {
                moving = false;
                return true;
            }
            x = nextX;
            currentDirection = dx < 0 ? Direction.LEFT : Direction.RIGHT;
            return false;
        }

        if (Math.abs(targetY - y) > EPSILON) {
            double dy = targetY - y;
            double step = Math.signum(dy) * Math.min(speed, Math.abs(dy));
            double nextY = y + step;
            if (!canMove(x, nextY)) {
                moving = false;
                return true;
            }
            if (!ignoreWalls && !canMoveOnFloor(x, nextY)) {
                moving = false;
                return true;
            }
            y = nextY;
            currentDirection = dy < 0 ? Direction.UP : Direction.DOWN;
            return false;
        }

        x = targetX;
        y = targetY;
        moving = false;
        return true;
    }

    // ==================== CHECK FLOOR ====================
    private boolean canMoveOnFloor(double nextX, double nextY) {
        if (!isValidMap() || hasBombAt(nextX, nextY)) return false;
        int left = (int) Math.floor(nextX / TILE_SIZE);
        int right = (int) Math.floor((nextX + width - EPSILON) / TILE_SIZE);
        int top = (int) Math.floor(nextY / TILE_SIZE);
        int bottom = (int) Math.floor((nextY + height - EPSILON) / TILE_SIZE);
        int maxRow = mapHash.length - 1;
        int maxCol = mapHash[0].length - 1;
        if (left < 1 || top < 1 || right >= maxCol || bottom >= maxRow) {
            return false;
        }
        for (int row = top; row <= bottom; row++) {
            for (int col = left; col <= right; col++) {
                if (!isWalkableFloorValue(mapHash[row][col])) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean canMove(double nextX, double nextY) {
        if (!isValidMap()) return false;
        int left = (int) Math.floor(nextX / TILE_SIZE);
        int right = (int) Math.floor((nextX + width - EPSILON) / TILE_SIZE);
        int top = (int) Math.floor(nextY / TILE_SIZE);
        int bottom = (int) Math.floor((nextY + height - EPSILON) / TILE_SIZE);
        boolean inBounds = left >= 1 && top >= 1
                && right < mapHash[0].length - 1
                && bottom < mapHash.length - 1;
        return inBounds && !hasBombAt(nextX, nextY);
    }

    private boolean isValidMap() {
        return mapHash != null
                && mapHash.length > 0
                && mapHash[0] != null
                && mapHash[0].length > 0;
    }

    private void escapeToFloor() {
        if (!isValidMap()) return;
        List<int[]> path = BFS.findNearest(mapHash, getCurrentCol(), getCurrentRow(),
                (col, row) -> insideMap(col, row) && !isBombAtCell(col, row),
                (col, row) -> insideMap(col, row)
                        && isWalkableFloorValue(mapHash[row][col])
                        && !isBombAtCell(col, row)
                        && canMoveOnFloor(getCellX(col), getCellY(row)));
        if (path.size() == 1) {
            int[] cell = path.get(0);
            setTargetCell(cell[0], cell[1], true);
            moveToTargetCell(true);
        } else {
            followPath(path);
        }
    }

    // ==================== BOUNDARY ====================
    private void clampToBounds() {
        if (!isValidMap()) return;
        double minX = TILE_SIZE;
        double minY = TILE_SIZE;
        double maxX = (mapHash[0].length - 1) * TILE_SIZE - width;
        double maxY = (mapHash.length - 1) * TILE_SIZE - height;
        x = Math.max(minX, Math.min(x, maxX));
        y = Math.max(minY, Math.min(y, maxY));
    }

    @Override
    public void move(Direction direction) {
        currentDirection = direction;
    }

    @Override
    public void draw(Graphics2D g2d) {
        if (!survival && !deading) return;
        if (!survival) {
            Image img = deadImgs[animIndex];
            if (img != null) {
                g2d.drawImage(img, (int) x, (int) y, width, height, null);
            }
            return;
        }
        Image img = currentDirection == Direction.LEFT ? leftImg : rightImg;
        if (img != null) {
            g2d.drawImage(img, (int) x, (int) y, width, height, null);
        } else {
            g2d.setColor(Color.ORANGE);
            g2d.fillRect((int) x, (int) y, width, height);
            g2d.setColor(Color.BLACK);
            g2d.drawRect((int) x, (int) y, width, height);
        }
        drawHpBar(g2d, width, 3);
    }
}
