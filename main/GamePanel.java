package main;

import java.awt.*;
import javax.swing.JPanel;
import entity.Player;
import tile.HazardManager;
import tile.TileManager;
import entity.Entity;
import entity.EntityManager;

public class GamePanel extends JPanel implements Runnable {

    // ===== Screen setting =====
    final int originalTileSize = 16;
    final int scale = 3;

    public final int tileSize = originalTileSize * scale;
    public final int maxScreenCol = 16;
    public final int maxScreenRow = 12;
    final int screenWidth = tileSize * maxScreenCol;
    final int screenHeight = tileSize * maxScreenRow;

    // ===== Game states =====
    public static final int PLAY_STATE = 0;
    public static final int GAME_OVER_STATE = 1;
    public static final int GAME_WIN_STATE = 2;
    public int gameState = PLAY_STATE;

    // ===== Core components =====
    public TileManager tileM;
    public EntityManager entityM;
    public HazardManager hazardM;
    public Player player;
    public KeyHandler keyH = new KeyHandler();
    public String currentMapPath = "/Resource/maps/map01.txt";

    final int FPS = 60;
    Thread gameThread;

    public Main mainApp; // Reference to main for switching screens

    public GamePanel(Main mainApp) {
        this.mainApp = mainApp;

        this.setPreferredSize(new Dimension(screenWidth, screenHeight));
        this.setBackground(Color.gray);
        this.setDoubleBuffered(true);
        this.addKeyListener(keyH);
        keyH.setGamePanel(this);
        this.setFocusable(true);

        // initialize everything
        tileM = new TileManager(this);
        hazardM = new HazardManager(this);
        entityM = new EntityManager(this, keyH);

        // Load entities from the map
        entityM.loadFromTileManager(tileM);

        // Example linking: button #0 controls spike #0
        hazardM.link(0, 0);

        // finally, create player (uses entityM for characters)
        player = new Player(this, keyH, entityM);
        entityM.characters = player.entityM.characters;
    }

    // Overloaded constructor for old code (without mainApp)
    public GamePanel() {
        this(null);
    }

    public void startGameThread() {
        if (gameThread == null || !gameThread.isAlive()) {
            gameThread = new Thread(this);
            gameThread.start();
        }
    }

    public void stopGameThread() {
        if (gameThread != null && gameThread.isAlive()) {
            Thread temp = gameThread;
            gameThread = null; // this stops the while loop in run()
            try {
                temp.join(100); // wait briefly for thread to close
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    // =============================================================
    // LEVEL RESTART / STATE CHANGES
    // =============================================================
    public void restartLevel() {
        System.out.println("Player died! Restarting level...");

        keyH.resetKeys();
        entityM.clearAll();

        tileM.resetAndLoadMap(currentMapPath);
        System.out.println("Loading map from: " + currentMapPath);
        hazardM = new HazardManager(this);
        hazardM.link(0, 0);
        entityM.loadFromTileManager(tileM);

        player = new Player(this, keyH, entityM);

        for (Entity e : entityM.getAllEntities()) {
            e.setGamePanel(this);
        }

        gameState = PLAY_STATE;
        repaint();
    }

    public void resetGame() {
        restartLevel();
        gameState = PLAY_STATE;
    }

    public void triggerGameOver() {
        System.out.println("GAME OVER — waiting for restart input");
        gameState = GAME_OVER_STATE;
        keyH.resetKeys();
    }

    public void triggerWin() {
        System.out.println("Level Complete!");
        gameState = GAME_WIN_STATE;
        keyH.resetKeys();
        repaint();
    }

    // =============================================================
    // MAIN LOOP
    // =============================================================
    @Override
    public void run() {
        double drawInterval = 1000000000 / FPS;
        double delta = 0;
        long lastTime = System.nanoTime();
        long currentTime;
        long timer = 0;
        int drawCount = 0;

        while (gameThread != null) {
            currentTime = System.nanoTime();
            delta += (currentTime - lastTime) / drawInterval;
            timer += (currentTime - lastTime);
            lastTime = currentTime;

            if (delta >= 1) {
                update();
                repaint();
                delta--;
                drawCount++;
            }

            if (timer >= 1000000000) {
                System.out.println("FPS:" + drawCount);
                drawCount = 0;
                timer = 0;
            }
        }
    }

    // =============================================================
    // UPDATE LOGIC
    // =============================================================
    public void update() {
        if (gameState == PLAY_STATE) {
            player.update();
            entityM.update();
            hazardM.update(entityM.getAllEntities());

            // Example win condition: step on bottom-right tile
            int playerCol = (player.getX() + tileSize / 2) / tileSize;
            int playerRow = (player.getY() + tileSize / 2) / tileSize;
            if (playerCol == maxScreenCol - 1 && playerRow == maxScreenRow - 1) {
                triggerWin();
            }
        } else if (gameState == GAME_OVER_STATE || gameState == GAME_WIN_STATE) {
            handleEndScreenInput();
        }
    }

    private void handleEndScreenInput() {
        if (keyH.yPress) {
            restartLevel();
            keyH.yPress = false;
        } else if (keyH.nPress && mainApp != null) {
            mainApp.showLevelSelect(); // go to level selection instead
            keyH.nPress = false;
        }
    }

    public void setMainApp(Main mainApp) {
        this.mainApp = mainApp;
    }

    // =============================================================
    // DRAWING
    // =============================================================
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        g2.setColor(Color.GRAY);
        g2.fillRect(0, 0, getWidth(), getHeight());

        tileM.draw(g2);
        entityM.draw(g2, player.getActiveIndex());

        drawTeleportUI(g2);

        if (gameState == GAME_OVER_STATE) {
            drawCenteredText(g2, "GAME OVER", "RESTART? Y / N", Color.WHITE);
        } else if (gameState == GAME_WIN_STATE) {
            drawWinScreen(g2);
        }

        g2.dispose();
    }

    private void drawCenteredText(Graphics2D g2, String msg1, String msg2, Color color) {
        g2.setFont(new Font("Arial", Font.BOLD, 48));
        g2.setColor(Color.BLACK);

        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;

        int msg1Width = g2.getFontMetrics().stringWidth(msg1);
        int msg2Width = g2.getFontMetrics().stringWidth(msg2);

        // Shadow
        g2.drawString(msg1, centerX - msg1Width / 2 + 2, centerY - 40 + 2);
        g2.drawString(msg2, centerX - msg2Width / 2 + 2, centerY + 40 + 2);

        // Text
        g2.setColor(color);
        g2.drawString(msg1, centerX - msg1Width / 2, centerY - 40);
        g2.drawString(msg2, centerX - msg2Width / 2, centerY + 40);
    }

    // Existing Teleport UI (unchanged)
    private void drawTeleportUI(Graphics2D g2) {
        if (!(player != null && player.getAllEntities() != null))
            return;

        entity.PlayableCharacter active = player.entityM.characters.get(player.getActiveIndex());
        if (!(active instanceof entity.Wizard wizard))
            return;

        if (!wizard.isTeleportMode())
            return;

        String message = "";
        if (wizard.isSelectingTarget()) {
            message = "Teleport Mode: Select entity (WASD to move, E to select, ESC to cancel)";
        } else if (wizard.isSelectingDestination()) {
            message = "Teleport Mode: Choose destination (WASD to move, E to confirm, ESC to cancel)";
        }

        if (message.isEmpty())
            return;

        int panelWidth = getWidth();
        int panelHeight = getHeight();

        g2.setColor(new Color(0, 0, 0, 150));
        g2.fillRoundRect(0, panelHeight - 80, panelWidth, 40, 20, 20);

        g2.setFont(new Font("Arial", Font.BOLD, 18));
        g2.setColor(Color.WHITE);
        int msgWidth = g2.getFontMetrics().stringWidth(message);
        g2.drawString(message, (panelWidth - msgWidth) / 2, panelHeight - 55);
    }

    private void drawWinScreen(Graphics2D g2) {
        String msg1 = "YOU WIN!";
        String msg2 = "Press Y to Restart or N to Exit to Level Selection";

        g2.setColor(Color.BLACK);
        g2.setFont(new Font("Arial", Font.BOLD, 48));
        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;
        int msg1Width = g2.getFontMetrics().stringWidth(msg1);
        g2.setFont(new Font("Arial", Font.BOLD, 24));
        int msg2Width = g2.getFontMetrics().stringWidth(msg2);

        g2.drawString(msg1, centerX - msg1Width / 2 + 2, centerY - 40 + 2);
        g2.drawString(msg2, centerX - msg2Width / 2 + 2, centerY + 40 + 2);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 48));
        g2.drawString(msg1, centerX - msg1Width / 2, centerY - 40);
        g2.setFont(new Font("Arial", Font.BOLD, 24));
        g2.drawString(msg2, centerX - msg2Width / 2, centerY + 40);
    }

}
