package UI;

import UI.Screen.GameSetupPanel;
import UI.Screen.HomeScreenPanel;
import UI.Screen.KeySettingsPanel;
import map.GameMap;
import manager.BombManager;
import manager.ItemManager;
import manager.PlayerController;
import entity.Player;
import entity.Bot;
import entity.PlayerOb;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class Main extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainContainer;
    private GameMap gameMap;

    private static final int TILE_SIZE = 45; // Kích thước ô hiển thị mặc định

    // Biến quản lý vòng lặp game (Game Loop)
    private Timer gameLoopTimer;

    public Main() {
        setTitle("Bomber Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Cho phép phóng to, thu nhỏ cửa sổ linh hoạt
        setResizable(true);

        gameMap = new GameMap();
        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);

        // Kích thước khởi tạo ban đầu dựa trên bản đồ
        int defaultWidth = Math.max(gameMap.getWidth() * TILE_SIZE, 800);
        int defaultHeight = Math.max(gameMap.getHeight() * TILE_SIZE, 600);
        Dimension initialSize = new Dimension(defaultWidth, defaultHeight);

        // Đặt kích thước tối thiểu để tránh cửa sổ bị kéo quá nhỏ gây lỗi giao diện
        setMinimumSize(new Dimension(600, 500));

        // 1. Màn hình Trang chủ
        HomeScreenPanel homeScreen = new HomeScreenPanel(
                () -> cardLayout.show(mainContainer, "SETUP"),        // Khi bấm Bắt đầu
                () -> cardLayout.show(mainContainer, "SETTINGS")     // Khi bấm Cài đặt phím
        );

        // 2. Màn hình Cài đặt phím
        KeySettingsPanel settingsScreen = new KeySettingsPanel(() -> {
            cardLayout.show(mainContainer, "HOME");
        });

        // 3. Màn hình Cấu hình
        GameSetupPanel setupScreen = new GameSetupPanel(gameMap, (gameMode, mapIndex) -> {
            gameMap.setMapIndex(mapIndex);

            // Khởi tạo phiên chơi game và bắt đầu Game Loop
            initGameSession(gameMode);

            cardLayout.show(mainContainer, "PLAYING");

            // Yêu cầu component nhận diện sự kiện bàn phím ngay khi vào game
            Component currentCard = null;
            for (Component comp : mainContainer.getComponents()) {
                if (comp.isVisible()) {
                    currentCard = comp;
                    break;
                }
            }
            if (currentCard != null) {
                currentCard.requestFocusInWindow();
            }
        });

        // Thêm tất cả các màn hình vào CardLayout container một cách ngăn nắp
        mainContainer.add(homeScreen, "HOME");
        mainContainer.add(settingsScreen, "SETTINGS");
        mainContainer.add(setupScreen, "SETUP");

        add(mainContainer);

        setPreferredSize(initialSize);
        pack();
        setLocationRelativeTo(null); // Hiển thị giữa màn hình
        setVisible(true);
    }

    private void initGameSession(int gameMode) {
        if (gameMap != null) {
            gameMap.resetMap();
        }
        ItemManager itemManager = new ItemManager();
        List<PlayerOb> players = new ArrayList<>();

        int mapWidth = gameMap.getWidth();
        int mapHeight = gameMap.getHeight();

        // Tọa độ 4 góc an toàn (tránh kẹt tường cố định biên)
        int xTopLeft = 1, yTopLeft = 1;
        int xTopRight = mapWidth - 2, yTopRight = 1;
        int xBottomLeft = 1, yBottomLeft = mapHeight - 2;
        int xBottomRight = mapWidth - 2, yBottomRight = mapHeight - 2;

        if (gameMode == 0) {
            // Chế độ 0: 1 Người chơi + 3 Bot
            players.add(new Player(xTopLeft, yTopLeft, 0.1, 3, 1, 1, "Player 1")); // Góc Trên-Trái
            players.add(new Bot(xTopRight, yTopRight, 0.1, 3, 1, 1));             // Góc Trên-Phải
            players.add(new Bot(xBottomLeft, yBottomLeft, 0.1, 3, 1, 1));         // Góc Dưới-Trái
            players.add(new Bot(xBottomRight, yBottomRight, 0.1, 3, 1, 1));       // Góc Dưới-Phải

        } else {
            // Chế độ 1: 2 Người chơi + 2 Bot
            players.add(new Player(xTopLeft, yTopLeft, 0.1, 3, 1, 1, "Player 1")); // Góc Trên-Trái
            players.add(new Player(xTopRight, yTopRight, 0.1, 3, 1, 1, "Player 2")); // Góc Trên-Phải
            players.add(new Bot(xBottomLeft, yBottomLeft, 0.1, 3, 1, 1));         // Góc Dưới-Trái
            players.add(new Bot(xBottomRight, yBottomRight, 0.1, 3, 1, 1));       // Góc Dưới-Phải
        }

        // Trích xuất an toàn Player 1 và Player 2 để phục vụ effectively final cho inner class
        Player p1 = (players.size() > 0 && players.get(0) instanceof Player) ? (Player) players.get(0) : null;
        Player p2 = (players.size() > 1 && players.get(1) instanceof Player) ? (Player) players.get(1) : null;

        BombManager bombManager = new BombManager(gameMap, players, itemManager);

        // Tạo màn hình chơi game chính
        MapPanel mapPanel = new MapPanel(gameMap, players, bombManager, itemManager);

        // Bật bộ điều khiển phím cho người chơi
        PlayerController playerController = new PlayerController(p1, p2, gameMap, mapPanel, bombManager);
        playerController.setupControls(mapPanel);

        mainContainer.add(mapPanel, "PLAYING");
        mainContainer.revalidate();
        mainContainer.repaint();

        mapPanel.setFocusable(true);
        mapPanel.requestFocusInWindow();

        // --- THIẾT LẬP GAME LOOP ---
        if (gameLoopTimer != null && gameLoopTimer.isRunning()) {
            gameLoopTimer.stop();
        }

        gameLoopTimer = new Timer(25, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 1. Xử lý di chuyển liên tục cho Player
                updatePlayerMovement(playerController, p1, p2, bombManager);

                // 2. Cập nhật AI di chuyển, né bom và đặt bom cho Bot
                for (PlayerOb p : players) {
                    if (p instanceof Bot && p.isAlive()) {
                        ((Bot) p).update(gameMap, bombManager);
                    }
                }

                // 3. Cập nhật trạng thái bom và vụ nổ
                bombManager.update();

                // 4. Kiểm tra va chạm nhặt item của người chơi/bot
                itemManager.checkPlayerCollision(players);

                // 5. Kiểm tra điều kiện Thắng / Thua
                checkWinCondition(players, p1, p2, gameMode);

                // 6. Yêu cầu vẽ lại màn hình game
                mapPanel.repaint();
            }
        });
        gameLoopTimer.start();
    }

    /**
     * Kiểm tra điều kiện thắng thua của trận đấu
     */
    private void checkWinCondition(List<PlayerOb> players, Player p1, Player p2, int gameMode) {
        if (gameLoopTimer == null || !gameLoopTimer.isRunning()) return;

        if (gameMode == 0) {
            // --- CHẾ ĐỘ: 1 Player + 3 Bot ---
            boolean p1Alive = (p1 != null && p1.isAlive());

            // Đếm số lượng Bot còn sống
            long livingBotsCount = players.stream()
                    .filter(p -> p instanceof Bot && p.isAlive())
                    .count();

            if (!p1Alive) {
                // Player chết -> Thua cuộc
                gameLoopTimer.stop();
                JOptionPane.showMessageDialog(this, "GAME OVER! Bạn đã bị tiêu diệt.", "Thua Cuộc", JOptionPane.INFORMATION_MESSAGE);
                cardLayout.show(mainContainer, "HOME"); // Quay về trang chủ
            } else if (livingBotsCount == 0) {
                // Player còn sống và tất cả Bot đã chết -> Thắng cuộc
                gameLoopTimer.stop();
                JOptionPane.showMessageDialog(this, "VICTORY! Bạn đã tiêu diệt toàn bộ Bot!", "Chiến Thắng", JOptionPane.INFORMATION_MESSAGE);
                cardLayout.show(mainContainer, "HOME"); // Quay về trang chủ
            }

        } else {
            // --- CHẾ ĐỘ: 2 Player + 2 Bot ---
            boolean p1Alive = (p1 != null && p1.isAlive());
            boolean p2Alive = (p2 != null && p2.isAlive());

            long livingPlayersCount = 0;
            if (p1Alive) livingPlayersCount++;
            if (p2Alive) livingPlayersCount++;

            long livingBotsCount = players.stream()
                    .filter(p -> p instanceof Bot && p.isAlive())
                    .count();

            // Nếu chỉ còn đúng 1 người chơi sống và không còn bot nào (hoặc ngược lại)
            if (livingPlayersCount + livingBotsCount <= 1) {
                gameLoopTimer.stop();
                String message = "Trận đấu kết thúc! ";
                if (p1Alive && !p2Alive) message += "Player 1 Thắng!";
                else if (!p1Alive && p2Alive) message += "Player 2 Thắng!";
                else if (p1Alive && p2Alive) message += "Cả 2 Người chơi cùng Thắng!";
                else message += "Tất cả đều đã hy sinh!";

                JOptionPane.showMessageDialog(this, message, "Kết Quả Trận Đấu", JOptionPane.INFORMATION_MESSAGE);
                cardLayout.show(mainContainer, "HOME");
            }
        }
    }

    /**
     * Kiểm tra các phím đang được giữ để di chuyển nhân vật mượt mà
     */
    private void updatePlayerMovement(PlayerController controller, Player p1, Player p2, BombManager bombManager) {
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::new);
    }
}