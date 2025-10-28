package entity;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import main.GamePanel;

public class Spike extends Entity {
    private BufferedImage spikeUp, spikeDown;
    private boolean active = true; // whether spike is extended
    private int id; // map id or reference if you use it

    public Spike(int x, int y, int id, boolean activeInitially, GamePanel gp) {
        super(gp);
        this.x = x;
        this.y = y;
        this.id = id;
        this.active = activeInitially;
        this.hitbox = new Rectangle(8, 8, 16, 16);
        loadSprites();
    }

    public int getId() {
        return this.id;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isActive() {
        return this.active;
    }

    public boolean checkAndHurt(Entity e) {
        if (!active)
            return false;
        if (e instanceof Thief)
            return false;

        Rectangle r = e.getWorldHitbox();
        Rectangle spikeBox = this.getWorldHitbox();

        // Small tolerance so edge doesn't misfire
        int inset = 2;
        spikeBox = new Rectangle(spikeBox.x + inset, spikeBox.y + inset,
                Math.max(0, spikeBox.width - inset * 2),
                Math.max(0, spikeBox.height - inset * 2));

        if (r.intersects(spikeBox)) {
            trigger(e);
            return true;
        }
        return false;
    }

    public void trigger(Entity mover) {
        if (mover instanceof PlayableCharacter) {
            GamePanel gp = mover.getGamePanel();
            if (gp != null && gp.gameState == GamePanel.PLAY_STATE) {
                // Just trigger game over state — do NOT restart yet.
                gp.triggerGameOver();
            }
        }
    }

    private void loadSprites() {
        if (spikeUp == null || spikeDown == null) {
            try {
                spikeUp = ImageIO.read(getClass().getResourceAsStream("/Resource/tiles/spike_active.png"));
                spikeDown = ImageIO.read(getClass().getResourceAsStream("/Resource/tiles/spike_inactive.png"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void draw(Graphics2D g2, int tileSize) {
        BufferedImage img = active ? spikeUp : spikeDown;
        if (img != null)
            g2.drawImage(img, x, y, tileSize, tileSize, null);
    }
}
