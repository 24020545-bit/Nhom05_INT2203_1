package Bomb;

import Player.Explosion;
import Player.PlayerOb;

import java.util.Timer;
import java.util.TimerTask;

public class Bomb {
    // Các thuộc tính riêng tư (Encapsulation)
    private int x;
    private int y;
    private int power;          // Bán kính nổ (được lấy từ thuộc tính power của Player.PlayerOb)
    private PlayerOb owner;     // Người chơi/Player.Bot đã đặt quả bom này
    private Map.GameMap gameMap;    // Tham chiếu bản đồ để thêm vụ nổ khi bom nổ
    private boolean isExploded; // Trạng thái kiểm tra bom đã nổ chưa
    private Timer timer;        // Giữ đối tượng Timer để có thể hủy hẹn giờ nếu nổ sớm

    /**
     * Constructor khởi tạo quả bom
     * @param x Tọa độ X trên bản đồ
     * @param y Tọa độ Y trên bản đồ
     * @param power Độ rộng/bán kính nổ
     * @param owner Người sở hữu bom
     * @param gameMap Bản đồ game chứa quả bom
     */
    public Bomb(int x, int y, int power, PlayerOb owner, Map.GameMap gameMap) {
        this.x = x;
        this.y = y;
        this.power = power;
        this.owner = owner;
        this.gameMap = gameMap;
        this.isExploded = false;

        startTimer(); // Bắt đầu đếm giờ 3 giây tự nổ
    }

    /**
     * Bắt đầu kích hoạt hẹn giờ 3000ms (3 giây)
     */
    public void startTimer() {
        timer = new Timer();

        // Sau 3000ms (3 giây) sẽ TỰ ĐỘNG gọi hàm explode()
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                explode();
            }
        }, 3000); // 3000 mili-giây = 3 giây
    }

    /**
     * Xử lý khi bom nổ
     */
    public void explode() {
        // Dùng synchronized để tránh xung đột nếu bom bị châm nổ dây chuyền
        synchronized (this) {
            if (isExploded) {
                return;
            }
            this.isExploded = true;

            // Hủy timer hẹn giờ (phòng trường hợp bom nổ sớm do phản ứng dây chuyền)
            if (timer != null) {
                timer.cancel();
            }
        }

        // 1. Tạo đối tượng vụ nổ (Explosion) tại vị trí quả bom (truyền gameMap vào)
        Explosion explosion = new Explosion(this.x, this.y, this.power, this.gameMap);

        // 2. Thêm vụ nổ vào danh sách quản lý của Map.GameMap
//        if (gameMap != null) {
//            gameMap.addExplosion(explosion);
//            gameMap.removeBomb(this); // Xóa quả bom này khỏi danh sách bom trên bản đồ
//        }

        // 3. Trả lại số lượng bom active cho người chơi sở hữu
        if (owner != null) {
            int currentActive = owner.getActiveBombCount();
            if (currentActive > 0) {
                owner.setActiveBombCount(currentActive - 1);
            }
        }
    }

    // ==========================================
    // GETTERS & SETTERS (Các phương thức truy xuất)
    // ==========================================

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getPower() {
        return power;
    }

    public void setPower(int power) {
        this.power = power;
    }

    public PlayerOb getOwner() {
        return owner;
    }

    public void setOwner(PlayerOb owner) {
        this.owner = owner;
    }

    public boolean isExploded() {
        return isExploded;
    }
}