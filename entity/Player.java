package entity;

import main.GamePanel;
import main.KeyHandler;
import java.util.ArrayList;

public class Player extends Entity {
    public final EntityManager entityM;
    final GamePanel gp;
    private final KeyHandler keyH;
    private int currentIndex = 0;

    public Player(GamePanel gp, KeyHandler keyH, EntityManager entityM) {
        this.gp = gp;
        this.keyH = keyH;
        this.entityM = entityM;

        if (entityM.characters.isEmpty()) {
            entityM.characters.add(new PlayableCharacter("knight", gp.tileSize, gp.tileSize, gp));
        }
    }

    public void update() {
        PlayableCharacter active = entityM.characters.get(currentIndex);

        // Character switching
        if (keyH.switchCharacter) {
            currentIndex = (currentIndex + 1) % entityM.characters.size();
            active = entityM.characters.get(currentIndex);
            keyH.switchCharacter = false;
            System.out.println("Now controlling: " + active.getRole());
        }

        // Movement input (disabled if Wizard is in teleport mode)
        boolean canMove = true;
        if (active instanceof Wizard w && w.isTeleportMode()) {
            canMove = false; // disable walking while teleporting
        }

        if (canMove && !active.movingToTarget) {
            int dirX = 0, dirY = 0;
            if (keyH.upPress)
                dirY = -1;
            else if (keyH.downPress)
                dirY = 1;
            else if (keyH.leftPress)
                dirX = -1;
            else if (keyH.rightPress)
                dirX = 1;

            if (dirX != 0 || dirY != 0) {
                entityM.moveEntity(active, dirX, dirY);
            }
        }

        // Update animation/interpolation
        for (int i = 0; i < entityM.characters.size(); i++) {
            PlayableCharacter pc = entityM.characters.get(i);
            if (i == currentIndex)
                pc.update(keyH);
            else
                pc.updateIdle();
            pc.interpolateToTarget();
        }

        for (Entity e : entityM.worldEntities) {
            if (e instanceof Box box)
                box.interpolateToTarget();
        }

        // Post-movement checks (spikes & buttons)
        handleHazardsAndButtons(active);
        entityM.handleEnemyContact(active);

        // Wizard teleport (manual ability)
        if (active instanceof Wizard wizard) {
            wizard.handleTeleportInput(keyH, entityM);
        }

    }

    private void handleHazardsAndButtons(Entity active) {
        gp.hazardM.update(entityM.getAllEntities());

        for (Spike s : entityM.spikes) {
            if (s.checkAndHurt(active)) {
                // Spike will handle gameOver trigger.
                // Stop further movement/updates, but don’t restart yet.
                return;
            }
        }

    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public ArrayList<Entity> getAllEntities() {
        return entityM.getAllEntities();
    }

    public int getActiveIndex() {
        return currentIndex;
    }
}
