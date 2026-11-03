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
import java.util.ArrayList;
import java.util.List;

public class BossBot extends Mob {

    private static final int TILE_SIZE = 32;
    private static final int MAX_PATROL_TILES = 6;
    private static final int DETECT_RADIUS_TILES = 6;

    private final int[][] mapHash;
    private final double startX;
    private final double startY;

    private final RandomMovement randomMovement;
    private final List<Mob> summonedMinions = new ArrayList<>();

    // --- MẢNG LƯU 6 ẢNH HOẠT ẢNH ---
    private Image[] aliveImgs = new Image[6];
    private int animIndex = 0;
    private int animTimer = 0;

    public BossBot(double x, double y, int width, int height,
                   int speed, int hp, long summonCooldown, int[][] mapHash) {

        super(x, y, width, height, speed, 0, 0, hp, 0);

        this.startX = x;
        this.startY = y;
        this.mapHash = mapHash;
        this.attackCooldown = summonCooldown;
        this.currentDirection = Direction.DOWN;

        this.randomMovement = new RandomMovement(
                x, y, width, height, speed, mapHash
        );

        // Gọi hàm load ảnh
        loadImages();
    }

    // --- HÀM LOAD 6 ẢNH VÀO MẢNG ---
    private void loadImages() {
        try {
            var b1 = getClass().getResource("/Bot/BossBot/boss1.png");
            var b2 = getClass().getResource("/Bot/BossBot/boss2.png");
            var b3 = getClass().getResource("/Bot/BossBot/boss3.png");
            var b4 = getClass().getResource("/Bot/BossBot/boss4.png");
            var b5 = getClass().getResource("/Bot/BossBot/boss5.png");
            var b6 = getClass().getResource("/Bot/BossBot/boss6.png");

            aliveImgs[0] = (b1 != null) ? new ImageIcon(b1).getImage() : null;
            aliveImgs[1] = (b2 != null) ? new ImageIcon(b2).getImage() : null;
            aliveImgs[2] = (b3 != null) ? new ImageIcon(b3).getImage() : null;
            aliveImgs[3] = (b4 != null) ? new ImageIcon(b4).getImage() : null;
            aliveImgs[4] = (b5 != null) ? new ImageIcon(b5).getImage() : null;
            aliveImgs[5] = (b6 != null) ? new ImageIcon(b6).getImage() : null;

        } catch (Exception e) {
            System.err.println("Lỗi khi tải ảnh BossBot: " + e.getMessage());
        }
    }

    @Override
    public void update() {
        if (!survival) return;

        summonedMinions.removeIf(minion -> !minion.isSurvival());

        if (isPlayerInDetectionRange() && summonedMinions.isEmpty()) {
            long currentTime = System.currentTimeMillis();

            if (currentTime - lastAttackTime >= attackCooldown) {
                summonMinions();
                lastAttackTime = currentTime;
            }
        }

        randomMovement.update(x, y);

        double nextX = randomMovement.getX();
        double nextY = randomMovement.getY();

        int originTileX = (int) (startX / TILE_SIZE);
        int originTileY = (int) (startY / TILE_SIZE);

        int nextTileX = (int) (nextX / TILE_SIZE);
        int nextTileY = (int) (nextY / TILE_SIZE);

        if (Math.abs(nextTileX - originTileX) <= MAX_PATROL_TILES
                && Math.abs(nextTileY - originTileY) <= MAX_PATROL_TILES) {
            x = nextX;
            y = nextY;
        }

        currentDirection = randomMovement.getCurrentDirection();

        // --- CẬP NHẬT HOẠT ẢNH (ANIMATION) ---
        animTimer++;
        if (animTimer > 10) { // Đổi số 10 để chỉnh tốc độ chuyển khung hình (lớn hơn thì chậm hơn)
            animIndex++;
            animTimer = 0;
        }
    }

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

    private void summonMinions() {
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

            if (spawnTileY < 0 || spawnTileY >= mapHash.length
                    || spawnTileX < 0
                    || spawnTileX >= mapHash[spawnTileY].length) {
                continue;
            }

            if (mapHash[spawnTileY][spawnTileX] != 0) continue;

            double spawnX = spawnTileX * TILE_SIZE;
            double spawnY = spawnTileY * TILE_SIZE;

            Mob minion;

            if (i < 2) {
                minion = new KondoriaBot(
                        spawnX, spawnY, TILE_SIZE, TILE_SIZE,
                        1, 3, 1, 1000, activePlayers, mapHash
                );
            } else {
                minion = new DollBot(
                        spawnX, spawnY, TILE_SIZE, TILE_SIZE,
                        1, 3, 1, 1000, activePlayers, mapHash
                );
            }

            summonedMinions.add(minion);
            Mobs.add(minion);
        }
    }

    @Override
    public void move(Direction direction) {
        currentDirection = direction;
    }

    @Override
    public void draw(Graphics2D g2d) {
        if (!survival && !deading) return;

        // --- CẬP NHẬT PHẦN VẼ ẢNH THEO MẢNG ---
        Image currentImg = aliveImgs[animIndex % 6];
        if (currentImg != null) {
            g2d.drawImage(currentImg, (int) x, (int) y, width, height, null);
        } else {
            // Khối màu phòng hờ nếu không load được ảnh
            g2d.setColor(new Color(128, 0, 128));
            g2d.fillRect((int) x, (int) y, width, height);

            g2d.setColor(Color.YELLOW);
            g2d.drawRect((int) x, (int) y, width, height);
        }

        // --- THANH MÁU (Đã sửa lỗi cú pháp) ---
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