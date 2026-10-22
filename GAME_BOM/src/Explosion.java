import java.util.Timer;
import java.util.TimerTask;

public class Explosion {
    private int x;              // Tọa độ X trên bản đồ
    private int y;              // Tọa độ Y trên bản đồ
    private int power;          // Bán kính ngọn lửa lan rộng
    private boolean isFinished; // Trạng thái báo hiệu vụ nổ đã kết thúc
    private GameMap gameMap;    // Tham chiếu đến bản đồ để tự xóa khi hết giờ

    /**
     * Constructor khởi tạo vụ nổ
     * @param x Tọa độ X
     * @param y Tọa độ Y
     * @param power Bán kính nổ
     * @param gameMap Bản đồ game (để tự loại bỏ vụ nổ khi hết thời gian)
     */
    public Explosion(int x, int y, int power, GameMap gameMap) {
        this.x = x;
        this.y = y;
        this.power = power;
        this.gameMap = gameMap;
        this.isFinished = false;

        startTimer(); // Tự động hẹn giờ kết thúc vụ nổ
    }

    /**
     * Hẹn giờ tồn tại của vụ nổ (ví dụ: 500 mili-giây = 0.5 giây)
     */
    public void startTimer() {
        Timer timer = new Timer();

        // Sau 500ms, vụ nổ sẽ tự động kết thúc
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                finish();
                timer.cancel(); // Hủy timer sau khi chạy xong
            }
        }, 500); // 500ms = 0.5 giây (bạn có thể đổi thành 300ms hoặc 1000ms tùy ý)
    }

    /**
     * Xử lý khi vụ nổ kết thúc
     */
    public void finish() {

        // Khối đồng bộ hóa (synchronized) dựa trên đối tượng hiện tại (this)
        // Đảm bảo chỉ có một luồng (thread) duy nhất được thực hiện kiểm tra và đổi trạng thái tại một thời điểm
        synchronized (this) {
            // Kiểm tra xem vụ nổ đã được kết thúc trước đó chưa
            if (isFinished) {
                return; // Nếu đã kết thúc rồi thì thoát luôn, tránh xử lý trùng lặp
            }
            // Đánh dấu trạng thái vụ nổ là đã hoàn thành
            this.isFinished = true;
        }
        // Tự động yêu cầu GameMap xóa vụ nổ này ra khỏi danh sách hiển thị
        if (gameMap != null) {
            gameMap.removeExplosion(this);
        }
    }

    // ==========================================
    // GETTERS & SETTERS
    // ==========================================

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getPower() {
        return power;
    }

    public boolean isFinished() {
        return isFinished;
    }
}