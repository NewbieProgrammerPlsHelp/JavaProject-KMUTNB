package main;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Map;
import tile.TileManager;

/**
 * Character introduction screen — shows all playable characters with their
 * sprite and description.
 * Accessed via "Character" button in Main Menu.
 */
public class CharacterPanel extends JPanel {
    private final Main mainApp;
    private final TileManager tileManager;

    public CharacterPanel(Main mainApp) {
        this.mainApp = mainApp;
        this.tileManager = new TileManager(null); // load sprite bank without GamePanel dependency

        setPreferredSize(new Dimension(16 * 16 * 3, 12 * 16 * 3)); // match your other panels
        setBackground(new Color(25, 25, 60));
        setLayout(new BorderLayout());

        JLabel title = new JLabel("Meet the Heroes", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 42));
        title.setForeground(Color.WHITE);
        add(title, BorderLayout.NORTH);

        // --- Center Grid for characters ---
        JPanel grid = new JPanel(new GridLayout(1, 3, 20, 10));
        grid.setOpaque(false);
        grid.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        addCharacterPanel(grid, "knight", "Knight — A brave warrior. Can destroy any enemies on contact");
        addCharacterPanel(grid, "thief",
                "Thief — Agile and quick, can sneak through tight spaces. Can't be detected by enemies. Can walk on spike");
        addCharacterPanel(grid, "wizard", "Wizard — Master of teleportation and magic attacks.");

        add(grid, BorderLayout.CENTER);

        // --- Back button at the bottom ---
        JButton backButton = new JButton("Back to Menu");
        backButton.setFont(new Font("Arial", Font.BOLD, 24));
        backButton.setFocusPainted(false);
        backButton.addActionListener(e -> mainApp.showMenu());
        JPanel backPanel = new JPanel();
        backPanel.setOpaque(false);
        backPanel.add(backButton);
        add(backPanel, BorderLayout.SOUTH);
    }

    private void addCharacterPanel(JPanel parent, String role, String description) {
        JPanel charPanel = new JPanel();
        charPanel.setOpaque(false);
        charPanel.setLayout(new BorderLayout());

        // --- Character sprite (centered) ---
        JLabel spriteLabel = new JLabel();
        spriteLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // get first frame (right-facing)
        Map<String, BufferedImage[]> sprites = tileManager.playableSprites;
        BufferedImage[] frames = sprites.get(role);
        if (frames != null && frames.length > 0) {
            spriteLabel.setIcon(new ImageIcon(frames[3].getScaledInstance(96, 96, Image.SCALE_SMOOTH)));
        } else {
            spriteLabel.setText("[Missing Sprite]");
            spriteLabel.setForeground(Color.RED);
        }

        // --- Character name + description ---
        JTextArea info = new JTextArea(description);
        info.setFont(new Font("Arial", Font.PLAIN, 18));
        info.setForeground(Color.WHITE);
        info.setBackground(new Color(0, 0, 0, 0));
        info.setEditable(false);
        info.setLineWrap(true);
        info.setWrapStyleWord(true);
        info.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        charPanel.add(spriteLabel, BorderLayout.CENTER);
        charPanel.add(info, BorderLayout.SOUTH);
        parent.add(charPanel);
    }
}
