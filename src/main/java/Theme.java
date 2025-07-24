import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.RenderingHints;

/**
 * Central visual theme for Gebeta — a warm, traditional Habesha (Ethiopian) look:
 * coffee-dark backgrounds, ochre gold, terracotta, and cream, with the Ethiopian
 * flag colours reserved as small accents. Every panel pulls its colours, fonts and
 * common paint helpers from here so the whole game reads as one design.
 */
public final class Theme {
    private Theme() {
    }

    // ---- palette (aligned with the game-board mockup) ----
    public static final Color NIGHT       = new Color(29, 19, 13);   // #1d130d page base
    public static final Color NIGHT_2     = new Color(36, 23, 16);   // #241710
    public static final Color NIGHT_3     = new Color(24, 15, 10);   // #180f0a
    public static final Color COFFEE      = new Color(43, 28, 17);   // #2b1c11 panels
    public static final Color GOLD        = new Color(229, 180, 90); // #e5b45a
    public static final Color GOLD_LIGHT  = new Color(243, 230, 203);// #f3e6cb (cream-gold)
    public static final Color GOLD_DARK   = new Color(208, 155, 60); // #d09b3c
    public static final Color TERRACOTTA  = new Color(192, 95, 42);  // #c05f2a (AI)
    public static final Color TERRACOTTA_LIGHT = new Color(210, 110, 66);
    public static final Color CREAM       = new Color(243, 230, 203);// #f3e6cb
    public static final Color PARCHMENT   = new Color(216, 195, 156);// #d8c39c body text
    public static final Color PARCHMENT_DARK = new Color(163, 128, 90); // #a3805a
    public static final Color WOOD        = new Color(124, 76, 39);  // #7c4c27 board top
    public static final Color WOOD_MID    = new Color(106, 63, 32);  // #6a3f20
    public static final Color WOOD_DARK   = new Color(93, 55, 25);   // #5d3719
    public static final Color BOARD_BORDER = new Color(67, 40, 15);  // #43280f

    // Stores (deep carved wells).
    public static final Color STORE_TOP    = new Color(74, 43, 18);  // #4a2b12
    public static final Color STORE_BOT    = new Color(58, 32, 9);   // #3a2009
    public static final Color STORE_BORDER = new Color(46, 26, 8);   // #2e1a08

    // Pits (light carved bowls).
    public static final Color PIT_TOP = new Color(240, 225, 192);    // #f0e1c0
    public static final Color PIT_MID = new Color(220, 195, 148);    // #dcc394
    public static final Color PIT_BOT = new Color(196, 164, 115);    // #c4a473

    // Count badge pills.
    public static final Color BADGE_BG     = new Color(58, 32, 9);   // #3a2009
    public static final Color BADGE_BORDER = new Color(87, 55, 26);  // #57371a

    // Player accents.
    public static final Color YOU_GREEN   = new Color(47, 125, 79);  // #2f7d4f
    public static final Color YOU_GREEN_D = new Color(28, 84, 52);   // #1c5434
    public static final Color AI_ORANGE   = new Color(192, 95, 42);  // #c05f2a
    public static final Color AI_ORANGE_D = new Color(138, 61, 23);  // #8a3d17

    // Ethiopian flag accents — used sparingly.
    public static final Color ETH_GREEN   = new Color(30, 122, 61);
    public static final Color ETH_YELLOW  = new Color(244, 196, 48);
    public static final Color ETH_RED     = new Color(206, 32, 43);

    // Translucent scrims for panels laid over the photographic background.
    public static final Color SCRIM       = new Color(28, 18, 12, 205);
    public static final Color SCRIM_LIGHT = new Color(43, 27, 18, 170);

    // ---- fonts ----
    public static Font display(int size) {
        return new Font("Serif", Font.BOLD, size);
    }

    public static Font heading(int size) {
        return new Font("SansSerif", Font.BOLD, size);
    }

    public static Font body(int size) {
        return new Font("SansSerif", Font.PLAIN, size);
    }

    // ---- paint helpers ----

    public static void enableAntialias(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    }

    /** A warm vertical gradient used as the base backdrop when no photo is present. */
    public static void paintBackdrop(Graphics2D g, int w, int h) {
        g.setPaint(new GradientPaint(0, 0, NIGHT_2, 0, h, NIGHT));
        g.fillRect(0, 0, w, h);
    }

    /** Fills a rounded rectangle with the given paint. */
    public static void fillRound(Graphics2D g, int x, int y, int w, int h, int arc, Paint paint) {
        g.setPaint(paint);
        g.fillRoundRect(x, y, w, h, arc, arc);
    }

    /** Draws a rounded rectangle outline in the given colour and thickness. */
    public static void strokeRound(Graphics2D g, int x, int y, int w, int h, int arc,
                                   Color color, float thickness) {
        g.setColor(color);
        g.setStroke(new BasicStroke(thickness));
        g.drawRoundRect(x, y, w, h, arc, arc);
    }

    /**
     * Draws a rounded "card": a scrim panel with a gold rim, the recurring surface for
     * titles, instructions and player info laid over the background.
     */
    public static void drawCard(Graphics2D g, int x, int y, int w, int h, int arc, Color fill) {
        g.setColor(new Color(0, 0, 0, 90));
        g.fillRoundRect(x + 4, y + 6, w, h, arc, arc); // soft drop shadow
        fillRound(g, x, y, w, h, arc, fill);
        strokeRound(g, x, y, w, h, arc, GOLD, 2.5f);
        g.setColor(new Color(GOLD_LIGHT.getRed(), GOLD_LIGHT.getGreen(), GOLD_LIGHT.getBlue(), 90));
        g.setStroke(new BasicStroke(1f));
        g.drawRoundRect(x + 4, y + 4, w - 8, h - 8, arc - 6, arc - 6);
    }

    /**
     * Draws a small traditional divider — a gold line with a centred diamond, echoing
     * Habesha telsem/embroidery motifs.
     */
    public static void drawDivider(Graphics2D g, int cx, int y, int halfWidth) {
        g.setColor(GOLD);
        g.setStroke(new BasicStroke(2f));
        g.drawLine(cx - halfWidth, y, cx - 12, y);
        g.drawLine(cx + 12, y, cx + halfWidth, y);
        int d = 7;
        int[] xs = {cx, cx + d, cx, cx - d};
        int[] ys = {y - d, y, y + d, y};
        g.fillPolygon(xs, ys, 4);
    }

    /**
     * A stable pseudo-random value in [0,1) for a given integer seed. Used to scatter
     * stones deterministically so they don't jitter between repaints.
     */
    public static double rand(int seed) {
        double x = Math.sin(seed * 127.1 + 311.7) * 43758.5453;
        return x - Math.floor(x);
    }

    /** Draws a string centred horizontally at the given baseline y. */
    public static void drawCentered(Graphics2D g, String text, int cx, int y) {
        int w = g.getFontMetrics().stringWidth(text);
        g.drawString(text, cx - w / 2, y);
    }

    /** The signature footer, centred near the bottom of a screen. */
    public static void drawFooter(Graphics2D g, int w, int h) {
        g.setFont(body(15));
        String text = "© 2026 Yabetse. Engineered with intention.";
        int y = h - 26;
        g.setColor(new Color(0, 0, 0, 130));
        drawCentered(g, text, w / 2 + 1, y + 1);
        g.setColor(new Color(GOLD_LIGHT.getRed(), GOLD_LIGHT.getGreen(), GOLD_LIGHT.getBlue(), 205));
        drawCentered(g, text, w / 2, y);
    }
}
