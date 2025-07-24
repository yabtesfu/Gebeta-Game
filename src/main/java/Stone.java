import java.awt.*;

public class Stone {
    private int x, y;
    private Color color;
    private static final int SIZE = 11;

    public Stone(int x, int y) {
        this.x = x;
        this.y = y;
        // Playing pieces look like polished coffee beans / dark river stones.
        Color[] colors = {
            new Color(58, 34, 20),
            new Color(78, 44, 24),
            new Color(44, 26, 16),
            new Color(96, 56, 30),
            new Color(120, 72, 38)
        };
        this.color = colors[(int) (Math.random() * colors.length)];
    }

    public void draw(Graphics2D g2d) {
        int r = SIZE;
        // Soft contact shadow.
        g2d.setColor(new Color(0, 0, 0, 70));
        g2d.fillOval(x - r / 2 + 1, y - r / 2 + 2, r, r);
        // Rounded stone with a subtle radial sheen.
        g2d.setPaint(new java.awt.RadialGradientPaint(
                new java.awt.Point(x - 2, y - 2), r,
                new float[]{0f, 1f},
                new Color[]{color.brighter(), color}));
        g2d.fillOval(x - r / 2, y - r / 2, r, r);
        // Tiny specular highlight.
        g2d.setColor(new Color(255, 240, 210, 150));
        g2d.fillOval(x - r / 2 + 2, y - r / 2 + 2, r / 3, r / 3);
        g2d.setColor(new Color(20, 12, 6, 180));
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