package UI.Screen;

import Bomb.BombManager;
import Items.ItemManager;
import Map.GameMap;
import Player.Bot;
import Player.Direction;
import Player.Player;
import Player.PlayerController;
import Player.PlayerOb;
import UI.MainFrame;
import UI.MapPanel;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class GamePanel extends JPanel {
    private final GameMap gameMap;
    private final List<PlayerOb> players;
    private final BombManager bombManager;
    private final MapPanel mapPanel;
    private final Timer gameLoop;
    private final ItemManager itemManager;
    private PlayerController playerController; // Lưu reference để lấy phím bấm

    public GamePanel(MainFrame frame, GameMap gameMap) {
        this.gameMap = gameMap;
        this.players = initPlayers();
        this.itemManager = new ItemManager();
        this.bombManager = new BombManager(gameMap, players, itemManager);
        this.mapPanel = new MapPanel(gameMap, players, bombManager, itemManager);

        setLayout(new BorderLayout());
        add(mapPanel, BorderLayout.CENTER);

        setupPlayerController();

        // 1 Game Loop duy nhất, tốc độ 25ms giúp cả Player và Bot di chuyển mượt mà đều đặn
        this.gameLoop = new Timer(30, e -> updateGame());
        this.gameLoop.start();
    }

    private List<PlayerOb> initPlayers() {
        List<PlayerOb> list = new ArrayList<>();
        int maxX = gameMap.getWidth() - 2;
        int maxY = gameMap.getHeight() - 2;

        list.add(new Player(1.0, 1.0, 0.1, 3, 2, 1, "Player 1"));
        list.add(new Bot((double) maxX, (double) maxY, 0.1, 3, 2, 1));
        list.add(new Bot((double) maxX, 1.0, 0.1, 3, 2, 1));
        list.add(new Bot(1.0, (double) maxY, 0.1, 3, 2, 1));

        return list;
    }

    private void setupPlayerController() {
        if (!players.isEmpty() && players.get(0) instanceof Player) {
            Player realPlayer = (Player) players.get(0);
            this.playerController = new PlayerController(realPlayer, gameMap, mapPanel, bombManager);
            playerController.setupControls(this);
        }
    }

    // --- MỘT GAME LOOP DUY NHẤT CHO TẤT CẢ MỌI THỨ ---
    private void updateGame() {
        // 1. Cập nhật di chuyển cho Player thật dựa trên phím đang bấm
        if (playerController != null && !players.isEmpty() && players.get(0).isAlive()) {
            Player realPlayer = (Player) players.get(0);
            var keys = playerController.getActiveKeys();
            if (keys.contains("UP")) realPlayer.move(Direction.UP, gameMap);
            else if (keys.contains("DOWN")) realPlayer.move(Direction.DOWN, gameMap);
            else if (keys.contains("LEFT")) realPlayer.move(Direction.LEFT, gameMap);
            else if (keys.contains("RIGHT")) realPlayer.move(Direction.RIGHT, gameMap);
        }

        // 2. Cập nhật hành động AI của tất cả các Bot
        for (PlayerOb p : players) {
            if (p instanceof Bot && p.isAlive()) {
                ((Bot) p).update(gameMap, bombManager);
            }
        }

        // 3. Cập nhật trạng thái bom và vụ nổ
        if (bombManager != null) {
            bombManager.update();
        }

        // 4. Kiểm tra va chạm nhặt Item
        if (itemManager != null) {
            itemManager.checkPlayerCollision(players);
        }

        // 5. Vẽ lại toàn bộ màn hình game một cách đồng bộ
        if (mapPanel != null) {
            mapPanel.repaint();
        }
    }
}