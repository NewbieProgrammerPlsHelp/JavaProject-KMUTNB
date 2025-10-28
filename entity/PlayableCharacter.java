package entity;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.InputStream;

import main.GamePanel;
import main.KeyHandler;

public class PlayableCharacter extends Entity {
    private String role;
    private int idleAnimSpeed, moveAnimSpeed;
    private BufferedImage l1, l2, l3, r1, r2, r3;

    public PlayableCharacter(String role, int x, int y, GamePanel gp) {
        super(gp);
        this.role = role;
        this.x = x;
        this.y = y;
        this.speed = 4; // pixels per frame (tweak for smoothness)
        this.direction = "R";
        setAnimSpeed();
        getImage();
        // initialize so character is on exact tile grid:
        // Assuming tile size exists later; if not, you will set in
        // Player.setupCharacters() after gp is available.
        this.targetX = x;
        this.targetY = y;
        this.movingToTarget = false;
        this.pushable = false;
    }

    private void setAnimSpeed() {
        switch (role) {
            case "knight":
                this.idleAnimSpeed = 20;
                this.moveAnimSpeed = 10;
                break;
            case "thief":
                this.idleAnimSpeed = 10;
                this.moveAnimSpeed = 5;
                break;
            case "wizard":
                this.idleAnimSpeed = 25;
                this.moveAnimSpeed = 15;
                break;
            default:
                this.idleAnimSpeed = 20;
                this.moveAnimSpeed = 10;
                break;
        }
    }

    private BufferedImage loadImage(String path) {
        try (InputStream is = getClass().getResourceAsStream(path)) {
            if (is == null) {
                System.err.println("Error 404: Resource not found: " + path);
                return null;
            }
            return javax.imageio.ImageIO.read(is);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void getImage() {
        String base = "/Resource/playableCharacter/" + this.role + "/";
        this.l1 = loadImage(base + this.role + "_L_1.png");
        this.l2 = loadImage(base + this.role + "_L_2.png");
        this.l3 = loadImage(base + this.role + "_L_3.png");
        this.r1 = loadImage(base + this.role + "_R_1.png");
        this.r2 = loadImage(base + this.role + "_R_2.png");
        this.r3 = loadImage(base + this.role + "_R_3.png");
    }

    /**
     * Called when this is the active (controlled) character.
     * Movement (actual pixel position) is *not* applied here — Player manages
     * proposed moves and collision.
     * This method controls animation and direction state.
     */
    public void update(KeyHandler keyH) {
        // direction is set by Player when computing movement. Here maintain moving flag
        // and animate.
        // movingToTarget flag should be set by Player when a target is accepted.
        this.moving = this.movingToTarget;

        this.spriteCounter++;
        if (this.moving) {
            if (this.spriteCounter > this.moveAnimSpeed) {
                // walking: alternate 1 <-> 3 (we use frames 2 and 3 if you prefer)
                this.spriteNum = (this.spriteNum == 1) ? 3 : 1;
                this.spriteCounter = 0;
            }
        } else {
            if (this.spriteCounter > this.idleAnimSpeed) {
                this.spriteNum = (this.spriteNum == 1) ? 2 : 1;
                this.spriteCounter = 0;
            }
        }
    }

    /** Called for all non-controlled characters so they still animate. */
    public void updateIdle() {
        this.moving = false;
        this.spriteCounter++;
        if (this.spriteCounter > this.idleAnimSpeed) {
            this.spriteNum = (this.spriteNum == 1) ? 2 : 1;
            this.spriteCounter = 0;
        }
    }

    public void draw(Graphics2D g2, int tileSize, boolean isActive) {
        BufferedImage img = null;
        switch (this.direction) {
            case "L":
                img = (spriteNum == 1) ? l1 : (spriteNum == 2 ? l2 : l3);
                break;
            default:
                img = (spriteNum == 1) ? r1 : (spriteNum == 2 ? r2 : r3);
                break;
        }

        // Draw character sprite
        if (img != null) {
            g2.drawImage(img, x, y, tileSize, tileSize, null);
        } else {
            g2.setColor(java.awt.Color.RED);
            g2.fillRect(x, y, tileSize, tileSize);
        }
    }

    public String getRole() {
        return role;
    }
}
