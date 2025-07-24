import java.awt.*;

public class Stone {
    private int x, y;
    private final Color color;
    private int size = 16;

    // Playing-piece palette from the board mockup.
    private static final Color[] COLORS = {
        new Color(138, 75, 35),   // #8a4b23
        new Color(93, 51, 23),    // #5d3317
        new Color(58, 35, 26),    // #3a231a
        new Color(168, 106, 53),  // #a86a35
        new Color(115, 65, 36)    // #734124
    };

    public Stone(int x, int y) {
        this.x = x;
        this.y = y;
        this.color = COLORS[(int) (Math.random() * COLORS.length)];
    }

    public void setSize(int size) {
        this.size = size;
    }

    public void draw(Graphics2D g2d) {
        int r = size;
        // Contact shadow.
        g2d.setColor(new Color(0, 0, 0, 80));
        g2d.fillOval(x - r / 2 + 1, y - r / 2 + 2, r, r);
        // Rounded stone with a sheen (light top-left, darker toward the edge).
        g2d.setPaint(new RadialGradientPaint(
                new Point(x - r / 4, y - r / 4), r,
                new float[]{0f, 1f},
                new Color[]{color.brighter(), color.darker()}));
        g2d.fillOval(x - r / 2, y - r / 2, r, r);
        // Specular highlight.
        g2d.setColor(new Color(255, 245, 220, 120));
        g2d.fillOval(x - r / 2 + 3, y - r / 2 + 2, r / 3, r / 3);
        // Dark inset edge.
        g2d.setColor(new Color(20, 12, 6, 150));
        g2d.setStroke(new BasicStroke(1));
        g2d.drawOval(x - r / 2, y - r / 2, r, r);
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() { return x; }
    public int getY() { return y; }
}
