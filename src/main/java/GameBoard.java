import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Bridges the pure {@link MancalaState} rules engine to the on-screen board.
 *
 * All game rules now live in {@link MancalaState}; this class keeps the visual
 * {@link Pit} objects (their positions, and the {@link Stone} objects drawn inside
 * them) in sync with that state after every move. Because the rules no longer live
 * here, they can be unit-tested and searched by the AI independently of rendering.
 */
public class GameBoard {
    private List<Pit> pits;
    private MancalaState state;
    private boolean animating;

    public GameBoard() {
        state = new MancalaState();
        pits = new ArrayList<>();
        initializeBoard();
    }

    private void initializeBoard() {
        pits.clear();

        // Player 0's six pits (bottom row), then Player 0's store on the right.
        for (int i = 0; i < 6; i++) {
            pits.add(new Pit(200 + i * 120, 400, 100, 100, false, 0));
        }
        pits.add(new Pit(1070, 200, 80, 300, true, 0));

        // Player 1's six pits (top row), then Player 1's store on the left.
        for (int i = 5; i >= 0; i--) {
            pits.add(new Pit(200 + i * 120, 200, 100, 100, false, 1));
        }
        pits.add(new Pit(50, 200, 80, 300, true, 1));

        syncVisuals();
    }

    /**
     * Makes each visual pit hold as many {@link Stone} objects as the rules engine
     * says it should, adding or removing stones so existing ones keep their colours.
     * Called at the end of an animation to guarantee the display matches the model.
     */
    public void syncVisuals() {
        for (int i = 0; i < MancalaState.SIZE; i++) {
            Pit pit = pits.get(i);
            int target = state.stones(i);
            while (pit.getStoneCount() > target) {
                pit.removeStone();
            }
            while (pit.getStoneCount() < target) {
                pit.addStone(new Stone(0, 0));
            }
        }
    }

    /**
     * Applies a move through the rules engine and returns a {@link MoveTrace} for the
     * caller to animate. The on-screen stones are deliberately left untouched so the
     * animation can play the move out step by step, then call {@link #syncVisuals()}.
     *
     * @return the trace, or {@code null} if the move was illegal.
     */
    public MoveTrace makeMove(int pitIndex) {
        return state.applyMoveTraced(pitIndex);
    }

    // ---- step-wise visual operations, driven by the animator ----

    /** Visually lifts every stone out of a pit (the start of a move). */
    public void visualPickUp(int index) {
        pits.get(index).removeAllStones();
    }

    /** Visually drops a single stone into a pit. */
    public void visualDrop(int index) {
        pits.get(index).addStone(new Stone(0, 0));
    }

    /** Visually performs a capture: empties both pits and fills the store. */
    public void visualCapture(int landingPit, int oppositePit, int store, int count) {
        pits.get(landingPit).removeAllStones();
        pits.get(oppositePit).removeAllStones();
        for (int i = 0; i < count; i++) {
            pits.get(store).addStone(new Stone(0, 0));
        }
    }

    /** While true, {@link #draw} skips the game-over overlay so the final move animates first. */
    public void setAnimating(boolean animating) {
        this.animating = animating;
    }

    private Pit getPlayerStore(int player) {
        return pits.get(MancalaState.storeIndex(player));
    }

    /**
     * Draws the board onto a pre-painted background (the caller paints the themed
     * backdrop first). Renders a carved wooden tray with a gold rim, the pits and
     * stores on top, and the winner banner when the game is over.
     */
    public void draw(Graphics2D g2d) {
        Theme.enableAntialias(g2d);
        drawTray(g2d);

        for (Pit pit : pits) {
            pit.draw(g2d);
        }

        drawStoreScore(g2d, getPlayerStore(0));
        drawStoreScore(g2d, getPlayerStore(1));

        if (state.isGameOver() && !animating) {
            drawGameOverBanner(g2d);
        }
    }

    /** The wooden playing surface: a rounded tray with a gold rim and inner bevel. */
    private void drawTray(Graphics2D g2d) {
        int x = 60;
        int y = 165;
        int w = 1080;
        int h = 420;
        int arc = 60;

        g2d.setColor(new Color(0, 0, 0, 110));
        g2d.fillRoundRect(x + 6, y + 10, w, h, arc, arc);

        g2d.setPaint(new GradientPaint(x, y, Theme.WOOD, x, y + h, Theme.WOOD_DARK));
        g2d.fillRoundRect(x, y, w, h, arc, arc);

        g2d.setStroke(new BasicStroke(6f));
        g2d.setColor(Theme.GOLD);
        g2d.drawRoundRect(x, y, w, h, arc, arc);
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.setColor(new Color(Theme.GOLD_LIGHT.getRed(), Theme.GOLD_LIGHT.getGreen(),
                Theme.GOLD_LIGHT.getBlue(), 120));
        g2d.drawRoundRect(x + 10, y + 10, w - 20, h - 20, arc - 14, arc - 14);
    }

    private void drawStoreScore(Graphics2D g2d, Pit store) {
        String score = String.valueOf(store.getStoneCount());
        g2d.setFont(Theme.display(34));
        int cx = store.getX() + store.getWidth() / 2;
        int y = store.getY() + store.getHeight() - 24;
        g2d.setColor(new Color(0, 0, 0, 130));
        Theme.drawCentered(g2d, score, cx + 2, y + 2);
        g2d.setColor(Theme.GOLD_LIGHT);
        Theme.drawCentered(g2d, score, cx, y);
    }

    private void drawGameOverBanner(Graphics2D g2d) {
        g2d.setColor(new Color(20, 13, 9, 190));
        g2d.fillRect(0, 0, 1200, 800);

        String winner = state.winnerText();
        g2d.setFont(Theme.display(52));
        FontMetrics fm = g2d.getFontMetrics();
        int cardW = Math.max(520, fm.stringWidth(winner) + 160);
        int cardH = 200;
        int cx = 600;
        int cardX = cx - cardW / 2;
        int cardY = 300;
        Theme.drawCard(g2d, cardX, cardY, cardW, cardH, 36, Theme.SCRIM);

        g2d.setColor(Theme.GOLD);
        g2d.setFont(Theme.heading(24));
        Theme.drawCentered(g2d, "GAME OVER", cx, cardY + 56);
        g2d.setColor(Theme.CREAM);
        g2d.setFont(Theme.display(46));
        Theme.drawCentered(g2d, winner, cx, cardY + 118);
        Theme.drawDivider(g2d, cx, cardY + 150, 150);
    }

    public Pit getPitAt(int x, int y) {
        for (Pit pit : pits) {
            if (pit.contains(x, y)) {
                return pit;
            }
        }
        return null;
    }

    public int getPitIndex(Pit pit) {
        return pits.indexOf(pit);
    }

    public boolean isGameOver() {
        return state.isGameOver();
    }

    public String getWinner() {
        return state.winnerText();
    }

    /** The visible stone count in a player's store (tracks the animation, not just the model). */
    public int getStoreScore(int player) {
        return pits.get(MancalaState.storeIndex(player)).getStoneCount();
    }

    public int getCurrentPlayer() {
        return state.currentPlayer();
    }

    /** An independent copy of the current position for the AI to search. */
    public MancalaState getStateCopy() {
        return state.copy();
    }

    public void resetGame() {
        state = new MancalaState();
        animating = false;
        initializeBoard();
    }
}
