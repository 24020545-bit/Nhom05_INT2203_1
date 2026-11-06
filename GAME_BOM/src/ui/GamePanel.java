package ui;

import entity.Player;
import entity.bot.BossBot;
import entity.bot.DollBot;
import entity.bot.KondoriaBot;
import entity.bot.BotSpawner;
import entity.mob.Mob;
import entity.mob.Mobs;
import bomb.Bomb;
import item.Item;
import map.GameMap;
import ui.screen.LevelSelectScreen;
import utils.SaveManager;
import utils.SoundManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class GamePanel extends JPanel {

    private final int TILE_SIZE = 32;
    private static final int HUD_HEIGHT = 45; // Chiều cao vừa đủ cho 2-3 dòng chữ HUD

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

            } catch (Exception e) {
                initDefaultGame(levelIndex, this.playerCount);
            }
        } else {
            initDefaultGame(levelIndex, this.playerCount);
        }

        adjustWindowSize();
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

    public void adjustWindowSize() {
        if (gameMap == null) return;

        int mapWidthPixel = gameMap.getWidth() * TILE_SIZE;
        int mapHeightPixel = gameMap.getHeight() * TILE_SIZE + HUD_HEIGHT;

        Dimension dim = new Dimension(mapWidthPixel, mapHeightPixel);
        setPreferredSize(dim);
        setMinimumSize(dim);

        if (parentFrame != null) {
            parentFrame.revalidate();
            parentFrame.pack();
            parentFrame.setLocationRelativeTo(null);
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

    private void initBots(int levelIndex, int numPlayers) {
        BotSpawner.setGrid(gameMap.getGrid());

        switch (levelIndex) {
            case 1 -> {
                BotSpawner.spawn("Doll", 5, 3, levelIndex, numPlayers);
                if (numPlayers >= 2) {
                    BotSpawner.spawn("Doll", 10, 5, levelIndex, numPlayers);
                }
            }
            case 2 -> {
                BotSpawner.spawn("Doll", 5, 3, levelIndex, numPlayers);
                BotSpawner.spawn("Kondoria", 7, 3, levelIndex, numPlayers);
                if (numPlayers >= 2) {
                    BotSpawner.spawn("Kondoria", 12, 7, levelIndex, numPlayers);
                }
            }
            case 3 -> {
                BotSpawner.spawn("Boss", 9, 5, levelIndex, numPlayers);
                BotSpawner.spawn("Kondoria", 2, 2, levelIndex, numPlayers);
            }
            case 4 -> {
                BotSpawner.spawn("Doll", 3, 3, levelIndex, numPlayers);
                BotSpawner.spawn("Doll", 12, 8, levelIndex, numPlayers);
                BotSpawner.spawn("Kondoria", 8, 4, levelIndex, numPlayers);
            }
            case 5 -> {
                BotSpawner.spawn("Boss", 10, 5, levelIndex, numPlayers);
                BotSpawner.spawn("Kondoria", 4, 3, levelIndex, numPlayers);
                BotSpawner.spawn("Kondoria", 15, 8, levelIndex, numPlayers);
            }
            default -> {
                BotSpawner.spawn("Doll", 3, 3, levelIndex, numPlayers);
                BotSpawner.spawn("Kondoria", 10, 8, levelIndex, numPlayers);
                BotSpawner.spawn("Boss", 11, 5, levelIndex, numPlayers);
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

        for (int i = Mobs.MOBS.size() - 1; i >= 0; i--) {
            Mob m = Mobs.MOBS.get(i);
            if (!m.isSurvival()) {
                SoundManager.playSoundEffect("/audio/dead1.wav");
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

                SoundManager.playSoundEffect("/audio/quaman.wav");

                JOptionPane.showMessageDialog(this, "Chúc mừng! Đội của bạn đã vượt qua Cửa ải " + currentLevel + "!", "Chiến Thắng!", JOptionPane.INFORMATION_MESSAGE);
                parentFrame.showScreen("LEVEL_SELECT");
            }
        } else {
            isLevelCompleted = true;
            gameTimer.stop();
            SaveManager.clearSaveFile();

            SoundManager.playSoundEffect("/audio/dead2.wav");

            JOptionPane.showMessageDialog(this, "Tất cả người chơi đã hy sinh! Trò chơi kết thúc.", "Thua Cuộc", JOptionPane.ERROR_MESSAGE);
            parentFrame.showScreen("LEVEL_SELECT");
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int mapWidthPixel = gameMap.getWidth() * TILE_SIZE;
        int offsetX = Math.max(0, (getWidth() - mapWidthPixel) / 2);

        // 1. VẼ THANH HUD TẠI VỊ TRÍ GÓC TÊN TRÊN
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 13));

        int yText = 15;
        for (int i = 0; i < players.size(); i++) {
            Player p = players.get(i);
            String status = p.isSurvival() ? "" : " [ĐÃ HY SINH]";
            g2d.drawString(
                    "P" + (i + 1) + " | HP: " + p.getHp()
                            + " | Sức mạnh: " + p.getPower()
                            + " | Tốc độ: " + p.getSpeed() + status,
                    offsetX + 10, yText
            );
            yText += 15;
        }
        g2d.drawString("Cửa ải: " + currentLevel + " [ESC để lưu & thoát]", offsetX + 10, yText);

        // 2. VẼ GAME MAP VÀ ENTITY NGAY DƯỚI THANH HUD
        g2d.translate(offsetX, HUD_HEIGHT);

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

        g2d.translate(-offsetX, -HUD_HEIGHT);
    }
}