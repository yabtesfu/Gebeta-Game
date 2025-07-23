import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * JUnit 5 tests for the {@link MancalaState} rules engine.
 *
 * The rules live in a pure, Swing-free class, which is exactly what makes this
 * suite possible: every case constructs a position and asserts on the resulting
 * board without any UI involved. Run with {@code ./gradlew test}.
 */
class MancalaStateTest {

    @Test
    @DisplayName("A new game starts with four stones per pit and Player 0 to move")
    void initialSetup() {
        MancalaState s = new MancalaState();
        for (int i = 0; i < MancalaState.SIZE; i++) {
            int expected = MancalaState.isStore(i) ? 0 : 4;
            assertEquals(expected, s.stones(i), "pit " + i);
        }
        assertEquals(0, s.currentPlayer());
        assertFalse(s.isGameOver());
        assertEquals(6, s.legalMoves().size());
    }

    @Test
    @DisplayName("Sowing distributes stones one per pit and passes the turn")
    void simpleSowing() {
        MancalaState s = new MancalaState();
        s.applyMove(0); // 4 stones from pit 0 land in pits 1..4
        assertEquals(0, s.stones(0));
        assertEquals(5, s.stones(1));
        assertEquals(5, s.stones(4));
        assertEquals(4, s.stones(5));
        assertEquals(1, s.currentPlayer());
    }

    @Test
    @DisplayName("Landing the last stone in your own store grants an extra turn")
    void extraTurnLandingInStore() {
        MancalaState s = new MancalaState();
        s.applyMove(2); // 4 stones from pit 2 land in 3, 4, 5, and the store
        assertEquals(1, s.scoreOf(0));
        assertEquals(0, s.currentPlayer(), "turn should stay with player 0");
    }

    @Test
    @DisplayName("Sowing skips the opponent's store")
    void skipsOpponentStore() {
        MancalaState s = MancalaState.fromBoard(
                new int[]{4, 4, 4, 4, 4, 4, 0, 9, 4, 4, 4, 4, 4, 0}, 1);
        int oppStoreBefore = s.stones(MancalaState.P0_STORE);
        s.applyMove(7); // player 1 sows 9 stones, wrapping past the opponent's store
        assertEquals(oppStoreBefore, s.stones(MancalaState.P0_STORE));
    }

    @Test
    @DisplayName("Last stone in your own empty pit captures the opposite pit")
    void captureOnOwnEmptyPit() {
        // Pit 0 has one stone -> lands in empty pit 1, capturing the 4 opposite (pit 11).
        MancalaState s = MancalaState.fromBoard(
                new int[]{1, 0, 4, 4, 4, 4, 0, 4, 4, 4, 4, 4, 4, 0}, 0);
        assertEquals(11, MancalaState.oppositeOf(1));
        s.applyMove(0);
        assertEquals(0, s.stones(1), "capturing pit emptied");
        assertEquals(0, s.stones(11), "opposite pit emptied");
        assertEquals(5, s.scoreOf(0), "captured 4 + own 1");
    }

    @Test
    @DisplayName("No capture when the opposite pit is empty")
    void noCaptureWhenOppositeEmpty() {
        MancalaState s = MancalaState.fromBoard(
                new int[]{1, 0, 4, 4, 4, 4, 0, 4, 4, 4, 4, 0, 4, 0}, 0);
        s.applyMove(0);
        assertEquals(1, s.stones(1), "lone stone stays put");
        assertEquals(0, s.scoreOf(0));
    }

    @Test
    @DisplayName("Illegal moves are rejected and do not change the turn")
    void illegalMovesRejected() {
        MancalaState s = new MancalaState();
        assertFalse(s.applyMove(MancalaState.P0_STORE), "cannot sow a store");
        assertFalse(s.applyMove(7), "cannot sow opponent's pit");
        assertFalse(s.applyMove(99), "cannot sow out-of-range index");
        assertEquals(0, s.currentPlayer());

        MancalaState empty = MancalaState.fromBoard(
                new int[]{0, 4, 4, 4, 4, 4, 0, 4, 4, 4, 4, 4, 4, 0}, 0);
        assertFalse(empty.applyMove(0), "cannot sow an empty pit");
    }

    @Test
    @DisplayName("Emptying a side ends the game and sweeps remaining stones")
    void endgameSweepsRemainingStones() {
        MancalaState s = MancalaState.fromBoard(
                new int[]{0, 0, 0, 0, 0, 1, 10, 2, 3, 0, 0, 0, 0, 5}, 0);
        s.applyMove(5); // lone stone lands in player 0's store, emptying their side
        assertTrue(s.isGameOver());
        assertEquals(11, s.scoreOf(0), "store had 10, plus the stone just sown in");
        assertEquals(5 + 2 + 3, s.scoreOf(1), "player 1's leftovers swept home");
        assertEquals(0, s.stones(7));
        assertEquals(0, s.stones(8));
    }

    @Test
    @DisplayName("A copy is independent of the original")
    void copyIsIndependent() {
        MancalaState original = new MancalaState();
        MancalaState clone = original.copy();
        clone.applyMove(0);
        assertEquals(4, original.stones(0), "original must be untouched");
    }

    @Test
    @DisplayName("The move trace records the sown pits, extra turn, and no capture")
    void traceRecordsSowingAndExtraTurn() {
        MancalaState s = new MancalaState();
        MoveTrace t = s.applyMoveTraced(2); // 4 stones -> pits 3, 4, 5, store 6
        assertEquals(2, t.source);
        assertArrayEquals(new int[]{3, 4, 5, 6}, t.drops);
        assertTrue(t.extraTurn, "last stone in own store");
        assertFalse(t.captured);
        assertFalse(t.gameOver);
    }

    @Test
    @DisplayName("The move trace records capture details")
    void traceRecordsCapture() {
        MancalaState s = MancalaState.fromBoard(
                new int[]{1, 0, 4, 4, 4, 4, 0, 4, 4, 4, 4, 4, 4, 0}, 0);
        MoveTrace t = s.applyMoveTraced(0);
        assertArrayEquals(new int[]{1}, t.drops);
        assertTrue(t.captured);
        assertEquals(1, t.captureLandingPit);
        assertEquals(11, t.captureOppositePit);
        assertEquals(MancalaState.P0_STORE, t.captureStore);
        assertEquals(5, t.capturedTotal);
    }

    @Test
    @DisplayName("An illegal move returns a null trace")
    void traceNullForIllegalMove() {
        MancalaState s = new MancalaState();
        assertNull(s.applyMoveTraced(MancalaState.P0_STORE));
        assertNull(s.applyMoveTraced(7));
    }

    @Test
    @DisplayName("All 48 stones are conserved throughout play")
    void totalStonesConserved() {
        MancalaState s = new MancalaState();
        int[] moves = {0, 7, 1, 8, 2, 9, 3, 10, 4, 11, 5, 12};
        for (int m : moves) {
            s.applyMove(m); // applyMove ignores any that have become illegal
        }
        int total = 0;
        for (int i = 0; i < MancalaState.SIZE; i++) {
            total += s.stones(i);
        }
        assertEquals(48, total);
    }
}
