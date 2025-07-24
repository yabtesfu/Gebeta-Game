import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Pit {
    private int x, y, width, height;
    private List<Stone> stones;
    private boolean isStore;
    private int player;
    
    public Pit(int x, int y, int width, int height, boolean isStore, int player) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.isStore = isStore;
        this.player = player;
        this.stones = new ArrayList<>();
    }
    
    public void addStone(Stone stone) {
        stones.add(stone);
        updateStonePositions();
    }
    
    public Stone removeStone() {
        if (stones.isEmpty()) return null;
        Stone stone = stones.remove(stones.size() - 1);
        updateStonePositions();
        return stone;
    }
    
    public List<Stone> removeAllStones() {
        List<Stone> removedStones = new ArrayList<>(stones);
        stones.clear();
        return removedStones;
    }
    
    public int getStoneCount() {
        return stones.size();
    }
    
    public boolean isEmpty() {
        return stones.isEmpty();
    }
    
    public boolean isStore() {
        return isStore;
    }
    
    public int getPlayer() {
        return player;
    }
    
    public boolean contains(int mouseX, int mouseY) {
        return mouseX >= x && mouseX <= x + width && 
               mouseY >= y && mouseY <= y + height;
    }
    
    private void updateStonePositions() {
        if (stones.isEmpty()) return;
        if (isStore) {
            int stoneSpacing = Math.min(15, height / (stones.size() + 1));
            for (int i = 0; i < stones.size(); i++) {
                int stoneY = y + height/2 + (i - stones.size()/2) * stoneSpacing;
                stones.get(i).setPosition(x + width/2, stoneY);
            }
        } else {
            int maxStonesPerRow = 4;
            int stoneSpacing = Math.min(12, Math.min(width / maxStonesPerRow, height / ((stones.size() + maxStonesPerRow - 1) / maxStonesPerRow)));
            for (int i = 0; i < stones.size(); i++) {
                int row = i / maxStonesPerRow;
                int col = i % maxStonesPerRow;
                int stoneX = x + width/2 + (col - maxStonesPerRow/2) * stoneSpacing;
                int stoneY = y + height/2 + (row - (stones.size() + maxStonesPerRow - 1) / maxStonesPerRow / 2) * stoneSpacing;
                stones.get(i).setPosition(stoneX, stoneY);
            }
        }
    }
    
    public void draw(Graphics2D g2d) {
        // Carved hollow: a soft radial gradient makes each pit look scooped out.
        java.awt.Paint fill;
        if (isStore) {
            fill = new java.awt.GradientPaint(x, y, new Color(150, 92, 50),
                    x, y + height, new Color(96, 56, 28));
        } else {
            fill = new java.awt.RadialGradientPaint(
                    new java.awt.Point(x + width / 2, y + height / 2),
                    Math.max(width, height) / 2f,
                    new float[]{0f, 1f},
                    new Color[]{new Color(236, 216, 176), new Color(196, 162, 116)});
        }
        g2d.setPaint(fill);
        g2d.fillOval(x, y, width, height);

        // Inner shadow ring for depth.
        g2d.setStroke(new BasicStroke(3));
        g2d.setColor(new Color(60, 36, 18, 130));
        g2d.drawOval(x + 3, y + 3, width - 6, height - 6);

        // Gold rim.
        g2d.setStroke(new BasicStroke(3.5f));
        g2d.setColor(new Color(212, 165, 62));
        g2d.drawOval(x, y, width, height);

        for (Stone stone : stones) {
            stone.draw(g2d);
        }

        if (!isStore) {
            String count = String.valueOf(stones.size());
            g2d.setFont(new Font("SansSerif", Font.BOLD, 18));
            FontMetrics fm = g2d.getFontMetrics();
            int textX = x + width / 2 - fm.stringWidth(count) / 2;
            int textY = y + height + 22;
            g2d.setColor(new Color(0, 0, 0, 140));
            g2d.drawString(count, textX + 1, textY + 1);
            g2d.setColor(new Color(244, 234, 212));
            g2d.drawString(count, textX, textY);
        }
    }
    
    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
} 