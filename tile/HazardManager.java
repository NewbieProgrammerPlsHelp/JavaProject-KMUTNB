package tile;

import entity.Button;
import entity.Spike;
import entity.Entity;
import main.GamePanel;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class HazardManager {
    private GamePanel gp;

    // Optional: mapping buttonID → list of spikeIDs (for linking)
    private Map<Integer, ArrayList<Integer>> linkMap = new HashMap<>();

    public HazardManager(GamePanel gp) {
        this.gp = gp;
    }

    // Linking button
    public void link(int buttonId, int... spikeIds) {
        ArrayList<Integer> linked = linkMap.computeIfAbsent(buttonId, k -> new ArrayList<>());
        for (int id : spikeIds)
            linked.add(id);
    }

    // Check button press and toggle
    public void update(ArrayList<Entity> allEntities) {
        for (Button b : gp.entityM.buttons) {
            boolean pressed = allEntities.stream()
                    .filter(e -> !(e instanceof Button) && e.active)
                    .anyMatch(e -> e.getWorldHitbox().intersects(b.getWorldHitbox()));

            if (b.isToggleType()) {
                // toggle-type button changes state only when pressed down, not held
                if (pressed && !b.wasPressedLastFrame()) {
                    b.toggle();
                    setLinkedSpikesActive(b.getId(), !b.isPressed()); // invert logic if needed
                }
            } else {
                // hold-type (default)
                if (pressed != b.isPressed()) {
                    if (pressed)
                        setLinkedSpikesActive(b.getId(), false);
                    else
                        setLinkedSpikesActive(b.getId(), true);
                    b.setPressed(pressed);
                }
            }
            b.setWasPressedLastFrame(pressed);

        }
    }

    private void setLinkedSpikesActive(int buttonId, boolean activeState) {
        ArrayList<Integer> linkedSpikes = linkMap.get(buttonId);
        if (linkedSpikes == null)
            return;

        for (Spike s : gp.entityM.spikes) {
            if (linkedSpikes.contains(s.getId())) {
                s.setActive(activeState);
            }
        }
    }

    /** Draws all hazard-type entities (optional if EntityManager handles it) */
    public void draw(Graphics2D g2) {
        for (Spike s : gp.entityM.spikes)
            s.draw(g2, gp.tileSize);
        for (Button b : gp.entityM.buttons)
            b.draw(g2, gp.tileSize);
    }
}
