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

        this.attackCooldown = summonCooldown;
        this.currentDirection = Direction.DOWN;

        // Sử dụng RandomMovement từ package utils
        this.randomMovement = new RandomMovement(
                x, y, width, height, speed, mapHash
        );

        loadImages();
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
        String[] directions = {
                "down", "up", "left", "right"
        };

        for (int d = 0; d < directions.length; d++) {
            for (int frame = 0; frame < 4; frame++) {
                String fileName = directions[d]
                        + "_0" + (frame + 1) + ".png";

                moveImgs[d][frame] = loadImage(fileName);
            }
        }

        for (int i = 0; i < 3; i++) {
            summonImgs[i] = loadImage(
                    "summon_0" + (i + 1) + ".png"
            );
        }
    }

    // ================= DIRECTION =================

    /**
     * Lấy chỉ số mảng tương ứng với hướng hiện tại của RandomMovement.
     * Index 0: DOWN, 1: UP, 2: LEFT, 3: RIGHT
     */
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

                // Chỉ sinh quái sau khi hiệu ứng kết thúc
                spawnMinions();
            }
        }
    }

    // ================= UPDATE =================

    @Override
    public void update() {
        if (!survival) return;

        // Xóa những quái đã chết
        summonedMinions.removeIf(minion -> !minion.isSurvival());

        // ĐANG TRIỆU HỒI:
        // Không di chuyển, không cập nhật RandomMovement
        if (isSummoning) {
            updateSummonAnimation();
            return;
        }

        // Kiểm tra Player và điều kiện sử dụng kỹ năng
        if (isPlayerInDetectionRange() && summonedMinions.isEmpty()) {
            long currentTime = System.currentTimeMillis();

            if (currentTime - lastAttackTime >= attackCooldown) {
                startSummoning();
                return;
            }
        }

        // Lưu vị trí trước khi di chuyển
        double oldX = x;
        double oldY = y;

        // Cập nhật di chuyển bằng RandomMovement
        randomMovement.update(x, y);

        double nextX = randomMovement.getX();
        double nextY = randomMovement.getY();

        // Kiểm tra giới hạn tuần tra
        int nextTileX = (int) (nextX / TILE_SIZE);
        int nextTileY = (int) (nextY / TILE_SIZE);

        boolean insidePatrolArea =
                Math.abs(nextTileX - startTileX) <= MAX_PATROL_TILES
                        && Math.abs(nextTileY - startTileY) <= MAX_PATROL_TILES;

        if (insidePatrolArea) {
            x = nextX;
            y = nextY;
        }

        // Đồng bộ hướng di chuyển từ RandomMovement
        currentDirection = randomMovement.getCurrentDirection();

        // Kiểm tra xem Boss có đang thực sự di chuyển không
        boolean isMoving = Math.abs(x - oldX) > 0.001 || Math.abs(y - oldY) > 0.001;

        updateMoveAnimation(isMoving);
    }

    // ================= START SUMMON =================

    private void startSummoning() {
        if (mapHash == null || mapHash.length == 0) return;

        isSummoning = true;

        summonAnimIndex = 0;
        summonAnimTimer = 0;

        // Reset animation di chuyển
        animIndex = 0;
        animTimer = 0;

        // Đánh dấu thời điểm bắt đầu hồi chiêu
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

        // 4 ô xung quanh Boss
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

            // Chỉ sinh quái ở ô trống
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

        // Đang triệu hồi
        if (isSummoning) {
            currentImg = summonImgs[summonAnimIndex];
        } else {
            int directionIndex = getDirectionIndex();
            currentImg = moveImgs[directionIndex][animIndex];
        }

        // Vẽ Boss
        if (currentImg != null) {
            g2d.drawImage(
                    currentImg,
                    (int) x,
                    (int) y,
                    width,
                    height,
                    null
            );
        } else {
            g2d.setColor(Color.MAGENTA);
            g2d.fillRect((int) x, (int) y, width, height);

            g2d.setColor(Color.YELLOW);
            g2d.drawRect((int) x, (int) y, width, height);
        }

        if (!survival) return;

        // Thanh máu
        int barHeight = 5;
        int barY = (int) y - 8;

        g2d.setColor(Color.DARK_GRAY);
        g2d.fillRect((int) x, barY, width, barHeight);

        if (maxHp > 0) {
            int hpWidth = (int) (
                    width * Math.max(0, Math.min(1, (double) hp / maxHp))
            );

            g2d.setColor(Color.GREEN);
            g2d.fillRect((int) x, barY, hpWidth, barHeight);
        }
    }
}