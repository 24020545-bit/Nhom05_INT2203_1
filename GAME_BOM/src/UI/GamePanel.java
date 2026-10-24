package UI;

import Bomb.BombManager;
import Map.GameMap;
import Player.Bot;
import Player.Player;
import Player.PlayerController;
import Player.PlayerOb;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class GamePanel extends JPanel {
    private final GameMap gameMap;
    private final List<PlayerOb> players; // Danh sách quản lý cả Player và các Bot
    private final BombManager bombManager;
    private final MapPanel mapPanel;
    private final Timer gameLoop;

    public GamePanel(MainFrame frame, GameMap gameMap) {
        this.gameMap = gameMap;

        // 1. Khởi tạo danh sách nhân vật (1 Player thật + 3 Bot) qua hàm riêng cho gọn
        this.players = initPlayers();

        // 2. Khởi tạo BombManager và MapPanel (truyền danh sách players vào)
        this.bombManager = new BombManager(gameMap,players);
        this.mapPanel = new MapPanel(gameMap, players, bombManager);

        // 3. Cài đặt giao diện chính chứa khung bản đồ
        setLayout(new BorderLayout());
        add(mapPanel, BorderLayout.CENTER);

        // 4. Gán phím điều khiển cho người chơi thật (Player đầu tiên - index 0)
        setupPlayerController();

        // 5. Khởi tạo và kích hoạt Game Loop (Chạy mỗi 50ms để cập nhật Bot và Bom)
        this.gameLoop = createGameLoop();
        this.gameLoop.start();
    }

    // --- CÁC HÀM PHỤ TRỢ GIÚP CONSTRUCTOR GỌN SẠCH ---

    private List<PlayerOb> initPlayers() {
        List<PlayerOb> list = new ArrayList<>();

        // Lấy kích thước thực tế từ bản đồ hiện tại
        int maxX = gameMap.getWidth() - 2;
        int maxY = gameMap.getHeight() - 2;

        // Player 1 (Người thật) ở góc trên trái (1, 1)
        list.add(new Player(1.0, 1.0, 1.0, 3, 2, 1, "Player 1"));

        // 3 Bot ở 3 góc còn lại bên trong khuôn viên map gạch
        list.add(new Bot((double) maxX, (double) maxY, 1.0, 3, 2, 1)); // Dưới phải
        list.add(new Bot((double) maxX, 1.0, 1.0, 3, 2, 1));          // Trên phải
        list.add(new Bot(1.0, (double) maxY, 1.0, 3, 2, 1));          // Dưới trái

        return list;
    }

    private void setupPlayerController() {
        if (!players.isEmpty() && players.get(0) instanceof Player) {
            Player realPlayer = (Player) players.get(0);
            PlayerController controller = new PlayerController(realPlayer, gameMap, mapPanel, bombManager);
            controller.setupControls(this);
        }
    }

    private Timer createGameLoop() {
        return new Timer(50, e -> {
            // Cập nhật hành động AI của tất cả các Bot
            for (PlayerOb p : players) {
                if (p instanceof Bot) {
                    ((Bot) p).update(gameMap, bombManager);
                }
            }

            // Cập nhật trạng thái bom và vụ nổ chung
            if (bombManager != null) {
                bombManager.update();
            }

            // Vẽ lại toàn bộ màn hình game
            if (mapPanel != null) {
                mapPanel.repaint();
            }
        });
    }
}