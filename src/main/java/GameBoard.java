import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Bridges the pure {@link MancalaState} rules engine to the on-screen board, drawn in
 * the board-mockup style: a warm wooden tray with deep stores at each end, light carved
 * pits between them, count pills, a glow on playable pits and a "free turn" hint.
 *
 * All rules live in {@link MancalaState}; this class keeps the visual {@link Pit}/{@link
 * Stone} objects in sync and renders them.
 */
public class GameBoard {
    private List<Pit> pits;
    private MancalaState state;
    private boolean animating;
    private boolean inputActive;
    private int lastIndex = -1;
    private String p0Label = "You";
    private String p1Label = "Computer";

    public GameBoard() {
        state = new MancalaState();
        pits = new ArrayList<>();
        initializeBoard();
    }

    private void initializeBoard() {
        pits.clear();

        // Player 0's six pits (bottom row), then Player 0's store on the right.
        for (int i = 0; i < 6; i++) {
            pits.add(new Pit(270 + i * 112, 405, 100, 100, false, 0));
        }
        pits.add(new Pit(950, 225, 110, 310, true, 0));

        // Player 1's six pits (top row, right-to-left), then Player 1's store on the left.
        for (int i = 5; i >= 0; i--) {
            pits.add(new Pit(270 + i * 112, 245, 100, 100, false, 1));
        }
        pits.add(new Pit(140, 225, 110, 310, true, 1));

        syncVisuals();
    }

    /** Keeps the on-screen stones matching the model, preserving colours where possible. */
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

    /** Applies a move via the rules engine and returns a trace for the caller to animate. */
    public MoveTrace makeMove(int pitIndex) {
        return state.applyMoveTraced(pitIndex);
    }

    // ---- step-wise visual operations, driven by the animator ----

    public void visualPickUp(int index) {
        pits.get(index).removeAllStones();
        lastIndex = index;
    }

    public void visualDrop(int index) {
        pits.get(index).addStone(new Stone(0, 0));
        lastIndex = index;
    }

    public void visualCapture(int landingPit, int oppositePit, int store, int count) {
        pits.get(landingPit).removeAllStones();
        pits.get(oppositePit).removeAllStones();
        for (int i = 0; i < count; i++) {
            pits.get(store).addStone(new Stone(0, 0));
        }
        lastIndex = store;
    }

    public void setAnimating(boolean animating) {
        this.animating = animating;
    }

    /** When true, the current player's non-empty pits glow as legal moves. */
    public void setInputActive(boolean inputActive) {
        this.inputActive = inputActive;
    }

    public void setPlayerLabels(String p0, String p1) {
        this.p0Label = p0;
        this.p1Label = p1;
    }

    // ---- drawing ----

    public void draw(Graphics2D g2d) {
        Theme.enableAntialias(g2d);
        drawTray(g2d);

        boolean canInput = inputActive && !state.isGameOver() && !animating;
        for (int i = 0; i < MancalaState.SIZE; i++) {
            boolean playable = canInput && !MancalaState.isStore(i)
                    && MancalaState.ownerOf(i) == state.currentPlayer() && state.stones(i) > 0;
            boolean last = (i == lastIndex);
            boolean hint = playable && state.landsInOwnStore(i);
            pits.get(i).draw(g2d, playable, last, hint);
        }

        // Faint sowing-direction cue between the rows.
        g2d.setColor(new Color(169, 113, 61, 120));
        g2d.setFont(Theme.body(12));
        Theme.drawCentered(g2d, "↺   SOWING DIRECTION   ↻", 600, 379);

        drawStoreOverlay(g2d, pits.get(MancalaState.storeIndex(1)), p1Label);
        drawStoreOverlay(g2d, pits.get(MancalaState.storeIndex(0)), p0Label);

        if (state.isGameOver() && !animating) {
            drawGameOverBanner(g2d);
        }
    }

    private void drawTray(Graphics2D g2d) {
        int x = 110, y = 195, w = 980, h = 350, arc = 46;

        g2d.setColor(new Color(0, 0, 0, 120));
        g2d.fillRoundRect(x + 6, y + 12, w, h, arc, arc);

        g2d.setPaint(new LinearGradientPaint(0, y, 0, y + h,
                new float[]{0f, 0.55f, 1f},
                new Color[]{Theme.WOOD, Theme.WOOD_MID, Theme.WOOD_DARK}));
        g2d.fillRoundRect(x, y, w, h, arc, arc);

        // Top bevel highlight and outer border.
        g2d.setStroke(new BasicStroke(2f));
        g2d.setColor(new Color(169, 113, 61, 110));
        g2d.drawRoundRect(x + 4, y + 4, w - 8, h - 8, arc - 8, arc - 8);
        g2d.setColor(Theme.BOARD_BORDER);
        g2d.drawRoundRect(x, y, w, h, arc, arc);
    }

    private void drawStoreOverlay(Graphics2D g2d, Pit store, String label) {
        int cx = store.getX() + store.getWidth() / 2;

        g2d.setFont(Theme.heading(12));
        g2d.setColor(new Color(201, 159, 102));
        Theme.drawCentered(g2d, label.toUpperCase(), cx, store.getY() + 24);

        String count = String.valueOf(store.getStoneCount());
        g2d.setFont(Theme.display(30));
        int by = store.getY() + store.getHeight() - 18;
        g2d.setColor(new Color(0, 0, 0, 140));
        Theme.drawCentered(g2d, count, cx + 1, by + 1);
        g2d.setColor(Theme.GOLD);
        Theme.drawCentered(g2d, count, cx, by);
    }

    private void drawGameOverBanner(Graphics2D g2d) {
        g2d.setColor(new Color(20, 13, 9, 190));
        g2d.fillRect(0, 0, 1200, 800);

        String winner = state.winnerText();
        g2d.setFont(Theme.display(48));
        FontMetrics fm = g2d.getFontMetrics();
        int cardW = Math.max(520, fm.stringWidth(winner) + 160);
        int cardH = 200;
        int cx = 600;
        int cardX = cx - cardW / 2;
        int cardY = 300;
        Theme.drawCard(g2d, cardX, cardY, cardW, cardH, 32, Theme.COFFEE);

        g2d.setColor(Theme.GOLD);
        g2d.setFont(Theme.heading(24));
        Theme.drawCentered(g2d, "GAME OVER", cx, cardY + 56);
        g2d.setColor(Theme.CREAM);
        g2d.setFont(Theme.display(46));
        Theme.drawCentered(g2d, winner, cx, cardY + 118);
        Theme.drawDivider(g2d, cx, cardY + 150, 150);
    }

    // ---- queries ----

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

    public MancalaState getStateCopy() {
        return state.copy();
    }

    /** Replaces the current position with a validated persisted snapshot. */
    public void restoreGame(int[] board, int currentPlayer) {
        state = MancalaState.fromBoard(board, currentPlayer);
        animating = false;
        lastIndex = -1;
        initializeBoard();
    }

    public void resetGame() {
        state = new MancalaState();
        animating = false;
        lastIndex = -1;
        initializeBoard();
    }
}
