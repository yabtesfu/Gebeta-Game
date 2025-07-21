import java.awt.*;

public class Stone {
    private int x, y;
    private Color color;
    private static final int SIZE = 8;
    
    public Stone(int x, int y) {
        this.x = x;
        this.y = y;
        Color[] colors = {
            new Color(139, 69, 19),
            new Color(160, 82, 45),
            new Color(205, 133, 63),
            new Color(210, 105, 30),
            new Color(244, 164, 96)
        };
        this.color = colors[(int)(Math.random() * colors.length)];
    }
    
    public void draw(Graphics2D g2d) {
        g2d.setColor(color);
        g2d.fillOval(x - SIZE/2, y - SIZE/2, SIZE, SIZE);
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(1));
        g2d.drawOval(x - SIZE/2, y - SIZE/2, SIZE, SIZE);
    }
    
    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }
    
    public int getX() { return x; }
    public int getY() { return y; }
} 