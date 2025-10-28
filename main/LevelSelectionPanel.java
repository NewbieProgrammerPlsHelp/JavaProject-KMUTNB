package main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class LevelSelectionPanel extends JPanel {
    private final Main mainApp;

    public LevelSelectionPanel(Main mainApp) {
        this.mainApp = mainApp;
        setPreferredSize(new Dimension(16 * 16 * 3, 12 * 16 * 3));
        setBackground(new Color(20, 20, 50));
        setLayout(new BorderLayout());

        // ===== TITLE =====
        JLabel title = new JLabel("Select Level", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 42));
        title.setForeground(Color.WHITE);
        title.setBorder(BorderFactory.createEmptyBorder(30, 0, 20, 0));
        add(title, BorderLayout.NORTH);

        // ===== LEVEL BUTTON GRID =====
        JPanel levelGrid = new JPanel(new GridLayout(3, 3, 15, 15));
        levelGrid.setOpaque(false);

        String[] levels = {
                "Level 1", "Level 2", "Level 3",
                "Level 4", "Level 5", "Level 6",
                "Level 7", "Level 8", "Level 9"
        };

        for (int i = 0; i < levels.length; i++) {
            int index = i + 1;
            levelGrid.add(makeButton(levels[i], e -> loadLevel("map0" + index + ".txt")));
        }

        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setOpaque(false);
        centerPanel.add(levelGrid);
        add(centerPanel, BorderLayout.CENTER);

        // ===== BACK BUTTON =====
        JButton back = makeButton("Back to Menu", e -> mainApp.showMenu());
        back.setFont(new Font("Arial", Font.BOLD, 24));
        back.setPreferredSize(new Dimension(200, 50));

        JPanel bottomPanel = new JPanel();
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 30, 0));
        bottomPanel.add(back);
        add(bottomPanel, BorderLayout.SOUTH);

    }

    private JButton makeButton(String text, ActionListener listener) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.BOLD, 28));
        btn.setFocusPainted(false);
        btn.addActionListener(listener);
        return btn;
    }

    private void loadLevel(String mapFile) {
        mainApp.showGamePanel(mapFile); // call the new overload
    }
}
