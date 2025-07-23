import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;

/**
 * Tiny sound engine that synthesizes its effects in code — there are no audio files
 * to ship. Each effect is one or more short sine-wave tones with a quick fade in/out
 * (so they don't click), rendered to PCM and played on a background thread.
 *
 * Every call is wrapped so a machine with no audio device simply stays silent
 * instead of crashing, which also keeps it safe to run under CI.
 */
public final class SoundPlayer {
    private static final float SAMPLE_RATE = 44_100f;
    private static volatile boolean muted = false;

    private SoundPlayer() {
    }

    public static void setMuted(boolean value) {
        muted = value;
    }

    public static boolean isMuted() {
        return muted;
    }

    /** A soft click as a stone drops into a pit. */
    public static void playDrop() {
        play(new int[]{660}, 70, 0.30);
    }

    /** A gentle lift as a pit is picked up. */
    public static void playPickup() {
        play(new int[]{440}, 55, 0.22);
    }

    /** A bright two-note flourish on a capture. */
    public static void playCapture() {
        play(new int[]{784, 1047}, 110, 0.38);
    }

    /** A short rising fanfare when the game ends. */
    public static void playGameOver() {
        play(new int[]{523, 659, 784, 1047}, 160, 0.40);
    }

    private static void play(int[] frequencies, int msPerTone, double volume) {
        if (muted) {
            return;
        }
        Thread t = new Thread(() -> {
            AudioFormat format = new AudioFormat(SAMPLE_RATE, 16, 1, true, false);
            try (SourceDataLine line = AudioSystem.getSourceDataLine(format)) {
                line.open(format);
                line.start();
                for (int freq : frequencies) {
                    byte[] buffer = synthesize(freq, msPerTone, volume);
                    line.write(buffer, 0, buffer.length);
                }
                line.drain();
            } catch (Exception e) {
                // No audio device (e.g. CI) — stay silent rather than fail.
            }
        }, "gebeta-sound");
        t.setDaemon(true);
        t.start();
    }

    /** Renders a single tone to signed 16-bit little-endian mono PCM. */
    private static byte[] synthesize(double frequency, int ms, double volume) {
        int samples = (int) (SAMPLE_RATE * ms / 1000.0);
        int fadeSamples = (int) (SAMPLE_RATE * 0.005); // 5 ms fade to avoid clicks
        byte[] out = new byte[samples * 2];
        for (int i = 0; i < samples; i++) {
            double envelope = 1.0;
            if (fadeSamples > 0) {
                envelope = Math.min(1.0, (double) Math.min(i, samples - i) / fadeSamples);
            }
            double sample = Math.sin(2 * Math.PI * frequency * i / SAMPLE_RATE) * volume * envelope;
            short value = (short) (sample * Short.MAX_VALUE);
            out[i * 2] = (byte) (value & 0xff);
            out[i * 2 + 1] = (byte) ((value >> 8) & 0xff);
        }
        return out;
    }
}
