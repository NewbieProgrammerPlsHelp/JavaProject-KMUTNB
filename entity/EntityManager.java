package entity;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import entity.Button.ButtonType;
import main.GamePanel;
import main.KeyHandler;
import tile.TileManager;

public class EntityManager {
    private GamePanel gp;
    private KeyHandler keyH;

    // ENTITY COLLECTION
    public ArrayList<PlayableCharacter> characters;
    public ArrayList<Entity> worldEntities;
    public ArrayList<Enemy> enemies;
    public ArrayList<Button> buttons;
    public ArrayList<Spike> spikes;
    public ArrayList<Key> keys;

    public EntityManager(GamePanel gp, KeyHandler keyH) {
        this.gp = gp;
        this.keyH = keyH;

        characters = new ArrayList<>();
        worldEntities = new ArrayList<>();
        enemies = new ArrayList<>();
        buttons = new ArrayList<>();
        spikes = new ArrayList<>();
        keys = new ArrayList<>();
    }

    // LOADING FROM TILE MANAGER
    public void loadFromTileManager(TileManager tm) {
        clearAll();

        // Playable Characters
        for (int[] pos : tm.playableCharacterPositions) {
            int col = pos[0], row = pos[1], type = pos[2];
            String role = switch (type) {
                case 0 -> "knight";
                case 1 -> "thief";
                case 2 -> "wizard";
                default -> "knight";
            };
            PlayableCharacter pc;
            switch (role) {
                case "knight" -> pc = new Knight(col * gp.tileSize, row * gp.tileSize, gp);
                case "thief" -> pc = new Thief(col * gp.tileSize, row * gp.tileSize, gp);
                case "wizard" -> pc = new Wizard(col * gp.tileSize, row * gp.tileSize, gp);
                default -> pc = new PlayableCharacter(role, col * gp.tileSize, row * gp.tileSize, gp);
            }
            characters.add(pc);

        }

        // Boxes
        for (int[] pos : tm.boxPositions) {
            int col = pos[0], row = pos[1], type = pos[2];
            BufferedImage sprite = (type >= 0 && type < tm.boxSprites.length) ? tm.boxSprites[type] : null;
            worldEntities.add(new Box(col * gp.tileSize, row * gp.tileSize, sprite));
        }

        // Enemies
        for (int[] pos : tm.enemyPositions) {
            int col = pos[0], row = pos[1], type = pos[2];

            // Even:Patrol Odd:Stationary
            boolean patrolEnabled = (type % 2 == 0);

            // Color Marking
            BufferedImage enemySprite = new BufferedImage(gp.tileSize, gp.tileSize, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = enemySprite.createGraphics();
            g2.setColor(patrolEnabled ? Color.RED : Color.BLUE);
            g2.fillRect(0, 0, gp.tileSize, gp.tileSize);
            g2.dispose();

            Enemy enemy = new Enemy(col * gp.tileSize, row * gp.tileSize, gp, patrolEnabled);
            enemies.add(enemy);
        }

        // initialize position (snap to grid)
        for (PlayableCharacter pc : characters) {
            pc.targetX = pc.x = (pc.x / gp.tileSize) * gp.tileSize;
            pc.targetY = pc.y = (pc.y / gp.tileSize) * gp.tileSize;
        }
        for (Entity e : worldEntities) {
            e.targetX = e.x;
            e.targetY = e.y;
        }
        for (Enemy en : enemies) {
            en.targetX = en.x;
            en.targetY = en.y;
        }

        // scan map codes for buttons & spikes
        for (int row = 0; row < gp.maxScreenRow; row++) {
            for (int col = 0; col < gp.maxScreenCol; col++) {
                String code = tm.mapCodes[col][row];
                if (code == null)
                    continue;
                try {
                    int num = Integer.parseInt(code);
                    // Button
                    // Hold
                    if (num >= 60 && num <= 64)
                        buttons.add(new Button(col * gp.tileSize, row * gp.tileSize, num - 60, ButtonType.HOLD));
                    // Toggle
                    else if (num >= 65 && num <= 69)
                        buttons.add(new Button(col * gp.tileSize, row * gp.tileSize, num - 60, ButtonType.TOGGLE));
                    // Spike
                    else if (num >= 70 && num <= 79)
                        spikes.add(new Spike(col * gp.tileSize, row * gp.tileSize, num - 70, true, gp));
                } catch (NumberFormatException ignored) {
                }
            }
        }
        // Keys & Chests
        for (int row = 0; row < gp.maxScreenRow; row++) {
            for (int col = 0; col < gp.maxScreenCol; col++) {
                String code = tm.mapCodes[col][row];
                if (code == null)
                    continue;

                try {
                    int num = Integer.parseInt(code);
                    int x = col * gp.tileSize;
                    int y = row * gp.tileSize;

                    if (num == 8) {
                        Key key = new Key(x, y, tm.tile.get(8).image);
                        keys.add(key);
                        worldEntities.add(key);
                        tm.mapCodes[col][row] = "0"; // make it grass again
                    } else if (num == 9) {
                        Chest chest = new Chest(x, y, tm.tile.get(9).image);
                        worldEntities.add(chest);
                        tm.mapCodes[col][row] = "0"; // also grass again
                    }

                } catch (NumberFormatException ignored) {
                }
            }
        }
    }

    // MAIN UPDATE
    public void update() {
        // Movement update
        for (Entity e : worldEntities)
            e.interpolateToTarget();

        // Enemy AI update
        for (Enemy e : enemies) {
            if (!e.active)
                continue;
            e.update();
        }

        // Enemy contact detection
        for (PlayableCharacter pc : characters)
            handleEnemyContact(pc);

        // Win/Loss Logic
        checkKeyWinOrDestruction();
    }

    // RESTARTING
    public void clearAll() {
        characters.clear();
        enemies.clear();
        worldEntities.clear();
        spikes.clear();
        buttons.clear();
        keys.clear();
    }

    // MOVEMENT AND COLLISION
    public boolean canMoveTo(Entity mover, int targetCol, int targetRow) {
        if (targetCol < 0 || targetCol >= gp.maxScreenCol || targetRow < 0 || targetRow >= gp.maxScreenRow)
            return false;

        int proposedX = targetCol * gp.tileSize;
        int proposedY = targetRow * gp.tileSize;

        // --- 1. TILE COLLISION ---
        if (gp.tileM.isTileCollidable(targetCol, targetRow)) {
            String raw = gp.tileM.mapCodes != null ? gp.tileM.mapCodes[targetCol][targetRow] : null;
            if (!isHazardCodeWalkableSafe(raw))
                return false; // solid tile
        }

        // --- 2. ENTITY COLLISION ---
        Rectangle moverPredicted = mover.getPredictedHitbox(proposedX, proposedY);
        for (Entity e : getAllEntities()) {
            if (e == mover || !e.active)
                continue;

            // Skip non-blocking entities like Enemies or Chest/Key (to allow overlap)
            if (e instanceof Enemy || e instanceof Key || e instanceof Chest)
                continue;

            if (moverPredicted.intersects(e.getWorldHitbox())) {
                // Allow enemies to move into player tiles (so they can "catch" them)
                if (mover instanceof Enemy && e instanceof PlayableCharacter) {
                    continue;
                }
                // --- Enemies should not block movement ---
                if (e instanceof Enemy) {
                    continue; // allow moving into enemy tile
                }
                // --- Normal push logic ---
                if (e.pushable) {
                    int pushCol = targetCol + Integer.signum(targetCol - (mover.x / gp.tileSize));
                    int pushRow = targetRow + Integer.signum(targetRow - (mover.y / gp.tileSize));
                    if (!canMoveTo(e, pushCol, pushRow))
                        return false;
                    return true;
                }
                return false; // other entities block
            }
        }
        return true; // all checks passed
    }

    /* Handles movement (including push logic). */
    public boolean moveEntity(PlayableCharacter pc, int dirX, int dirY) {
        int tileSize = gp.tileSize;
        int curCol = (pc.x + tileSize / 2) / tileSize;
        int curRow = (pc.y + tileSize / 2) / tileSize;
        int nextCol = curCol + dirX;
        int nextRow = curRow + dirY;

        if (!canMoveTo(pc, nextCol, nextRow))
            return false;

        int nextX = nextCol * tileSize;
        int nextY = nextRow * tileSize;
        Entity blocking = getBlockingEntity(pc, nextX, nextY);

        // --- Special handling for Enemy blocking ---
        if (blocking instanceof Enemy) {
            Enemy enemy = (Enemy) blocking;
            // if knight, kill enemy and allow movement
            if (pc instanceof Knight) {
                enemy.active = false; // enemy dies instantly
                System.out.println("⚔️ Knight killed an enemy!");
                // allow movement to proceed (do not return)
            } else {
                // other roles die on contact; trigger game-over and abort movement
                pc.active = false;
                if (gp != null)
                    gp.triggerGameOver();
                System.out.println(pc.getRole() + " was caught by an enemy!");
                return false;
            }
        }

        // If blocking pushable entity, try to push it
        if (blocking != null && blocking.pushable) {
            int pushX = blocking.x + dirX * tileSize;
            int pushY = blocking.y + dirY * tileSize;
            if (!canMoveTo(blocking, pushX / tileSize, pushY / tileSize))
                return false;
            blocking.targetX = pushX;
            blocking.targetY = pushY;
            blocking.movingToTarget = true;
        } else if (blocking != null && !(blocking instanceof Enemy)) {
            // If it's some other non-enemy blocking entity, prevent movement
            return false; // blocked by solid entity
        }

        // success — move player
        pc.targetX = nextX;
        pc.targetY = nextY;
        pc.movingToTarget = true;
        if (dirX < 0)
            pc.direction = "L";
        else if (dirX > 0)
            pc.direction = "R";

        // trigger hazard interactions
        checkHazardActivation(pc);

        return true;
    }

    /** Finds a blocking entity at a given pixel position. */
    private Entity getBlockingEntity(Entity mover, int proposedX, int proposedY) {
        Rectangle pred = mover.getPredictedHitbox(proposedX, proposedY);
        for (Entity e : getAllEntities()) {
            if (e == mover || !e.active)
                continue;
            if (pred.intersects(e.getWorldHitbox()))
                return e;
        }
        return null;
    }

    // HAZARD UTILITIES
    public void checkHazardActivation(Entity mover) {
        Rectangle hit = mover.getWorldHitbox();

        for (Button b : buttons) {
            boolean pressedNow = hit.intersects(b.getWorldHitbox());

            if (b.isToggleType()) {
                if (pressedNow && !b.wasPressedLastFrame()) {
                    b.toggle();
                    // example: toggle spikes or doors
                    // setLinkedSpikesActive(b.getId(), !b.isPressed());
                }
            } else {
                b.setPressed(pressedNow);
            }

            b.setWasPressedLastFrame(pressedNow);
        }

        for (Spike s : spikes) {
            s.checkAndHurt(mover);
        }
    }

    public void addButton(int col, int row, int id) {
        int x = col * gp.tileSize;
        int y = row * gp.tileSize;
        Button btn = new Button(x, y, id);
        this.buttons.add(btn);
        this.worldEntities.add(btn);
    }

    public void addSpike(int col, int row, int id) {
        int x = col * gp.tileSize;
        int y = row * gp.tileSize;
        Spike s = new Spike(x, y, id, true, gp);
        this.spikes.add(s);
        this.worldEntities.add(s);
    }

    public void handleEnemyContact(PlayableCharacter pc) {
        if (!pc.active)
            return; // skip dead character

        for (Enemy enemy : enemies) {
            if (!enemy.active)
                continue;
            if (pc.getWorldHitbox().intersects(enemy.getWorldHitbox())) {

                if (pc instanceof Knight knight) {
                    knight.checkEnemyCollision(this); // knight kills enemy
                    System.out.println("Knight collided with enemy!");
                    return;
                }

                // thief and wizard die on contact
                pc.active = false;
                if (gp != null)
                    gp.triggerGameOver();
                System.out.println(pc.getRole() + " was caught by an enemy!");
                return;
            }
        }
    }

    // =========================================================
    // UTILITY
    // =========================================================
    public ArrayList<Entity> getAllEntities() {
        ArrayList<Entity> all = new ArrayList<>();
        all.addAll(characters);
        all.addAll(worldEntities);
        all.addAll(enemies);
        return all;
    }

    public ArrayList<Enemy> getEnemiesAtTile(int col, int row) {
        ArrayList<Enemy> result = new ArrayList<>();
        int tileSize = gp.tileSize;

        for (Enemy enemy : enemies) {
            if (!enemy.active)
                continue;
            int eCol = (enemy.x + tileSize / 2) / tileSize;
            int eRow = (enemy.y + tileSize / 2) / tileSize;
            if (eCol == col && eRow == row) {
                result.add(enemy);
            }
        }
        return result;
    }

    /**
     * Returns the first active enemy found at the given tile coordinates, or null
     * if none.
     */
    public Enemy getEnemyAtTile(int col, int row) {
        int tileSize = gp.tileSize;
        for (Enemy enemy : enemies) {
            if (!enemy.active)
                continue;
            int eCol = (enemy.x + tileSize / 2) / tileSize;
            int eRow = (enemy.y + tileSize / 2) / tileSize;
            if (eCol == col && eRow == row) {
                return enemy;
            }
        }
        return null;
    }

    private boolean isHazardCodeWalkable(int code) {
        return (code >= 60 && code <= 69) || (code >= 70 && code <= 79) || (code >= 100 && code <= 114);
    }

    private boolean isHazardCodeWalkableSafe(String raw) {
        if (raw == null)
            return false;
        try {
            return isHazardCodeWalkable(Integer.parseInt(raw));
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    private void checkKeyWinOrDestruction() {
        int tileSize = gp.tileSize;

        for (Key key : new ArrayList<>(keys)) {
            if (!key.active)
                continue;

            Rectangle keyBox = key.getWorldHitbox();

            // Win: Key overlaps Chest
            for (Entity e : worldEntities) {
                if (e instanceof Chest chest && chest.active) {
                    if (keyBox.intersects(chest.getWorldHitbox())) {
                        key.active = false;
                        System.out.println("Key reached the treasure chest!");
                        gp.triggerWin();
                        return;
                    }
                }
            }

            // Destroyed by hazards: only check spikes that are active at this tile
            int col = (key.x + tileSize / 2) / tileSize;
            int row = (key.y + tileSize / 2) / tileSize;

            for (Spike s : spikes) {
                if (!s.isActive())
                    continue;

                int spikeCol = (s.x + tileSize / 2) / tileSize;
                int spikeRow = (s.y + tileSize / 2) / tileSize;

                if (spikeCol == col && spikeRow == row) {
                    System.out.println("Key destroyed by hazard!");
                    key.active = false;
                    worldEntities.remove(key);
                    keys.remove(key);
                    break; // only destroy once
                }
            }
        }
    }

    // =========================================================
    // DRAWING
    // =========================================================
    public void draw(Graphics2D g2, int activeCharacterIndex) {
        ArrayList<Entity> all = new ArrayList<>();
        all.addAll(spikes);
        all.addAll(buttons);
        all.addAll(worldEntities);
        all.addAll(characters);
        all.addAll(enemies);

        // Layer Priority
        for (Entity e : all) {
            if (e instanceof Spike || e instanceof Button)
                e.renderLayer = 0; // ground layer
            else if (e instanceof Box)
                e.renderLayer = 1; // mid layer
            else if (e instanceof PlayableCharacter)
                e.renderLayer = 2; // top layer
            else
                e.renderLayer = 1; // default
        }

        // sort primarily by layer, then by y-bottom
        all.sort((a, b) -> {
            int layerDiff = Integer.compare(a.renderLayer, b.renderLayer);
            if (layerDiff != 0)
                return layerDiff;
            int aBottom = a.y + a.hitbox.y + a.hitbox.height;
            int bBottom = b.y + b.hitbox.y + b.hitbox.height;
            return Integer.compare(aBottom, bBottom);
        });

        for (Entity e : all) {
            if (e instanceof PlayableCharacter pc) {
                boolean isActive = (activeCharacterIndex >= 0 && activeCharacterIndex < characters.size())
                        && pc == characters.get(activeCharacterIndex);
                if (isActive) {
                    g2.setColor(Color.YELLOW);
                    g2.fillRect(pc.x + 4, pc.y + gp.tileSize - 6, gp.tileSize - 8, 3);
                }
                pc.draw(g2, gp.tileSize, isActive);
            } else if (e instanceof Box box)
                box.draw(g2, gp.tileSize);
            else if (e instanceof Enemy en) {
                if (!en.active)
                    continue; // skip dead enemies
                en.draw(g2, gp.tileSize);
            } else if (e instanceof Button b)
                b.draw(g2, gp.tileSize);
            else if (e instanceof Spike s)
                s.draw(g2, gp.tileSize);
            else if (e instanceof Key key)
                key.draw(g2, gp.tileSize);
            else if (e instanceof Chest chest)
                chest.draw(g2, gp.tileSize);

        }
    }
}
