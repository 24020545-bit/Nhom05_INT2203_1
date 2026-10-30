package ui;

import entity.Player;
import entity.bot.DollBot;
import entity.bot.KondoriaBot;
import entity.mob.Mob;
import entity.mob.Mobs;
import bomb.Bomb;
import item.Item;
import map.GameMap;
import ui.screen.LevelSelectScreen;


import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

public class GamePanel extends JPanel {

    private final int TILE_SIZE = 32;
    private final GameFrame parentFrame;
    private final int currentLevel;

    private GameMap gameMap;
    private Player player;
    private List<Bomb> activeBombs;
    private Timer gameTimer;
    private boolean isLevelCompleted = false; // Cờ hiệu tránh lặp sự kiện kết thúc màn

    public GamePanel(GameFrame parentFrame, int levelIndex) {
        this.parentFrame = parentFrame;
        this.currentLevel = levelIndex;

        setFocusable(true);
        setBackground(Color.DARK_GRAY);
        setPreferredSize(new Dimension(800, 600));

        // Xóa danh sách mob cũ trước khi vào màn mới
        Mobs.MOBS.clear();

        // 1. Khởi tạo bản đồ
        gameMap = new GameMap();

        // 2. Khởi tạo Player
        player = new Player(TILE_SIZE, TILE_SIZE, gameMap.getGrid());
        Mobs.add(player);

        // 3. Khởi tạo danh sách bom toàn cục
        activeBombs = new ArrayList<>();

        // 4. Khởi tạo Bot tùy theo cấp độ (Càng cao càng nhiều bot)
        Mobs.add(new DollBot(5 * TILE_SIZE, 3 * TILE_SIZE, TILE_SIZE, TILE_SIZE, 1, gameMap.getGrid()));
        if (levelIndex >= 2) {
            Mobs.add(new KondoriaBot(7 * TILE_SIZE, 3 * TILE_SIZE, TILE_SIZE, TILE_SIZE, 1, player, gameMap.getGrid()));
        }

        // 5. Khởi tạo Item
        Mobs.add(new Item(3 * TILE_SIZE, TILE_SIZE, Item.ItemType.RANGE_UP, gameMap.getGrid()));
        Mobs.add(new Item(4 * TILE_SIZE, TILE_SIZE, Item.ItemType.SPEED_UP, gameMap.getGrid()));

        // 6. Xử lý bàn phím
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    gameTimer.stop();
                    parentFrame.showScreen("LEVEL_SELECT");
                }
                player.handleKeyPressed(e, activeBombs);
            }

            @Override
            public void keyReleased(KeyEvent e) {
                player.handleKeyReleased(e);
            }
        });

        // 7. Game Loop - 60 FPS
        gameTimer = new Timer(16, e -> {
            updateGame();
            repaint();
        });

        gameTimer.start();
    }

    // Cập nhật trạng thái game
    private void updateGame() {
        if (isLevelCompleted) return;

        // Cập nhật tất cả Mob
        for (int i = 0; i < Mobs.MOBS.size(); i++) {
            Mob mob = Mobs.MOBS.get(i);
            mob.update();
        }

        // Xóa Mob đã chết, ngoại trừ Player
        Mobs.MOBS.removeIf(
                m -> !m.isSurvival() && !(m instanceof Player)
        );

        // Cập nhật bom toàn cục
        for (int i = activeBombs.size() - 1; i >= 0; i--) {
            Bomb bomb = activeBombs.get(i);
            bomb.update();

            if (!bomb.isActive()) {
                activeBombs.remove(i);
            }
        }

        // ==========================================
        // KIỂM TRA ĐIỀU KIỆN THẮNG: TIÊU DIỆT HẾT BOT
        // ==========================================
        if (player.isSurvival()) {
            // Đếm số lượng bot còn sống (Lọc bỏ Player và Item)
            long remainingBots = Mobs.MOBS.stream()
                    .filter(m -> !(m instanceof Player) && !(m instanceof Item) && m.isSurvival())
                    .count();

            // Nếu không còn con bot nào trên bản đồ -> THẮNG
            if (remainingBots == 0) {
                isLevelCompleted = true;
                gameTimer.stop();

                // 1. Mở khóa màn chơi tiếp theo và tự động lưu vào save.properties
                LevelSelectScreen.unlockNextLevel(currentLevel);

                // 2. Thông báo cho người chơi
                JOptionPane.showMessageDialog(
                        this,
                        "Chúc mừng! Bạn đã tiêu diệt hết bot và vượt qua Cửa ải " + currentLevel + "!",
                        "Chiến Thắng!",
                        JOptionPane.INFORMATION_MESSAGE
                );

                // 3. Quay trở lại màn hình chọn cửa ải
                parentFrame.showScreen("LEVEL_SELECT");
            }
        }

        // Kiểm tra điều kiện thua (Player chết)
        if (!player.isSurvival()) {
            isLevelCompleted = true;
            gameTimer.stop();
            JOptionPane.showMessageDialog(
                    this,
                    "Bạn đã bị tiêu diệt! Trò chơi kết thúc.",
                    "Thua Cuộc",
                    JOptionPane.ERROR_MESSAGE
            );
            parentFrame.showScreen("LEVEL_SELECT");
        }
    }

    // Vẽ bản đồ lấy dữ liệu từ GameMap
    private void drawMap(Graphics2D g2d) {
        int[][] grid = gameMap.getGrid();
        for (int y = 0; y < gameMap.getHeight(); y++) {
            for (int x = 0; x < gameMap.getWidth(); x++) {
                int tile = grid[y][x];
                switch (tile) {
                    case GameMap.EMPTY -> g2d.setColor(Color.DARK_GRAY);
                    case GameMap.WALL_BRICK -> g2d.setColor(new Color(160, 90, 50));
                    case GameMap.WALL_SOLID -> g2d.setColor(Color.GRAY);
                    default -> g2d.setColor(Color.DARK_GRAY);
                }
                g2d.fillRect(x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                g2d.setColor(Color.BLACK);
                g2d.drawRect(x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // 1. Vẽ bản đồ
        drawMap(g2d);

        // 2. Vẽ bom và hiệu ứng nổ
        for (Bomb bomb : activeBombs) {
            bomb.draw(g2d);
        }

        // 3. Vẽ Player, Bot và Item
        for (Mob mob : Mobs.MOBS) {
            mob.draw(g2d);
        }

        // 4. Vẽ HUD
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        g2d.drawString(
                "Cửa ải: " + currentLevel + " | HP: " + player.getHp()
                        + " | Sức mạnh bom: " + player.getPower()
                        + " | Tốc độ: " + player.getSpeed()
                        + " [ESC để thoát]",
                10,
                20
        );
    }
}