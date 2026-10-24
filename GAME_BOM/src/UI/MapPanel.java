package UI;

import Bomb.BombManager;
import Map.GameMap;
import Player.Player;

import javax.swing.*;
import java.awt.*;

public class MapPanel extends JPanel {
    private final GameMap gameMap;
    private final Player player;
    private final BombManager bombManager;
    private boolean showGrid = true;
    private static final int TILE_SIZE = 22;

    public MapPanel(GameMap gameMap) {
        this(gameMap, null, null);
    }

    public MapPanel(GameMap gameMap, Player player, BombManager bombManager) {
        this.gameMap = gameMap;
        this.player = player;
        this.bombManager = bombManager;

        // Đã sửa lại lỗi cú pháp phép nhân (*)
        int dimension = GameMap.SIZE * TILE_SIZE;
        setPreferredSize(new Dimension(dimension, dimension));
        setBackground(new Color(34, 139, 34));
    }

    public void setShowGrid(boolean showGrid) {
        this.showGrid = showGrid;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 1. Vẽ bản đồ (Lớp dưới cùng)
        MapRenderer.draw(g2d, gameMap, TILE_SIZE, showGrid);

        // 2. Vẽ hiệu ứng lửa vụ nổ (Đã bổ sung thêm tham số gameMap vào đây)
        if (bombManager != null) {
            ExplosionRenderer.draw(g2d, bombManager.getActiveExplosions(), gameMap, TILE_SIZE);
        }

        // 3. Vẽ Bom (Lớp trên lửa)
        if (bombManager != null) {
            BombRenderer.draw(g2d, bombManager, TILE_SIZE);
        }

        // 4. Vẽ Người chơi (Lớp trên cùng)
        if (player != null) {
            EntityRenderer.draw(g2d, player, TILE_SIZE, Color.RED);
        }
    }

    public static int getTileSize() {
        return TILE_SIZE;
    }
}