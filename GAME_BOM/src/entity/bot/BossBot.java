package entity.bot;

import entity.Player;
import entity.mob.Mob;
import entity.mob.Mobs;
import utils.Direction;
import utils.RandomMovement;

import java.awt.Graphics2D;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class BossBot extends Mob {
    private int[][] mapHash;
    private final int TILE_SIZE = 32;

    private final double startX;
    private final double startY;
    private static final int MAX_PATROL_TILES = 6;
    private static final int DETECT_RADIUS_TILES = 6;

    private final RandomMovement randomMovement;
    private final List<KondoriaBot> summonedMinions = new ArrayList<>();

    // Khởi tạo BossBot, thiết lập tọa độ, máu, tốc độ và thời gian hồi chiêu triệu hồi
    public BossBot(double x, double y, int width, int height, int speed, int hp, long summonCooldown, int[][] mapHash) {
        super(x, y, width, height, speed, 0, 0, hp, 0);
        this.startX = x;
        this.startY = y;
        this.mapHash = mapHash;
        this.currentDirection = Direction.DOWN;
        this.attackCooldown = summonCooldown;

        this.randomMovement = new RandomMovement(x, y, width, height, speed, mapHash);
    }

    // Cập nhật trạng thái của Boss mỗi khung hình (kiểm tra minion, xử lý logic dùng skill triệu hồi và di chuyển tuần tra)
    @Override
    public void update() {
        if (!survival) return;

        summonedMinions.removeIf(minion -> !minion.isSurvival());

        if (isPlayerInDetectionRange() && summonedMinions.isEmpty()) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastAttackTime >= attackCooldown) {
                summonKondoriaMinions();
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

        if (Math.abs(nextTileX - originTileX) <= MAX_PATROL_TILES &&
                Math.abs(nextTileY - originTileY) <= MAX_PATROL_TILES) {
            x = nextX;
            y = nextY;
        }

        this.currentDirection = randomMovement.getCurrentDirection();
    }

    // Kiểm tra xem có người chơi nào nằm trong phạm vi 6 ô phát hiện của Boss hay không
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

                if (distanceX <= DETECT_RADIUS_TILES && distanceY <= DETECT_RADIUS_TILES) {
                    return true;
                }
            }
        }
        return false;
    }

    // Thực hiện sinh ra các con KondoriaBot đệ tử xung quanh vị trí hiện tại của Boss
    private void summonKondoriaMinions() {
        if (mapHash == null) return;

        System.out.println("Boss triệu hồi đợt KondoriaBot mới (đợt cũ đã bị tiêu diệt hết)!");

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

        for (int[] offset : offsets) {
            int spawnTileX = currentTileX + offset[0];
            int spawnTileY = currentTileY + offset[1];

            if (spawnTileY >= 0 && spawnTileY < mapHash.length &&
                    spawnTileX >= 0 && spawnTileX < mapHash[0].length) {

                if (mapHash[spawnTileY][spawnTileX] == 0) {
                    double spawnX = spawnTileX * TILE_SIZE;
                    double spawnY = spawnTileY * TILE_SIZE;

                    KondoriaBot minion = new KondoriaBot(spawnX, spawnY, TILE_SIZE, TILE_SIZE, 1, 3, 1, 1000, activePlayers, mapHash);

                    summonedMinions.add(minion);
                    Mobs.add(minion);
                }
            }
        }
    }

    // Thiết lập hướng di chuyển hiện tại cho Boss
    @Override
    public void move(Direction direction) {
        this.currentDirection = direction;
    }

    // Vẽ hình ảnh/giao diện của Boss và thanh máu lên màn hình game
    @Override
    public void draw(Graphics2D g2d) {
        if (!survival && !deading) return;

        g2d.setColor(new Color(128, 0, 128));
        g2d.fillRect((int) x, (int) y, width, height);

        g2d.setColor(Color.YELLOW);
        g2d.drawRect((int) x, (int) y, width, height);

        g2d.setColor(Color.RED);
        g2d.fillRect((int) x, (int) y - 6, width, 4);
        g2d.setColor(Color.GREEN);
        int currentHpWidth = (int) (width * ((double) hp / maxHp));
        g2d.fillRect((int) x, (int) y - 6, currentHpWidth, 4);
    }
}