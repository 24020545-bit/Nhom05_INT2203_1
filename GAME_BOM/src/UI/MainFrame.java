package UI;

import Map.GameMap;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private GameMap gameMap;

    public MainFrame() {
        gameMap = new GameMap();

        setTitle("Bomberman");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        mainPanel.add(new HomePanel(this), "HOME");
        mainPanel.add(new SelectMapPanel(this, gameMap), "SELECT");

        add(mainPanel);

        showScreen("HOME");
        pack();
        setLocationRelativeTo(null);
    }

    public void showScreen(String name) {
        cardLayout.show(mainPanel, name);
    }

    public void startGame(int mapIndex) {
        gameMap.setMapIndex(mapIndex);

        GamePanel gamePanel = new GamePanel(this, gameMap);

        mainPanel.add(gamePanel, "GAME");
        cardLayout.show(mainPanel, "GAME");

        pack();
        setLocationRelativeTo(null);
    }
}