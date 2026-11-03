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
import utils.SoundManager; // THÊM IMPORT SOUND MANAGER

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
        this.playerCount = Math.clamp(playerCount, 1, 2);

        setFocusable(true);
        setBackground(Color.DARK_GRAY);

        Mobs.MOBS.clear();
        gameMap = new GameMap();
        gameMap.loadLevelMap(levelIndex);

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

                adjustWindowSize();

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

    /** Điều chỉnh kích thước cửa sổ vừa khít với bản đồ chơi và canh giữa tuyệt đối[cite: 1] */
    public void adjustWindowSize() {
        int mapWidthPixel = gameMap.getWidth() * TILE_SIZE;
        int mapHeightPixel = gameMap.getHeight() * TILE_SIZE + 40;

        Dimension dim = new Dimension(mapWidthPixel, mapHeightPixel);
        setPreferredSize(dim);
        setSize(dim);

        if (parentFrame != null) {
            parentFrame.pack();
            parentFrame.setLocationRelativeTo(null); // Canh giữa màn hình[cite: 1]
        }
    }

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
        adjustWindowSize();

        double[][] spawnPositions = {
                {TILE_SIZE, TILE_SIZE},
                {(gameMap.getWidth() - 2) * TILE_SIZE, (gameMap.getHeight() - 2) * TILE_SIZE}
        };

        for (int i = 0; i < numPlayers; i++) {
            Player p = new Player(spawnPositions[i][0], spawnPositions[i][1], gameMap.getGrid());
            p.setPlayerName("Player " + (i + 1));
            players.add(p);
            Mobs.add(p);
        }

        initBots(levelIndex, numPlayers);
    }

    private void spawnBot(String type, int tileX, int tileY, int speed, int hp, int power, long cooldown, Player target, Player[] allPlayers, int[][] grid) {
        double x = tileX * TILE_SIZE;
        double y = tileY * TILE_SIZE;
        Mob bot = switch (type) {
            case "Doll" -> new DollBot(x, y, TILE_SIZE, TILE_SIZE, speed, hp, power, cooldown, target, grid);
            case "Kondoria" -> new KondoriaBot(x, y, TILE_SIZE, TILE_SIZE, speed, hp, power, cooldown, allPlayers, grid);
            case "Boss" -> new BossBot(x, y, TILE_SIZE, TILE_SIZE, speed, hp, cooldown, grid);
            default -> null;
        };
        if (bot != null) Mobs.add(bot);
    }

    private void initBots(int levelIndex, int numPlayers) {
        Player[] activePlayersArray = players.toArray(new Player[0]);
        Player targetForDoll = activePlayersArray[0];

        double diff = (numPlayers >= 2) ? 1.25 : 1.0;

        switch (levelIndex) {
            case 1 -> {
                spawnBot("Doll", 5, 3, (int)(1*diff), (int)(2*diff), (int)(1*diff), (long)(1500/diff), targetForDoll, activePlayersArray, gameMap.getGrid());
                if (numPlayers >= 2) {
                    spawnBot("Doll", 10, 5, (int)(1*diff), (int)(2*diff), (int)(1*diff), (long)(1500/diff), targetForDoll, activePlayersArray, gameMap.getGrid());
                }
            }
            case 2 -> {
                spawnBot("Doll", 5, 3, (int)(1*diff), (int)(3*diff), (int)(1*diff), (long)(1200/diff), targetForDoll, activePlayersArray, gameMap.getGrid());
                spawnBot("Kondoria", 7, 3, (int)(1*diff), (int)(3*diff), (int)(1*diff), (long)(1000/diff), targetForDoll, activePlayersArray, gameMap.getGrid());
                if (numPlayers >= 2) {
                    spawnBot("Kondoria", 12, 7, (int)(1*diff), (int)(3*diff), (int)(1*diff), (long)(1000/diff), targetForDoll, activePlayersArray, gameMap.getGrid());
                }
            }
            case 3 -> {
                int bossHp = (int)((25 + numPlayers * 5) * diff);
                spawnBot("Boss", 8, 5, (int)(1*diff), bossHp, 0, (long)(6000/diff), targetForDoll, activePlayersArray, gameMap.getGrid());
                spawnBot("Kondoria", 2, 2, (int)(1*diff), (int)(4*diff), (int)(1*diff), (long)(800/diff), targetForDoll, activePlayersArray, gameMap.getGrid());
            }
            case 4 -> {
                spawnBot("Doll", 3, 3, (int)(2*diff), (int)(4*diff), (int)(1*diff), (long)(900/diff), targetForDoll, activePlayersArray, gameMap.getGrid());
                spawnBot("Doll", 12, 8, (int)(2*diff), (int)(4*diff), (int)(1*diff), (long)(900/diff), targetForDoll, activePlayersArray, gameMap.getGrid());
                spawnBot("Kondoria", 8, 4, (int)(1*diff), (int)(6*diff), (int)(1*diff), (long)(700/diff), targetForDoll, activePlayersArray, gameMap.getGrid());
            }
            case 5 -> {
                int bossHp = (int)((35 + numPlayers * 5) * diff);
                spawnBot("Boss", 10, 5, (int)(1*diff), bossHp, 0, (long)(5000/diff), targetForDoll, activePlayersArray, gameMap.getGrid());
                spawnBot("Kondoria", 4, 3, (int)(1*diff), (int)(6*diff), (int)(1*diff), (long)(600/diff), targetForDoll, activePlayersArray, gameMap.getGrid());
                spawnBot("Kondoria", 15, 8, (int)(1*diff), (int)(6*diff), (int)(1*diff), (long)(600/diff), targetForDoll, activePlayersArray, gameMap.getGrid());
            }
            default -> {
                spawnBot("Doll", 3, 3, (int)(2*diff), (int)(6*diff), (int)(1*diff), (long)(700/diff), targetForDoll, activePlayersArray, gameMap.getGrid());
                spawnBot("Kondoria", 10, 8, (int)(2*diff), (int)(10*diff), (int)(1*diff), (long)(400/diff), targetForDoll, activePlayersArray, gameMap.getGrid());
                spawnBot("Boss", 11, 5, (int)(1*diff), (int)(55*diff), 0, (long)(3500/diff), targetForDoll, activePlayersArray, gameMap.getGrid());
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

        // Kiểm tra và phát âm thanh khi nhân vật (Player hoặc Bot) chết
        for (int i = Mobs.MOBS.size() - 1; i >= 0; i--) {
            Mob m = Mobs.MOBS.get(i);
            if (!m.isSurvival()) {
                if (m instanceof Player) {
                    SoundManager.playSoundEffect("/audio/dead1.wav");
                } else {
                    SoundManager.playSoundEffect("/audio/dead1.wav");
                }
                Mobs.MOBS.remove(i);
            }
        }

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

                // PHÁT ÂM THANH CHIẾN THẮNG / QUA MÀN
                SoundManager.playSoundEffect("/audio/quaman.wav"); // Bạn có thể đổi thành "/audio/win.wav" nếu có file riêng

                JOptionPane.showMessageDialog(this, "Chúc mừng! Đội của bạn đã vượt qua Cửa ải " + currentLevel + "!", "Chiến Thắng!", JOptionPane.INFORMATION_MESSAGE);
                parentFrame.showScreen("LEVEL_SELECT");
            }
        } else {
            isLevelCompleted = true;
            gameTimer.stop();
            SaveManager.clearSaveFile();

            // PHÁT ÂM THANH GAME OVER KHI THUA CUỘC
            SoundManager.playSoundEffect("/audio/dead2.wav");

            JOptionPane.showMessageDialog(this, "Tất cả người chơi đã hy sinh! Trò chơi kết thúc.", "Thua Cuộc", JOptionPane.ERROR_MESSAGE);
            parentFrame.showScreen("LEVEL_SELECT");
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        int mapWidthPixel = gameMap.getWidth() * TILE_SIZE;
        int mapHeightPixel = gameMap.getHeight() * TILE_SIZE;
        int panelWidth = getWidth();
        int panelHeight = getHeight();

        int offsetX = Math.max(0, (panelWidth - mapWidthPixel) / 2);
        int offsetY = Math.max(0, (panelHeight - mapHeightPixel) / 2);

        g2d.translate(offsetX, offsetY);

        gameMap.draw(g2d);

        for (Item item : activeItems) {
            item.draw(g2d);
        }

        for (Bomb bomb : activeBombs) {
            bomb.draw(g2d);
        }

        for (Mob mob : Mobs.MOBS) {
            mob.draw(g2d);
        }

        g2d.translate(-offsetX, -offsetY);

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