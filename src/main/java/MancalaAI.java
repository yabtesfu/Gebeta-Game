import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Time-bounded iterative-deepening AI with alpha-beta pruning, move ordering and
 * a transposition table. A deadline never discards the best move from the last
 * fully completed iteration, making search both stronger and predictable for UI use.
 */
public class MancalaAI {
    private static final int WIN_BONUS = 10_000;
    private static final int MAX_TABLE_ENTRIES = 250_000;

    private enum Bound { EXACT, LOWER, UPPER }

    private static final class PositionKey {
        private final int[] board;
        private final int player;
        private final int hash;

        PositionKey(MancalaState state) {
            board = state.toBoard();
            player = state.currentPlayer();
            hash = 31 * Arrays.hashCode(board) + player;
        }

        @Override public int hashCode() { return hash; }

        @Override
        public boolean equals(Object other) {
            if (this == other) return true;
            if (!(other instanceof PositionKey)) return false;
            PositionKey key = (PositionKey) other;
            return player == key.player && Arrays.equals(board, key.board);
        }
    }

    private static final class TableEntry {
        final int depth;
        final int value;
        final Bound bound;
        final int bestMove;

        TableEntry(int depth, int value, Bound bound, int bestMove) {
            this.depth = depth;
            this.value = value;
            this.bound = bound;
            this.bestMove = bestMove;
        }
    }

    private static final class OrderedMove {
        final int pit;
        final MancalaState child;
        final int priority;

        OrderedMove(int pit, MancalaState child, int priority) {
            this.pit = pit;
            this.child = child;
            this.priority = priority;
        }
    }

    private static final class SearchTimeout extends RuntimeException {
        static final SearchTimeout INSTANCE = new SearchTimeout();
        private SearchTimeout() { super(null, null, false, false); }
    }

    public static final class SearchStats {
        public final int completedDepth;
        public final long nodes;
        public final long cacheHits;
        public final long cutoffs;
        public final long elapsedMillis;
        public final boolean timedOut;

        SearchStats(int completedDepth, long nodes, long cacheHits, long cutoffs,
                    long elapsedMillis, boolean timedOut) {
            this.completedDepth = completedDepth;
            this.nodes = nodes;
            this.cacheHits = cacheHits;
            this.cutoffs = cutoffs;
            this.elapsedMillis = elapsedMillis;
            this.timedOut = timedOut;
        }
    }

    private final int maxDepth;
    private final int aiPlayer;
    private final long timeBudgetMillis;
    private final Map<PositionKey, TableEntry> table = new HashMap<>();

    private long deadlineNanos;
    private long nodes;
    private long cacheHits;
    private long cutoffs;
    private SearchStats lastStats = new SearchStats(0, 0, 0, 0, 0, false);

    public MancalaAI(int maxDepth, int aiPlayer) {
        this(maxDepth, aiPlayer, defaultTimeBudget(maxDepth));
    }

    public MancalaAI(int maxDepth, int aiPlayer, long timeBudgetMillis) {
        this.maxDepth = Math.max(1, maxDepth);
        this.aiPlayer = aiPlayer;
        this.timeBudgetMillis = Math.max(1, timeBudgetMillis);
    }

    private static long defaultTimeBudget(int depth) {
        if (depth <= 2) return 120;
        if (depth <= 6) return 700;
        return 1_800;
    }

    /** Returns a legal move, or -1 when none exists. Safe to call on a worker thread. */
    public int chooseMove(MancalaState state) {
        List<Integer> legal = state.legalMoves();
        if (legal.isEmpty()) {
            lastStats = new SearchStats(0, 0, 0, 0, 0, false);
            return -1;
        }

        long started = System.nanoTime();
        deadlineNanos = started + timeBudgetMillis * 1_000_000L;
        nodes = 0;
        cacheHits = 0;
        cutoffs = 0;
        table.clear();

        int bestMove = legal.get(0); // guaranteed fallback even under a tiny deadline
        int completedDepth = 0;
        boolean timedOut = false;
        for (int depth = 1; depth <= maxDepth; depth++) {
            try {
                bestMove = searchRoot(state, depth, bestMove);
                completedDepth = depth;
            } catch (SearchTimeout timeout) {
                timedOut = true;
                break;
            }
        }

        long elapsed = (System.nanoTime() - started) / 1_000_000L;
        lastStats = new SearchStats(completedDepth, nodes, cacheHits, cutoffs, elapsed, timedOut);
        return bestMove;
    }

    public SearchStats lastSearchStats() {
        return lastStats;
    }

    private int searchRoot(MancalaState state, int depth, int preferredMove) {
        checkDeadline();
        List<OrderedMove> moves = orderedMoves(state, preferredMove);
        boolean maximizing = state.currentPlayer() == aiPlayer;
        int bestMove = moves.get(0).pit;
        int bestValue = maximizing ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        int alpha = Integer.MIN_VALUE;
        int beta = Integer.MAX_VALUE;

        for (OrderedMove move : moves) {
            int value = minimax(move.child, depth - 1, alpha, beta);
            if ((maximizing && value > bestValue) || (!maximizing && value < bestValue)) {
                bestValue = value;
                bestMove = move.pit;
            }
            if (maximizing) alpha = Math.max(alpha, bestValue);
            else beta = Math.min(beta, bestValue);
        }

        putEntry(new PositionKey(state), new TableEntry(depth, bestValue, Bound.EXACT, bestMove));
        return bestMove;
    }

    private int minimax(MancalaState state, int depth, int alpha, int beta) {
        checkDeadline();
        nodes++;
        if (state.isGameOver() || depth == 0) return evaluate(state);

        PositionKey key = new PositionKey(state);
        TableEntry cached = table.get(key);
        int originalAlpha = alpha;
        int originalBeta = beta;
        int preferredMove = -1;
        if (cached != null) {
            preferredMove = cached.bestMove;
            if (cached.depth >= depth) {
                cacheHits++;
                if (cached.bound == Bound.EXACT) return cached.value;
                if (cached.bound == Bound.LOWER) alpha = Math.max(alpha, cached.value);
                else beta = Math.min(beta, cached.value);
                if (alpha >= beta) return cached.value;
            }
        }

        List<OrderedMove> moves = orderedMoves(state, preferredMove);
        if (moves.isEmpty()) return evaluate(state);
        boolean maximizing = state.currentPlayer() == aiPlayer;
        int best = maximizing ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        int bestMove = moves.get(0).pit;

        for (OrderedMove move : moves) {
            int value = minimax(move.child, depth - 1, alpha, beta);
            if ((maximizing && value > best) || (!maximizing && value < best)) {
                best = value;
                bestMove = move.pit;
            }
            if (maximizing) alpha = Math.max(alpha, best);
            else beta = Math.min(beta, best);
            if (alpha >= beta) {
                cutoffs++;
                break;
            }
        }

        Bound bound = best <= originalAlpha ? Bound.UPPER
                : best >= originalBeta ? Bound.LOWER : Bound.EXACT;
        putEntry(key, new TableEntry(depth, best, bound, bestMove));
        return best;
    }

    private List<OrderedMove> orderedMoves(MancalaState state, int preferredMove) {
        List<OrderedMove> ordered = new ArrayList<>();
        int player = state.currentPlayer();
        int scoreBefore = state.scoreOf(player);
        for (int move : state.legalMoves()) {
            MancalaState child = state.copy();
            MoveTrace trace = child.applyMoveTraced(move);
            int priority = (child.scoreOf(player) - scoreBefore) * 1_000;
            if (trace.extraTurn) priority += 600;
            if (trace.captured) priority += trace.capturedTotal * 120;
            if (trace.gameOver) priority += 2_000;
            if (move == preferredMove) priority += 1_000_000;
            // Stable pit preference makes equal searches deterministic.
            priority += player == 0 ? move : (12 - move);
            ordered.add(new OrderedMove(move, child, priority));
        }
        ordered.sort(Comparator.comparingInt((OrderedMove move) -> move.priority).reversed());
        return ordered;
    }

    private void putEntry(PositionKey key, TableEntry entry) {
        TableEntry existing = table.get(key);
        if (existing != null && existing.depth > entry.depth) return;
        if (existing != null || table.size() < MAX_TABLE_ENTRIES) table.put(key, entry);
    }

    private void checkDeadline() {
        if (Thread.currentThread().isInterrupted() || System.nanoTime() >= deadlineNanos) {
            throw SearchTimeout.INSTANCE;
        }
    }

    private int evaluate(MancalaState state) {
        int opponent = 1 - aiPlayer;
        int myScore = state.scoreOf(aiPlayer);
        int opponentScore = state.scoreOf(opponent);
        int value = (myScore - opponentScore) * 100;
        if (state.isGameOver()) {
            if (myScore > opponentScore) value += WIN_BONUS;
            else if (opponentScore > myScore) value -= WIN_BONUS;
            return value;
        }

        int mySide = 0;
        int opponentSide = 0;
        for (int i = 0; i < MancalaState.SIZE; i++) {
            if (MancalaState.isStore(i)) continue;
            if (MancalaState.ownerOf(i) == aiPlayer) mySide += state.stones(i);
            else opponentSide += state.stones(i);
        }
        return value + mySide - opponentSide;
    }
}
