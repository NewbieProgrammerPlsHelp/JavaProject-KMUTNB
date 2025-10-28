package entity;

import java.awt.*;
import java.awt.image.BufferedImage;

import main.GamePanel;

public class Entity {
    // pixel coordinates (top-left)
    public int x, y;

    // store pixel coordinates of the target
    public int targetX, targetY;
    public boolean movingToTarget = false;

    // per-frame interpolation speed in pixels
    public int speed = 4;

    // rendering/misc
    protected GamePanel gp;

    public String direction = "R";
    public int spriteCounter = 0;
    public int spriteNum = 1;
    public boolean moving = false;
    public boolean solid = true;
    public boolean pushable = false;
    public boolean active = true; // generic "alive/active" flag
    public int renderLayer = 1;
    // default hitbox (offset inside sprite). Modify if sprite sizes differ.
    public Rectangle hitbox = new Rectangle(8, 16, 32, 32);
    public int hitboxOffsetX = 0;
    public int hitboxOffsetY = 0;

    // Constructor
    public Entity() {

    }

    public Entity(GamePanel gp) {
        this.gp = gp;
    }

    public void setGamePanel(GamePanel gp) {
        this.gp = gp;
    }

    public GamePanel getGamePanel() {
        return this.gp;
    }

    // return entity own hit-box
    public Rectangle getWorldHitbox() {
        return new Rectangle(x + hitbox.x + hitboxOffsetX,
                y + hitbox.y + hitboxOffsetY,
                hitbox.width,
                hitbox.height);
    }

    // Predict where the entity's hitbox would be
    // Useful for collision checks before committing movement.
    public Rectangle getPredictedHitbox(int newX, int newY) {
        return new Rectangle(newX + hitbox.x + hitboxOffsetX,
                newY + hitbox.y + hitboxOffsetY,
                hitbox.width,
                hitbox.height);
    }

    // Check collision
    public boolean collidesWith(Entity other, int proposedX, int proposedY) {
        Rectangle myHitbox = getPredictedHitbox(proposedX, proposedY);
        Rectangle otherHitbox = other.getWorldHitbox();
        return myHitbox.intersects(otherHitbox);
    }

    // updates x,y and movingToTarget
    public void interpolateToTarget() {
        if (!movingToTarget)
            return;

        if (x < targetX) {
            x = Math.min(targetX, x + speed);
        } else if (x > targetX) {
            x = Math.max(targetX, x - speed);
        }

        if (y < targetY) {
            y = Math.min(targetY, y + speed);
        } else if (y > targetY) {
            y = Math.max(targetY, y - speed);
        }

        if (x == targetX && y == targetY) {
            movingToTarget = false;
        }
    }

    // Draw helper (subclasses may override)
    public void drawAt(Graphics2D g2, BufferedImage img, int tileSize) {
        if (img == null)
            return;
        int offsetX = 2;
        int offsetY = 0;
        g2.drawImage(img, x + offsetX, y + offsetY, tileSize, tileSize, null);
    }
}
