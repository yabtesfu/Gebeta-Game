import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class GamePersistenceTest {
    private static final class MemoryStore implements GamePersistence.Store {
        private final Map<String, String> values = new HashMap<>();

        public String get(String key, String fallback) { return values.getOrDefault(key, fallback); }
        public void put(String key, String value) { values.put(key, value); }
        public int getInt(String key, int fallback) {
            try { return Integer.parseInt(values.get(key)); }
            catch (RuntimeException missingOrInvalid) { return fallback; }
        }
        public void putInt(String key, int value) { values.put(key, String.valueOf(value)); }
        public boolean getBoolean(String key, boolean fallback) {
            String value = values.get(key);
            return value == null ? fallback : Boolean.parseBoolean(value);
        }
        public void putBoolean(String key, boolean value) { values.put(key, String.valueOf(value)); }
        public void remove(String key) { values.remove(key); }
    }

    @Test
    void savesAndLoadsAnIndependentGameSnapshot() {
        GamePersistence persistence = new GamePersistence(new MemoryStore());
        MancalaState state = new MancalaState();
        state.applyMove(0);

        persistence.save(state, true, 5);
        GamePersistence.SavedGame saved = persistence.load().orElseThrow();

        assertArrayEquals(state.toBoard(), saved.board());
        assertEquals(state.currentPlayer(), saved.currentPlayer());
        assertTrue(saved.vsComputer());
        assertEquals(5, saved.aiDepth());

        int[] mutableCopy = saved.board();
        mutableCopy[0] = 99;
        assertArrayEquals(state.toBoard(), persistence.load().orElseThrow().board());
    }

    @Test
    void clearsCompletedAndExplicitlyDiscardedGames() {
        GamePersistence persistence = new GamePersistence(new MemoryStore());
        persistence.save(new MancalaState(), false, 0);
        assertTrue(persistence.hasSavedGame());
        persistence.clearSavedGame();
        assertFalse(persistence.hasSavedGame());

        MancalaState ending = MancalaState.fromBoard(
                new int[]{0, 0, 0, 0, 0, 1, 10, 2, 3, 0, 0, 0, 0, 5}, 0);
        ending.applyMove(5);
        persistence.save(ending, false, 0);
        assertFalse(persistence.hasSavedGame());
    }

    @Test
    void rejectsCorruptSavesWithoutThrowing() {
        MemoryStore store = new MemoryStore();
        store.putBoolean("save.present", true);
        store.putInt("save.version", 1);
        store.put("save.board", "4,4,broken");
        GamePersistence persistence = new GamePersistence(store);

        assertTrue(persistence.load().isEmpty());
        assertFalse(store.getBoolean("save.present", true));
    }

    @Test
    void tracksComputerAndLocalResultsSeparately() {
        GamePersistence persistence = new GamePersistence(new MemoryStore());
        persistence.recordResult(true, 30, 18);
        persistence.recordResult(true, 20, 28);
        persistence.recordResult(true, 24, 24);
        persistence.recordResult(false, 27, 21);
        persistence.recordResult(false, 19, 29);
        persistence.recordResult(false, 24, 24);

        GamePersistence.Stats stats = persistence.stats();
        assertEquals(1, stats.humanWins);
        assertEquals(1, stats.computerWins);
        assertEquals(1, stats.computerTies);
        assertEquals(1, stats.player1Wins);
        assertEquals(1, stats.player2Wins);
        assertEquals(1, stats.localTies);
    }
}
