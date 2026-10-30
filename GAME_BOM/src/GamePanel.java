import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class GamePanel extends JPanel implements Runnable {

    // Cấu hình màn hình / kích thước ô (Tile)
    public static final int ORIGINAL_TILE_SIZE = 16; // Kích thước gốc của tile (16x16)
    public static final int SCALE = 3; // Hệ số phóng to
    public static final int TILE_SIZE = ORIGINAL_TILE_SIZE * SCALE; // 48x48 pixels mỗi ô

    // Kích thước màn hình (ví dụ: 31 cột x 13 hàng theo chuẩn Bomberman)
    public static final int MAX_SCREEN_COL = 31;
    public static final int MAX_SCREEN_ROW = 13;
    public static final int SCREEN_WIDTH = TILE_SIZE * MAX_SCREEN_COL;
    public static final int SCREEN_HEIGHT = TILE_SIZE * MAX_SCREEN_ROW;

    // FPS (Frames Per Second)
    private final int FPS = 60;

    // Game Thread quản lý vòng lặp
    private Thread gameThread;
    private boolean isRunning = false;

    // --- CÁC THÀNH PHẦN KHÁC (Gắn kết từ code của nhóm bạn) ---
    // Ví dụ: MapLoader / Map, Player, danh sách Enemies, Bombs, v.v.
    // private Map map;
    // private Player player;
    // private List<Entity> entities;
    // private KeyHandler keyH = new KeyHandler(); // Lớp sử lý phím bấm riêng

    public GamePanel() {
        this.setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        this.setBackground(Color.BLACK);
        this.setDoubleBuffered(true); // Tăng hiệu năng hiển thị đồ họa

        // Đăng ký bộ lắng nghe bàn phím
        // this.addKeyListener(keyH);
        this.setFocusable(true);

        // Khởi tạo các đối tượng game (Bản đồ, Người chơi, Kẻ địch...)
        initGame();
    }

    /**
     * Khởi tạo bản đồ, người chơi và các thực thể từ các class khác.
     */
    private void initGame() {
        // TODO: Khởi tạo Map từ file text (ví dụ: map.loadMap("res/levels/level1.txt"));
        // TODO: Khởi tạo Player tại vị trí bắt đầu.
        // TODO: Khởi tạo danh sách kẻ địch (Doll, Kondoria, Oneal...).
    }

    /**
     * Bắt đầu luồng chạy game (Game Thread).
     */
    public void startGameThread() {
        gameThread = new Thread(this);
        isRunning = true;
        gameThread.start();
    }

    @Override
    public void run() {
        // Game Loop chuẩn dùng Delta Time hoặc Sleep Timer để giữ ổn định 60 FPS
        double drawInterval = 1000000000.0 / FPS;
        double delta = 0;
        long lastTime = System.nanoTime();
        long currentTime;
        long timer = 0;
        int drawCount = 0;

        while (isRunning) {
            currentTime = System.nanoTime();
            delta += (currentTime - lastTime) / drawInterval;
            timer += (currentTime - lastTime);
            lastTime = currentTime;

            if (delta >= 1) {
                // 1. Cập nhật vị trí, trạng thái, va chạm
                update();

                // 2. Vẽ lại màn hình
                repaint();

                delta--;
                drawCount++;
            }

            // Kiểm tra mỗi giây để debug FPS nếu cần
            if (timer >= 1000000000.0) {
                // System.out.println("FPS: " + drawCount);
                drawCount = 0;
                timer = 0;
            }
        }
    }

    /**
     * Cập nhật logic game ở mỗi frame (60 lần/giây).
     */
    private void update() {
        // TODO: Gọi hàm update của Map, Player, Bombs, Enemies (Doll, Kondoria, Oneal...)
        // Ví dụ:
        // player.update();
        // for(Entity enemy : enemies) { enemy.update(); }
        // checkCollisions();
    }

    /**
     * Vẽ đồ họa lên màn hình Swing.
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Ép kiểu sang Graphics2D để có các tính năng đồ họa nâng cao hơn nếu cần
        Graphics2D g2 = (Graphics2D) g;

        // TODO: Thứ tự vẽ rất quan trọng (Z-index / Layer)
        // 1. Vẽ bản đồ / nền (Map / Tiles)
        // if (map != null) map.draw(g2);

        // 2. Vẽ các vật thể / bom (Bombs & Flames)

        // 3. Vẽ người chơi và kẻ địch (Player, Doll, Oneal, Kondoria...)
        // if (player != null) player.draw(g2);

        // 4. Giải phóng tài nguyên đồ họa 2D
        g2.dispose();
    }
}