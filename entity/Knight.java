package entity;

import main.GamePanel;

public class Knight extends PlayableCharacter {

    public Knight(int x, int y, GamePanel gp) {
        super("knight", x, y, gp);
    }

    /** Kills enemy on contact instead of dying. */
    public void checkEnemyCollision(EntityManager entityM) {
        for (Enemy e : entityM.enemies) {
            if (!e.active)
                continue;
            if (this.getWorldHitbox().intersects(e.getWorldHitbox())) {
                e.active = false; // enemy dies
                System.out.println("⚔️ Knight killed an enemy!");
            }
        }
    }
}
