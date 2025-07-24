import javax.swing.JButton;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * A rounded, gradient-filled button in the Habesha theme: a warm base colour with a
 * gold rim, a soft top highlight, and hover/press feedback. Text is drawn by the
 * superclass on top of the custom-painted body.
 */
public class ThemedButton extends JButton {
    private final Color base;
    private boolean hover;
    private boolean pressed;

    /** A gold primary button. */
    public static ThemedButton primary(String text) {
        return new ThemedButton(text, Theme.GOLD_DARK);
    }

    /** A terracotta secondary button. */
    public static ThemedButton secondary(String text) {
        return new ThemedButton(text, Theme.TERRACOTTA);
    }

    /** A muted coffee button for tertiary actions. */
    public static ThemedButton subtle(String text) {
        return new ThemedButton(text, Theme.WOOD);
    }

    public ThemedButton(String text, Color base) {
        super(text);
        this.base = base;
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setOpaque(false);
        setForeground(Theme.CREAM);
        setFont(Theme.heading(18));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setRolloverEnabled(true);

        // Track hover/press ourselves so the effect is reliable across look-and-feels.
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hover = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hover = false;
                pressed = false;
                repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                pressed = true;
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                pressed = false;
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        Theme.enableAntialias(g2);

        int w = getWidth();
        int h = getHeight();
        int arc = Math.min(h, 30);

        Color top = hover ? base.brighter() : base;
        Color bottom = hover ? base : base.darker();
        if (pressed) {
            top = base.darker();
            bottom = base.darker().darker();
        }

        // Hover glow.
        if (hover && !pressed) {
            g2.setColor(new Color(Theme.GOLD_LIGHT.getRed(), Theme.GOLD_LIGHT.getGreen(),
                    Theme.GOLD_LIGHT.getBlue(), 70));
            g2.fillRoundRect(0, 0, w - 1, h - 1, arc + 4, arc + 4);
        }

        // Drop shadow.
        g2.setColor(new Color(0, 0, 0, 90));
        g2.fillRoundRect(2, pressed ? 3 : 4, w - 4, h - 5, arc, arc);

        // Body gradient.
        g2.setPaint(new GradientPaint(0, 0, top, 0, h, bottom));
        g2.fillRoundRect(2, pressed ? 2 : 0, w - 5, h - 5, arc, arc);

        // Glossy top highlight.
        g2.setColor(new Color(255, 255, 255, hover ? 85 : 45));
        g2.fillRoundRect(6, 3, w - 15, (h - 5) / 2, arc - 6, arc - 6);

        // Gold rim.
        g2.setStroke(new BasicStroke(hover ? 2.6f : 2f));
        g2.setColor(hover ? Theme.GOLD_LIGHT : Theme.GOLD);
        g2.drawRoundRect(2, pressed ? 2 : 0, w - 5, h - 5, arc, arc);

        g2.dispose();
        super.paintComponent(g);
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        d.height = Math.max(d.height, 44);
        return d;
    }
}
