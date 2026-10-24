package Player;

import Bomb.BombManager;
import Map.GameMap;
import UI.MapPanel;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Set;

public class PlayerController {
    private final Player player;
    private final GameMap gameMap;
    private final MapPanel mapPanel;
    private final BombManager bombManager; // Thêm BombManager để xử lý đặt bom

    private final Set<String> activeKeys = new HashSet<>();
    private Timer gameLoopTimer;

    // Cập nhật Constructor nhận thêm bombManager
    public PlayerController(Player player, GameMap gameMap, MapPanel mapPanel, BombManager bombManager) {
        this.player = player;
        this.gameMap = gameMap;
        this.mapPanel = mapPanel;
        this.bombManager = bombManager;
        initGameLoop();
    }

    public void setupControls(JComponent component) {
        bindKey(component, "UP", Direction.UP);
        bindKey(component, "DOWN", Direction.DOWN);
        bindKey(component, "LEFT", Direction.LEFT);
        bindKey(component, "RIGHT", Direction.RIGHT);

        // Gán phím SPACE để đặt bom
        bindActionKey(component, "SPACE", "place_bomb", () -> {
            tryPlaceBomb();
        });
    }

    private void bindKey(JComponent component, String key, Direction direction) {
        InputMap inputMap = component.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = component.getActionMap();

        String pressAction = "press_" + key;
        String releaseAction = "release_" + key;

        inputMap.put(KeyStroke.getKeyStroke("pressed " + key), pressAction);
        actionMap.put(pressAction, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                activeKeys.add(key);
            }
        });

        inputMap.put(KeyStroke.getKeyStroke("released " + key), releaseAction);
        actionMap.put(releaseAction, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                activeKeys.remove(key);
            }
        });
    }

    // Hàm phụ trợ để bind phím hành động (như phím Space)
    private void bindActionKey(JComponent component, String keyStroke, String actionKey, Runnable action) {
        InputMap inputMap = component.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = component.getActionMap();

        inputMap.put(KeyStroke.getKeyStroke(keyStroke), actionKey);
        actionMap.put(actionKey, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                action.run();
            }
        });
    }

    private void initGameLoop() {
        // Tốc độ lặp game (Delay tính bằng mili-giây, ví dụ 15-20ms cho chuyển động mượt)
        int initialDelay = 20;

        gameLoopTimer = new Timer(initialDelay, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!activeKeys.isEmpty()) {
                    updatePlayerPosition();
                }

                // Cập nhật trạng thái bom (tự động dọn dẹp bom nổ)
                if (bombManager != null) {
                    bombManager.update();
                }

                // Vẽ lại màn hình liên tục để cập nhật vị trí player và bom
                mapPanel.repaint();
            }
        });
        gameLoopTimer.start();
    }

    private void updatePlayerPosition() {
        if (!player.isAlive()) return;

        if (activeKeys.contains("UP")) {
            player.move(Direction.UP, gameMap);
        } else if (activeKeys.contains("DOWN")) {
            player.move(Direction.DOWN, gameMap);
        } else if (activeKeys.contains("LEFT")) {
            player.move(Direction.LEFT, gameMap);
        } else if (activeKeys.contains("RIGHT")) {
            player.move(Direction.RIGHT, gameMap);
        }
    }

    // ==========================================
    // LOGIC XỬ LÝ ĐẶT BOM KHI NHẤN PHÍM SPACE
    // ==========================================
    private void tryPlaceBomb() {
        if (!player.isAlive() || bombManager == null) {
            return;
        }

        // 1. Lấy tọa độ ô lưới (làm tròn từ vị trí thực tế của player)
        int tileX = (int) Math.round(player.getX());
        int tileY = (int) Math.round(player.getY());

        // 2. Kiểm tra xem người chơi còn lượt đặt bom không (activeBombCount < bombLimit)
        if (player.getActiveBombCount() < player.getBombLimit()) {
            // 3. Yêu cầu BombManager đặt bom tại ô (tileX, tileY)
            boolean success = bombManager.placeBomb(tileX, tileY, player.getPower(), player);

            if (success) {
                // Nếu đặt thành công, tăng số lượng bom đang active của player lên 1
                player.setActiveBombCount(player.getActiveBombCount() + 1);
                mapPanel.repaint(); // Vẽ lại ngay lập tức để hiện quả bom
            }
        }
    }
}