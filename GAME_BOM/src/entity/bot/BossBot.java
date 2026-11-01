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

    // --- TỌA ĐỘ GỐC VÀ PHẠM VI HOẠT ĐỘNG ---
    private final double startX;
    private final double startY;
    private static final int MAX_PATROL_TILES = 6; // Phạm vi di chuyển tối đa 6 ô tính từ gốc
    private static final int DETECT_RADIUS_TILES = 6; // Phạm vi phát hiện player để dùng skill là 6 ô

    private final RandomMovement randomMovement;

    // --- DANH SÁCH QUẢN LÝ QUÁI CON ĐÃ TRIỆU HỒI ---
    private final List<Mob> summonedMinions = new ArrayList<>();

    // --- CONSTRUCTOR KHỞI TẠO NHANH ---
    public BossBot(double x, double y, int width, int height, int speed, int hp, long summonCooldown, int[][] mapHash) {
        super(x, y, width, height, speed, 0, 0, hp, 0); // Boss không gây sát thương va chạm (attackPower = 0)
        this.startX = x;
        this.startY = y;
        this.mapHash = mapHash;
        this.currentDirection = Direction.DOWN;
        this.attackCooldown = summonCooldown; // Thời gian hồi kỹ năng triệu hồi

        // Khởi tạo bộ di chuyển ngẫu nhiên
        this.randomMovement = new RandomMovement(x, y, width, height, speed, mapHash);
    }

    @Override
    public void update() {
        if (!survival) return;

        // Xóa những minion đã chết khỏi danh sách theo dõi
        summonedMinions.removeIf(minion -> !minion.isSurvival());

        // 1. ĐIỀU KIỆN DÙNG SKILL: Phải trong phạm vi 6 ô, đã hết thời gian hồi chiêu VÀ tất cả minion cũ đã chết sạch
        if (isPlayerInDetectionRange() && summonedMinions.isEmpty()) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastAttackTime >= attackCooldown) {
                summonMinions();
                lastAttackTime = currentTime; // Đánh dấu mốc thời gian vừa dùng kỹ năng
            }
        }

        // 2. Di chuyển tuần tra ngẫu nhiên (sử dụng RandomMovement có kiểm tra tường)
        randomMovement.update(x, y);

        double nextX = randomMovement.getX();
        double nextY = randomMovement.getY();

        // Kiểm tra xem vị trí tiếp theo có vượt quá vùng giới hạn 6 ô quanh điểm xuất phát không
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

    // Kiểm tra xem có người chơi nào nằm trong phạm vi 6 ô không
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

    // Hàm thực hiện sinh ra 2 con Doll và 2 con KondoriaBot xung quanh Boss
    private void summonMinions() {
        if (mapHash == null) return;

        System.out.println("Boss triệu hồi: 2 Doll và 2 KondoriaBot!");

        // Lấy danh sách tất cả các Player đang sống trong game
        Player[] activePlayers = Mobs.MOBS.stream()
                .filter(m -> m instanceof Player && m.isSurvival())
                .map(m -> (Player) m)
                .toArray(Player[]::new);

        if (activePlayers.length == 0) return;

        // 4 hướng xung quanh Boss: [Phải, Trái, Lên, Xuống]
        int[][] offsets = {
                {1, 0},   // Vị trí 0: Phải (Kondoria 1)
                {-1, 0},  // Vị trí 1: Trái (Kondoria 2)
                {0, -1},  // Vị trí 2: Lên (Doll 1)
                {0, 1}    // Vị trí 3: Xuống (Doll 2)
        };

        int currentTileX = (int) (x / TILE_SIZE);
        int currentTileY = (int) (y / TILE_SIZE);

        for (int i = 0; i < offsets.length; i++) {
            int spawnTileX = currentTileX + offsets[i][0];
            int spawnTileY = currentTileY + offsets[i][1];

            if (spawnTileY >= 0 && spawnTileY < mapHash.length &&
                    spawnTileX >= 0 && spawnTileX < mapHash[0].length) {

                if (mapHash[spawnTileY][spawnTileX] == 0) {
                    double spawnX = spawnTileX * TILE_SIZE;
                    double spawnY = spawnTileY * TILE_SIZE;

                    Mob minion;
                    // i = 0, 1 tạo KondoriaBot; i = 2, 3 tạo DollBot (hoặc Doll tùy theo tên class bot Doll trong code của bạn)
                    if (i < 2) {
                        minion = new KondoriaBot(spawnX, spawnY, TILE_SIZE, TILE_SIZE, 1, 3, 1, 1000, activePlayers, mapHash);
                    } else {
                        // Lưu ý: Nếu class Doll của bạn có tên khác (ví dụ Doll hoặc DollBot), hãy sửa lại tên class cho đúng với dự án
                        minion = new DollBot(spawnX, spawnY, TILE_SIZE, TILE_SIZE, 1, 3, 1, 1000, activePlayers, mapHash);
                    }

                    // Thêm vào danh sách quản lý minion của Boss và danh sách Mobs chung của game
                    summonedMinions.add(minion);
                    Mobs.add(minion);
                }
            }
        }
    }

    @Override
    public void move(Direction direction) {
        this.currentDirection = direction;
    }

    @Override
    public void draw(Graphics2D g2d) {
        if (!survival && !deading) return;

        g2d.setColor(new Color(128, 0, 128)); // Màu tím Boss
        g2d.fillRect((int) x, (int) y, width, height);

        g2d.setColor(Color.YELLOW);
        g2d.drawRect((int) x, (int) y, width, height);

        // Vẽ thanh máu (Đã sửa lại lỗi thiếu dấu - và *)
        g2d.setColor(Color.RED);
        g2d.fillRect((int) x, (int) y - 6, width, 4);
        g2d.setColor(Color.GREEN);
        int currentHpWidth = (int) (width * ((double) hp / maxHp));
        g2d.fillRect((int) x, (int) y - 6, currentHpWidth, 4);
    }
}