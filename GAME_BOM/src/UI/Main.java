package UI;

import UI.Screen.GameSetupPanel;
import UI.Screen.HomeScreenPanel;
import UI.Screen.KeySettingsPanel;
import map.GameMap;
import manager.GameManager;

import javax.swing.*;
import java.awt.*;

public class Main extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainContainer;
    private GameMap gameMap;

    private static final int TILE_SIZE = 45;
    private GameManager currentGameManager;

    public Main() {
        setTitle("Bomber Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(true);

        gameMap = new GameMap();
        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);

        int defaultWidth = Math.max(gameMap.getWidth() * TILE_SIZE, 800);
        int defaultHeight = Math.max(gameMap.getHeight() * TILE_SIZE, 600);
        Dimension initialSize = new Dimension(defaultWidth, defaultHeight);

        setMinimumSize(new Dimension(600, 500));

        // 1. Màn hình Trang chủ
        HomeScreenPanel homeScreen = new HomeScreenPanel(
                () -> cardLayout.show(mainContainer, "SETUP"),
                () -> cardLayout.show(mainContainer, "SETTINGS")
        );

        // 2. Màn hình Cài đặt phím
        KeySettingsPanel settingsScreen = new KeySettingsPanel(() -> {
            cardLayout.show(mainContainer, "HOME");
        });

        // 3. Màn hình Cấu hình
        GameSetupPanel setupScreen = new GameSetupPanel(gameMap, (gameMode, mapIndex) -> {
            gameMap.setMapIndex(mapIndex);

            // Khởi tạo phiên chơi game qua GameManager
            initGameSession(gameMode);
        });

        mainContainer.add(homeScreen, "HOME");
        mainContainer.add(settingsScreen, "SETTINGS");
        mainContainer.add(setupScreen, "SETUP");

        add(mainContainer);

        setPreferredSize(initialSize);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void initGameSession(int gameMode) {
        currentGameManager = new GameManager(
                gameMap,
                gameMode,
                () -> {
                    // Khi kết thúc game (Thắng/Thua) -> Quay về Home
                    cardLayout.show(mainContainer, "HOME");
                }
        );

        mainContainer.add(currentGameManager.getMapPanel(), "PLAYING");
        mainContainer.revalidate();
        mainContainer.repaint();

        cardLayout.show(mainContainer, "PLAYING");

        // Bắt đầu game loop
        currentGameManager.startGameLoop();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::new);
    }
}