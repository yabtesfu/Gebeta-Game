import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class MancalaAITest {
    @Test
    void iterativeSearchReturnsALegalDeterministicMove() {
        MancalaState state = new MancalaState();
        MancalaAI first = new MancalaAI(6, 0, 5_000);
        MancalaAI second = new MancalaAI(6, 0, 5_000);
        int move = first.chooseMove(state);

        assertTrue(state.isLegalMove(move));
        assertEquals(move, second.chooseMove(state));
        assertEquals(6, first.lastSearchStats().completedDepth);
        assertTrue(first.lastSearchStats().nodes > 0);
        assertTrue(first.lastSearchStats().cutoffs > 0);
    }

    @Test
    void transpositionTableIsUsedDuringADeepSearch() {
        MancalaAI ai = new MancalaAI(8, 0, 5_000);
        ai.chooseMove(new MancalaState());
        assertTrue(ai.lastSearchStats().cacheHits > 0);
    }

    @Test
    void deadlineKeepsTheLastCompletedLegalMove() {
        MancalaState state = new MancalaState();
        MancalaAI ai = new MancalaAI(40, 0, 20);
        int move = ai.chooseMove(state);

        assertTrue(state.isLegalMove(move));
        assertTrue(ai.lastSearchStats().timedOut);
        assertTrue(ai.lastSearchStats().completedDepth >= 1);
        assertTrue(ai.lastSearchStats().elapsedMillis < 1_000);
    }
}
