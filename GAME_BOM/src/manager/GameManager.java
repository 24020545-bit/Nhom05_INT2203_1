package manager;

import UI.MapPanel;
import entity.Bot;
import entity.Player;
import entity.PlayerOb;
import map.GameMap;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class GameManager {
    private GameMap gameMap;
    private ItemManager itemManager;
    private BombManager bombManager;
    private PlayerController playerController;
    private List<PlayerOb> players;

    private Timer gameLoopTimer;
    private MapPanel mapPanel;

    private int gameMode;
    private Runnable onGameOver;

    public GameManager(GameMap gameMap, int gameMode, Runnable onGameOver) {
        this.gameMap = gameMap;
        this.gameMode = gameMode;
        this.onGameOver = onGameOver;

        initSession();
    }

    private void initSession() {
        if (gameMap != null) {
            gameMap.resetMap();
        }
        itemManager = new ItemManager();
        players = new ArrayList<>();

        int mapWidth = gameMap.getWidth();
        int mapHeight = gameMap.getHeight();

        // Tọa độ 4 góc an toàn
        int xTopLeft = 1, yTopLeft = 1;
        int xTopRight = mapWidth - 2, yTopRight = 1;
        int xBottomLeft = 1, yBottomLeft = mapHeight - 2;
        int xBottomRight = mapWidth - 2, yBottomRight = mapHeight - 2;

        if (gameMode == 0) {
            // Chế độ 0: 1 Người chơi + 3 Bot
            players.add(new Player(xTopLeft, yTopLeft, 0.1, 3, 1, 1, "Player 1"));
            players.add(new Bot(xTopRight, yTopRight, 0.1, 3, 1, 1));
            players.add(new Bot(xBottomLeft, yBottomLeft, 0.1, 3, 1, 1));
            players.add(new Bot(xBottomRight, yBottomRight, 0.1, 3, 1, 1));
        } else {
            // Chế độ 1: 2 Người chơi + 2 Bot
            players.add(new Player(xTopLeft, yTopLeft, 0.1, 3, 1, 1, "Player 1"));
            players.add(new Player(xTopRight, yTopRight, 0.1, 3, 1, 1, "Player 2"));
            players.add(new Bot(xBottomLeft, yBottomLeft, 0.1, 3, 1, 1));
            players.add(new Bot(xBottomRight, yBottomRight, 0.1, 3, 1, 1));
        }

        Player p1 = (players.size() > 0 && players.get(0) instanceof Player) ? (Player) players.get(0) : null;
        Player p2 = (players.size() > 1 && players.get(1) instanceof Player) ? (Player) players.get(1) : null;

        bombManager = new BombManager(gameMap, players, itemManager);

        // Khởi tạo MapPanel không còn nhận callback pause
        mapPanel = new MapPanel(gameMap, players, bombManager, itemManager);

        // Bật bộ điều khiển phím cho người chơi
        playerController = new PlayerController(p1, p2, gameMap, mapPanel, bombManager);
        playerController.setupControls(mapPanel);

        // Thiết lập Game Loop (25ms)
        gameLoopTimer = new Timer(25, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 1. Xử lý di chuyển liên tục cho Player
                updatePlayerMovement(playerController, p1, p2);

                // 2. Cập nhật AI di chuyển, né bom và đặt bom cho Bot
                for (PlayerOb p : players) {
                    if (p instanceof Bot && p.isAlive()) {
                        ((Bot) p).update(gameMap, bombManager);
                    }
                }

                // 3. Cập nhật trạng thái bom và vụ nổ
                bombManager.update();

                // 4. Kiểm tra va chạm nhặt item
                itemManager.checkPlayerCollision(players);

                // 5. Kiểm tra điều kiện Thắng / Thua
                checkWinCondition(p1, p2);

                // 6. Vẽ lại màn hình game
                mapPanel.repaint();
            }
        });
    }

    public void startGameLoop() {
        if (gameLoopTimer != null && !gameLoopTimer.isRunning()) {
            gameLoopTimer.start();
        }
        if (mapPanel != null) {
            mapPanel.requestFocusInWindow();
        }
    }

    public void stopGameLoop() {
        if (gameLoopTimer != null && gameLoopTimer.isRunning()) {
            gameLoopTimer.stop();
        }
    }

    public MapPanel getMapPanel() {
        return mapPanel;
    }

    private void updatePlayerMovement(PlayerController controller, Player p1, Player p2) {
        if (controller == null) return;
        var activeKeys = controller.getActiveKeys();

        if (p1 != null && p1.isAlive()) {
            if (activeKeys.contains("P1_UP"))    p1.move(utils.Direction.UP, gameMap, bombManager);
            if (activeKeys.contains("P1_DOWN"))  p1.move(utils.Direction.DOWN, gameMap, bombManager);
            if (activeKeys.contains("P1_LEFT"))  p1.move(utils.Direction.LEFT, gameMap, bombManager);
            if (activeKeys.contains("P1_RIGHT")) p1.move(utils.Direction.RIGHT, gameMap, bombManager);
        }

        if (p2 != null && p2.isAlive()) {
            if (activeKeys.contains("P2_UP"))    p2.move(utils.Direction.UP, gameMap, bombManager);
            if (activeKeys.contains("P2_DOWN"))  p2.move(utils.Direction.DOWN, gameMap, bombManager);
            if (activeKeys.contains("P2_LEFT"))  p2.move(utils.Direction.LEFT, gameMap, bombManager);
            if (activeKeys.contains("P2_RIGHT")) p2.move(utils.Direction.RIGHT, gameMap, bombManager);
        }
    }

    private void checkWinCondition(Player p1, Player p2) {
        if (gameLoopTimer == null || !gameLoopTimer.isRunning()) return;

        if (gameMode == 0) {
            boolean p1Alive = (p1 != null && p1.isAlive());
            long livingBotsCount = players.stream().filter(p -> p instanceof Bot && p.isAlive()).count();

            if (!p1Alive) {
                stopGameLoop();
                JOptionPane.showMessageDialog(mapPanel, "GAME OVER! Bạn đã bị tiêu diệt.", "Thua Cuộc", JOptionPane.INFORMATION_MESSAGE);
                if (onGameOver != null) onGameOver.run();
            } else if (livingBotsCount == 0) {
                stopGameLoop();
                JOptionPane.showMessageDialog(mapPanel, "VICTORY! Bạn đã tiêu diệt toàn bộ Bot!", "Chiến Thắng", JOptionPane.INFORMATION_MESSAGE);
                if (onGameOver != null) onGameOver.run();
            }

        } else {
            boolean p1Alive = (p1 != null && p1.isAlive());
            boolean p2Alive = (p2 != null && p2.isAlive());

            long livingPlayersCount = 0;
            if (p1Alive) livingPlayersCount++;
            if (p2Alive) livingPlayersCount++;

            long livingBotsCount = players.stream().filter(p -> p instanceof Bot && p.isAlive()).count();

            if (livingPlayersCount + livingBotsCount <= 1) {
                stopGameLoop();
                String message = "Trận đấu kết thúc! ";
                if (p1Alive && !p2Alive) message += "Player 1 Thắng!";
                else if (!p1Alive && p2Alive) message += "Player 2 Thắng!";
                else if (p1Alive && p2Alive) message += "Cả 2 Người chơi cùng Thắng!";
                else message += "Tất cả đều đã hy sinh!";

                JOptionPane.showMessageDialog(mapPanel, message, "Kết Quả Trận Đấu", JOptionPane.INFORMATION_MESSAGE);
                if (onGameOver != null) onGameOver.run();
            }
        }
    }
}