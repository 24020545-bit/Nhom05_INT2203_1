package UI;

import UI.Screen.HUDPanel;
import manager.BombManager;
import manager.ItemManager;
import map.GameMap;
import entity.Mod;
import bomb.Bomb;
import bomb.Explosion;
import item.Items;
import utils.Constants;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class MapPanel extends JPanel {
    private final GameMap gameMap;
    private final List<Mod> players;
    private final BombManager bombManager;
    private final ItemManager itemManager;
    private boolean showGrid = true;

    public MapPanel(GameMap gameMap, List<Mod> players, BombManager bombManager, ItemManager itemManager) {
        this.gameMap = gameMap;
        this.players = players;
        this.bombManager = bombManager;
        this.itemManager = itemManager;

        int widthPx = gameMap.getWidth() * Constants.TILE_SIZE;
        int heightPx = gameMap.getHeight() * Constants.TILE_SIZE;
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
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        // 1. Kích thước gốc của bản đồ
        int baseMapWidth = gameMap.getWidth() * Constants.TILE_SIZE;
        int baseMapHeight = gameMap.getHeight() * Constants.TILE_SIZE;

        // 2. Tính tỷ lệ co giãn (Scale) khi phóng to cửa sổ
        double scaleX = (double) getWidth() / baseMapWidth;
        double scaleY = (double) getHeight() / baseMapHeight;
        double scale = Math.min(scaleX, scaleY);

        Graphics2D gMap = (Graphics2D) g2d.create();

        int scaledWidth = (int) (baseMapWidth * scale);
        int scaledHeight = (int) (baseMapHeight * scale);
        int offsetX = (getWidth() - scaledWidth) / 2;
        int offsetY = (getHeight() - scaledHeight) / 2;

        gMap.translate(offsetX, offsetY);
        gMap.scale(scale, scale);

        // - Vẽ bản đồ
        MapRenderer.draw(gMap, gameMap, Constants.TILE_SIZE, showGrid);

        // - Vẽ Items
        if (itemManager != null && itemManager.getItemsList() != null) {
            for (Items item : itemManager.getItemsList()) {
                if (!item.isCollected()) {
                    item.draw(gMap, Constants.TILE_SIZE);
                }
            }
        }

        // - Vẽ Vụ nổ
        if (bombManager != null && bombManager.getActiveExplosions() != null) {
            for (Explosion exp : bombManager.getActiveExplosions()) {
                exp.draw(gMap, Constants.TILE_SIZE);
            }
        }

        // - Vẽ Bom
        if (bombManager != null && bombManager.getActiveBombs() != null) {
            for (Bomb bomb : bombManager.getActiveBombs()) {
                bomb.draw(gMap, Constants.TILE_SIZE);
            }
        }

        // - Vẽ Người chơi và Bot
        if (players != null) {
            for (Mod p : players) {
                if (p.isAlive()) {
                    Color color = (p instanceof Bot) ? Color.BLUE : Color.RED;
                    p.draw(gMap, Constants.TILE_SIZE, color);
                }
            }
        }

        gMap.dispose();

        // 3. Vẽ khung HUD thông tin
        HUDPanel.draw(g2d, players, getWidth(), getHeight());
    }

    public void zoomMap(boolean isZoomIn) {
        if (isZoomIn) {
            Constants.zoomIn();
        } else {
            Constants.zoomOut();
        }

        int widthPx = gameMap.getWidth() * Constants.TILE_SIZE;
        int heightPx = gameMap.getHeight() * Constants.TILE_SIZE;
        setPreferredSize(new Dimension(widthPx, heightPx));
        revalidate();
        repaint();
    }

    public static int getTileSize() {
        return Constants.TILE_SIZE;
    }
}