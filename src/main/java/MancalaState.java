import java.util.ArrayList;
import java.util.List;

/**
 * Pure, self-contained representation of a Gebeta (Kalah-style Mancala) position.
 *
 * This class holds ONLY the rules of the game as an array of stone counts. It has
 * no dependency on Swing or any rendering code, which is what makes it possible to
 * unit-test the rules in isolation and to let the AI simulate thousands of
 * hypothetical positions via {@link #copy()} without touching anything on screen.
 *
 * Board layout (14 slots, sown counter-clockwise):
 * <pre>
 *   indices 0..5   -> Player 0's six pits
 *   index   6      -> Player 0's store
 *   indices 7..12  -> Player 1's six pits
 *   index   13     -> Player 1's store
 * </pre>
 */
public class MancalaState {
    public static final int SIZE = 14;
    public static final int P0_STORE = 6;
    public static final int P1_STORE = 13;
    public static final int STONES_PER_PIT = 4;

    private final int[] board;
    private int currentPlayer; // 0 or 1
    private boolean gameOver;

    /** Creates a fresh game with four stones in each pit and Player 0 to move. */
    public MancalaState() {
        board = new int[SIZE];
        for (int i = 0; i < SIZE; i++) {
            if (!isStore(i)) {
                board[i] = STONES_PER_PIT;
            }
        }
        currentPlayer = 0;
        gameOver = false;
    }

    private MancalaState(int[] board, int currentPlayer, boolean gameOver) {
        this.board = board.clone();
        this.currentPlayer = currentPlayer;
        this.gameOver = gameOver;
    }

    /** Returns an independent deep copy — the unit the AI mutates while searching. */
    public MancalaState copy() {
        return new MancalaState(board, currentPlayer, gameOver);
    }

    /**
     * Builds a position from an explicit 14-slot board — handy for tests, puzzles,
     * and loading a saved game.
     *
     * @param board         stone counts for slots 0..13 (see class layout)
     * @param currentPlayer the side to move (0 or 1)
     */
    public static MancalaState fromBoard(int[] board, int currentPlayer) {
        if (board.length != SIZE) {
            throw new IllegalArgumentException("board must have exactly " + SIZE + " slots");
        }
        return new MancalaState(board, currentPlayer, false);
    }

    // ---- static board geometry helpers ----

    public static boolean isStore(int index) {
        return index == P0_STORE || index == P1_STORE;
    }

    /** Which player owns the given slot (pit or store). */
    public static int ownerOf(int index) {
        return (index <= P0_STORE) ? 0 : 1;
    }

    public static int storeIndex(int player) {
        return (player == 0) ? P0_STORE : P1_STORE;
    }

    /** The pit directly across the board, used for captures. */
    public static int oppositeOf(int index) {
        return 12 - index;
    }

    // ---- queries ----

    public int stones(int index) {
        return board[index];
    }

    public int currentPlayer() {
        return currentPlayer;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public int scoreOf(int player) {
        return board[storeIndex(player)];
    }

    public boolean isLegalMove(int pit) {
        if (gameOver) return false;
        if (pit < 0 || pit >= SIZE) return false;
        if (isStore(pit)) return false;
        if (ownerOf(pit) != currentPlayer) return false;
        return board[pit] > 0;
    }

    /** All pit indices the current player may legally sow from. */
    public List<Integer> legalMoves() {
        List<Integer> moves = new ArrayList<>();
        for (int i = 0; i < SIZE; i++) {
            if (isLegalMove(i)) {
                moves.add(i);
            }
        }
        return moves;
    }

    /** Human-readable result once the game is over, or {@code null} while in play. */
    public String winnerText() {
        if (!gameOver) return null;
        int s0 = scoreOf(0);
        int s1 = scoreOf(1);
        if (s0 > s1) return "Player 1 Wins!";
        if (s1 > s0) return "Player 2 Wins!";
        return "It's a Tie!";
    }

    // ---- the single rule that mutates state ----

    /**
     * Sows the stones from {@code pit} for the current player, applying captures,
     * extra turns and end-of-game collection exactly once.
     *
     * @return {@code true} if the move was legal and applied, {@code false} otherwise
     *         (the state is left untouched on an illegal move).
     */
    public boolean applyMove(int pit) {
        return applyMoveTraced(pit) != null;
    }

    /**
     * Identical to {@link #applyMove(int)} but returns a {@link MoveTrace} describing
     * what happened, so the UI can animate the move. This is the single, authoritative
     * implementation of the sowing rules.
     *
     * @return the trace, or {@code null} if the move was illegal (state untouched).
     */
    public MoveTrace applyMoveTraced(int pit) {
        if (!isLegalMove(pit)) {
            return null;
        }

        MoveTrace trace = new MoveTrace();
        trace.source = pit;

        int opponentStore = (currentPlayer == 0) ? P1_STORE : P0_STORE;
        int count = board[pit];
        board[pit] = 0;

        // Sow counter-clockwise, skipping the opponent's store.
        int index = pit;
        int[] drops = new int[count];
        for (int i = 0; i < count; i++) {
            index = (index + 1) % SIZE;
            if (index == opponentStore) {
                index = (index + 1) % SIZE;
            }
            board[index]++;
            drops[i] = index;
        }
        trace.drops = drops;
        int last = index;

        // Capture: last stone lands in one of the current player's own, previously
        // empty pits -> grab it plus everything in the pit directly opposite.
        if (!isStore(last) && ownerOf(last) == currentPlayer && board[last] == 1) {
            int opposite = oppositeOf(last);
            if (board[opposite] > 0) {
                int captured = board[opposite] + board[last];
                board[opposite] = 0;
                board[last] = 0;
                board[storeIndex(currentPlayer)] += captured;

                trace.captured = true;
                trace.captureLandingPit = last;
                trace.captureOppositePit = opposite;
                trace.captureStore = storeIndex(currentPlayer);
                trace.capturedTotal = captured;
            }
        }

        boolean extraTurn = (last == storeIndex(currentPlayer));
        trace.extraTurn = extraTurn;

        checkGameOver();
        if (!gameOver && !extraTurn) {
            currentPlayer = 1 - currentPlayer;
        }
        trace.gameOver = gameOver;
        return trace;
    }

    /**
     * Ends the game once either side has emptied all of its pits, sweeping each
     * player's remaining stones into their own store.
     */
    private void checkGameOver() {
        boolean p0HasStones = false;
        boolean p1HasStones = false;
        for (int i = 0; i <= 5; i++) {
            if (board[i] > 0) p0HasStones = true;
        }
        for (int i = 7; i <= 12; i++) {
            if (board[i] > 0) p1HasStones = true;
        }

        if (!p0HasStones || !p1HasStones) {
            gameOver = true;
            for (int i = 0; i <= 5; i++) {
                board[P0_STORE] += board[i];
                board[i] = 0;
            }
            for (int i = 7; i <= 12; i++) {
                board[P1_STORE] += board[i];
                board[i] = 0;
            }
        }
    }
}
