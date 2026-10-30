
import entity.Player;
import entity.bot.DollBot;
import entity.bot.KondoriaBot;
import entity.mob.Mob;
import entity.mob.Mobs;
import bomb.Bomb;
import item.Item;
import map.GameMap;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

public class GamePanel extends JPanel {

    private final int TILE_SIZE = 32;

    private GameMap gameMap;
    private Player player;
    private List<Bomb> activeBombs;
    private Timer gameTimer;

    public GamePanel() {

        setFocusable(true);
        setBackground(Color.DARK_GRAY);
        setPreferredSize(new Dimension(800, 600));

        // 1. Khởi tạo bản đồ từ GameMap
        gameMap = new GameMap();

        // 2. Khởi tạo Player
        player = new Player(TILE_SIZE, TILE_SIZE, gameMap.getGrid());
        Mobs.add(player);

        // 3. Khởi tạo danh sách bom
        activeBombs = new ArrayList<>();

        // 4. Khởi tạo Bot
        Mobs.add(new DollBot(
                5 * TILE_SIZE,
                3 * TILE_SIZE,
                TILE_SIZE,
                TILE_SIZE,
                1,
                gameMap.getGrid()
        ));

        Mobs.add(new KondoriaBot(
                7 * TILE_SIZE,
                3 * TILE_SIZE,
                TILE_SIZE,
                TILE_SIZE,
                1,
                player,
                gameMap.getGrid()
        ));

        // 5. Khởi tạo Item
        Mobs.add(new Item(
                3 * TILE_SIZE,
                TILE_SIZE,
                Item.ItemType.RANGE_UP,
                gameMap.getGrid()
        ));

        Mobs.add(new Item(
                4 * TILE_SIZE,
                TILE_SIZE,
                Item.ItemType.SPEED_UP,
                gameMap.getGrid()
        ));

        // 6. Xử lý bàn phím
        addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {
                player.handleKeyPressed(e);
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

        // Cập nhật tất cả Mob
        for (int i = 0; i < Mobs.MOBS.size(); i++) {
            Mob mob = Mobs.MOBS.get(i);
            mob.update();
        }

        // Xóa Mob đã chết, ngoại trừ Player
        Mobs.MOBS.removeIf(
                m -> !m.isSurvival() && !(m instanceof Player)
        );

        // Cập nhật bom
        for (int i = activeBombs.size() - 1; i >= 0; i--) {

            Bomb bomb = activeBombs.get(i);

            bomb.update();

            if (!bomb.isActive()) {
                activeBombs.remove(i);
            }
        }
    }

    // Vẽ bản đồ lấy dữ liệu từ GameMap
    private void drawMap(Graphics2D g2d) {

        int[][] grid = gameMap.getGrid();

        for (int y = 0; y < gameMap.getHeight(); y++) {
            for (int x = 0; x < gameMap.getWidth(); x++) {

                int tile = grid[y][x];

                switch (tile) {

                    case GameMap.EMPTY:
                        g2d.setColor(Color.DARK_GRAY);
                        break;

                    case GameMap.WALL_BRICK:
                        g2d.setColor(new Color(160, 90, 50));
                        break;

                    case GameMap.WALL_SOLID:
                        g2d.setColor(Color.GRAY);
                        break;

                    default:
                        // Các ô chứa Item (6-9)
                        g2d.setColor(Color.DARK_GRAY);
                        break;
                }

                g2d.fillRect(
                        x * TILE_SIZE,
                        y * TILE_SIZE,
                        TILE_SIZE,
                        TILE_SIZE
                );

                g2d.setColor(Color.BLACK);

                g2d.drawRect(
                        x * TILE_SIZE,
                        y * TILE_SIZE,
                        TILE_SIZE,
                        TILE_SIZE
                );
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
                "HP: " + player.getHp()
                        + " | Sức mạnh bom: " + player.getPower()
                        + " | Tốc độ: " + player.getSpeed(),
                10,
                20
        );
    }

    public static void main(String[] args) {

        JFrame frame = new JFrame("Bomberman Game");

        GamePanel panel = new GamePanel();

        frame.add(panel);

        frame.pack();

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.setLocationRelativeTo(null);

        frame.setVisible(true);
    }
}
