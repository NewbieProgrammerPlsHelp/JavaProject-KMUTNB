package entity;

import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.image.BufferedImage;

public class Box extends Entity {
    private BufferedImage sprite;

    public Box(int x, int y, BufferedImage sprite) {
        this.x = x;
        this.y = y;
        this.targetX = x;
        this.targetY = y;
        this.pushable = true; // Crates can be pushed
        this.sprite = sprite;
        this.speed = 4;
    }

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
        if (sprite != null) {
            drawAt(g2, sprite, tileSize);
        } else {
            g2.setColor(Color.ORANGE);
            g2.fillRect(x, y, tileSize, tileSize);
        }
    }
}
