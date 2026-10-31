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
import utils.SaveManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class GamePanel extends JPanel {

    private final int TILE_SIZE = 32;
    private final GameFrame parentFrame;
    private final int currentLevel;

    private GameMap gameMap;
    private Player player;
    private List<Bomb> activeBombs;
    private Timer gameTimer;
    private boolean isLevelCompleted = false;

    // 1. Constructor cho chơi mới từ đầu
    public GamePanel(GameFrame parentFrame, int levelIndex) {
        this(parentFrame, levelIndex, null);
    }

    // 2. Constructor hỗ trợ khôi phục từ file save dở
    // Constructor hỗ trợ khôi phục toàn diện từ file save dở
    public GamePanel(GameFrame parentFrame, int levelIndex, Properties saveProps) {
        this.parentFrame = parentFrame;
        this.currentLevel = levelIndex;

        setFocusable(true);
        setBackground(Color.DARK_GRAY);
        setPreferredSize(new Dimension(800, 600));

        Mobs.MOBS.clear();
        gameMap = new GameMap();
        activeBombs = new ArrayList<>();

        // 1. NẾU CÓ FILE SAVE DỞ -> KHÔI PHỤC MAP, PLAYER VÀ BOT
        if (saveProps != null) {
            try {
                // Khôi phục Map Grid
                String gridStr = saveProps.getProperty("mapGrid");
                if (gridStr != null && !gridStr.isEmpty()) {
                    String[] parts = gridStr.split(",");
                    int height = gameMap.getHeight();
                    int width = gameMap.getWidth();
                    int[][] loadedGrid = new int[height][width];
                    int index = 0;
                    for (int y = 0; y < height; y++) {
                        for (int x = 0; x < width; x++) {
                            if (index < parts.length) {
                                loadedGrid[y][x] = Integer.parseInt(parts[index++]);
                            }
                        }
                    }
                    gameMap.setGrid(loadedGrid);
                }

                // Khôi phục Player
                player = new Player(TILE_SIZE, TILE_SIZE, gameMap.getGrid());
                player.setX(Double.parseDouble(saveProps.getProperty("playerX", String.valueOf(TILE_SIZE))));
                player.setY(Double.parseDouble(saveProps.getProperty("playerY", String.valueOf(TILE_SIZE))));
                // player.setHp(Integer.parseInt(saveProps.getProperty("playerHp", "1")));
                Mobs.add(player);

                // Khôi phục danh sách Bot đã lưu
                int botCount = Integer.parseInt(saveProps.getProperty("botCount", "0"));
                for (int i = 0; i < botCount; i++) {
                    String type = saveProps.getProperty("bot_" + i + "_type");
                    double bx = Double.parseDouble(saveProps.getProperty("bot_" + i + "_x"));
                    double by = Double.parseDouble(saveProps.getProperty("bot_" + i + "_y"));
                    int bhp = Integer.parseInt(saveProps.getProperty("bot_" + i + "_hp"));

                    if (type != null && type.contains("DollBot")) {
                        DollBot bot = new DollBot(bx, by, TILE_SIZE, TILE_SIZE, 1, gameMap.getGrid());
                        bot.setHp(bhp);
                        Mobs.add(bot);
                    } else if (type != null && type.contains("KondoriaBot")) {
                        KondoriaBot bot = new KondoriaBot(bx, by, TILE_SIZE, TILE_SIZE, 1, player, gameMap.getGrid());
                        bot.setHp(bhp);
                        Mobs.add(bot);
                    }
                }
                System.out.println("Đã khôi phục thành công toàn bộ trạng thái game dở!");

            } catch (Exception e) {
                System.err.println("Lỗi khôi phục trạng thái game dở: " + e.getMessage());
                // Fallback nếu lỗi: Khởi tạo mới hoàn toàn
                initDefaultGame(levelIndex);
            }
        } else {
            // 2. KHỞI TẠO MỚI HOÀN TOÀN NẾU KHÔNG CÓ SAVE
            initDefaultGame(levelIndex);
        }

        // Xử lý phím bấm (ESC để lưu cả Map, Player và Bot)
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    gameTimer.stop();
                    // Lưu trạng thái đầy đủ kèm theo grid của map
                    SaveManager.saveGameProgress(currentLevel, player, gameMap.getGrid());
                    JOptionPane.showMessageDialog(GamePanel.this, "Đã lưu tiến trình màn chơi (Map, Bot, Nhân vật)!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                    parentFrame.showScreen("LEVEL_SELECT");
                }
                player.handleKeyPressed(e, activeBombs);
            }

            @Override
            public void keyReleased(KeyEvent e) {
                player.handleKeyReleased(e);
            }
        });

        gameTimer = new Timer(16, e -> {
            updateGame();
            repaint();
        });

        gameTimer.start();
    }

    // Hàm phụ trợ khởi tạo mặc định khi chơi mới
    private void initDefaultGame(int levelIndex) {
        player = new Player(TILE_SIZE, TILE_SIZE, gameMap.getGrid());
        Mobs.add(player);
        initBots(levelIndex);
        // Khởi tạo Item
        Mobs.add(new Item(3 * TILE_SIZE, TILE_SIZE, Item.ItemType.RANGE_UP, gameMap.getGrid()));
        Mobs.add(new Item(4 * TILE_SIZE, TILE_SIZE, Item.ItemType.SPEED_UP, gameMap.getGrid()));
    }

    private void initBots(int levelIndex) {
        if (levelIndex == 1) {
            Mobs.add(new DollBot(5 * TILE_SIZE, 3 * TILE_SIZE, TILE_SIZE, TILE_SIZE, 1, gameMap.getGrid()));
        } else if (levelIndex == 2) {
            Mobs.add(new DollBot(5 * TILE_SIZE, 3 * TILE_SIZE, TILE_SIZE, TILE_SIZE, 1, gameMap.getGrid()));
            Mobs.add(new KondoriaBot(7 * TILE_SIZE, 3 * TILE_SIZE, TILE_SIZE, TILE_SIZE, 1, player, gameMap.getGrid()));
        } else {
            Mobs.add(new DollBot(5 * TILE_SIZE, 3 * TILE_SIZE, TILE_SIZE, TILE_SIZE, 1, gameMap.getGrid()));
            Mobs.add(new KondoriaBot(7 * TILE_SIZE, 3 * TILE_SIZE, TILE_SIZE, TILE_SIZE, 1, player, gameMap.getGrid()));
            Mobs.add(new KondoriaBot(10 * TILE_SIZE, 8 * TILE_SIZE, TILE_SIZE, TILE_SIZE, 2, player, gameMap.getGrid()));
        }
    }

    private void updateGame() {
        if (isLevelCompleted) return;

        for (int i = 0; i < Mobs.MOBS.size(); i++) {
            Mob mob = Mobs.MOBS.get(i);
            mob.update();
        }

        Mobs.MOBS.removeIf(m -> !m.isSurvival() && !(m instanceof Player));

        for (int i = activeBombs.size() - 1; i >= 0; i--) {
            Bomb bomb = activeBombs.get(i);
            bomb.update();
            if (!bomb.isActive()) {
                activeBombs.remove(i);
            }
        }

        // Kiểm tra thắng
        if (player.isSurvival()) {
            long remainingBots = Mobs.MOBS.stream()
                    .filter(m -> !(m instanceof Player) && !(m instanceof Item) && m.isSurvival())
                    .count();

            if (remainingBots == 0) {
                isLevelCompleted = true;
                gameTimer.stop();
                LevelSelectScreen.unlockNextLevel(currentLevel);
                SaveManager.clearSaveFile(); // Xóa save dở vì đã thắng màn

                JOptionPane.showMessageDialog(this, "Chúc mừng! Bạn đã vượt qua Cửa ải " + currentLevel + "!", "Chiến Thắng!", JOptionPane.INFORMATION_MESSAGE);
                parentFrame.showScreen("LEVEL_SELECT");
            }
        }

        // Kiểm tra thua
        if (!player.isSurvival()) {
            isLevelCompleted = true;
            gameTimer.stop();
            SaveManager.clearSaveFile(); // Xóa save dở vì đã thua

            JOptionPane.showMessageDialog(this, "Bạn đã bị tiêu diệt! Trò chơi kết thúc.", "Thua Cuộc", JOptionPane.ERROR_MESSAGE);
            parentFrame.showScreen("LEVEL_SELECT");
        }
    }

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

        drawMap(g2d);

        for (Bomb bomb : activeBombs) {
            bomb.draw(g2d);
        }

        for (Mob mob : Mobs.MOBS) {
            mob.draw(g2d);
        }

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        g2d.drawString(
                "Cửa ải: " + currentLevel + " | HP: " + player.getHp()
                        + " | Sức mạnh bom: " + player.getPower()
                        + " | Tốc độ: " + player.getSpeed()
                        + " [ESC để lưu & thoát]",
                10,
                20
        );
    }
}