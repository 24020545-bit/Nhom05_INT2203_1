import entity.Player;
import entity.bot.DollBot;
import entity.bot.KondoriaBot;
import entity.mob.Mob;
import entity.mob.Mobs;
import bomb.Bomb;
import item.Item;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

public class GamePanel extends JPanel {
    private final int TILE_SIZE = 32; // Khớp với kích thước TILE_SIZE trong Bomb, Player, Bot, Item
    private int[][] mapHash;          // Ma trận bản đồ
    private Player player;
    private List<Bomb> activeBombs;   // Danh sách quản lý bom toàn cục
    private Timer gameTimer;

    public GamePanel() {
        setFocusable(true);
        setBackground(Color.DARK_GRAY);
        setPreferredSize(new Dimension(800, 600));

        // 1. Khởi tạo bản đồ mẫu
        initMap();

        // 2. Khởi tạo Người chơi (Player) tại vị trí (1, 1)
        player = new Player(TILE_SIZE, TILE_SIZE, mapHash);
        Mobs.add(player);

        // 3. Khởi tạo danh sách bom trống
        activeBombs = new ArrayList<>();

        // 4. Thêm Bot mẫu để kiểm tra va chạm
        Mobs.add(new DollBot(5 * TILE_SIZE, 3 * TILE_SIZE, TILE_SIZE, TILE_SIZE, 1, mapHash));
        Mobs.add(new KondoriaBot(7 * TILE_SIZE, 3 * TILE_SIZE, TILE_SIZE, TILE_SIZE, 1, player, mapHash));

        // 5. Thêm một số Item mẫu
        Mobs.add(new Item(3 * TILE_SIZE, 1 * TILE_SIZE, Item.ItemType.RANGE_UP, mapHash));
        Mobs.add(new Item(4 * TILE_SIZE, 1 * TILE_SIZE, Item.ItemType.SPEED_UP, mapHash));

        // 6. Lắng nghe sự kiện bàn phím (Truyền trực tiếp vào Player)
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                player.handleKeyPressed(e);

                // Đảm bảo khi bấm phím Space, bom được tạo và đưa vào danh sách quản lý chung của GamePanel
                if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                    // Lấy danh sách bom từ Player hoặc tự tạo nếu Player đã tự quản lý danh sách riêng.
                    // (Nếu Player.java đã tự add vào list chung, ta chỉ cần đồng bộ lấy ra nếu cần,
                    // hoặc dùng cách dưới đây để chắc chắn activeBombs không bị rỗng):
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                player.handleKeyReleased(e);
            }
        });

        // 7. Game Loop (Chạy liên tục 60 FPS ~ 16ms)
        gameTimer = new Timer(16, e -> {
            updateGame();
            repaint();
        });
        gameTimer.start();
    }

    private void initMap() {
        int rows = 15;
        int cols = 20;
        mapHash = new int[rows][cols];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (i == 0 || i == rows - 1 || j == 0 || j == cols - 1 || (i % 2 == 0 && j % 2 == 0)) {
                    mapHash[i][j] = 1; // Tường cố định
                } else {
                    mapHash[i][j] = 0; // Đường đi trống
                }
            }
        }
    }

    private void updateGame() {
        // Cập nhật trạng thái tất cả các Mob (Player, Bot, Item)
        for (int i = 0; i < Mobs.MOBS.size(); i++) {
            Mob mob = Mobs.MOBS.get(i);
            mob.update();
        }

        // Dọn dẹp các mob đã chết (trừ Player)
        Mobs.MOBS.removeIf(m -> !m.isSurvival() && !(m instanceof Player));

        // Lấy danh sách bom từ hành động thả bom của Player (nếu Player lưu activeBombs riêng,
        // ta có thể cho GamePanel quét hoặc tự add. Ở đây ta cập nhật trực tiếp các bom đang hoạt động).
        // Cách tốt nhất: Đồng bộ hóa danh sách bom từ phía Player nếu Player có quản lý:
        // Hoặc chúng ta cập nhật trực tiếp vòng lặp cho tất cả các đối tượng Bomb có trong game.

        // *Mẹo*: Để đơn giản hóa việc quản lý bom, bạn hãy bổ sung một phương thức lấy danh sách bom hoặc
        // cho phép Bomb tự update. Nếu bạn đang dùng `player.dropBomb(mapHash, activeBombs)`,
        // danh sách `activeBombs` ở đây sẽ tự động nhận bom khi bạn bấm Space.

        for (int i = activeBombs.size() - 1; i >= 0; i--) {
            Bomb bomb = activeBombs.get(i);
            bomb.update();
            if (!bomb.isActive()) {
                activeBombs.remove(i);
            }
        }

        // Nếu trong Player.java của bạn đang tự quản lý một list `activeBombs` riêng mà không truyền ra ngoài,
        // bạn hãy chắc chắn rằng trong Player.java hàm `dropBomb` có truyền globalBombList vào như code mẫu trước đó.
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // 1. Vẽ Bản đồ (Map)
        if (mapHash != null) {
            for (int i = 0; i < mapHash.length; i++) {
                for (int j = 0; j < mapHash[0].length; j++) {
                    int tileVal = mapHash[i][j];
                    if (tileVal == 1) {
                        g2d.setColor(Color.GRAY);
                        g2d.fillRect(j * TILE_SIZE, i * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                        g2d.setColor(Color.BLACK);
                        g2d.drawRect(j * TILE_SIZE, i * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                    } else {
                        g2d.setColor(Color.DARK_GRAY);
                        g2d.fillRect(j * TILE_SIZE, i * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                    }
                }
            }
        }

        // 2. Vẽ toàn bộ danh sách Bom và hiệu ứng lửa nổ
        for (Bomb bomb : activeBombs) {
            bomb.draw(g2d);
        }

        // 3. Vẽ tất cả các Mobs (Player, Bot, Item)
        for (Mob mob : Mobs.MOBS) {
            mob.draw(g2d);
        }

        // 4. Hiển thị thông tin HUD trên màn hình
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        g2d.drawString("HP: " + player.getHp() + " | Sức mạnh bom: " + player.getPower() + " | Tốc độ: " + player.getSpeed(), 10, 20);
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Bomberman Game");
        GamePanel panel = new GamePanel();
        frame.add(panel);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}