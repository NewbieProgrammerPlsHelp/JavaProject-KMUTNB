package main;

import javax.swing.*;
import java.awt.*;

public class Main {
    private JFrame window;
    private JPanel mainPanel; // holds all screens
    private CardLayout cardLayout;
    private GamePanel gamePanel;
    private MainMenuPanel menuPanel;
    private LevelSelectionPanel levelSelectionPanel;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Main().init());
    }

    public void init() {
        window = new JFrame("Test");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(false);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        menuPanel = new MainMenuPanel(this);
        gamePanel = new GamePanel(this);
        levelSelectionPanel = new LevelSelectionPanel(this);

        mainPanel.add(menuPanel, "menu");
        mainPanel.add(gamePanel, "game");
        mainPanel.add(levelSelectionPanel, "levelSelect");

        window.add(mainPanel);
        window.pack();
        window.setLocationRelativeTo(null);
        window.setVisible(true);

        showMenu(); // start in menu
    }

    public void showMenu() {
        gamePanel.stopGameThread();
        cardLayout.show(mainPanel, "menu");
    }

    public void showLevelSelect() {
        gamePanel.stopGameThread();
        cardLayout.show(mainPanel, "levelSelect");
    }

    public void showGamePanel(String mapFile) {
        // Stop any old game thread (avoid two loops at once)
        gamePanel.stopGameThread();

        // Set new map path
        String mapPath = "/Resource/maps/" + mapFile;
        gamePanel.currentMapPath = mapPath;

        // Reload everything properly
        gamePanel.tileM.resetAndLoadMap(mapPath);
        gamePanel.entityM.clearAll();
        gamePanel.entityM.loadFromTileManager(gamePanel.tileM);
        gamePanel.hazardM = new tile.HazardManager(gamePanel);
        gamePanel.hazardM.link(0, 0);

        // Restart game state cleanly
        gamePanel.restartLevel();
        gamePanel.gameState = GamePanel.PLAY_STATE;

        // Switch to game screen
        cardLayout.show(mainPanel, "game");
        gamePanel.requestFocusInWindow();
        gamePanel.startGameThread();
    }

    public void showCharacterPanel() {
        gamePanel.stopGameThread();
        mainPanel.add(new CharacterPanel(this), "character");
        cardLayout.show(mainPanel, "character");
    }

    public void backToMenu() {
        gamePanel.stopGameThread(); // optional, to stop running game loop
        showMenu();
    }

}
