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
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class GamePanel extends JPanel {

    private static final int TILE_SIZE = 32;
    private static final int WINDOW_WIDTH = 1100;
    private static final int WINDOW_HEIGHT = 800;
    private static final int HUD_HEIGHT = 65;

    private final GameFrame parentFrame;
    private final int currentLevel;
    private final int playerCount;
    private final GameMap gameMap;
    private final List<Player> players;
    private final List<Bomb> activeBombs;
    private final List<Item> activeItems;
    private final Timer gameTimer;

    private boolean isLevelCompleted = false;
    private BufferedImage backgroundImage;

    private JPanel hudPanel;
    private JScrollPane mapScrollPane;
    private MapCanvas mapCanvas;

    // =========================================================================
    // 1. CONSTRUCTORS & KHỞI TẠO
    // =========================================================================

    public GamePanel(GameFrame parentFrame, int levelIndex, int playerCount) {
        this(parentFrame, levelIndex, playerCount, null);
    }

    public GamePanel(GameFrame parentFrame, int levelIndex, int playerCount, Properties saveProps) {
        this.parentFrame = parentFrame;
        this.currentLevel = levelIndex;
        this.playerCount = Math.clamp(playerCount, 1, 2);

        setLayout(new BorderLayout(0, 8));
        setOpaque(false);
        setFocusable(true);

        loadBackgroundImage();
        Mobs.MOBS.clear();

        gameMap = new GameMap();
        gameMap.loadLevelMap(levelIndex);

        activeBombs = new ArrayList<>();
        activeItems = new ArrayList<>();
        players = new ArrayList<>();

        if (saveProps != null) {
            loadGameFromSave(saveProps, levelIndex);
        } else {
            initDefaultGame(levelIndex, this.playerCount);
        }

        loadItemsFromGrid();
        createGameLayout();
        adjustWindowSize();
        initKeyListeners();

        gameTimer = new Timer(16, e -> {
            updateGame();
            repaint();
            if (hudPanel != null) hudPanel.repaint();
            if (mapCanvas != null) mapCanvas.repaint();
        });
        gameTimer.start();

        SwingUtilities.invokeLater(this::requestFocusInWindow);
    }

    // =========================================================================
    // 2. KHỞI TẠO DỮ LIỆU & MAP & BOTS
    // =========================================================================

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

    private void loadGameFromSave(Properties saveProps, int levelIndex) {
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
            e.printStackTrace();
            players.clear();
            Mobs.MOBS.clear();
            initDefaultGame(levelIndex, this.playerCount);
        }
    }

    private void initBots(int levelIndex, int numPlayers) {
        BotSpawner.setGrid(gameMap.getGrid());

        switch (levelIndex) {
            case 1 -> {
                BotSpawner.spawn("Doll", 19, 1, levelIndex, numPlayers);
                BotSpawner.spawn("Doll", 1, 19, levelIndex, numPlayers);
                BotSpawner.spawn("Kondoria", 10, 9, levelIndex, numPlayers);
                BotSpawner.spawn("Kondoria", 2, 11, levelIndex, numPlayers);
            }
            case 2 -> {
                BotSpawner.spawn("Doll", 18, 1, levelIndex, numPlayers);
                BotSpawner.spawn("Doll", 2, 17, levelIndex, numPlayers);
                BotSpawner.spawn("Kondoria", 9, 8, levelIndex, numPlayers);
                BotSpawner.spawn("Kondoria", 17, 9, levelIndex, numPlayers);
            }
            case 3 -> {
                BotSpawner.spawn("Boss", 8, 18, levelIndex, numPlayers);
                BotSpawner.spawn("Doll", 19, 2, levelIndex, numPlayers);
                BotSpawner.spawn("Doll", 28, 7, levelIndex, numPlayers);
                BotSpawner.spawn("Kondoria", 18, 14, levelIndex, numPlayers);
                BotSpawner.spawn("Kondoria", 10, 6, levelIndex, numPlayers);
            }
            case 4 -> {
                BotSpawner.spawn("Boss", 16, 2, levelIndex, numPlayers);
                BotSpawner.spawn("Doll", 7, 11, levelIndex, numPlayers);
                BotSpawner.spawn("Doll", 26, 1, levelIndex, numPlayers);
                BotSpawner.spawn("Kondoria", 17, 11, levelIndex, numPlayers);
                BotSpawner.spawn("Kondoria", 9, 4, levelIndex, numPlayers);
            }
            case 5 -> {
                BotSpawner.spawn("Boss", 15, 10, levelIndex, numPlayers);
                BotSpawner.spawn("Doll", 26, 2, levelIndex, numPlayers);
                BotSpawner.spawn("Doll", 3, 17, levelIndex, numPlayers);
                BotSpawner.spawn("Doll", 15, 17, levelIndex, numPlayers);
                BotSpawner.spawn("Kondoria", 24, 10, levelIndex, numPlayers);
                BotSpawner.spawn("Kondoria", 6, 8, levelIndex, numPlayers);
            }
            case 6 -> {
                BotSpawner.spawn("Boss", 7, 6, levelIndex, numPlayers);
                BotSpawner.spawn("Boss", 22, 12, levelIndex, numPlayers);
                BotSpawner.spawn("Doll", 2, 2, levelIndex, numPlayers);
                BotSpawner.spawn("Doll", 27, 17, levelIndex, numPlayers);
                BotSpawner.spawn("Kondoria", 5, 12, levelIndex, numPlayers);
                BotSpawner.spawn("Kondoria", 24, 7, levelIndex, numPlayers);
                BotSpawner.spawn("Kondoria", 14, 5, levelIndex, numPlayers);
            }
            default -> {
                BotSpawner.spawn("Boss", 16, 2, levelIndex, numPlayers);
                BotSpawner.spawn("Doll", 7, 11, levelIndex, numPlayers);
                BotSpawner.spawn("Kondoria", 17, 11, levelIndex, numPlayers);
            }
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

    private void loadBackgroundImage() {
        try {
            backgroundImage = ImageIO.read(getClass().getResource("/Screen/GamePanel.png"));
        } catch (Exception e) {
            System.err.println("Không thể tải ảnh nền GamePanel.png");
            e.printStackTrace();
        }
    }

    // =========================================================================
    // 3. GIAO DIỆN (UI & LAYOUT)
    // =========================================================================

    private void createGameLayout() {
        setLayout(new BorderLayout(0, 8));

        hudPanel = new JPanel(new GridLayout(1, playerCount, 10, 0));
        hudPanel.setOpaque(false);
        hudPanel.setPreferredSize(new Dimension(WINDOW_WIDTH, HUD_HEIGHT));
        hudPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 0, 10));

        for (int i = 0; i < playerCount; i++) {
            hudPanel.add(new PlayerInfoPanel(players.get(i)));
        }
        add(hudPanel, BorderLayout.NORTH);

        mapCanvas = new MapCanvas();
        mapScrollPane = new JScrollPane(mapCanvas);
        mapScrollPane.setBorder(null);
        mapScrollPane.setOpaque(false);
        mapScrollPane.getViewport().setOpaque(false);
        mapScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        mapScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        mapScrollPane.getHorizontalScrollBar().setUnitIncrement(TILE_SIZE);
        mapScrollPane.getVerticalScrollBar().setUnitIncrement(TILE_SIZE);

        add(mapScrollPane, BorderLayout.CENTER);
    }

    public void adjustWindowSize() {
        Dimension targetDim = new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT);
        setPreferredSize(targetDim);

        if (parentFrame != null) {
            Dimension currentSize = parentFrame.getSize();
            if (currentSize.width < WINDOW_WIDTH || currentSize.height < WINDOW_HEIGHT) {
                parentFrame.getContentPane().setPreferredSize(targetDim);
                parentFrame.pack();
                parentFrame.setLocationRelativeTo(null);
            } else {
                parentFrame.revalidate();
                parentFrame.repaint();
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();

        if (backgroundImage != null) {
            g2.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        } else {
            g2.setColor(Color.DARK_GRAY);
            g2.fillRect(0, 0, getWidth(), getHeight());
        }
        g2.dispose();
    }

    // =========================================================================
    // 4. XỬ LÝ SỰ KIỆN BÀN PHÍM (INPUT HANDLING)
    // =========================================================================

    private void initKeyListeners() {
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    gameTimer.stop();
                    SaveManager.saveGameProgress(currentLevel, players, gameMap.getGrid());
                    JOptionPane.showMessageDialog(GamePanel.this, "Đã lưu tiến trình màn chơi!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                    parentFrame.showScreen("LEVEL_SELECT");
                    return;
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
                if (!players.isEmpty()) handlePlayer1Input(e, false);
                if (players.size() >= 2) handlePlayer2Input(e, false);
            }
        });
    }

    private void handlePlayer1Input(KeyEvent e, boolean isPressed) {
        if (players.isEmpty()) return;
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

    // =========================================================================
    // 5. VÒNG LẶP GAME & CẬP NHẬT TRẠNG THÁI (GAME LOOP & UPDATE)
    // =========================================================================

    private void updateGame() {
        if (isLevelCompleted) return;

        for (int i = 0; i < Mobs.MOBS.size(); i++) {
            Mobs.MOBS.get(i).update();
        }

        for (Item item : activeItems) {
            for (Player p : players) {
                if (p.isSurvival()) item.update(p);
            }
        }
        activeItems.removeIf(Item::isCollected);

        for (int i = Mobs.MOBS.size() - 1; i >= 0; i--) {
            Mob m = Mobs.MOBS.get(i);
            if (!m.isSurvival()) {
                if (!(m instanceof Player)) SoundManager.playSoundEffect("/audio/dead1.wav");
                Mobs.MOBS.remove(i);
            }
        }

        for (int i = activeBombs.size() - 1; i >= 0; i--) {
            Bomb bomb = activeBombs.get(i);
            bomb.update();
            if (!bomb.isActive()) activeBombs.remove(i);
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

    // =========================================================================
    // 6. CÁC CLASS NỘI BỘ (INNER CLASSES: MapCanvas & PlayerInfoPanel)
    // =========================================================================

    private class PlayerInfoPanel extends JPanel {
        private final Player player;

        public PlayerInfoPanel(Player player) {
            this.player = player;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            g2.setColor(new Color(52, 68, 90, 235));
            g2.fillRoundRect(0, 0, w, h, 12, 12);

            // 1. Tên Player ở bên trái
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Arial", Font.BOLD, 15));
            g2.drawString(player.getPlayerName(), 12, 28);

            String status = player.isSurvival() ? "" : " (Đã hy sinh)";

            // 2. Các chỉ số xếp dọc ở bên phải
            g2.setFont(new Font("Arial", Font.PLAIN, 12));
            g2.drawString("HP: " + player.getHp() + status, 110, 18);
            g2.drawString("Bomb: " + player.getActiveBombCount() + "/" + player.getBombLimit(), 110, 33);
            g2.drawString("Power: " + player.getPower(), 110, 48);
            g2.drawString("Speed: " + player.getSpeed(), 200, 48);

            g2.dispose();
        }
    }

    private class MapCanvas extends JPanel implements Scrollable {
        public MapCanvas() {
            setOpaque(false);
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(gameMap.getWidth() * TILE_SIZE, gameMap.getHeight() * TILE_SIZE);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int mapWidth = gameMap.getWidth() * TILE_SIZE;
            int mapHeight = gameMap.getHeight() * TILE_SIZE;
            int offsetX = Math.max(0, (getWidth() - mapWidth) / 2);
            int offsetY = Math.max(0, (getHeight() - mapHeight) / 2);

            g2.translate(offsetX, offsetY);

            gameMap.draw(g2);
            for (Item item : activeItems) item.draw(g2);
            for (Bomb bomb : activeBombs) bomb.draw(g2);
            for (Mob mob : Mobs.MOBS) mob.draw(g2);

            g2.dispose();
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            if (getParent() instanceof JViewport viewport) {
                return getPreferredSize().width <= viewport.getWidth();
            }
            return false;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            if (getParent() instanceof JViewport viewport) {
                return getPreferredSize().height <= viewport.getHeight();
            }
            return false;
        }

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return new Dimension(900, 650);
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
            return TILE_SIZE;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            return orientation == SwingConstants.HORIZONTAL
                    ? visibleRect.width - TILE_SIZE
                    : visibleRect.height - TILE_SIZE;
        }
    }
}