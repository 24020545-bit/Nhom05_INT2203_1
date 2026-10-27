import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class GamePanel extends JPanel {
    private GameMap gameMap;
    private PlayerOb player;
    private final int TILE_SIZE = 40; // Kích thước của 1 ô (bom, nhân vật)

    public GamePanel() {
        setFocusable(true);
        setBackground(Color.DARK_GRAY);

        // Khởi tạo hệ thống
        gameMap = new GameMap();

        // Tạo nhân vật ẩn danh kế thừa từ PlayerOb bạn đã viết
        // Tọa độ (100,100), tốc độ 5, mạng 3, giới hạn 2 bom, sức công phá 1 ô
        player = new PlayerOb(100, 100, 5.0, 3, 2) {
            @Override
            public void move(Direction direction) {
                // Logic di chuyển phức tạp sẽ để ở đây sau
            }
        };

        // Lắng nghe bàn phím
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int key = e.getKeyCode();
                int speed = (int) player.getSpeed();

                // Di chuyển
                if (key == KeyEvent.VK_W) player.setY(player.getY() - speed);
                if (key == KeyEvent.VK_S) player.setY(player.getY() + speed);
                if (key == KeyEvent.VK_A) player.setX(player.getX() - speed);
                if (key == KeyEvent.VK_D) player.setX(player.getX() + speed);

                // Đặt bom
                if (key == KeyEvent.VK_SPACE) {
                    player.placeBomb(gameMap);
                }
            }
        });

        // Game Loop: Vẽ lại màn hình liên tục (khoảng 60 khung hình/giây)
        new Timer(16, e -> repaint()).start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // 1. VẼ CÁC QUẢ BOM (Hình tròn đỏ)
        g.setColor(Color.RED);
        for (Bomb bomb : gameMap.getBombs()) {
            g.fillOval(bomb.getX(), bomb.getY(), TILE_SIZE, TILE_SIZE);
        }

        // 2. VẼ CÁC VỤ NỔ (Hình chữ thập màu cam)
        g.setColor(Color.ORANGE);
        for (Explosion exp : gameMap.getExplosions()) {
            int ex = exp.getX();
            int ey = exp.getY();
            int power = exp.getPower();

            // Vẽ tâm vụ nổ
            g.fillRect(ex, ey, TILE_SIZE, TILE_SIZE);

            // Vẽ tia lửa lan ra 4 hướng dựa trên power
            for (int i = 1; i <= power; i++) {
                g.fillRect(ex + (i * TILE_SIZE), ey, TILE_SIZE, TILE_SIZE); // Phải
                g.fillRect(ex - (i * TILE_SIZE), ey, TILE_SIZE, TILE_SIZE); // Trái
                g.fillRect(ex, ey + (i * TILE_SIZE), TILE_SIZE, TILE_SIZE); // Dưới
                g.fillRect(ex, ey - (i * TILE_SIZE), TILE_SIZE, TILE_SIZE); // Trên
            }
        }

        // 3. VẼ NHÂN VẬT (Hình vuông xanh dương)
        g.setColor(Color.CYAN);
        g.fillRect(player.getX(), player.getY(), TILE_SIZE, TILE_SIZE);

        // Hiển thị trạng thái số lượng bom
        g.setColor(Color.WHITE);
        g.drawString("Bom đang đặt: " + player.getActiveBombCount() + "/" + player.getBombLimit(), 10, 20);
    }

    // Cửa sổ khởi chạy (Có thể tách ra file GameWindow riêng)
    public static void main(String[] args) {
        JFrame frame = new JFrame("Bomberman - Chạy Thử");
        frame.add(new GamePanel());
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}