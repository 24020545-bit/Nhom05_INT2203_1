package UI;

import Bomb.BombManager;
import Map.GameMap;
import Player.PlayerOb;
import Player.Bot;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class MapPanel extends JPanel {
    private final GameMap gameMap;
    private final List<PlayerOb> players;
    private final BombManager bombManager;
    private boolean showGrid = true;
    private static final int TILE_SIZE = 22;

    public MapPanel(GameMap gameMap) {
        this(gameMap, null, null);
    }

    public MapPanel(GameMap gameMap, List<PlayerOb> players, BombManager bombManager) {
        this.gameMap = gameMap;
        this.players = players;
        this.bombManager = bombManager;

        int widthPx = gameMap.getWidth() * TILE_SIZE;
        int heightPx = gameMap.getHeight() * TILE_SIZE;
        setPreferredSize(new Dimension(widthPx, heightPx));

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

        // 1. Tính toán khoảng cách canh giữa (Offset) để bản đồ nằm giữa màn hình
        int totalMapWidthPx = gameMap.getWidth() * TILE_SIZE;
        int totalMapHeightPx = gameMap.getHeight() * TILE_SIZE;

        int offsetX = Math.max(0, (getWidth() - totalMapWidthPx) / 2);
        int offsetY = Math.max(0, (getHeight() - totalMapHeightPx) / 2);

        // 2. Dịch chuyển hệ tọa độ g2d để gom bản đồ, bom, lửa và nhân vật vào giữa khung hình
        g2d.translate(offsetX, offsetY);

        // - Vẽ bản đồ (Lớp dưới cùng)
        MapRenderer.draw(g2d, gameMap, TILE_SIZE, showGrid);

        // - Vẽ hiệu ứng lửa vụ nổ
        if (bombManager != null) {
            ExplosionRenderer.draw(g2d, bombManager.getActiveExplosions(), gameMap, TILE_SIZE);
        }

        // - Vẽ Bom (Lớp trên lửa)
        if (bombManager != null) {
            BombRenderer.draw(g2d, bombManager, TILE_SIZE);
        }

        // - Vẽ tất cả Người chơi và Bot (Lớp trên cùng)
        if (players != null) {
            for (PlayerOb p : players) {
                if (p.isAlive()) {
                    Color color = (p instanceof Bot) ? Color.BLUE : Color.RED;
                    EntityRenderer.draw(g2d, p, TILE_SIZE, color);
                }
            }
        }

        // Trả lại hệ tọa độ ban đầu để vẽ HUD cố định
        g2d.translate(-offsetX, -offsetY);

        // 3. Vẽ khung HUD phân bố ở 4 góc màn hình
        HUDPanel.draw(g2d, players, getWidth(), getHeight());
    }

    public static int getTileSize() {
        return TILE_SIZE;
    }
}