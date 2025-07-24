import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

/**
 * A themed content card: a rounded scrim panel with a gold rim that paints itself
 * behind whatever it contains. Because the text lives <em>inside</em> this component
 * (with padding from the border), it can never drift outside the box — the card and
 * its contents are one component, so they move and resize together.
 *
 * Children are stacked vertically; add struts between them for spacing.
 */
public class CardPanel extends JPanel {
    private final int arc;

    public CardPanel(int arc, int padVertical, int padHorizontal) {
        this.arc = arc;
        setOpaque(false);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(padVertical, padHorizontal, padVertical, padHorizontal));
        setAlignmentX(Component.CENTER_ALIGNMENT);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        Theme.enableAntialias(g2);
        // Inset a few pixels so the drop shadow drawn by drawCard stays within bounds.
        Theme.drawCard(g2, 2, 2, getWidth() - 8, getHeight() - 10, arc, Theme.SCRIM);
        g2.dispose();
        super.paintComponent(g);
    }

    /** A centred gold divider with a diamond, sized to sit neatly in the card. */
    public static JComponent divider(int width) {
        JComponent line = new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                Theme.enableAntialias(g2);
                Theme.drawDivider(g2, getWidth() / 2, getHeight() / 2, width / 2 - 20);
                g2.dispose();
            }
        };
        line.setAlignmentX(Component.CENTER_ALIGNMENT);
        Dimension size = new Dimension(width, 24);
        line.setPreferredSize(size);
        line.setMaximumSize(size);
        line.setMinimumSize(size);
        return line;
    }
}
