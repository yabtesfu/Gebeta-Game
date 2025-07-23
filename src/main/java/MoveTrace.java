/**
 * A record of everything a single move did, produced by
 * {@link MancalaState#applyMoveTraced(int)}.
 *
 * The rules engine stays the single source of truth: it computes the move once and
 * hands back this trace, which the UI replays as an animation (pick up the source
 * pit, drop one stone into each pit in {@link #drops} order, then play the capture).
 * Nothing here depends on Swing.
 */
public class MoveTrace {
    /** The pit the stones were lifted from. */
    public int source;

    /** The pits that received a stone, in the order they were sown. */
    public int[] drops;

    /** True if the move ended in a capture. */
    public boolean captured;
    /** The pit whose lone last stone triggered the capture. */
    public int captureLandingPit;
    /** The opposite pit that was captured from. */
    public int captureOppositePit;
    /** The store the captured stones were moved into. */
    public int captureStore;
    /** Total stones moved into the store by the capture (opposite pit + the landing stone). */
    public int capturedTotal;

    /** True if the move earned the player another turn. */
    public boolean extraTurn;
    /** True if this move ended the game. */
    public boolean gameOver;
}
