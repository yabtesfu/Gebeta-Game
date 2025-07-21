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

        syncPitsFromState();
    }

    /**
     * Makes each visual pit hold as many {@link Stone} objects as the rules engine
     * says it should, adding or removing stones so existing ones keep their colours.
     */
    private void syncPitsFromState() {
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

    /** Applies a move through the rules engine and refreshes the visuals. */
    public boolean makeMove(int pitIndex) {
        boolean moved = state.applyMove(pitIndex);
        if (moved) {
            syncPitsFromState();
        }
        return moved;
    }

    private Pit getPlayerStore(int player) {
        return pits.get(MancalaState.storeIndex(player));
    }

    public void draw(Graphics2D g2d) {
        g2d.setColor(new Color(245, 245, 220));
        g2d.fillRect(0, 0, 1200, 800);

        g2d.setColor(new Color(139, 69, 19));
        g2d.setStroke(new BasicStroke(5));
        g2d.drawRect(40, 180, 1120, 390);

        for (Pit pit : pits) {
            pit.draw(g2d);
        }

        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        FontMetrics fm = g2d.getFontMetrics();

        Pit player1Store = getPlayerStore(0);
        String p1Score = "Score: " + player1Store.getStoneCount();
        int p1x = player1Store.getX() + (player1Store.getWidth() - fm.stringWidth(p1Score)) / 2;
        int p1y = player1Store.getY() + player1Store.getHeight() / 2;
        g2d.drawString(p1Score, p1x, p1y);

        Pit player2Store = getPlayerStore(1);
        String p2Score = "Score: " + player2Store.getStoneCount();
        int p2x = player2Store.getX() + (player2Store.getWidth() - fm.stringWidth(p2Score)) / 2;
        int p2y = player2Store.getY() + player2Store.getHeight() / 2;
        g2d.drawString(p2Score, p2x, p2y);

        if (state.isGameOver()) {
            g2d.setColor(new Color(0, 0, 0, 150));
            g2d.fillRect(0, 0, 1200, 800);

            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 36));
            FontMetrics bigFm = g2d.getFontMetrics();
            String winner = state.winnerText();
            int textX = 600 - bigFm.stringWidth(winner) / 2;
            int textY = 400;
            g2d.drawString(winner, textX, textY);
        }
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

    public int getCurrentPlayer() {
        return state.currentPlayer();
    }

    /** An independent copy of the current position for the AI to search. */
    public MancalaState getStateCopy() {
        return state.copy();
    }

    public void resetGame() {
        state = new MancalaState();
        initializeBoard();
    }
}
