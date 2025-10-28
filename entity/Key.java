package entity;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class Key extends Entity {
    private BufferedImage sprite;

    public Key(int x, int y, BufferedImage sprite) {
        this.x = x;
        this.y = y;
        this.targetX = x;
        this.targetY = y;
        this.pushable = true; // can be pushed like a box
        this.sprite = sprite;
        this.speed = 4;
    }

    @Override
    public void interpolateToTarget() {
        if (movingToTarget) {
            if (x < targetX)
                x += speed;
            if (x > targetX)
                x -= speed;
            if (y < targetY)
                y += speed;
            if (y > targetY)
                y -= speed;

            if (Math.abs(x - targetX) < speed && Math.abs(y - targetY) < speed) {
                x = targetX;
                y = targetY;
                movingToTarget = false;
            }
        }
    }

    public void draw(Graphics2D g2, int tileSize) {
        if (sprite != null)
            drawAt(g2, sprite, tileSize);
    }
}
