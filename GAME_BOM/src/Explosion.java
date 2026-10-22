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
//khi ket tuc vuu no thi sao
    public void finish() {
        synchronized (this) {
            if (isFinished) {
                return;
            }
            this.isFinished = true;
        }
        if (gameMap != null) {
            gameMap.removeExplosion(this);
        }
    }
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