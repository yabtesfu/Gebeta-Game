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
        if (isStore) {
            g2d.setColor(new Color(210, 180, 140));
        } else {
            g2d.setColor(new Color(245, 222, 179));
        }
        g2d.fillOval(x, y, width, height);
        g2d.setColor(new Color(139, 69, 19));
        g2d.setStroke(new BasicStroke(3));
        g2d.drawOval(x, y, width, height);
        for (Stone stone : stones) {
            stone.draw(g2d);
        }
        if (!isStore) {
            g2d.setColor(Color.BLACK);
            g2d.setFont(new Font("Nyala", Font.BOLD, 16));
            String count = String.valueOf(stones.size());
            FontMetrics fm = g2d.getFontMetrics();
            int textX = x + width/2 - fm.stringWidth(count)/2;
            int textY = y + height + 20;
            g2d.drawString(count, textX, textY);
        }
    }
    
    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
} 