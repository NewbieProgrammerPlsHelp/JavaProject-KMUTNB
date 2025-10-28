package entity;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;

import main.GamePanel;

public class Enemy extends Entity {
    private BufferedImage[] spriteRightFrames;
    private BufferedImage[] spriteLeftFrames;
    private BufferedImage spriteRight, spriteLeft;
    private int currentFrame = 0;
    private int frameCounter = 0;
    private int frameDelay = 6; // adjust for animation speed

    // ===== AI fields =====
    private int patrolDistance = 3; // tiles to patrol before turning
    private int startX, startY;
    private String patrolDir = "R"; // "L", "R", "U", or "D"
    private boolean patrolAxisRandomized = false;
    private int moveCooldown = 2;
    private int moveTimer = 0;
    private boolean patrolEnabled = true;
    private boolean returningHome = false;

    // ===== Chase settings =====
    private int detectRange = 2;
    private boolean chasing = false;

    public Enemy(int x, int y, GamePanel gp, boolean patrolEnabled) {
        super(gp);
        this.x = x;
        this.y = y;
        this.speed = 2;
        this.pushable = false;
        this.startX = x;
        this.startY = y;
        this.patrolEnabled = patrolEnabled;
        loadSprites();
    }

    public Enemy(int x, int y, GamePanel gp) {
        this(x, y, gp, true);
    }

    public void update() {
        if (!active)
            return;

        if (movingToTarget) {
            interpolateToTarget();

            // Animate while sliding between tiles
            frameCounter++;
            if (frameCounter >= frameDelay) {
                frameCounter = 0;
                currentFrame = (currentFrame + 1) % spriteRightFrames.length;
                spriteRight = spriteRightFrames[currentFrame];
                spriteLeft = spriteLeftFrames[currentFrame];
            }
            return;
        }

        if (moveTimer > 0) {
            moveTimer--;
            return;
        }

        PlayableCharacter target = findNearestCharacter();
        if (target != null && isWithinDetectionRange(target)) {
            chasing = true;
        } else if (chasing && (target == null || !isWithinDetectionRange(target) || !canSeePlayer(target))) {
            chasing = false;
        }

        if (chasing && target != null) {
            performChase(target);
        } else {
            if (!patrolEnabled && (x != startX || y != startY)) {
                // Stationary enemy returns home after losing target
                performReturnToStart();
            } else {
                performPatrol();
            }
        }
        moveTimer = moveCooldown;

        // ✅ Always animate (even when idle)
        int delay = movingToTarget ? frameDelay : frameDelay * 2; // idle anim slower
        frameCounter++;
        if (frameCounter >= delay) {
            frameCounter = 0;
            currentFrame = (currentFrame + 1) % spriteRightFrames.length;
            spriteRight = spriteRightFrames[currentFrame];
            spriteLeft = spriteLeftFrames[currentFrame];
        }
    }

    // =========================================================
    // PATROL BEHAVIOR (horizontal OR vertical)
    // =========================================================
    private void performPatrol() {
        if (!patrolEnabled)
            return;

        int tileSize = gp.tileSize;
        int curCol = (x + tileSize / 2) / tileSize;
        int curRow = (y + tileSize / 2) / tileSize;

        // Randomize initial patrol direction once
        if (!patrolAxisRandomized) {
            if (Math.random() < 0.5) {
                patrolDir = (Math.random() < 0.5) ? "L" : "R";
            } else {
                patrolDir = (Math.random() < 0.5) ? "U" : "D";
            }
            patrolAxisRandomized = true;
        }

        int dirX = 0, dirY = 0;
        switch (patrolDir) {
            case "R" -> dirX = 1;
            case "L" -> dirX = -1;
            case "U" -> dirY = -1;
            case "D" -> dirY = 1;
        }

        int nextCol = curCol + dirX;
        int nextRow = curRow + dirY;

        boolean canMoveTile = gp.entityM.canMoveTo(this, nextCol, nextRow);
        boolean tooFar = (dirX != 0)
                ? Math.abs((x + dirX * tileSize) - startX) > patrolDistance * tileSize
                : Math.abs((y + dirY * tileSize) - startY) > patrolDistance * tileSize;

        Enemy occupied = gp.entityM.getEnemyAtTile(nextCol, nextRow);
        boolean occupiedByEnemy = (occupied != null && occupied != this);

        if (!canMoveTile || tooFar || occupiedByEnemy) {
            // Reverse direction
            switch (patrolDir) {
                case "R" -> patrolDir = "L";
                case "L" -> patrolDir = "R";
                case "U" -> patrolDir = "D";
                case "D" -> patrolDir = "U";
            }
            return;
        }

        targetX = x + dirX * tileSize;
        targetY = y + dirY * tileSize;
        movingToTarget = true;
    }

    // =========================================================
    // CHASE BEHAVIOR (no overlap)
    // =========================================================
    private void performChase(PlayableCharacter target) {
        int tileSize = gp.tileSize;

        int enemyCol = (x + tileSize / 2) / tileSize;
        int enemyRow = (y + tileSize / 2) / tileSize;
        int playerCol = (target.x + tileSize / 2) / tileSize;
        int playerRow = (target.y + tileSize / 2) / tileSize;

        int dirX = Integer.compare(playerCol, enemyCol);
        int dirY = Integer.compare(playerRow, enemyRow);

        int nextCol = enemyCol;
        int nextRow = enemyRow;

        // Prefer axis with larger distance
        if (Math.abs(playerCol - enemyCol) >= Math.abs(playerRow - enemyRow)) {
            nextCol = enemyCol + dirX;
        } else {
            nextRow = enemyRow + dirY;
        }

        // Fallback if blocked or occupied
        if (!gp.entityM.canMoveTo(this, nextCol, nextRow)
                || (gp.entityM.getEnemyAtTile(nextCol, nextRow) != null
                        && gp.entityM.getEnemyAtTile(nextCol, nextRow) != this)) {
            if (Math.abs(playerCol - enemyCol) >= Math.abs(playerRow - enemyRow)) {
                nextCol = enemyCol;
                nextRow = enemyRow + dirY;
            } else {
                nextCol = enemyCol + dirX;
                nextRow = enemyRow;
            }
        }

        Enemy occupied = gp.entityM.getEnemyAtTile(nextCol, nextRow);
        boolean occupiedByEnemy = (occupied != null && occupied != this);

        if (!gp.entityM.canMoveTo(this, nextCol, nextRow) || occupiedByEnemy)
            return;

        targetX = nextCol * tileSize;
        targetY = nextRow * tileSize;
        movingToTarget = true;

        patrolDir = (dirX > 0) ? "R" : (dirX < 0 ? "L" : patrolDir);
    }

    // Returning Home
    private void performReturnToStart() {
        int tileSize = gp.tileSize;
        int enemyCol = (x + tileSize / 2) / tileSize;
        int enemyRow = (y + tileSize / 2) / tileSize;
        int startCol = (startX + tileSize / 2) / tileSize;
        int startRow = (startY + tileSize / 2) / tileSize;

        int dirX = Integer.compare(startCol, enemyCol);
        int dirY = Integer.compare(startRow, enemyRow);
        int nextCol = enemyCol + dirX;
        int nextRow = enemyRow + dirY;

        if (!gp.entityM.canMoveTo(this, nextCol, nextRow))
            return;

        targetX = nextCol * tileSize;
        targetY = nextRow * tileSize;
        movingToTarget = true;

        patrolDir = (dirX > 0) ? "R" : (dirX < 0 ? "L" : patrolDir);
    }

    // =========================================================
    // DETECTION & VISIBILITY
    // =========================================================
    private PlayableCharacter findNearestCharacter() {
        if (gp == null || gp.entityM == null)
            return null;
        PlayableCharacter nearest = null;
        double minDist = Double.MAX_VALUE;

        for (PlayableCharacter pc : gp.entityM.characters) {
            if (!pc.active)
                continue;
            if ("thief".equalsIgnoreCase(pc.getRole()))
                continue;

            double dx = pc.x - this.x;
            double dy = pc.y - this.y;
            double dist = Math.sqrt(dx * dx + dy * dy);
            if (dist < minDist) {
                minDist = dist;
                nearest = pc;
            }
        }
        return nearest;
    }

    private boolean isWithinDetectionRange(PlayableCharacter pc) {
        int tileSize = gp.tileSize;
        double dx = Math.abs(pc.x - this.x);
        double dy = Math.abs(pc.y - this.y);
        double distTiles = Math.sqrt(dx * dx + dy * dy) / tileSize;
        return distTiles <= detectRange;
    }

    private boolean canSeePlayer(PlayableCharacter pc) {
        int tileSize = gp.tileSize;
        int enemyCol = (x + tileSize / 2) / tileSize;
        int enemyRow = (y + tileSize / 2) / tileSize;
        int playerCol = (pc.x + tileSize / 2) / tileSize;
        int playerRow = (pc.y + tileSize / 2) / tileSize;

        int dx = Math.abs(playerCol - enemyCol);
        int dy = Math.abs(playerRow - enemyRow);
        int sx = (enemyCol < playerCol) ? 1 : -1;
        int sy = (enemyRow < playerRow) ? 1 : -1;
        int err = dx - dy;

        int cx = enemyCol;
        int cy = enemyRow;

        while (true) {
            if (gp.tileM.isTileCollidable(cx, cy) && !(cx == enemyCol && cy == enemyRow))
                return false;
            if (cx == playerCol && cy == playerRow)
                return true;
            int e2 = 2 * err;
            if (e2 > -dy) {
                err -= dy;
                cx += sx;
            }
            if (e2 < dx) {
                err += dx;
                cy += sy;
            }
        }
    }

    // =========================================================
    // DRAWING
    // =========================================================
    public void draw(Graphics2D g2, int tileSize) {
        if (!active)
            return;
        BufferedImage current = ("R".equals(patrolDir)) ? spriteRight : spriteLeft;
        if (current != null)
            g2.drawImage(current, x, y, tileSize, tileSize, null);
        else {
            g2.setColor(chasing ? Color.ORANGE : Color.RED);
            g2.fillRect(x, y, tileSize, tileSize);
        }
    }

    // Sprite Loading
    private void loadSprites() {
        try {
            String basePath = "/Resource/enemies/";

            if (patrolEnabled) {
                spriteRightFrames = sliceSpriteSheet(
                        ImageIO.read(getClass().getResourceAsStream(basePath + "BloodshotEye_right.png")), 4);
                spriteLeftFrames = sliceSpriteSheet(
                        ImageIO.read(getClass().getResourceAsStream(basePath + "BloodshotEye_left.png")), 4);
            } else {
                spriteRightFrames = sliceSpriteSheet(
                        ImageIO.read(getClass().getResourceAsStream(basePath + "OcularWatcher_right.png")), 4);
                spriteLeftFrames = sliceSpriteSheet(
                        ImageIO.read(getClass().getResourceAsStream(basePath + "OcularWatcher_left.png")), 4);
            }

            currentFrame = 0;
            spriteRight = spriteRightFrames[currentFrame];
            spriteLeft = spriteLeftFrames[currentFrame];

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private BufferedImage[] sliceSpriteSheet(BufferedImage sheet, int cols) {
        int frameWidth = sheet.getWidth() / cols;
        int frameHeight = sheet.getHeight();
        BufferedImage[] frames = new BufferedImage[cols];
        for (int i = 0; i < cols; i++)
            frames[i] = sheet.getSubimage(i * frameWidth, 0, frameWidth, frameHeight);
        return frames;
    }
}
