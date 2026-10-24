package UI;

import Map.GameMap;
import Player.Player;
import Player.PlayerController;

import javax.swing.*;
import java.awt.*;

public class GamePanel extends JPanel {
    private final GameMap gameMap;
    private final Player player;
    private final MapPanel mapPanel;

    public GamePanel(MainFrame frame, GameMap gameMap) {
        this.gameMap = gameMap;

        // 1. Tạo người chơi
        player = new Player(
                1, 1, 0, 3, 2, 1, "Player 1"
        );

        // 2. Tạo giao diện bản đồ
        mapPanel = new MapPanel(gameMap, player);

        // 3. Tạo bộ điều khiển bàn phím
        PlayerController controller =
                new PlayerController(player, gameMap, mapPanel);

        // 4. Kích hoạt điều khiển bàn phím
        controller.setupControls(this);

        // 5. Hiển thị bản đồ
        setLayout(new BorderLayout());
        add(mapPanel, BorderLayout.CENTER);
    }
}