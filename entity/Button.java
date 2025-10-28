package entity;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;

public class Button extends Entity {
    public enum ButtonType {
        HOLD, TOGGLE
    }

    private static BufferedImage[] unpressedSprites;
    private static BufferedImage[] pressedSprites;
    private boolean isPressed = false;
    private boolean wasPressedLastFrame = false;
    private int id;
    private ButtonType type = ButtonType.HOLD;

    public Button(int x, int y, int id) {
        this(x, y, id, ButtonType.HOLD);
    }

    public Button(int x, int y, int id, ButtonType type) {
        this.x = x;
        this.y = y;
        this.solid = false;
        this.pushable = false;
        this.id = id;
        this.type = type;
        this.hitbox = new Rectangle(4, 6, 24, 12);
        loadSprites();
    }

    private void loadSprites() {
        if (unpressedSprites == null || pressedSprites == null) {
            try {
                unpressedSprites = new BufferedImage[] {
                        ImageIO.read(getClass().getResourceAsStream("/Resource/tiles/button0_unpressed.png")),
                        ImageIO.read(getClass().getResourceAsStream("/Resource/tiles/button1_unpressed.png"))
                };
                pressedSprites = new BufferedImage[] {
                        ImageIO.read(getClass().getResourceAsStream("/Resource/tiles/button0_pressed.png")),
                        ImageIO.read(getClass().getResourceAsStream("/Resource/tiles/button1_pressed.png"))
                };
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isPressed() {
        return this.isPressed;
    }

    public boolean wasPressedLastFrame() {
        return this.wasPressedLastFrame;
    }

    public void setWasPressedLastFrame(boolean val) {
        this.wasPressedLastFrame = val;
    }

    public void press() {
        this.isPressed = true;
    }

    public void release() {
        this.isPressed = false;
    }

    public void toggle() {
        this.isPressed = !this.isPressed;
    }

    public void setPressed(boolean b) {
        this.isPressed = b;
    }

    public boolean isToggleType() {
        return type == ButtonType.TOGGLE;
    }

    public int getId() {
        return this.id;
    }

    public void draw(Graphics2D g2, int tileSize) {
        BufferedImage img = isPressed ? pressedSprites[id % pressedSprites.length]
                : unpressedSprites[id % unpressedSprites.length];
        if (img != null)
            g2.drawImage(img, x, y, tileSize, tileSize, null);
    }

}
