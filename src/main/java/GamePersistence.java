import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.prefs.Preferences;

/**
 * Stores the one resumable game and cumulative results using Java Preferences.
 * The format is deliberately small and versioned so corrupt or future data can be
 * rejected without preventing the game from starting.
 */
public final class GamePersistence {
    private static final int FORMAT_VERSION = 1;
    private static final String SAVE_PRESENT = "save.present";
    private static final String SAVE_VERSION = "save.version";
    private static final String SAVE_BOARD = "save.board";
    private static final String SAVE_PLAYER = "save.player";
    private static final String SAVE_VS_COMPUTER = "save.vsComputer";
    private static final String SAVE_AI_DEPTH = "save.aiDepth";

    private static final String AI_WINS = "stats.ai.humanWins";
    private static final String AI_LOSSES = "stats.ai.computerWins";
    private static final String AI_TIES = "stats.ai.ties";
    private static final String LOCAL_P0_WINS = "stats.local.p0Wins";
    private static final String LOCAL_P1_WINS = "stats.local.p1Wins";
    private static final String LOCAL_TIES = "stats.local.ties";

    interface Store {
        String get(String key, String fallback);
        void put(String key, String value);
        int getInt(String key, int fallback);
        void putInt(String key, int value);
        boolean getBoolean(String key, boolean fallback);
        void putBoolean(String key, boolean value);
        void remove(String key);
    }

    private static final class PreferencesStore implements Store {
        private final Preferences preferences;

        PreferencesStore(Preferences preferences) {
            this.preferences = preferences;
        }

        public String get(String key, String fallback) { return preferences.get(key, fallback); }
        public void put(String key, String value) { preferences.put(key, value); }
        public int getInt(String key, int fallback) { return preferences.getInt(key, fallback); }
        public void putInt(String key, int value) { preferences.putInt(key, value); }
        public boolean getBoolean(String key, boolean fallback) {
            return preferences.getBoolean(key, fallback);
        }
        public void putBoolean(String key, boolean value) { preferences.putBoolean(key, value); }
        public void remove(String key) { preferences.remove(key); }
    }

    private static final class MemoryStore implements Store {
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

    public static final class SavedGame {
        private final int[] board;
        private final int currentPlayer;
        private final boolean vsComputer;
        private final int aiDepth;

        private SavedGame(int[] board, int currentPlayer, boolean vsComputer, int aiDepth) {
            this.board = board.clone();
            this.currentPlayer = currentPlayer;
            this.vsComputer = vsComputer;
            this.aiDepth = aiDepth;
        }

        public int[] board() { return board.clone(); }
        public int currentPlayer() { return currentPlayer; }
        public boolean vsComputer() { return vsComputer; }
        public int aiDepth() { return aiDepth; }
    }

    public static final class Stats {
        public final int humanWins;
        public final int computerWins;
        public final int computerTies;
        public final int player1Wins;
        public final int player2Wins;
        public final int localTies;

        private Stats(int humanWins, int computerWins, int computerTies,
                      int player1Wins, int player2Wins, int localTies) {
            this.humanWins = humanWins;
            this.computerWins = computerWins;
            this.computerTies = computerTies;
            this.player1Wins = player1Wins;
            this.player2Wins = player2Wins;
            this.localTies = localTies;
        }
    }

    private final Store store;

    public GamePersistence() {
        this(new PreferencesStore(Preferences.userNodeForPackage(GamePersistence.class)));
    }

    GamePersistence(Store store) {
        this.store = store;
    }

    static GamePersistence transientPersistence() {
        return new GamePersistence(new MemoryStore());
    }

    public void save(MancalaState state, boolean vsComputer, int aiDepth) {
        if (state.isGameOver()) {
            clearSavedGame();
            return;
        }
        int[] board = state.toBoard();
        StringBuilder encoded = new StringBuilder();
        for (int i = 0; i < board.length; i++) {
            if (i > 0) encoded.append(',');
            encoded.append(board[i]);
        }

        // Mark valid last so a partially written record is never offered for resume.
        store.putBoolean(SAVE_PRESENT, false);
        store.putInt(SAVE_VERSION, FORMAT_VERSION);
        store.put(SAVE_BOARD, encoded.toString());
        store.putInt(SAVE_PLAYER, state.currentPlayer());
        store.putBoolean(SAVE_VS_COMPUTER, vsComputer);
        store.putInt(SAVE_AI_DEPTH, vsComputer ? Math.max(1, aiDepth) : 0);
        store.putBoolean(SAVE_PRESENT, true);
    }

    public Optional<SavedGame> load() {
        if (!store.getBoolean(SAVE_PRESENT, false)
                || store.getInt(SAVE_VERSION, -1) != FORMAT_VERSION) {
            return Optional.empty();
        }
        try {
            String[] parts = store.get(SAVE_BOARD, "").split(",", -1);
            if (parts.length != MancalaState.SIZE) throw new IllegalArgumentException("bad board size");
            int[] board = new int[MancalaState.SIZE];
            int total = 0;
            for (int i = 0; i < board.length; i++) {
                board[i] = Integer.parseInt(parts[i]);
                if (board[i] < 0) throw new IllegalArgumentException("negative stones");
                total += board[i];
            }
            if (total != 48) throw new IllegalArgumentException("bad stone total");
            int player = store.getInt(SAVE_PLAYER, -1);
            if (player != 0 && player != 1) throw new IllegalArgumentException("bad player");
            boolean vsComputer = store.getBoolean(SAVE_VS_COMPUTER, false);
            int depth = store.getInt(SAVE_AI_DEPTH, 0);
            if (vsComputer && depth < 1) throw new IllegalArgumentException("bad AI depth");
            return Optional.of(new SavedGame(board, player, vsComputer, depth));
        } catch (RuntimeException invalidSave) {
            clearSavedGame();
            return Optional.empty();
        }
    }

    public boolean hasSavedGame() {
        return load().isPresent();
    }

    public void clearSavedGame() {
        store.putBoolean(SAVE_PRESENT, false);
        store.remove(SAVE_BOARD);
        store.remove(SAVE_PLAYER);
        store.remove(SAVE_VS_COMPUTER);
        store.remove(SAVE_AI_DEPTH);
    }

    public void recordResult(boolean vsComputer, int player0Score, int player1Score) {
        if (vsComputer) {
            if (player0Score > player1Score) increment(AI_WINS);
            else if (player1Score > player0Score) increment(AI_LOSSES);
            else increment(AI_TIES);
        } else {
            if (player0Score > player1Score) increment(LOCAL_P0_WINS);
            else if (player1Score > player0Score) increment(LOCAL_P1_WINS);
            else increment(LOCAL_TIES);
        }
    }

    public Stats stats() {
        return new Stats(
                nonNegative(AI_WINS), nonNegative(AI_LOSSES), nonNegative(AI_TIES),
                nonNegative(LOCAL_P0_WINS), nonNegative(LOCAL_P1_WINS), nonNegative(LOCAL_TIES));
    }

    private int nonNegative(String key) {
        return Math.max(0, store.getInt(key, 0));
    }

    private void increment(String key) {
        store.putInt(key, nonNegative(key) + 1);
    }
}
