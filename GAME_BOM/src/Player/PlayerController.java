package Player;

import Bomb.BombManager;
import Map.GameMap;
import UI.MapPanel;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.Set;

public class PlayerController {
    private final Player player;
    private final GameMap gameMap;
    private final MapPanel mapPanel;
    private final BombManager bombManager; // Có thể giữ hoặc bỏ nếu đặt bom xử lý chỗ khác

    private final Set<String> activeKeys = new HashSet<>();

    public PlayerController(Player player, GameMap gameMap, MapPanel mapPanel, BombManager bombManager) {
        this.player = player;
        this.gameMap = gameMap;
        this.mapPanel = mapPanel;
        this.bombManager = bombManager;
        // KHÔNG CÒN TẠO TIMER Ở ĐÂY NỮA
    }

    public void setupControls(JComponent component) {
        bindKey(component, "UP", Direction.UP);
        bindKey(component, "DOWN", Direction.DOWN);
        bindKey(component, "LEFT", Direction.LEFT);
        bindKey(component, "RIGHT", Direction.RIGHT);

        bindActionKey(component, "SPACE", "place_bomb", this::tryPlaceBomb);
    }

    private void bindKey(JComponent component, String key, Direction direction) {
        InputMap inputMap = component.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = component.getActionMap();

        inputMap.put(KeyStroke.getKeyStroke("pressed " + key), "press_" + key);
        actionMap.put("press_" + key, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) { activeKeys.add(key); }
        });

        inputMap.put(KeyStroke.getKeyStroke("released " + key), "release_" + key);
        actionMap.put("release_" + key, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) { activeKeys.remove(key); }
        });
    }

    private void bindActionKey(JComponent component, String keyStroke, String actionKey, Runnable action) {
        InputMap inputMap = component.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = component.getActionMap();

        inputMap.put(KeyStroke.getKeyStroke(keyStroke), actionKey);
        actionMap.put(actionKey, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) { action.run(); }
        });
    }

    // Hàm cung cấp phím đang bấm cho GamePanel xử lý
    public Set<String> getActiveKeys() {
        return activeKeys;
    }

    private void tryPlaceBomb() {
        if (!player.isAlive() || bombManager == null) return;

        int tileX = (int) Math.round(player.getX());
        int tileY = (int) Math.round(player.getY());

        if (player.getActiveBombCount() < player.getBombLimit()) {
            boolean success = bombManager.placeBomb(tileX, tileY, player.getPower(), player);
            if (success) {
                player.setActiveBombCount(player.getActiveBombCount() + 1);
                mapPanel.repaint();
            }
        }
    }
}