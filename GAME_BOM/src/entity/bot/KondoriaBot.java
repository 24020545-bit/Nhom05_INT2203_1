
package entity.bot;

import entity.Player;
import entity.mob.Mob;
import utils.Direction;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import javax.swing.ImageIcon;

public class KondoriaBot extends Mob {

    private static final int TILE_SIZE = 32;
    private static final int CHASE_RADIUS = 4;
    private static final double EPSILON = 0.01;

    private final Player[] players;
    private final int[][] mapHash;
    private final Random random = new Random();

    private Player currentTarget;

    // Hướng đi lang thang
    private Direction wanderDir = Direction.LEFT;

    // Vị trí ô đích hiện tại
    private int targetCol = -1;
    private int targetRow = -1;
    private boolean moving = false;

    // BFS thoát khỏi tường
    private boolean escapingWall = false;
    private final List<int[]> escapePath = new ArrayList<>();
    private int escapeIndex = 0;

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

    // Tọa độ X của tâm một ô
    private double getCellX(int col) {
        return col * TILE_SIZE + (TILE_SIZE - width) / 2.0;
    }

    // Tọa độ Y của tâm một ô
    private double getCellY(int row) {
        return row * TILE_SIZE + (TILE_SIZE - height) / 2.0;
    }

    // Lấy cột hiện tại
    private int getCurrentCol() {
        return (int) Math.round(
                (x + width / 2.0) / TILE_SIZE - 0.5
        );
    }

    // Lấy hàng hiện tại
    private int getCurrentRow() {
        return (int) Math.round(
                (y + height / 2.0) / TILE_SIZE - 0.5
        );
    }

    private boolean isAtCellCenter() {
        double centerX = getCellX(getCurrentCol());
        double centerY = getCellY(getCurrentRow());

        return Math.abs(x - centerX) < EPSILON
                && Math.abs(y - centerY) < EPSILON;
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

        // Nếu đang di chuyển đến một ô thì phải đi hết ô đó
        if (moving) {
            boolean finished = moveToTargetCell(true);

            if (finished) {
                moving = false;
            }

            updateAnimation();
            return;
        }

        // Có Player: truy đuổi
        if (currentTarget != null) {
            escapingWall = false;
            escapePath.clear();
            escapeIndex = 0;

            chasePlayer();
        }

        // Không có Player
        else {
            // Nếu đang ở trong tường thì phải thoát ra
            if (!canMoveOnFloor(x, y)) {
                escapingWall = true;

                if (escapePath.isEmpty()) {
                    findEscapePath();
                }

                escapeToFloor();
            }

            // Nếu đang ở sàn thì đi lang thang
            else {
                escapingWall = false;
                escapePath.clear();
                escapeIndex = 0;

                randomMove();
            }
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

            int playerCol = (int) (
                    (player.getX() + player.getWidth() / 2.0)
                            / TILE_SIZE
            );

            int playerRow = (int) (
                    (player.getY() + player.getHeight() / 2.0)
                            / TILE_SIZE
            );

            int dx = playerCol - botCol;
            int dy = playerRow - botRow;

            if (Math.abs(dx) <= CHASE_RADIUS
                    && Math.abs(dy) <= CHASE_RADIUS) {

                double distance = dx * dx + dy * dy;

                if (distance < minDistance) {
                    minDistance = distance;
                    currentTarget = player;
                }
            }
        }
    }

    // ==================== CHASE PLAYER ====================

    private void chasePlayer() {
        if (currentTarget == null) return;

        int botCol = getCurrentCol();
        int botRow = getCurrentRow();

        int playerCol = (int) (
                (currentTarget.getX()
                        + currentTarget.getWidth() / 2.0)
                        / TILE_SIZE
        );

        int playerRow = (int) (
                (currentTarget.getY()
                        + currentTarget.getHeight() / 2.0)
                        / TILE_SIZE
        );

        int dx = playerCol - botCol;
        int dy = playerRow - botRow;

        if (dx == 0 && dy == 0) return;

        // Chọn trục có khoảng cách lớn hơn
        if (Math.abs(dx) >= Math.abs(dy) && dx != 0) {
            setTargetCell(
                    botCol + (dx > 0 ? 1 : -1),
                    botRow
            );
        } else {
            setTargetCell(
                    botCol,
                    botRow + (dy > 0 ? 1 : -1)
            );
        }

        moveToTargetCell(true);
    }

    // ==================== RANDOM MOVEMENT ====================

    private void randomMove() {
        Direction[] directions = {
                Direction.UP,
                Direction.DOWN,
                Direction.LEFT,
                Direction.RIGHT
        };

        // Xáo trộn thứ tự thử hướng
        for (int i = directions.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);

            Direction temp = directions[i];
            directions[i] = directions[j];
            directions[j] = temp;
        }

        for (Direction direction : directions) {
            int col = getCurrentCol();
            int row = getCurrentRow();

            switch (direction) {
                case LEFT:
                    col--;
                    break;
                case RIGHT:
                    col++;
                    break;
                case UP:
                    row--;
                    break;
                case DOWN:
                    row++;
                    break;
                default:
                    continue;
            }

            double nextX = getCellX(col);
            double nextY = getCellY(row);

            if (canMoveOnFloor(nextX, nextY)) {
                wanderDir = direction;
                setTargetCell(col, row);
                moveToTargetCell(false);
                return;
            }
        }
    }

    // ==================== SET GRID TARGET ====================

    private void setTargetCell(int col, int row) {
        targetCol = col;
        targetRow = row;
        moving = true;
    }

    // Di chuyển đến tâm ô đích, chỉ theo một trục tại một thời điểm
    private boolean moveToTargetCell(boolean ignoreWalls) {
        if (targetCol < 0 || targetRow < 0) return true;

        double targetX = getCellX(targetCol);
        double targetY = getCellY(targetRow);

        // Ưu tiên di chuyển trục X
        if (Math.abs(targetX - x) > EPSILON) {
            double dx = targetX - x;

            double step = Math.signum(dx)
                    * Math.min(speed, Math.abs(dx));

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

            currentDirection = dx < 0
                    ? Direction.LEFT : Direction.RIGHT;

            return false;
        }

        // Sau khi X đã đến đích mới di chuyển Y
        if (Math.abs(targetY - y) > EPSILON) {
            double dy = targetY - y;

            double step = Math.signum(dy)
                    * Math.min(speed, Math.abs(dy));

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

            currentDirection = dy < 0
                    ? Direction.UP : Direction.DOWN;

            return false;
        }

        // Đã đến tâm ô
        x = targetX;
        y = targetY;

        moving = false;
        return true;
    }

    // ==================== CHECK FLOOR ====================

    private boolean canMoveOnFloor(double nextX, double nextY) {
        if (!isValidMap()) return false;

        int left = (int) Math.floor(nextX / TILE_SIZE);
        int right = (int) Math.floor(
                (nextX + width - EPSILON) / TILE_SIZE
        );

        int top = (int) Math.floor(nextY / TILE_SIZE);
        int bottom = (int) Math.floor(
                (nextY + height - EPSILON) / TILE_SIZE
        );

        int maxRow = mapHash.length - 1;
        int maxCol = mapHash[0].length - 1;

        if (left < 1 || top < 1
                || right >= maxCol || bottom >= maxRow) {
            return false;
        }

        for (int row = top; row <= bottom; row++) {
            for (int col = left; col <= right; col++) {
                if (mapHash[row][col] != 0) {
                    return false;
                }
            }
        }

        return true;
    }

    // Chỉ kiểm tra biên bản đồ, cho phép xuyên tường
    private boolean canMove(double nextX, double nextY) {
        if (!isValidMap()) return false;

        int left = (int) Math.floor(nextX / TILE_SIZE);
        int right = (int) Math.floor(
                (nextX + width - EPSILON) / TILE_SIZE
        );

        int top = (int) Math.floor(nextY / TILE_SIZE);
        int bottom = (int) Math.floor(
                (nextY + height - EPSILON) / TILE_SIZE
        );

        return left >= 1 && top >= 1
                && right < mapHash[0].length - 1
                && bottom < mapHash.length - 1;
    }

    private boolean isValidMap() {
        return mapHash != null
                && mapHash.length > 0
                && mapHash[0] != null
                && mapHash[0].length > 0;
    }

    // ==================== BFS ESCAPE ====================

    private void findEscapePath() {
        escapePath.clear();
        escapeIndex = 0;

        if (!isValidMap()) return;

        int rows = mapHash.length;
        int cols = mapHash[0].length;

        int startCol = getCurrentCol();
        int startRow = getCurrentRow();

        if (startCol < 1 || startRow < 1
                || startCol >= cols - 1
                || startRow >= rows - 1) {
            return;
        }

        boolean[][] visited = new boolean[rows][cols];

        int[][] parentCol = new int[rows][cols];
        int[][] parentRow = new int[rows][cols];

        Queue<int[]> queue = new LinkedList<>();

        queue.add(new int[]{startCol, startRow});
        visited[startRow][startCol] = true;

        int[][] directions = {
                {0, -1},
                {0, 1},
                {-1, 0},
                {1, 0}
        };

        int endCol = -1;
        int endRow = -1;

        while (!queue.isEmpty()) {
            int[] cell = queue.poll();

            int col = cell[0];
            int row = cell[1];

            double floorX = getCellX(col);
            double floorY = getCellY(row);

            // Tìm ô sàn mà toàn bộ hitbox nằm vừa
            if (mapHash[row][col] == 0
                    && canMoveOnFloor(floorX, floorY)) {

                endCol = col;
                endRow = row;
                break;
            }

            for (int[] dir : directions) {
                int nextCol = col + dir[0];
                int nextRow = row + dir[1];

                if (nextCol < 1 || nextRow < 1
                        || nextCol >= cols - 1
                        || nextRow >= rows - 1
                        || visited[nextRow][nextCol]) {
                    continue;
                }

                visited[nextRow][nextCol] = true;

                parentCol[nextRow][nextCol] = col;
                parentRow[nextRow][nextCol] = row;

                queue.add(new int[]{nextCol, nextRow});
            }
        }

        if (endCol == -1) return;

        // Truy ngược đường đi từ đích về vị trí ban đầu
        int col = endCol;
        int row = endRow;

        while (col != startCol || row != startRow) {
            escapePath.add(new int[]{col, row});

            int previousCol = parentCol[row][col];
            int previousRow = parentRow[row][col];

            col = previousCol;
            row = previousRow;
        }

        Collections.reverse(escapePath);
    }

    // ==================== ESCAPE WALL ====================

    private void escapeToFloor() {
        if (escapePath.isEmpty()) {
            if (canMoveOnFloor(x, y)) {
                finishEscape();
            } else {
                findEscapePath();
            }
            return;
        }

        if (escapeIndex >= escapePath.size()) {
            if (canMoveOnFloor(x, y)) {
                finishEscape();
            } else {
                findEscapePath();
            }
            return;
        }

        int[] nextCell = escapePath.get(escapeIndex);

        setTargetCell(nextCell[0], nextCell[1]);

        // Đang thoát tường nên bỏ qua kiểm tra gạch và tường
        moveToTargetCell(true);

        if (!moving) {
            if (canMoveOnFloor(x, y)) {
                finishEscape();
            } else {
                escapeIndex++;

                if (escapeIndex >= escapePath.size()) {
                    escapePath.clear();
                    escapeIndex = 0;
                    findEscapePath();
                }
            }
        }
    }

    private void finishEscape() {
        escapingWall = false;
        escapePath.clear();
        escapeIndex = 0;
        moving = false;

        targetCol = -1;
        targetRow = -1;
    }

    // ==================== BOUNDARY ====================

    private void clampToBounds() {
        if (!isValidMap()) return;

        double minX = TILE_SIZE;
        double minY = TILE_SIZE;

        double maxX = (mapHash[0].length - 1)
                * TILE_SIZE - width;

        double maxY = (mapHash.length - 1)
                * TILE_SIZE - height;

        x = Math.max(minX, Math.min(x, maxX));
        y = Math.max(minY, Math.min(y, maxY));
    }

    // ==================== MOVE ====================

    @Override
    public void move(Direction direction) {
        currentDirection = direction;
    }

    // ==================== DRAW ====================

    @Override
    public void draw(Graphics2D g2d) {
        if (!survival && !deading) return;

        if (!survival) {
            Image img = deadImgs[animIndex];

            if (img != null) {
                g2d.drawImage(
                        img, (int) x, (int) y,
                        width, height, null
                );
            }

            return;
        }

        Image img = currentDirection == Direction.LEFT
                ? leftImg : rightImg;

        if (img != null) {
            g2d.drawImage(
                    img, (int) x, (int) y,
                    width, height, null
            );
        } else {
            g2d.setColor(Color.ORANGE);
            g2d.fillRect((int) x, (int) y, width, height);

            g2d.setColor(Color.BLACK);
            g2d.drawRect((int) x, (int) y, width, height);
        }
    }
}
