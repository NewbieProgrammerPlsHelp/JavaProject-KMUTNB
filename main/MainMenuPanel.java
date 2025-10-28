package main;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class MainMenuPanel extends JPanel {

    private final Main mainApp;

    public MainMenuPanel(Main mainApp) {
        this.mainApp = mainApp;
        setPreferredSize(new Dimension(16 * 16 * 3, 12 * 16 * 3)); // same size as GamePanel
        setBackground(new Color(30, 30, 30));
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 0, 15, 0);
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("Adventure of Three", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 48));
        title.setForeground(Color.WHITE);

        JButton lvlSelectBtn = makeButton("Level Select", e -> mainApp.showLevelSelect());
        JButton characterBtn = makeButton("Character", e -> mainApp.showCharacterPanel());
        JButton exitButton = makeButton("Exit", e -> System.exit(0));

        gbc.gridy = 0;
        add(title, gbc);
        gbc.gridy = 1;
        add(lvlSelectBtn, gbc);
        gbc.gridy = 2;
        add(characterBtn, gbc);
        gbc.gridy = 3;
        add(exitButton, gbc);
    }

    private JButton makeButton(String text, ActionListener listener) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.BOLD, 24));
        btn.setFocusPainted(false);
        btn.addActionListener(listener);
        return btn;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Optional: simple background gradient
        Graphics2D g2 = (Graphics2D) g;
        GradientPaint gp = new GradientPaint(0, 0, new Color(50, 50, 80), 0, getHeight(), new Color(20, 20, 40));
        g2.setPaint(gp);
        g2.fillRect(0, 0, getWidth(), getHeight());
    }
}
