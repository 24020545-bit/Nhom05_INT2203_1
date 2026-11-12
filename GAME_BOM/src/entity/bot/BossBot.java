package entity.bot;

import entity.Player;
import entity.mob.Mob;
import entity.mob.Mobs;
import utils.Direction;
import utils.RandomMovement;

import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Image;
import javax.swing.ImageIcon;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class BossBot extends Mob {

    private static final int TILE_SIZE = 32;
    private static final int MAX_PATROL_TILES = 6;
    private static final int DETECT_RADIUS_TILES = 6;

    private static final String IMAGE_PATH = "/Bot/BossBot/";

    private final int[][] mapHash;
    private final int[][] patrolMapHash; // Bản đồ riêng có tường bao quanh vùng tuần tra
    private final int startTileX;
    private final int startTileY;

    private final RandomMovement randomMovement;
    private final List<Mob> summonedMinions = new ArrayList<>();

    // 4 hướng, mỗi hướng 4 frame (0: DOWN, 1: UP, 2: LEFT, 3: RIGHT)
    private final Image[][] moveImgs = new Image[4][4];

    // 3 frame triệu hồi
    private final Image[] summonImgs = new Image[3];

    // Animation di chuyển
    private int animIndex = 0;
    private int animTimer = 0;
    private static final int MOVE_ANIM_DELAY = 10;

    // Animation triệu hồi
    private boolean isSummoning = false;
    private int summonAnimIndex = 0;
    private int summonAnimTimer = 0;
    private static final int SUMMON_ANIM_DELAY = 16;

    public BossBot(double x, double y, int width, int height,
                   int speed, int hp, long summonCooldown,
                   int[][] mapHash) {

        super(x, y, width, height, speed, 0, 0, hp, 0);

        this.mapHash = mapHash;
        this.startTileX = (int) (x / TILE_SIZE);
        this.startTileY = (int) (y / TILE_SIZE);

        this.patrolMapHash = createPatrolMap();
        syncPatrolMap();

        this.attackCooldown = summonCooldown;
        this.currentDirection = Direction.DOWN;

        // Truyền patrolMapHash để RandomMovement tự đổi hướng khi chạm biên tuần tra
        this.randomMovement = new RandomMovement(
                x, y, width, height, speed, patrolMapHash
        );

        loadImages();
    }

    // ================= PATROL MAP SYNC =================

    private int[][] createPatrolMap() {
        if (mapHash == null || mapHash.length == 0) return new int[0][0];
        return new int[mapHash.length][mapHash[0].length];
    }

    /**
     * Đồng bộ trạng thái bản đồ trong vùng tuần tra,
     * đồng thời khóa cứng (gán = 2) mọi ô nằm ngoài MAX_PATROL_TILES
     */
    private void syncPatrolMap() {
        if (mapHash == null || patrolMapHash.length == 0) return;

        for (int r = 0; r < mapHash.length; r++) {
            for (int c = 0; c < mapHash[r].length; c++) {
                boolean insidePatrol =
                        Math.abs(c - startTileX) <= MAX_PATROL_TILES
                                && Math.abs(r - startTileY) <= MAX_PATROL_TILES;

                if (insidePatrol) {
                    patrolMapHash[r][c] = mapHash[r][c];
                } else {
                    patrolMapHash[r][c] = 2; // Coi như tường cứng để Boss tự quay đầu
                }
            }
        }
    }

    // ================= LOAD IMAGE =================

    private Image loadImage(String fileName) {
        String path = IMAGE_PATH + fileName;
        URL imageURL = getClass().getResource(path);

        if (imageURL == null) {
            System.err.println("Không tìm thấy ảnh BossBot: " + path);
            return null;
        }

        return new ImageIcon(imageURL).getImage();
    }

    private void loadImages() {
        String[] directions = {"down", "up", "left", "right"};

        for (int d = 0; d < directions.length; d++) {
            for (int frame = 0; frame < 4; frame++) {
                String fileName = directions[d] + "_0" + (frame + 1) + ".png";
                moveImgs[d][frame] = loadImage(fileName);
            }
        }

        for (int i = 0; i < 3; i++) {
            summonImgs[i] = loadImage("summon_0" + (i + 1) + ".png");
        }
    }

    // ================= DIRECTION =================

    private int getDirectionIndex() {
        Direction dir = (randomMovement != null) ? randomMovement.getCurrentDirection() : currentDirection;
        if (dir == null) return 0;

        return switch (dir) {
            case DOWN -> 0;
            case UP -> 1;
            case LEFT -> 2;
            case RIGHT -> 3;
        };
    }

    // ================= MOVE ANIMATION =================

    private void updateMoveAnimation(boolean isMoving) {
        if (!isMoving) {
            animIndex = 0;
            animTimer = 0;
            return;
        }

        animTimer++;

        if (animTimer >= MOVE_ANIM_DELAY) {
            animIndex = (animIndex + 1) % 4;
            animTimer = 0;
        }
    }

    // ================= SUMMON ANIMATION =================

    private void updateSummonAnimation() {
        if (!isSummoning) return;

        summonAnimTimer++;

        if (summonAnimTimer >= SUMMON_ANIM_DELAY) {
            summonAnimIndex++;
            summonAnimTimer = 0;

            if (summonAnimIndex >= summonImgs.length) {
                isSummoning = false;
                summonAnimIndex = 0;
                summonAnimTimer = 0;
                animIndex = 0;
                animTimer = 0;

                spawnMinions();
            }
        }
    }

    // ================= UPDATE =================

    @Override
    public void update() {
        if (!survival) return;

        summonedMinions.removeIf(minion -> !minion.isSurvival());

        if (isSummoning) {
            updateSummonAnimation();
            return;
        }

        if (isPlayerInDetectionRange() && summonedMinions.isEmpty()) {
            long currentTime = System.currentTimeMillis();

            if (currentTime - lastAttackTime >= attackCooldown) {
                startSummoning();
                return;
            }
        }

        // Cập nhật map tuần tra (để nhận biết bom mới đặt hoặc gạch vừa bị phá)
        syncPatrolMap();

        double oldX = x;
        double oldY = y;

        randomMovement.update(x, y);

        x = randomMovement.getX();
        y = randomMovement.getY();
        currentDirection = randomMovement.getCurrentDirection();

        boolean isMoving = Math.abs(x - oldX) > 0.001 || Math.abs(y - oldY) > 0.001;
        updateMoveAnimation(isMoving);
    }

    // ================= START SUMMON =================

    private void startSummoning() {
        if (mapHash == null || mapHash.length == 0) return;

        isSummoning = true;
        summonAnimIndex = 0;
        summonAnimTimer = 0;
        animIndex = 0;
        animTimer = 0;
        lastAttackTime = System.currentTimeMillis();
    }

    // ================= DETECT PLAYER =================

    private boolean isPlayerInDetectionRange() {
        if (mapHash == null || Mobs.MOBS == null) {
            return false;
        }

        int bossTileX = (int) (x / TILE_SIZE);
        int bossTileY = (int) (y / TILE_SIZE);

        for (Mob mob : Mobs.MOBS) {
            if (!(mob instanceof Player) || !mob.isSurvival()) {
                continue;
            }

            int playerTileX = (int) (mob.getX() / TILE_SIZE);
            int playerTileY = (int) (mob.getY() / TILE_SIZE);

            int distanceX = Math.abs(bossTileX - playerTileX);
            int distanceY = Math.abs(bossTileY - playerTileY);

            if (distanceX <= DETECT_RADIUS_TILES && distanceY <= DETECT_RADIUS_TILES) {
                return true;
            }
        }

        return false;
    }

    // ================= SUMMON =================

    private void spawnMinions() {
        if (mapHash == null || mapHash.length == 0) return;

        Player[] activePlayers = Mobs.MOBS.stream()
                .filter(m -> m instanceof Player && m.isSurvival())
                .map(m -> (Player) m)
                .toArray(Player[]::new);

        if (activePlayers.length == 0) return;

        int[][] offsets = {
                {1, 0},
                {-1, 0},
                {0, -1},
                {0, 1}
        };

        int currentTileX = (int) (x / TILE_SIZE);
        int currentTileY = (int) (y / TILE_SIZE);

        for (int i = 0; i < offsets.length; i++) {
            int spawnTileX = currentTileX + offsets[i][0];
            int spawnTileY = currentTileY + offsets[i][1];

            if (spawnTileY < 0
                    || spawnTileY >= mapHash.length
                    || spawnTileX < 0
                    || spawnTileX >= mapHash[spawnTileY].length) {
                continue;
            }

            if (mapHash[spawnTileY][spawnTileX] != 0) {
                continue;
            }

            double spawnX = spawnTileX * TILE_SIZE;
            double spawnY = spawnTileY * TILE_SIZE;

            Mob minion;

            if (i < 2) {
                minion = new KondoriaBot(
                        spawnX, spawnY,
                        TILE_SIZE, TILE_SIZE,
                        1, 3, 1, 1000,
                        activePlayers, mapHash
                );
            } else {
                minion = new DollBot(
                        spawnX, spawnY,
                        TILE_SIZE, TILE_SIZE,
                        1, 3, 1, 1000,
                        activePlayers, mapHash
                );
            }

            summonedMinions.add(minion);
            Mobs.add(minion);
        }
    }

    // ================= MOVE =================

    @Override
    public void move(Direction direction) {
        if (isSummoning) return;
        currentDirection = direction;
    }

    // ================= DRAW =================

    @Override
    public void draw(Graphics2D g2d) {
        if (!survival && !deading) return;

        Image currentImg;

        if (isSummoning) {
            currentImg = summonImgs[summonAnimIndex];
        } else {
            int directionIndex = getDirectionIndex();
            currentImg = moveImgs[directionIndex][animIndex];
        }

        if (currentImg != null) {
            g2d.drawImage(currentImg, (int) x, (int) y, width, height, null);
        } else {
            g2d.setColor(Color.MAGENTA);
            g2d.fillRect((int) x, (int) y, width, height);

            g2d.setColor(Color.YELLOW);
            g2d.drawRect((int) x, (int) y, width, height);
        }

        if (!survival) return;

        int barHeight = 5;
        int barY = (int) y - 8;

        g2d.setColor(Color.DARK_GRAY);
        g2d.fillRect((int) x, barY, width, barHeight);

        if (maxHp > 0) {
            int hpWidth = (int) (width * Math.max(0, Math.min(1, (double) hp / maxHp)));
            g2d.setColor(Color.GREEN);
            g2d.fillRect((int) x, barY, hpWidth, barHeight);
        }
    }
}