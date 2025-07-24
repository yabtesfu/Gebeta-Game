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

    // ---- palette ----
    public static final Color NIGHT       = new Color(28, 18, 12);   // deep espresso
    public static final Color NIGHT_2     = new Color(54, 34, 22);   // warm brown
    public static final Color COFFEE      = new Color(43, 27, 18);
    public static final Color GOLD        = new Color(212, 165, 62);
    public static final Color GOLD_LIGHT  = new Color(242, 206, 122);
    public static final Color GOLD_DARK   = new Color(150, 104, 34);
    public static final Color TERRACOTTA  = new Color(180, 74, 34);
    public static final Color TERRACOTTA_LIGHT = new Color(210, 110, 66);
    public static final Color CREAM       = new Color(244, 234, 212);
    public static final Color PARCHMENT   = new Color(228, 208, 168);
    public static final Color PARCHMENT_DARK = new Color(198, 172, 128);
    public static final Color WOOD        = new Color(96, 60, 33);
    public static final Color WOOD_DARK   = new Color(64, 39, 21);

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
