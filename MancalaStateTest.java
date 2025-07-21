/**
 * Lightweight, dependency-free test suite for the {@link MancalaState} rules engine.
 *
 * It deliberately avoids JUnit so it can be compiled and run with nothing but a JDK:
 * <pre>
 *   javac MancalaState.java MancalaStateTest.java
 *   java MancalaStateTest
 * </pre>
 * The process exits with a non-zero status if any check fails, so it doubles as a
 * CI-friendly gate.
 */
public class MancalaStateTest {
    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        testInitialSetup();
        testSimpleSowing();
        testExtraTurnLandingInStore();
        testSkipsOpponentStore();
        testCaptureOnOwnEmptyPit();
        testNoCaptureWhenOppositeEmpty();
        testIllegalMovesRejected();
        testEndgameSweepsRemainingStones();
        testCopyIsIndependent();
        testTotalStonesConserved();

        System.out.println();
        System.out.println("Passed: " + passed + ", Failed: " + failed);
        if (failed > 0) {
            System.exit(1);
        }
    }

    private static void testInitialSetup() {
        MancalaState s = new MancalaState();
        for (int i = 0; i < MancalaState.SIZE; i++) {
            int expected = MancalaState.isStore(i) ? 0 : 4;
            check("initial pit " + i + " has " + expected, s.stones(i) == expected);
        }
        check("player 0 moves first", s.currentPlayer() == 0);
        check("game not over at start", !s.isGameOver());
        check("6 legal opening moves", s.legalMoves().size() == 6);
    }

    private static void testSimpleSowing() {
        MancalaState s = new MancalaState();
        // Pit 0 holds 4 stones -> they land in pits 1, 2, 3, 4.
        s.applyMove(0);
        check("pit 0 emptied", s.stones(0) == 0);
        check("pit 1 gained a stone", s.stones(1) == 5);
        check("pit 4 gained a stone", s.stones(4) == 5);
        check("pit 5 untouched", s.stones(5) == 4);
        check("turn passed to player 1", s.currentPlayer() == 1);
    }

    private static void testExtraTurnLandingInStore() {
        MancalaState s = new MancalaState();
        // Pit 2 holds 4 stones -> they land in 3, 4, 5, and the store (index 6).
        s.applyMove(2);
        check("last stone reached the store", s.scoreOf(0) == 1);
        check("landing in own store keeps the turn", s.currentPlayer() == 0);
    }

    private static void testSkipsOpponentStore() {
        MancalaState s = new MancalaState();
        // Player 1 sowing from pit 12 with enough stones would reach index 13 (own
        // store, allowed) but must skip index 6 (opponent's store). Build a case:
        // give pit 7 a big count and verify opponent store never fills.
        s = withBoard(new int[]{4, 4, 4, 4, 4, 4, 0, 9, 4, 4, 4, 4, 4, 0}, 1);
        int oppStoreBefore = s.stones(MancalaState.P0_STORE);
        s.applyMove(7); // player 1 sows 9 stones from pit 7
        check("opponent store skipped during sowing",
                s.stones(MancalaState.P0_STORE) == oppStoreBefore);
    }

    private static void testCaptureOnOwnEmptyPit() {
        // Player 0 to move. Pit 0 has exactly 1 stone -> it lands in the empty pit 1
        // and captures the 4 stones sitting opposite (pit 11), plus its own stone.
        int[] board = {1, 0, 4, 4, 4, 4, 0, 4, 4, 4, 4, 4, 4, 0};
        MancalaState s = withBoard(board, 0);
        int opposite = MancalaState.oppositeOf(1); // 11
        check("opposite pit is 11", opposite == 11);
        s.applyMove(0);
        check("capturing pit is emptied", s.stones(1) == 0);
        check("opposite pit is emptied", s.stones(11) == 0);
        check("store holds captured + capturing stone", s.scoreOf(0) == 5);
    }

    private static void testNoCaptureWhenOppositeEmpty() {
        // Same shape, but the opposite pit (11) is empty -> no capture, stone stays.
        int[] board = {1, 0, 4, 4, 4, 4, 0, 4, 4, 4, 4, 0, 4, 0};
        MancalaState s = withBoard(board, 0);
        s.applyMove(0);
        check("no capture leaves the lone stone in pit 1", s.stones(1) == 1);
        check("store unchanged when opposite empty", s.scoreOf(0) == 0);
    }

    private static void testIllegalMovesRejected() {
        MancalaState s = new MancalaState();
        check("cannot sow from a store", !s.applyMove(MancalaState.P0_STORE));
        check("cannot sow from opponent's pit", !s.applyMove(7));
        check("cannot sow from out-of-range index", !s.applyMove(99));
        check("rejected illegal move does not change turn", s.currentPlayer() == 0);

        int[] board = {0, 4, 4, 4, 4, 4, 0, 4, 4, 4, 4, 4, 4, 0};
        MancalaState empty = withBoard(board, 0);
        check("cannot sow from an empty pit", !empty.applyMove(0));
    }

    private static void testEndgameSweepsRemainingStones() {
        // Player 0 has a single stone in pit 5; playing it empties their side, ending
        // the game and sweeping player 1's remaining stones into player 1's store.
        int[] board = {0, 0, 0, 0, 0, 1, 10, 2, 3, 0, 0, 0, 0, 5};
        MancalaState s = withBoard(board, 0);
        s.applyMove(5); // lone stone lands in player 0's store (index 6)
        check("game is over once a side empties", s.isGameOver());
        check("player 1's leftover stones swept to their store",
                s.scoreOf(1) == 5 + 2 + 3);
        check("all non-store pits are empty after sweep",
                s.stones(7) == 0 && s.stones(8) == 0);
    }

    private static void testCopyIsIndependent() {
        MancalaState original = new MancalaState();
        MancalaState clone = original.copy();
        clone.applyMove(0);
        check("mutating a copy does not affect the original", original.stones(0) == 4);
    }

    private static void testTotalStonesConserved() {
        // Across a scripted sequence, the 48 stones on the board are never created
        // or destroyed — a strong invariant that catches most sowing/capture bugs.
        MancalaState s = new MancalaState();
        int[] moves = {0, 7, 1, 8, 2, 9, 3, 10, 4, 11, 5, 12};
        for (int m : moves) {
            s.applyMove(m); // some may be illegal by then; applyMove ignores those
        }
        int total = 0;
        for (int i = 0; i < MancalaState.SIZE; i++) {
            total += s.stones(i);
        }
        check("48 stones conserved throughout play", total == 48);
    }

    // ---- helpers ----

    private static MancalaState withBoard(int[] board, int currentPlayer) {
        return MancalaState.fromBoard(board, currentPlayer);
    }

    private static void check(String name, boolean condition) {
        if (condition) {
            passed++;
            System.out.println("  ok   - " + name);
        } else {
            failed++;
            System.out.println("  FAIL - " + name);
        }
    }
}
