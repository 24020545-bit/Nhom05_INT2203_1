package entity.bot;

import bomb.Bomb;
import entity.Player;
import entity.mob.Mob;
import utils.BFS;
import utils.Direction;
import utils.RandomMovement;

import java.util.List;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Image;
import javax.swing.ImageIcon;

public class DollBot extends Mob {

    private final Player[] players;
    private final int[][] mapHash;
    private static final int TILE_SIZE = 32;
    private static final int CHASE_RADIUS = 4;
    private static final double EPSILON = 0.01;

    private final RandomMovement randomMovement;

    private Player currentTarget;

    private int targetCol = -1;
    private int targetRow = -1;
    private boolean moving = false;

    private Image leftImg;
    private Image rightImg;
    private Image dameImg;
    private final Image[] deadImgs = new Image[2];

    private int animIndex = 0;
    private int animTimer = 0;

    public DollBot(double x, double y, int width, int height,
                   int speed, int hp, int attackPower,
                   long attackCooldown, Player[] players,
                   int[][] mapHash) {

        super(x, y, width, height, speed, 0, 0,
                hp, attackPower);

        this.players = players;
        this.mapHash = mapHash;
        this.attackCooldown = attackCooldown;

        this.randomMovement = new RandomMovement(
                x, y, width, height, speed, mapHash
        );

        this.currentDirection =
                randomMovement.getCurrentDirection();

        loadImages();
    }

    public DollBot(double x, double y, int width, int height,
                   int speed, int hp, int attackPower,
                   long attackCooldown, Player targetPlayer,
                   int[][] mapHash) {

        this(x, y, width, height, speed, hp, attackPower,
                attackCooldown,
                targetPlayer != null
                        ? new Player[]{targetPlayer} : null,
                mapHash);
    }

    private void loadImages() {
        try {
            var l = getClass().getResource(
                    "/Bot/DollBot/left.png");

            var r = getClass().getResource(
                    "/Bot/DollBot/right.png");

            var d = getClass().getResource(
                    "/Bot/DollBot/dame.png");

            var dead1 = getClass().getResource(
                    "/Bot/DollBot/dead1.png");

            var dead2 = getClass().getResource(
                    "/Bot/DollBot/dead2.png");

            leftImg = l != null
                    ? new ImageIcon(l).getImage() : null;

            rightImg = r != null
                    ? new ImageIcon(r).getImage() : null;

            dameImg = d != null
                    ? new ImageIcon(d).getImage() : null;

            deadImgs[0] = dead1 != null
                    ? new ImageIcon(dead1).getImage() : null;

            deadImgs[1] = dead2 != null
                    ? new ImageIcon(dead2).getImage() : null;

        } catch (Exception e) {
            System.err.println(
                    "Lỗi khi tải ảnh DollBot: " + e.getMessage());
        }
    }

    @Override
    public void update() {
        if (!survival) return;

        attackPlayer();

        if (moving) {
            moveToTargetCell();
        } else {
            if (!chasePlayer()) {
                randomMove();
            }
        }

        animTimer++;

        if (animTimer > 10) {
            animIndex++;
            animTimer = 0;
        }
    }

    private void attackPlayer() {
        if (players == null) return;

        long currentTime = System.currentTimeMillis();

        for (Player player : players) {
            if (player == null || !player.isSurvival())
                continue;

            if (getBounds().intersects(player.getBounds())
                    && currentTime - lastAttackTime
                    >= attackCooldown) {

                player.takeDamage(attackPower);
                lastAttackTime = currentTime;
            }
        }
    }

    private boolean chasePlayer() {
        currentTarget = null;

        if (players == null || mapHash == null
                || mapHash.length == 0
                || mapHash[0].length == 0) {
            return false;
        }

        int startCol = getCurrentCol();
        int startRow = getCurrentRow();

        if (!insideMap(startCol, startRow))
            return false;

        List<int[]> bestPath = null;

        for (Player player : players) {
            if (player == null || !player.isSurvival())
                continue;

            int endCol = (int) ((player.getX()
                    + player.getWidth() / 2.0) / TILE_SIZE);

            int endRow = (int) ((player.getY()
                    + player.getHeight() / 2.0) / TILE_SIZE);

            if (!insideMap(endCol, endRow))
                continue;

            if (Math.abs(endCol - startCol) > CHASE_RADIUS
                    || Math.abs(endRow - startRow)
                    > CHASE_RADIUS) {
                continue;
            }

            List<int[]> path = BFS.findPath(
                    mapHash,
                    startCol,
                    startRow,
                    endCol,
                    endRow,
                    this::canVisit
            );

            if (path.isEmpty()) continue;

            if (bestPath == null
                    || path.size() < bestPath.size()) {

                bestPath = path;
                currentTarget = player;
            }
        }

        if (bestPath == null) return false;

        if (bestPath.size() == 1)
            return true;

        double gridX = startCol * TILE_SIZE;
        double gridY = startRow * TILE_SIZE;

        if (Math.abs(x - gridX) > EPSILON
                || Math.abs(y - gridY) > EPSILON) {

            if (!randomMovement.isValidPosition(gridX, gridY))
                return false;

            targetCol = startCol;
            targetRow = startRow;

        } else {
            int[] nextCell = bestPath.get(1);

            targetCol = nextCell[0];
            targetRow = nextCell[1];
        }

        moving = true;
        moveToTargetCell();

        return true;
    }

    private void moveToTargetCell() {
        double targetX = targetCol * TILE_SIZE;
        double targetY = targetRow * TILE_SIZE;

        double dx = targetX - x;
        double dy = targetY - y;

        double nextX = x;
        double nextY = y;

        if (Math.abs(dx) > EPSILON) {

            nextX += Math.signum(dx)
                    * Math.min(speed, Math.abs(dx));

            currentDirection = dx > 0
                    ? Direction.RIGHT : Direction.LEFT;

        } else if (Math.abs(dy) > EPSILON) {

            nextY += Math.signum(dy)
                    * Math.min(speed, Math.abs(dy));

            currentDirection = dy > 0
                    ? Direction.DOWN : Direction.UP;

        } else {
            moving = false;
            return;
        }

        if (!randomMovement.isValidPosition(nextX, nextY)) {
            moving = false;
            return;
        }

        x = nextX;
        y = nextY;

        if (Math.abs(x - targetX) <= EPSILON
                && Math.abs(y - targetY) <= EPSILON) {

            x = targetX;
            y = targetY;

            moving = false;
        }
    }

    private boolean canVisit(int col, int row) {
        if (!insideMap(col, row)) return false;

        int value = mapHash[row][col];

        return value == 0
                || value == Bomb.TILE_BOMB_PLAYER_ON
                || value == Bomb.TILE_BOMB_SOLID;
    }

    private boolean insideMap(int col, int row) {
        return row >= 1
                && row < mapHash.length - 1
                && col >= 1
                && col < mapHash[0].length - 1;
    }

    private int getCurrentCol() {
        return (int) ((x + width / 2.0) / TILE_SIZE);
    }

    private int getCurrentRow() {
        return (int) ((y + height / 2.0) / TILE_SIZE);
    }

    private void randomMove() {
        randomMovement.update(x, y);

        x = randomMovement.getX();
        y = randomMovement.getY();

        currentDirection =
                randomMovement.getCurrentDirection();
    }

    @Override
    public void move(Direction direction) {
        this.currentDirection = direction;
    }

    @Override
    public void draw(Graphics2D g2d) {
        if (!survival && !deading) return;

        if (!survival) {
            Image deadImg =
                    deadImgs[animIndex % deadImgs.length];

            if (deadImg != null) {
                g2d.drawImage(deadImg, (int) x, (int) y,
                        width, height, null);
            }

            return;
        }

        Image currentImg;

        if (currentDirection == Direction.LEFT) {
            currentImg = leftImg != null
                    ? leftImg : rightImg;
        } else {
            currentImg = rightImg != null
                    ? rightImg : leftImg;
        }

        if (currentImg != null) {
            g2d.drawImage(currentImg, (int) x, (int) y,
                    width, height, null);
        } else {
            g2d.setColor(new Color(219, 112, 147));
            g2d.fillRect((int) x, (int) y, width, height);

            g2d.setColor(Color.BLACK);
            g2d.drawRect((int) x, (int) y, width, height);
        }

        drawHpBar(g2d, width, 3);
    }
}