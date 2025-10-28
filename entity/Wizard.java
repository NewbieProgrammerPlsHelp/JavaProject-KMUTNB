package entity;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.List;

import main.GamePanel;
import main.KeyHandler;

public class Wizard extends PlayableCharacter {

    // --- Teleport system ---
    private boolean teleportSelectingTarget = false;
    private boolean teleportSelectingDestination = false;
    private Entity selectedEntity = null;

    private int cursorCol, cursorRow;
    private int centerCol, centerRow;
    private static final int TELEPORT_RADIUS = 2; // easy to tweak later

    public Wizard(int x, int y, GamePanel gp) {
        super("wizard", x, y, gp);
    }

    public boolean isTeleportMode() {
        return teleportSelectingTarget || teleportSelectingDestination;
    }

    public boolean isSelectingTarget() {
        return teleportSelectingTarget;
    }

    public boolean isSelectingDestination() {
        return teleportSelectingDestination;
    }

    public void handleTeleportInput(KeyHandler keyH, EntityManager entityM) {
        int tileSize = gp.tileSize;

        // --- ENTER TELEPORT MODE ---
        if (!isTeleportMode() && keyH.teleportPress) {
            teleportSelectingTarget = true;
            keyH.teleportPress = false;

            centerCol = x / tileSize;
            centerRow = y / tileSize;
            cursorCol = centerCol;
            cursorRow = centerRow;
            selectedEntity = null;
            System.out.println("Wizard entered teleport target selection.");
            return;
        }

        // --- IF NOT IN ANY TELEPORT MODE, RETURN ---
        if (!isTeleportMode())
            return;

        // --- CANCEL (ESC) ---
        if (keyH.escapePress) {
            teleportSelectingTarget = false;
            teleportSelectingDestination = false;
            selectedEntity = null;
            keyH.escapePress = false;
            System.out.println("Teleport canceled.");
            return;
        }

        // --- MOVE CURSOR (WASD) ---
        int moveX = 0, moveY = 0;
        if (keyH.upPress)
            moveY = -1;
        else if (keyH.downPress)
            moveY = 1;
        else if (keyH.leftPress)
            moveX = -1;
        else if (keyH.rightPress)
            moveX = 1;

        if (moveX != 0 || moveY != 0) {
            int nextCol = cursorCol + moveX;
            int nextRow = cursorRow + moveY;
            if (Math.abs(nextCol - centerCol) <= TELEPORT_RADIUS && Math.abs(nextRow - centerRow) <= TELEPORT_RADIUS) {
                cursorCol = nextCol;
                cursorRow = nextRow;
            }
            keyH.upPress = keyH.downPress = keyH.leftPress = keyH.rightPress = false;
        }

        // --- PHASE 1: SELECT TARGET ---
        if (teleportSelectingTarget && keyH.teleportPress) {
            keyH.teleportPress = false;
            Entity found = findEntityAt(cursorCol, cursorRow, entityM.getAllEntities());

            if (found != null && (found instanceof PlayableCharacter || found instanceof Box || found instanceof Key)) {
                selectedEntity = found;
                teleportSelectingTarget = false;
                teleportSelectingDestination = true;
                System.out.println("Selected entity: " + found.getClass().getSimpleName());
            } else {
                System.out.println("No valid entity to teleport at cursor.");
            }
            return;
        }

        // --- PHASE 2: SELECT DESTINATION ---
        if (teleportSelectingDestination && keyH.teleportPress) {
            keyH.teleportPress = false;
            if (isTileWalkable(cursorCol, cursorRow)) {
                int tx = cursorCol * tileSize;
                int ty = cursorRow * tileSize;
                if (selectedEntity != null) {
                    selectedEntity.x = tx;
                    selectedEntity.y = ty;
                    selectedEntity.targetX = tx;
                    selectedEntity.targetY = ty;
                    selectedEntity.movingToTarget = false;
                    System.out.println("Teleported " + selectedEntity.getClass().getSimpleName() +
                            " to (" + cursorCol + "," + cursorRow + ")");
                }
                teleportSelectingDestination = false;
                selectedEntity = null;
            } else {
                System.out.println("Invalid teleport location.");
            }
        }
    }

    /** Finds an entity at a given tile. */
    private Entity findEntityAt(int col, int row, List<Entity> all) {
        int tileSize = gp.tileSize;
        Rectangle checkArea = new Rectangle(col * tileSize, row * tileSize, tileSize, tileSize);
        for (Entity e : all) {
            if (!e.active)
                continue;
            if (e.getWorldHitbox().intersects(checkArea))
                return e;
        }
        return null;
    }

    /** Checks if target tile is safe to teleport to. */
    private boolean isTileWalkable(int col, int row) {
        if (col < 0 || col >= gp.maxScreenCol || row < 0 || row >= gp.maxScreenRow)
            return false;
        // check for soid tiles
        if (gp.tileM.isTileCollidable(col, row))
            return false;

        int tileSize = gp.tileSize;
        int targetX = col * tileSize;
        int targetY = row * tileSize;
        Rectangle check = new Rectangle(targetX, targetY, tileSize, tileSize);

        // check for enemy
        for (Entity e : gp.entityM.getAllEntities()) {
            if (!e.active || e == this)
                continue;
            if (e.getWorldHitbox().intersects(check) && e.solid)
                return false;
        }

        // check for hazard
        for (Spike spike : gp.entityM.spikes) {
            if (spike.isActive() && spike.getWorldHitbox().intersects(check))
                return false;
        }
        return true;
    }

    /** Draw wizard + teleport overlays. */
    @Override
    public void draw(Graphics2D g2, int tileSize, boolean isActive) {
        super.draw(g2, tileSize, isActive);

        if (!isTeleportMode())
            return;

        // Draw teleport radius
        g2.setColor(new Color(0, 255, 255, 60));
        for (int c = centerCol - TELEPORT_RADIUS; c <= centerCol + TELEPORT_RADIUS; c++) {
            for (int r = centerRow - TELEPORT_RADIUS; r <= centerRow + TELEPORT_RADIUS; r++) {
                if (Math.abs(c - centerCol) + Math.abs(r - centerRow) <= TELEPORT_RADIUS) {
                    g2.drawRect(c * tileSize, r * tileSize, tileSize, tileSize);
                }
            }
        }

        // Cursor highlight
        if (teleportSelectingTarget)
            g2.setColor(new Color(0, 255, 0, 120));
        else if (teleportSelectingDestination)
            g2.setColor(new Color(255, 255, 0, 120));
        g2.fillRect(cursorCol * tileSize, cursorRow * tileSize, tileSize, tileSize);
    }
}
