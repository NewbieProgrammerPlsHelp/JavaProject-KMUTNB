package main;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class KeyHandler implements KeyListener {

    public boolean upPress, leftPress, downPress, rightPress;
    public boolean switchCharacter, teleportPress, escapePress;
    public boolean yPress, nPress;
    private GamePanel gp; // link to the panel

    public void setGamePanel(GamePanel gp) {
        this.gp = gp;
    }

    public void resetKeys() {
        upPress = downPress = leftPress = rightPress = false;
        switchCharacter = false;
        teleportPress = escapePress = false;
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();

        // Handle differently depending on game state
        if (gp != null && gp.gameState == GamePanel.GAME_OVER_STATE) {
            if (code == KeyEvent.VK_Y) {
                gp.restartLevel();
            }
            if (code == KeyEvent.VK_N) {
                if (gp.mainApp != null) {
                    gp.mainApp.backToMenu();
                }

            }
            return; // block other input during game over
        }

        if (code == KeyEvent.VK_UP || code == KeyEvent.VK_W)
            upPress = true;
        if (code == KeyEvent.VK_LEFT || code == KeyEvent.VK_A)
            leftPress = true;
        if (code == KeyEvent.VK_DOWN || code == KeyEvent.VK_S)
            downPress = true;
        if (code == KeyEvent.VK_RIGHT || code == KeyEvent.VK_D)
            rightPress = true;
        if (code == KeyEvent.VK_Q)
            switchCharacter = true;
        if (code == KeyEvent.VK_E)
            teleportPress = true;
        if (code == KeyEvent.VK_ESCAPE)
            escapePress = true;
        if (code == KeyEvent.VK_Y)
            yPress = true;
        if (code == KeyEvent.VK_N)
            nPress = true;
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();

        if (code == KeyEvent.VK_UP || code == KeyEvent.VK_W)
            upPress = false;
        if (code == KeyEvent.VK_LEFT || code == KeyEvent.VK_A)
            leftPress = false;
        if (code == KeyEvent.VK_DOWN || code == KeyEvent.VK_S)
            downPress = false;
        if (code == KeyEvent.VK_RIGHT || code == KeyEvent.VK_D)
            rightPress = false;
        if (code == KeyEvent.VK_Q)
            switchCharacter = false;
        if (code == KeyEvent.VK_E)
            teleportPress = false;
        if (code == KeyEvent.VK_ESCAPE)
            escapePress = false;
        if (code == KeyEvent.VK_Y)
            yPress = false;
        if (code == KeyEvent.VK_N)
            nPress = false;
    }
}
