package entity;

import main.GamePanel;

public class Thief extends PlayableCharacter {

    public Thief(int x, int y, GamePanel gp) {
        super("thief", x, y, gp);
    }

    /** Thief ignores spikes entirely. */
    public boolean canIgnoreSpikes() {
        return true;
    }
}
