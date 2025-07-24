import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * A pit or store: geometry, its {@link Stone} objects, hit-testing, and drawing in the
 * board-mockup style. Non-store pits are light carved bowls with a spiral of stones and
 * a count pill; stores are deep rounded wells.
 */
public class Pit {
    private int x, y, width, height;
    private final List<Stone> stones;
    private final boolean isStore;
    private final int player;

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
        List<Stone> removed = new ArrayList<>(stones);
        stones.clear();
        return removed;
    }

    public int getStoneCount() { return stones.size(); }
    public boolean isEmpty() { return stones.isEmpty(); }
    public boolean isStore() { return isStore; }
    public int getPlayer() { return player; }

    public boolean contains(int mouseX, int mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    private int shownCount() {
        return Math.min(stones.size(), isStore ? 26 : 15);
    }

    /** Positions stones deterministically: a scatter in stores, a spiral in pits. */
    private void updateStonePositions() {
        int seed = x * 31 + y * 7;
        int shown = shownCount();
        if (isStore) {
            for (int k = 0; k < stones.size(); k++) {
                Stone s = stones.get(k);
                s.setSize(15);
                if (k >= shown) continue;
                int sx = x + (int) ((0.22 + Theme.rand(seed + k * 7 + 1) * 0.56) * width);
                int sy = y + (int) ((0.30 + Theme.rand(seed + k * 7 + 2) * 0.58) * height);
                s.setPosition(sx, sy);
            }
        } else {
            int cx = x + width / 2;
            int cy = y + height / 2;
            for (int k = 0; k < stones.size(); k++) {
                Stone s = stones.get(k);
                s.setSize(16);
                if (k >= shown) continue;
                double t = k * 2.399 + Theme.rand(seed + k) * 0.9;
                double rr = 0.30 * Math.sqrt((k + 0.55) / Math.max(shown, 7)) * width;
                s.setPosition(cx + (int) (rr * Math.cos(t)), cy + (int) (rr * Math.sin(t)));
            }
        }
    }

    /**
     * Draws this pit/store.
     *
     * @param playable highlight it as a legal move (gold glow)
     * @param last     ring it as the pit the last stone landed in
     * @param hint     show a "free turn" badge
     */
    public void draw(Graphics2D g2d, boolean playable, boolean last, boolean hint) {
        if (isStore) {
            drawStore(g2d);
        } else {
            drawPit(g2d, playable, last, hint);
        }
    }

    private void drawStore(Graphics2D g2d) {
        int arc = Math.min(width, height);
        g2d.setPaint(new RadialGradientPaint(
                new Point(x + width / 2, y + (int) (height * 0.35f)),
                Math.max(width, height) * 0.6f,
                new float[]{0f, 1f},
                new Color[]{Theme.STORE_TOP, Theme.STORE_BOT}));
        g2d.fillRoundRect(x, y, width, height, arc, arc);

        g2d.setStroke(new BasicStroke(3f));
        g2d.setColor(new Color(0, 0, 0, 120));
        g2d.drawRoundRect(x + 3, y + 4, width - 6, height - 8, arc - 8, arc - 8);
        g2d.setStroke(new BasicStroke(2f));
        g2d.setColor(Theme.STORE_BORDER);
        g2d.drawRoundRect(x, y, width, height, arc, arc);

        drawStones(g2d);
    }

    private void drawPit(Graphics2D g2d, boolean playable, boolean last, boolean hint) {
        if (playable) {
            g2d.setColor(new Color(229, 180, 90, 40));
            g2d.setStroke(new BasicStroke(10f));
            g2d.drawOval(x - 7, y - 7, width + 14, height + 14);
            g2d.setColor(new Color(229, 180, 90, 95));
            g2d.setStroke(new BasicStroke(6f));
            g2d.drawOval(x - 4, y - 4, width + 8, height + 8);
            g2d.setColor(new Color(229, 180, 90, 210));
            g2d.setStroke(new BasicStroke(3f));
            g2d.drawOval(x - 2, y - 2, width + 4, height + 4);
        }

        g2d.setPaint(new RadialGradientPaint(
                new Point(x + width / 2, y + (int) (height * 0.38f)),
                width * 0.75f,
                new float[]{0f, 0.7f, 1f},
                new Color[]{Theme.PIT_TOP, Theme.PIT_MID, Theme.PIT_BOT}));
        g2d.fillOval(x, y, width, height);

        // Inner carved shading.
        g2d.setStroke(new BasicStroke(3f));
        g2d.setColor(new Color(80, 50, 20, 90));
        g2d.drawOval(x + 4, y + 5, width - 8, height - 8);
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.setColor(Theme.BOARD_BORDER);
        g2d.drawOval(x, y, width, height);

        if (last && !playable) {
            g2d.setStroke(new BasicStroke(2.5f));
            g2d.setColor(new Color(243, 230, 203, 170));
            g2d.drawOval(x - 3, y - 3, width + 6, height + 6);
        }

        drawStones(g2d);

        int cx = x + width / 2;
        boolean bottom = (player == 0);
        int badgeY = bottom ? y + height + 4 : y - 26;
        drawCountBadge(g2d, cx, badgeY);
        if (hint) {
            int hintY = bottom ? y + height + 30 : y - 52;
            drawHintBadge(g2d, cx, hintY);
        }
    }

    private void drawStones(Graphics2D g2d) {
        int shown = shownCount();
        for (int k = 0; k < shown; k++) {
            stones.get(k).draw(g2d);
        }
    }

    private void drawCountBadge(Graphics2D g2d, int cx, int y) {
        String text = String.valueOf(stones.size());
        g2d.setFont(Theme.heading(15));
        FontMetrics fm = g2d.getFontMetrics();
        int tw = fm.stringWidth(text);
        int w = Math.max(28, tw + 16);
        int h = 22;
        int bx = cx - w / 2;
        g2d.setColor(Theme.BADGE_BG);
        g2d.fillRoundRect(bx, y, w, h, h, h);
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.setColor(Theme.BADGE_BORDER);
        g2d.drawRoundRect(bx, y, w, h, h, h);
        g2d.setColor(Theme.PIT_TOP);
        g2d.drawString(text, cx - tw / 2, y + h - 6);
    }

    private void drawHintBadge(Graphics2D g2d, int cx, int y) {
        String text = "FREE TURN";
        g2d.setFont(Theme.heading(11));
        FontMetrics fm = g2d.getFontMetrics();
        int tw = fm.stringWidth(text);
        int w = tw + 16;
        int h = 20;
        int bx = cx - w / 2;
        g2d.setColor(Theme.GOLD);
        g2d.fillRoundRect(bx, y, w, h, h, h);
        g2d.setColor(new Color(42, 26, 14));
        g2d.drawString(text, cx - tw / 2, y + h - 6);
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
}
