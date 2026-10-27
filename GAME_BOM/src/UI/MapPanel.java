package UI;

import manager.BombManager;
import manager.ItemManager;
import map.GameMap;
import entity.PlayerOb;
import entity.Bot;
import bomb.Bomb;
import bomb.Explosion;
import item.Items;
import utils.Constants;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class MapPanel extends JPanel {
    private final GameMap gameMap;
    private final List<PlayerOb> players;
    private final BombManager bombManager;
    private final ItemManager itemManager;
    private boolean showGrid = true;

    // Các constructor hỗ trợ đa dạng tham số
    public MapPanel(GameMap gameMap) {
        this(gameMap, null, null, null);
    }

    public MapPanel(GameMap gameMap, List<PlayerOb> players, BombManager bombManager) {
        this(gameMap, players, bombManager, null);
    }

    public MapPanel(GameMap gameMap, List<PlayerOb> players, BombManager bombManager, ItemManager itemManager) {
        this.gameMap = gameMap;
        this.players = players;
        this.bombManager = bombManager;
        this.itemManager = itemManager;

        // Sử dụng Constants.TILE_SIZE để tính kích thước khung hình
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

        // 1. Tính toán khoảng cách canh giữa (Offset) dựa trên Constants.TILE_SIZE
        int totalMapWidthPx = gameMap.getWidth() * Constants.TILE_SIZE;
        int totalMapHeightPx = gameMap.getHeight() * Constants.TILE_SIZE;

        int offsetX = Math.max(0, (getWidth() - totalMapWidthPx) / 2);
        int offsetY = Math.max(0, (getHeight() - totalMapHeightPx) / 2);

        // 2. Dịch chuyển hệ tọa độ g2d
        g2d.translate(offsetX, offsetY);

        // - Vẽ bản đồ (Lớp dưới cùng) - Dùng MapRenderer vì Map là lưới dữ liệu tĩnh
        MapRenderer.draw(g2d, gameMap, Constants.TILE_SIZE, showGrid);

        // - Vẽ Items (Gọi trực tiếp hàm draw của từng Item)
        if (itemManager != null && itemManager.getItemsList() != null) {
            for (Items item : itemManager.getItemsList()) {
                if (!item.isCollected()) {
                    item.draw(g2d, Constants.TILE_SIZE);
                }
            }
        }

        // - Vẽ hiệu ứng lửa vụ nổ (Gọi trực tiếp hàm draw của từng Explosion)
        if (bombManager != null && bombManager.getActiveExplosions() != null) {
            for (Explosion exp : bombManager.getActiveExplosions()) {
                exp.draw(g2d, Constants.TILE_SIZE);
            }
        }

        // - Vẽ Bom (Gọi trực tiếp hàm draw của từng Bomb)
        if (bombManager != null && bombManager.getActiveBombs() != null) {
            for (Bomb bomb : bombManager.getActiveBombs()) {
                bomb.draw(g2d, Constants.TILE_SIZE);
            }
        }

        // - Vẽ tất cả Người chơi và Bot (Gọi trực tiếp hàm draw của PlayerOb)
        if (players != null) {
            for (PlayerOb p : players) {
                if (p.isAlive()) {
                    Color color = (p instanceof Bot) ? Color.BLUE : Color.RED;
                    p.draw(g2d, Constants.TILE_SIZE, color);
                }
            }
        }

        // Trả lại hệ tọa độ ban đầu để vẽ HUD cố định
        g2d.translate(-offsetX, -offsetY);

        // 3. Vẽ khung HUD phân bố ở 4 góc màn hình
        HUDPanel.draw(g2d, players, getWidth(), getHeight());
    }

    public void zoomMap(boolean isZoomIn) {
        if (isZoomIn) {
            Constants.zoomIn();
        } else {
            Constants.zoomOut();
        }

        // Tính lại kích thước mới cho MapPanel dựa trên TILE_SIZE mới
        int widthPx = gameMap.getWidth() * Constants.TILE_SIZE;
        int heightPx = gameMap.getHeight() * Constants.TILE_SIZE;
        setPreferredSize(new Dimension(widthPx, heightPx));

        // Cập nhật lại giao diện Swing
        revalidate();
        repaint();
    }

    public static int getTileSize() {
        return Constants.TILE_SIZE;
    }
}