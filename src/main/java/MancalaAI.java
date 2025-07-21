import java.util.List;

/**
 * Computer opponent for Gebeta built on the classic minimax search with
 * alpha-beta pruning.
 *
 * The AI explores the game tree up to {@code maxDepth} plies ahead, scoring each
 * leaf position from its own point of view and assuming the human plays the reply
 * that is worst for the AI. Alpha-beta pruning discards branches that cannot
 * affect the outcome, so the effective branching factor stays small even at high
 * depth.
 *
 * Gebeta's "extra turn" rule (landing your last stone in your own store lets you
 * move again) is handled naturally: {@link MancalaState#applyMove(int)} leaves the
 * current player unchanged in that case, so the search simply keeps that ply as a
 * maximizing node and the AI is rewarded for chaining free moves together.
 */
public class MancalaAI {
    private static final int WIN_BONUS = 10_000;

    private final int maxDepth;
    private final int aiPlayer;

    /**
     * @param maxDepth how many plies to look ahead — higher is stronger and slower.
     *                 Difficulty maps to depth (e.g. 1 = greedy, 5 = solid, 9 = hard).
     * @param aiPlayer which side the computer controls (0 or 1).
     */
    public MancalaAI(int maxDepth, int aiPlayer) {
        this.maxDepth = Math.max(1, maxDepth);
        this.aiPlayer = aiPlayer;
    }

    /**
     * Picks the best legal pit for the current player of {@code state}.
     *
     * @return the chosen pit index, or -1 if there are no legal moves.
     */
    public int chooseMove(MancalaState state) {
        List<Integer> moves = state.legalMoves();
        if (moves.isEmpty()) {
            return -1;
        }

        int bestMove = moves.get(0);
        int bestValue = Integer.MIN_VALUE;
        int alpha = Integer.MIN_VALUE;
        int beta = Integer.MAX_VALUE;

        for (int move : moves) {
            MancalaState child = state.copy();
            child.applyMove(move);
            int value = minimax(child, maxDepth - 1, alpha, beta);
            if (value > bestValue) {
                bestValue = value;
                bestMove = move;
            }
            alpha = Math.max(alpha, bestValue);
        }
        return bestMove;
    }

    private int minimax(MancalaState state, int depth, int alpha, int beta) {
        if (state.isGameOver() || depth == 0) {
            return evaluate(state);
        }

        List<Integer> moves = state.legalMoves();
        if (moves.isEmpty()) {
            return evaluate(state);
        }

        boolean maximizing = (state.currentPlayer() == aiPlayer);
        if (maximizing) {
            int best = Integer.MIN_VALUE;
            for (int move : moves) {
                MancalaState child = state.copy();
                child.applyMove(move);
                best = Math.max(best, minimax(child, depth - 1, alpha, beta));
                alpha = Math.max(alpha, best);
                if (beta <= alpha) break; // opponent already has a better option elsewhere
            }
            return best;
        } else {
            int best = Integer.MAX_VALUE;
            for (int move : moves) {
                MancalaState child = state.copy();
                child.applyMove(move);
                best = Math.min(best, minimax(child, depth - 1, alpha, beta));
                beta = Math.min(beta, best);
                if (beta <= alpha) break;
            }
            return best;
        }
    }

    /**
     * Scores a position from the AI's perspective. The store difference dominates;
     * a small bonus for stones still sitting on the AI's own side rewards keeping
     * material in reach, and terminal wins/losses are weighted heavily so the
     * search always prefers a guaranteed win over marginal store gains.
     */
    private int evaluate(MancalaState state) {
        int opponent = 1 - aiPlayer;
        int myScore = state.scoreOf(aiPlayer);
        int oppScore = state.scoreOf(opponent);
        int value = (myScore - oppScore) * 100;

        if (state.isGameOver()) {
            if (myScore > oppScore) value += WIN_BONUS;
            else if (oppScore > myScore) value -= WIN_BONUS;
            return value;
        }

        int mySide = 0;
        int oppSide = 0;
        for (int i = 0; i < MancalaState.SIZE; i++) {
            if (MancalaState.isStore(i)) continue;
            if (MancalaState.ownerOf(i) == aiPlayer) {
                mySide += state.stones(i);
            } else {
                oppSide += state.stones(i);
            }
        }
        value += (mySide - oppSide);
        return value;
    }
}
