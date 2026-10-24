package UI;

import Bomb.BombManager;
import Map.GameMap;
import Player.Player;
import Player.PlayerController;

import javax.swing.*;
import java.awt.*;

public class GamePanel extends JPanel {
    private final GameMap gameMap;
    private final Player player;
    private final BombManager bombManager;
    private final MapPanel mapPanel;

    public GamePanel(MainFrame frame, GameMap gameMap) {
        this.gameMap = gameMap;

        // 1. Khởi tạo đối tượng
        player = new Player(1, 1, 0, 3, 2, 1, "Player 1");
        bombManager = new BombManager(gameMap);

        // 2. Truyền đầy đủ player và bombManager vào MapPanel
        // để MapPanel gom chung và gọi các Renderer vẽ xếp lớp chuẩn xác
        mapPanel = new MapPanel(gameMap, player, bombManager);

        // 3. Giao diện chính chứa khung bản đồ
        setLayout(new BorderLayout());
        add(mapPanel, BorderLayout.CENTER);

        // 4. Phím bấm điều khiển
        PlayerController controller = new PlayerController(player, gameMap, mapPanel, bombManager);
        controller.setupControls(this);
    }

    // Đã loại bỏ hoàn toàn paintComponent ở đây
    // vì việc vẽ hiển thị xếp lớp (Map -> Bomb -> Entity) đã được giao trọn gói cho MapPanel kết hợp với các Renderer!
}