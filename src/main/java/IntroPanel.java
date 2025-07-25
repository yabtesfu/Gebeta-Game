import javax.swing.*;
import java.awt.*;

public class IntroPanel extends JPanel {
    private Gebeta parent;
    private JPanel resumeSection;
    private JLabel statsLabel;

    public IntroPanel(Gebeta parent) {
        this.parent = parent;
        setLayout(new GridBagLayout());
        setOpaque(true);
        setupComponents();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        Theme.enableAntialias(g2);
        BackgroundManager.paint(g2, getWidth(), getHeight(), 0);
        Theme.drawFooter(g2, getWidth(), getHeight());
    }

    private void setupComponents() {
        CardPanel card = new CardPanel(40, 36, 48);

        card.add(centeredLabel("ገበጣ", Theme.display(70), Theme.GOLD_LIGHT));
        card.add(Box.createVerticalStrut(2));
        card.add(centeredLabel("GEBETA", Theme.display(58), Theme.CREAM));
        card.add(Box.createVerticalStrut(8));
        card.add(centeredLabel("Traditional Ethiopian Mancala", Theme.body(18), Theme.PARCHMENT));
        card.add(Box.createVerticalStrut(20));

        resumeSection = new JPanel();
        resumeSection.setOpaque(false);
        resumeSection.setLayout(new BoxLayout(resumeSection, BoxLayout.Y_AXIS));
        resumeSection.add(menuButton(ThemedButton.primary("Resume Game"),
                e -> parent.resumeGame()));
        resumeSection.add(Box.createVerticalStrut(12));
        resumeSection.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(resumeSection);

        card.add(menuButton(ThemedButton.secondary("Two Players"),
                e -> parent.startGame(false, 0)));
        card.add(Box.createVerticalStrut(12));
        card.add(menuButton(ThemedButton.secondary("Play vs Computer"),
                e -> chooseDifficultyAndPlay()));
        card.add(Box.createVerticalStrut(12));
        card.add(menuButton(ThemedButton.subtle("How to Play"),
                e -> parent.showPanel("HELP")));
        card.add(Box.createVerticalStrut(12));
        card.add(menuButton(ThemedButton.subtle("About"),
                e -> parent.showPanel("ABOUT")));
        card.add(Box.createVerticalStrut(12));

        statsLabel = centeredLabel("", Theme.body(13), Theme.PARCHMENT_DARK);
        card.add(statsLabel);
        card.add(Box.createVerticalStrut(12));
        card.add(menuButton(new ThemedButton("Exit", Theme.ETH_RED),
                e -> System.exit(0)));

        add(card);
        refreshPersistence();
    }

    /** Refreshes the resume control and record whenever the menu becomes visible. */
    public void refreshPersistence() {
        if (resumeSection == null || statsLabel == null) return;
        if (parent == null) {
            resumeSection.setVisible(false);
            statsLabel.setText("VS AI  W 0 · L 0 · D 0     LOCAL  P1 0 · P2 0 · D 0");
            return;
        }
        resumeSection.setVisible(parent.hasSavedGame());
        GamePersistence.Stats stats = parent.stats();
        statsLabel.setText(String.format(
                "VS AI  W %d · L %d · D %d     LOCAL  P1 %d · P2 %d · D %d",
                stats.humanWins, stats.computerWins, stats.computerTies,
                stats.player1Wins, stats.player2Wins, stats.localTies));
        revalidate();
        repaint();
    }

    private JButton menuButton(ThemedButton button, java.awt.event.ActionListener onClick) {
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        Dimension size = new Dimension(320, 52);
        button.setPreferredSize(size);
        button.setMaximumSize(size);
        button.setMinimumSize(size);
        button.addActionListener(onClick);
        return button;
    }

    private JLabel centeredLabel(String text, Font font, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(font);
        label.setForeground(color);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        return label;
    }

    /** Asks the player for a difficulty, then starts a game against the AI. */
    private void chooseDifficultyAndPlay() {
        int depth = ThemedDialog.chooseDifficulty(this);
        if (depth < 0) {
            return; // cancelled
        }
        parent.startGame(true, depth);
    }
}
