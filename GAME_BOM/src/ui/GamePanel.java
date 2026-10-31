package ui;

import entity.Player;
import entity.bot.BossBot;
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
    private final int playerCount;

    private final GameMap gameMap;
    private final List<Player> players;
    private final List<Bomb> activeBombs;
    private final List<Item> activeItems;
    private final Timer gameTimer;
    private boolean isLevelCompleted = false;

    public GamePanel(GameFrame parentFrame, int levelIndex, int playerCount) {
        this(parentFrame, levelIndex, playerCount, null);
    }

    public GamePanel(GameFrame parentFrame, int levelIndex, int playerCount, Properties saveProps) {
        this.parentFrame = parentFrame;
        this.currentLevel = levelIndex;
        this.playerCount = Math.clamp(playerCount, 1, 2); // Giới hạn từ 1 đến 2 người chơi

        setFocusable(true);
        setBackground(Color.DARK_GRAY);
        setPreferredSize(new Dimension(800, 600));

        Mobs.MOBS.clear();
        gameMap = new GameMap();
        activeBombs = new ArrayList<>();
        activeItems = new ArrayList<>();
        players = new ArrayList<>();

        if (saveProps != null) {
            try {
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

                // Khôi phục danh sách người chơi từ save
                int savedPlayerCount = Integer.parseInt(saveProps.getProperty("playerCount", "1"));
                for (int i = 0; i < savedPlayerCount; i++) {
                    Player p = new Player(TILE_SIZE, TILE_SIZE, gameMap.getGrid());
                    p.setPlayerName("Player " + (i + 1));
                    p.setX(Double.parseDouble(saveProps.getProperty("player_" + i + "_x", String.valueOf(TILE_SIZE))));
                    p.setY(Double.parseDouble(saveProps.getProperty("player_" + i + "_y", String.valueOf(TILE_SIZE))));
                    p.setHp(Integer.parseInt(saveProps.getProperty("player_" + i + "_hp", "3")));
                    p.setPower(Integer.parseInt(saveProps.getProperty("player_" + i + "_power", "1")));
                    p.setSpeed(Integer.parseInt(saveProps.getProperty("player_" + i + "_speed", "2")));
                    players.add(p);
                    Mobs.add(p);
                }

                // Khôi phục Bot từ save
                int botCount = Integer.parseInt(saveProps.getProperty("botCount", "0"));
                Player[] activePlayersArray = players.toArray(new Player[0]);
                for (int i = 0; i < botCount; i++) {
                    String type = saveProps.getProperty("bot_" + i + "_type");
                    double bx = Double.parseDouble(saveProps.getProperty("bot_" + i + "_x"));
                    double by = Double.parseDouble(saveProps.getProperty("bot_" + i + "_y"));
                    int bhp = Integer.parseInt(saveProps.getProperty("bot_" + i + "_hp"));

                    if (type != null && type.contains("DollBot")) {
                        DollBot bot = new DollBot(bx, by, TILE_SIZE, TILE_SIZE, 1, bhp, 1, 1200, activePlayersArray[0], gameMap.getGrid());
                        Mobs.add(bot);
                    } else if (type != null && type.contains("KondoriaBot")) {
                        KondoriaBot bot = new KondoriaBot(bx, by, TILE_SIZE, TILE_SIZE, 1, bhp, 1, 1000, activePlayersArray, gameMap.getGrid());
                        Mobs.add(bot);
                    } else if (type != null && type.contains("BossBot")) {
                        BossBot boss = new BossBot(bx, by, TILE_SIZE, TILE_SIZE, 1, bhp, 6000, gameMap.getGrid());
                        Mobs.add(boss);
                    }
                }
                System.out.println("Đã khôi phục thành công trạng thái game dở!");

            } catch (Exception e) {
                System.err.println("Lỗi khôi phục trạng thái game dở: " + e.getMessage());
                initDefaultGame(levelIndex, this.playerCount);
            }
        } else {
            initDefaultGame(levelIndex, this.playerCount);
        }

        // Tự động quét bản đồ (grid) để tạo Item từ các số 5 -> 9
        loadItemsFromGrid();

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    gameTimer.stop();
                    SaveManager.saveGameProgress(currentLevel, players, gameMap.getGrid());
                    JOptionPane.showMessageDialog(GamePanel.this, "Đã lưu tiến trình màn chơi!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                    parentFrame.showScreen("LEVEL_SELECT");
                }

                if (!players.isEmpty() && players.getFirst().isSurvival()) {
                    handlePlayer1Input(e, true);
                }
                if (players.size() >= 2 && players.get(1).isSurvival()) {
                    handlePlayer2Input(e, true);
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (!players.isEmpty()) {
                    handlePlayer1Input(e, false);
                }
                if (players.size() >= 2) {
                    handlePlayer2Input(e, false);
                }
            }
        });

        gameTimer = new Timer(16, e -> {
            updateGame();
            repaint();
        });

        gameTimer.start();
    }

    // Phương thức quét mảng grid để khởi tạo Item từ file map (giá trị 5 -> 9)
    private void loadItemsFromGrid() {
        int[][] grid = gameMap.getGrid();
        for (int y = 0; y < grid.length; y++) {
            for (int x = 0; x < grid[y].length; x++) {
                int val = grid[y][x];
                Item.ItemType type = switch (val) {
                    case 5 -> Item.ItemType.DAMAGE_UP;
                    case 6 -> Item.ItemType.SPEED_UP;
                    case 7 -> Item.ItemType.RANGE_UP;
                    case 8 -> Item.ItemType.HEAL;
                    case 9 -> Item.ItemType.BOMB_UP;
                    default -> null;
                };

                if (type != null) {
                    activeItems.add(new Item(x * TILE_SIZE, y * TILE_SIZE, type, grid));
                }
            }
        }
    }

    // Người chơi 1: W, A, S, D + SPACE (Đặt bom)
    private void handlePlayer1Input(KeyEvent e, boolean isPressed) {
        Player p1 = players.getFirst();
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_W || key == KeyEvent.VK_S || key == KeyEvent.VK_A || key == KeyEvent.VK_D || key == KeyEvent.VK_SPACE) {
            if (isPressed) {
                if (key == KeyEvent.VK_SPACE) {
                    p1.dropBomb(gameMap.getGrid(), activeBombs);
                } else {
                    p1.handleKeyPressed(e, activeBombs);
                }
            } else {
                p1.handleKeyReleased(e);
            }
        }
    }

    // Người chơi 2: Các phím Mũi tên + ENTER (Đặt bom)
    private void handlePlayer2Input(KeyEvent e, boolean isPressed) {
        if (players.size() < 2) return;
        Player p2 = players.get(1);
        int key = e.getKeyCode();

        if (key == KeyEvent.VK_UP || key == KeyEvent.VK_DOWN || key == KeyEvent.VK_LEFT || key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_ENTER) {
            if (isPressed) {
                if (key == KeyEvent.VK_ENTER) {
                    p2.dropBomb(gameMap.getGrid(), activeBombs);
                } else {
                    p2.handleKeyPressed(mapToP2Key(e), activeBombs);
                }
            } else {
                p2.handleKeyReleased(mapToP2Key(e));
            }
        }
    }

    private KeyEvent mapToP2Key(KeyEvent e) {
        int code = e.getKeyCode();
        int mappedCode = code;
        if (code == KeyEvent.VK_UP) mappedCode = KeyEvent.VK_W;
        else if (code == KeyEvent.VK_DOWN) mappedCode = KeyEvent.VK_S;
        else if (code == KeyEvent.VK_LEFT) mappedCode = KeyEvent.VK_A;
        else if (code == KeyEvent.VK_RIGHT) mappedCode = KeyEvent.VK_D;

        return new KeyEvent(e.getComponent(), e.getID(), e.getWhen(), e.getModifiersEx(), mappedCode, e.getKeyChar());
    }

    private void initDefaultGame(int levelIndex, int numPlayers) {
        double[][] spawnPositions = {
                {TILE_SIZE, TILE_SIZE},
                {(gameMap.getWidth() - 2) * TILE_SIZE, (gameMap.getHeight() - 2) * TILE_SIZE}
        };

        for (int i = 0; i < numPlayers; i++) {
            Player p = new Player(spawnPositions[i][0], spawnPositions[i][1], gameMap.getGrid());
            p.setPlayerName("Player " + (i + 1)); // Gán tên P1, P2
            players.add(p);
            Mobs.add(p);
        }

        initBots(levelIndex, numPlayers);
    }

    private void initBots(int levelIndex, int numPlayers) {
        Player[] activePlayersArray = players.toArray(new Player[0]);
        Player targetForDoll = activePlayersArray[0];

        double diff = (numPlayers >= 2) ? 1.25 : 1.0;

        switch (levelIndex) {
            case 1 -> {
                Mobs.add(new DollBot(5 * TILE_SIZE, 3 * TILE_SIZE, TILE_SIZE, TILE_SIZE,
                        (int)(1 * diff), (int)(2 * diff), (int)(1 * diff), (long)(1500 / diff),
                        targetForDoll, gameMap.getGrid()));
                if (numPlayers >= 2) {
                    Mobs.add(new DollBot(10 * TILE_SIZE, 5 * TILE_SIZE, TILE_SIZE, TILE_SIZE,
                            (int)(1 * diff), (int)(2 * diff), (int)(1 * diff), (long)(1500 / diff),
                            targetForDoll, gameMap.getGrid()));
                }
            }
            case 2 -> {
                Mobs.add(new DollBot(5 * TILE_SIZE, 3 * TILE_SIZE, TILE_SIZE, TILE_SIZE,
                        (int)(1 * diff), (int)(3 * diff), (int)(1 * diff), (long)(1200 / diff),
                        targetForDoll, gameMap.getGrid()));
                Mobs.add(new KondoriaBot(7 * TILE_SIZE, 3 * TILE_SIZE, TILE_SIZE, TILE_SIZE,
                        (int)(1 * diff), (int)(3 * diff), (int)(1 * diff), (long)(1000 / diff),
                        activePlayersArray, gameMap.getGrid()));
                if (numPlayers >= 2) {
                    Mobs.add(new KondoriaBot(12 * TILE_SIZE, 7 * TILE_SIZE, TILE_SIZE, TILE_SIZE,
                            (int)(1 * diff), (int)(3 * diff), (int)(1 * diff), (long)(1000 / diff),
                            activePlayersArray, gameMap.getGrid()));
                }
            }
            case 3 -> {
                int baseBossHp = 25 + (numPlayers * 5);
                Mobs.add(new BossBot(8 * TILE_SIZE, 5 * TILE_SIZE, TILE_SIZE, TILE_SIZE,
                        (int)(1 * diff), (int)(baseBossHp * diff), (long)(6000 / diff),
                        gameMap.getGrid()));
                Mobs.add(new KondoriaBot(2 * TILE_SIZE, 2 * TILE_SIZE, TILE_SIZE, TILE_SIZE,
                        (int)(1 * diff), (int)(4 * diff), (int)(1 * diff), (long)(800 / diff),
                        activePlayersArray, gameMap.getGrid()));
            }
            case 4 -> {
                Mobs.add(new DollBot(3 * TILE_SIZE, 3 * TILE_SIZE, TILE_SIZE, TILE_SIZE,
                        (int)(2 * diff), (int)(4 * diff), (int)(1 * diff), (long)(900 / diff),
                        targetForDoll, gameMap.getGrid()));
                Mobs.add(new DollBot(12 * TILE_SIZE, 8 * TILE_SIZE, TILE_SIZE, TILE_SIZE,
                        (int)(2 * diff), (int)(4 * diff), (int)(1 * diff), (long)(900 / diff),
                        targetForDoll, gameMap.getGrid()));
                Mobs.add(new KondoriaBot(8 * TILE_SIZE, 4 * TILE_SIZE, TILE_SIZE, TILE_SIZE,
                        (int)(1 * diff), (int)(6 * diff), (int)(1 * diff), (long)(700 / diff),
                        activePlayersArray, gameMap.getGrid()));
            }
            case 5 -> {
                int bossHp = 35 + (numPlayers * 5);
                Mobs.add(new BossBot(10 * TILE_SIZE, 5 * TILE_SIZE, TILE_SIZE, TILE_SIZE,
                        (int)(1 * diff), (int)(bossHp * diff), (long)(5000 / diff),
                        gameMap.getGrid()));
                Mobs.add(new KondoriaBot(4 * TILE_SIZE, 3 * TILE_SIZE, TILE_SIZE, TILE_SIZE,
                        (int)(1 * diff), (int)(6 * diff), (int)(1 * diff), (long)(600 / diff),
                        activePlayersArray, gameMap.getGrid()));
                Mobs.add(new KondoriaBot(15 * TILE_SIZE, 8 * TILE_SIZE, TILE_SIZE, TILE_SIZE,
                        (int)(1 * diff), (int)(6 * diff), (int)(1 * diff), (long)(600 / diff),
                        activePlayersArray, gameMap.getGrid()));
            }
            default -> {
                Mobs.add(new DollBot(3 * TILE_SIZE, 3 * TILE_SIZE, TILE_SIZE, TILE_SIZE,
                        (int)(2 * diff), (int)(6 * diff), (int)(1 * diff), (long)(700 / diff),
                        targetForDoll, gameMap.getGrid()));
                Mobs.add(new KondoriaBot(10 * TILE_SIZE, 8 * TILE_SIZE, TILE_SIZE, TILE_SIZE,
                        (int)(2 * diff), (int)(10 * diff), (int)(1 * diff), (long)(400 / diff),
                        activePlayersArray, gameMap.getGrid()));
                Mobs.add(new BossBot(11 * TILE_SIZE, 5 * TILE_SIZE, TILE_SIZE, TILE_SIZE,
                        (int)(1 * diff), (int)(55 * diff), (long)(3500 / diff),
                        gameMap.getGrid()));
            }
        }
    }

    private void updateGame() {
        if (isLevelCompleted) return;

        for (int i = 0; i < Mobs.MOBS.size(); i++) {
            Mob mob = Mobs.MOBS.get(i);
            mob.update();
        }

        for (Item item : activeItems) {
            for (Player p : players) {
                if (p.isSurvival()) {
                    item.update(p);
                }
            }
        }
        activeItems.removeIf(Item::isCollected);

        Mobs.MOBS.removeIf(m -> !m.isSurvival());

        for (int i = activeBombs.size() - 1; i >= 0; i--) {
            Bomb bomb = activeBombs.get(i);
            bomb.update();
            if (!bomb.isActive()) {
                activeBombs.remove(i);
            }
        }

        boolean anyPlayerAlive = players.stream().anyMatch(Player::isSurvival);
        if (anyPlayerAlive) {
            long remainingBots = Mobs.MOBS.stream()
                    .filter(m -> !(m instanceof Player) && m.isSurvival())
                    .count();

            if (remainingBots == 0) {
                isLevelCompleted = true;
                gameTimer.stop();
                LevelSelectScreen.unlockNextLevel(currentLevel);
                SaveManager.clearSaveFile();

                JOptionPane.showMessageDialog(this, "Chúc mừng! Đội của bạn đã vượt qua Cửa ải " + currentLevel + "!", "Chiến Thắng!", JOptionPane.INFORMATION_MESSAGE);
                parentFrame.showScreen("LEVEL_SELECT");
            }
        } else {
            isLevelCompleted = true;
            gameTimer.stop();
            SaveManager.clearSaveFile();

            JOptionPane.showMessageDialog(this, "Tất cả người chơi đã hy sinh! Trò chơi kết thúc.", "Thua Cuộc", JOptionPane.ERROR_MESSAGE);
            parentFrame.showScreen("LEVEL_SELECT");
        }
    }

    private void drawMap(Graphics2D g2d) {
        int[][] grid = gameMap.getGrid();
        for (int y = 0; y < gameMap.getHeight(); y++) {
            for (int x = 0; x < gameMap.getWidth(); x++) {
                int tile = grid[y][x];
                // Các ô chứa item (5 -> 9) khi vẽ bản đồ sẽ hiển thị nền đất trống (EMPTY)
                if (tile >= 5 && tile <= 9) {
                    g2d.setColor(Color.DARK_GRAY);
                } else {
                    switch (tile) {
                        case GameMap.WALL_BRICK -> g2d.setColor(new Color(160, 90, 50));
                        case GameMap.WALL_SOLID -> g2d.setColor(Color.GRAY);
                        default -> g2d.setColor(Color.DARK_GRAY);
                    }
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

        for (Item item : activeItems) {
            item.draw(g2d);
        }

        for (Bomb bomb : activeBombs) {
            bomb.draw(g2d);
        }

        for (Mob mob : Mobs.MOBS) {
            mob.draw(g2d);
        }

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 13));
        int yOffset = 20;
        for (int i = 0; i < players.size(); i++) {
            Player p = players.get(i);
            String status = p.isSurvival() ? "" : " [ĐÃ HI SINH]";
            g2d.drawString(
                    "P" + (i + 1) + " | HP: " + p.getHp()
                            + " | Sức mạnh: " + p.getPower()
                            + " | Tốc độ: " + p.getSpeed() + status,
                    10, yOffset
            );
            yOffset += 18;
        }
        g2d.drawString("Cửa ải: " + currentLevel + " [ESC để lưu & thoát]", 10, yOffset + 5);
    }
}