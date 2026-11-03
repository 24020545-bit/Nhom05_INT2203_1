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

    // Thư mục chứa ảnh Boss
    private static final String IMAGE_PATH = "/Bot/BossBot/";

    private final int[][] mapHash;
    private final double startX;
    private final double startY;

    private final RandomMovement randomMovement;
    private final List<Mob> summonedMinions = new ArrayList<>();

    // Ảnh di chuyển: DOWN, UP, LEFT, RIGHT
    // Mỗi hướng có 4 frame
    private final Image[][] moveImgs = new Image[4][4];

    // 3 frame hiệu ứng triệu hồi
    private final Image[] summonImgs = new Image[3];

    // Animation di chuyển
    private int animIndex = 0;
    private int animTimer = 0;
    private static final int MOVE_ANIM_DELAY = 10;

    // Animation triệu hồi
    private boolean isSummoning = false;
    private int summonAnimIndex = 0;
    private int summonAnimTimer = 0;

    private static final int SUMMON_ANIM_DELAY = 8;

    public BossBot(double x, double y, int width, int height,
                   int speed, int hp, long summonCooldown,
                   int[][] mapHash) {

        super(x, y, width, height, speed, 0, 0, hp, 0);

        this.startX = x;
        this.startY = y;
        this.mapHash = mapHash;

        this.attackCooldown = summonCooldown;
        this.currentDirection = Direction.DOWN;

        this.randomMovement = new RandomMovement(
                x, y, width, height, speed, mapHash
        );

        loadImages();
    }

    // ==================================================
    // LOAD ẢNH
    // ==================================================

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

        // 4 hướng, mỗi hướng 4 frame
        String[] directions = {
                "down", "up", "left", "right"
        };

        for (int direction = 0; direction < directions.length; direction++) {

            for (int frame = 0; frame < 4; frame++) {

                String fileName = directions[direction]
                        + "_0" + (frame + 1) + ".png";

                moveImgs[direction][frame] = loadImage(fileName);
            }
        }

        // Ảnh hiệu ứng triệu hồi
        for (int i = 0; i < 3; i++) {

            String fileName = "summon_0" + (i + 1) + ".png";

            summonImgs[i] = loadImage(fileName);
        }
    }

    // ==================================================
    // LẤY INDEX THEO HƯỚNG DI CHUYỂN
    // ==================================================

    private int getDirectionIndex() {

        switch (currentDirection) {

            case DOWN:
                return 0;

            case UP:
                return 1;

            case LEFT:
                return 2;

            case RIGHT:
                return 3;

            default:
                return 0;
        }
    }

    // ==================================================
    // CẬP NHẬT ANIMATION DI CHUYỂN
    // ==================================================

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

    // ==================================================
    // CẬP NHẬT ANIMATION TRIỆU HỒI
    // ==================================================

    private void updateSummonAnimation() {

        if (!isSummoning) return;

        summonAnimTimer++;

        if (summonAnimTimer >= SUMMON_ANIM_DELAY) {

            summonAnimIndex++;
            summonAnimTimer = 0;

            // Đã chạy hết 3 frame
            if (summonAnimIndex >= summonImgs.length) {

                isSummoning = false;
                summonAnimIndex = 0;
                summonAnimTimer = 0;

                animIndex = 0;
                animTimer = 0;
            }
        }
    }

    // ==================================================
    // UPDATE
    // ==================================================

    @Override
    public void update() {

        if (!survival) return;

        // Xóa những quái đã chết khỏi danh sách
        summonedMinions.removeIf(
                minion -> !minion.isSurvival()
        );

        // Nếu đang triệu hồi thì không di chuyển
        if (isSummoning) {

            updateSummonAnimation();
            return;
        }

        // Kiểm tra khoảng cách với Player
        if (isPlayerInDetectionRange()
                && summonedMinions.isEmpty()) {

            long currentTime = System.currentTimeMillis();

            if (currentTime - lastAttackTime >= attackCooldown) {

                summonMinions();

                lastAttackTime = currentTime;

                if (isSummoning) {
                    updateSummonAnimation();
                    return;
                }
            }
        }

        // Lưu vị trí cũ
        double oldX = x;
        double oldY = y;

        // Cập nhật di chuyển
        randomMovement.update(x, y);

        double nextX = randomMovement.getX();
        double nextY = randomMovement.getY();

        int originTileX = (int) (startX / TILE_SIZE);
        int originTileY = (int) (startY / TILE_SIZE);

        int nextTileX = (int) (nextX / TILE_SIZE);
        int nextTileY = (int) (nextY / TILE_SIZE);

        // Giới hạn vùng tuần tra
        if (Math.abs(nextTileX - originTileX) <= MAX_PATROL_TILES
                && Math.abs(nextTileY - originTileY) <= MAX_PATROL_TILES) {

            x = nextX;
            y = nextY;
        }

        // Cập nhật hướng
        currentDirection = randomMovement.getCurrentDirection();

        // Kiểm tra Boss có đang di chuyển không
        boolean isMoving = (x != oldX || y != oldY);

        // Cập nhật animation theo hướng
        updateMoveAnimation(isMoving);
    }

    // ==================================================
    // PHÁT HIỆN PLAYER
    // ==================================================

    private boolean isPlayerInDetectionRange() {

        if (mapHash == null) return false;

        int bossTileX = (int) (x / TILE_SIZE);
        int bossTileY = (int) (y / TILE_SIZE);

        for (Mob mob : Mobs.MOBS) {

            if (mob instanceof Player && mob.isSurvival()) {

                int playerTileX = (int) (mob.getX() / TILE_SIZE);
                int playerTileY = (int) (mob.getY() / TILE_SIZE);

                int distanceX = Math.abs(bossTileX - playerTileX);
                int distanceY = Math.abs(bossTileY - playerTileY);

                if (distanceX <= DETECT_RADIUS_TILES
                        && distanceY <= DETECT_RADIUS_TILES) {

                    return true;
                }
            }
        }

        return false;
    }

    // ==================================================
    // TRIỆU HỒI QUÁI
    // ==================================================

    private void summonMinions() {

        if (mapHash == null || mapHash.length == 0) return;

        Player[] activePlayers = Mobs.MOBS.stream()

                .filter(m -> m instanceof Player && m.isSurvival())

                .map(m -> (Player) m)

                .toArray(Player[]::new);

        if (activePlayers.length == 0) return;

        // Bắt đầu hiệu ứng triệu hồi
        isSummoning = true;
        summonAnimIndex = 0;
        summonAnimTimer = 0;

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

            if (spawnTileY < 0 || spawnTileY >= mapHash.length
                    || spawnTileX < 0
                    || spawnTileX >= mapHash[spawnTileY].length) {

                continue;
            }

            // Chỉ triệu hồi ở ô trống
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

    // ==================================================
    // MOVE
    // ==================================================

    @Override
    public void move(Direction direction) {

        if (isSummoning) return;

        currentDirection = direction;
    }

    // ==================================================
    // DRAW
    // ==================================================

    @Override
    public void draw(Graphics2D g2d) {

        if (!survival && !deading) return;

        Image currentImg;

        // Đang sử dụng kỹ năng triệu hồi
        if (isSummoning) {

            currentImg = summonImgs[summonAnimIndex];

        } else {

            // Animation di chuyển theo hướng
            int directionIndex = getDirectionIndex();

            currentImg = moveImgs[directionIndex][animIndex];
        }

        // Vẽ ảnh Boss
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

            // Ảnh dự phòng khi không load được
            g2d.setColor(Color.MAGENTA);
            g2d.fillRect((int) x, (int) y, width, height);

            g2d.setColor(Color.YELLOW);
            g2d.drawRect((int) x, (int) y, width, height);
        }

        // Không vẽ thanh máu nếu Boss đã chết
        if (!survival) return;

        // Thanh máu Boss
        int barHeight = 5;
        int barY = (int) y - 8; // Đã sửa lỗi thiếu dấu trừ

        g2d.setColor(Color.DARK_GRAY);

        g2d.fillRect(
                (int) x,
                barY,
                width,
                barHeight
        );

        if (maxHp > 0) {

            int hpWidth = (int) (
                    width * Math.max( // Đã sửa lỗi thiếu dấu nhân (*)
                            0,
                            Math.min(1, (double) hp / maxHp)
                    )
            );

            g2d.setColor(Color.GREEN);

            g2d.fillRect(
                    (int) x,
                    barY,
                    hpWidth,
                    barHeight
            );
        }
    }
}