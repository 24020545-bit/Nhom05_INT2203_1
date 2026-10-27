package manager;

import entity.Player;
import map.GameMap;
import UI.MapPanel;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.Set;

public class PlayerController {
    private final Player player1;
    private final Player player2;
    private final MapPanel mapPanel;
    private final BombManager bombManager;

    private final Set<String> activeKeys = new HashSet<>();

    public PlayerController(Player player1, Player player2, GameMap gameMap, MapPanel mapPanel, BombManager bombManager) {
        this.player1 = player1;
        this.player2 = player2;
        this.mapPanel = mapPanel;
        this.bombManager = bombManager;
    }

    public void setupControls(JComponent component) {
        // --- PLAYER 1 (Dựa theo KeyConfig) ---
        bindKey(component, KeyConfig.p1Up, "P1_UP");
        bindKey(component, KeyConfig.p1Down, "P1_DOWN");
        bindKey(component, KeyConfig.p1Left, "P1_LEFT");
        bindKey(component, KeyConfig.p1Right, "P1_RIGHT");
        bindActionKey(component, KeyConfig.p1Bomb, "P1_PLACE_BOMB", () -> tryPlaceBomb(player1));

        // --- PLAYER 2 (Dựa theo KeyConfig) ---
        bindKey(component, KeyConfig.p2Up, "P2_UP");
        bindKey(component, KeyConfig.p2Down, "P2_DOWN");
        bindKey(component, KeyConfig.p2Left, "P2_LEFT");
        bindKey(component, KeyConfig.p2Right, "P2_RIGHT");
        bindActionKey(component, KeyConfig.p2Bomb, "P2_PLACE_BOMB", () -> tryPlaceBomb(player2)); // (hoặc p2Bomb)
    }

    private void bindKey(JComponent component, int keyCode, String actionName) {
        InputMap inputMap = component.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = component.getActionMap();

        KeyStroke pressedKey = KeyStroke.getKeyStroke(keyCode, 0, false);
        KeyStroke releasedKey = KeyStroke.getKeyStroke(keyCode, 0, true);

        inputMap.put(pressedKey, "press_" + actionName);
        actionMap.put("press_" + actionName, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                activeKeys.add(actionName);
            }
        });

        inputMap.put(releasedKey, "release_" + actionName);
        actionMap.put("release_" + actionName, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                activeKeys.remove(actionName);
            }
        });
    }

    private void bindActionKey(JComponent component, int keyCode, String actionKey, Runnable action) {
        InputMap inputMap = component.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = component.getActionMap();

        inputMap.put(KeyStroke.getKeyStroke(keyCode, 0, false), actionKey);
        actionMap.put(actionKey, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                action.run();
            }
        });
    }

    public Set<String> getActiveKeys() {
        return activeKeys;
    }

    private void tryPlaceBomb(Player p) {
        if (p == null || !p.isAlive() || bombManager == null) return;

        int tileX = (int) Math.round(p.getX());
        int tileY = (int) Math.round(p.getY());

        if (p.getActiveBombCount() < p.getBombLimit()) {
            boolean success = bombManager.placeBomb(tileX, tileY, p.getPower(), p);
            if (success) {
                p.setActiveBombCount(p.getActiveBombCount() + 1);
                mapPanel.repaint();
            }
        }
    }
}