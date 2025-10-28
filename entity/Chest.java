package entity;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class Chest extends Entity {
    private BufferedImage sprite;

    public Chest(int x, int y, BufferedImage sprite) {
        this.x = x;
        this.y = y;
        this.targetX = x;
        this.targetY = y;
        this.pushable = false; // static, cannot move
        this.sprite = sprite;
    }

    public void draw(Graphics2D g2, int tileSize) {
        if (sprite != null)
            drawAt(g2, sprite, tileSize);
    }
}
