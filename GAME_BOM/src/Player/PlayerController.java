package Player;

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

    private final Set<String> activeKeys = new HashSet<>();
    private Timer gameLoopTimer;

    public PlayerController(Player player, GameMap gameMap, MapPanel mapPanel) {
        this.player = player;
        this.gameMap = gameMap;
        this.mapPanel = mapPanel;
        initGameLoop();
    }

    public void setupControls(JComponent component) {
        bindKey(component, "UP", Direction.UP);
        bindKey(component, "DOWN", Direction.DOWN);
        bindKey(component, "LEFT", Direction.LEFT);
        bindKey(component, "RIGHT", Direction.RIGHT);

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

    private void initGameLoop() {
        // LẤY TRỰC TIẾP speed CỦA PLAYER LÀM DELAY CHO TIMER (đơn vị: mili-giây)
        // Lưu ý: Vì speed ở Player đang là double, ta ép kiểu sang int.
        int initialDelay = 40 - (int)player.getSpeed();

        gameLoopTimer = new Timer(initialDelay, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!activeKeys.isEmpty()) {
                    updatePlayerPosition();
                    mapPanel.repaint();
                }
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
}