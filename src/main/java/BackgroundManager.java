import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.MultipleGradientPaint;
import java.awt.Point;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.GradientPaint;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

/**
 * Paints the shared background used across every screen: a photograph of Habesha
 * people playing gebeta, warmed toward a candle-lit tone and dimmed so the interface
 * on top stays readable.
 *
 * "Varying" backgrounds are supported two ways:
 *   1. Each screen passes a {@code variant} index that subtly shifts the warm tint.
 *   2. Any extra photos dropped into {@code src/main/resources} named background1.png,
 *      background2.png, … are picked up automatically and join the rotation.
 *
 * Rendered results are cached per size + variant so the (large) source photo is only
 * processed when something actually changes — important because the board repaints
 * many times per second during the sowing animation.
 */
public final class BackgroundManager {
    private static final List<BufferedImage> PHOTOS = new ArrayList<>();
    private static boolean loaded = false;

    // Simple one-slot cache for the most recently rendered backdrop.
    private static BufferedImage cache;
    private static int cacheW = -1;
    private static int cacheH = -1;
    private static int cacheVariant = Integer.MIN_VALUE;

    private static BufferedImage gameCache;
    private static int gameCacheW = -1;
    private static int gameCacheH = -1;

    private BackgroundManager() {
    }

    /**
     * Paints the game-screen backdrop: the dark warm gradient from the mockup with the
     * Habesha photo laid over it at 40% opacity, so the scene shows through subtly.
     */
    public static void paintGame(Graphics2D g, int w, int h) {
        if (w <= 0 || h <= 0) {
            return;
        }
        if (gameCache == null || gameCacheW != w || gameCacheH != h) {
            gameCache = renderGame(w, h);
            gameCacheW = w;
            gameCacheH = h;
        }
        g.drawImage(gameCache, 0, 0, null);
    }

    private static BufferedImage renderGame(int w, int h) {
        ensureLoaded();
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = out.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        // Dark warm vertical gradient (#241710 -> #1d130d @60% -> #180f0a).
        g.setPaint(new LinearGradientPaint(0, 0, 0, h,
                new float[]{0f, 0.6f, 1f},
                new Color[]{Theme.NIGHT_2, Theme.NIGHT, Theme.NIGHT_3}));
        g.fillRect(0, 0, w, h);

        // The photograph at 40% opacity on top.
        if (!PHOTOS.isEmpty()) {
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.40f));
            drawCover(g, PHOTOS.get(0), w, h);
            g.setComposite(AlphaComposite.SrcOver);
        }

        // Gentle vignette to focus the centre.
        RadialGradientPaint vignette = new RadialGradientPaint(
                new Point(w / 2, h / 2),
                Math.max(w, h) * 0.75f,
                new float[]{0f, 1f},
                new Color[]{new Color(0, 0, 0, 0), new Color(0, 0, 0, 120)},
                MultipleGradientPaint.CycleMethod.NO_CYCLE);
        g.setPaint(vignette);
        g.fillRect(0, 0, w, h);

        g.dispose();
        return out;
    }

    public static int variantCount() {
        ensureLoaded();
        return Math.max(1, PHOTOS.size());
    }

    private static synchronized void ensureLoaded() {
        if (loaded) {
            return;
        }
        loaded = true;
        // The primary photo, then any optional extras the developer drops in.
        String[] names = {"/background.png", "/background1.png", "/background2.png",
                "/background3.png", "/background4.png", "/background5.png"};
        for (String name : names) {
            BufferedImage img = tryLoad(name);
            if (img != null) {
                PHOTOS.add(img);
            }
        }
    }

    private static BufferedImage tryLoad(String resource) {
        try (InputStream in = BackgroundManager.class.getResourceAsStream(resource)) {
            return in == null ? null : ImageIO.read(in);
        } catch (Exception e) {
            return null;
        }
    }

    /** Paints the backdrop for the given variant, covering the whole component. */
    public static void paint(Graphics2D g, int w, int h, int variant) {
        if (w <= 0 || h <= 0) {
            return;
        }
        if (cache == null || cacheW != w || cacheH != h || cacheVariant != variant) {
            cache = render(w, h, variant);
            cacheW = w;
            cacheH = h;
            cacheVariant = variant;
        }
        g.drawImage(cache, 0, 0, null);
    }

    private static BufferedImage render(int w, int h, int variant) {
        ensureLoaded();
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = out.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        if (PHOTOS.isEmpty()) {
            Theme.paintBackdrop(g, w, h);
        } else {
            BufferedImage photo = PHOTOS.get(Math.floorMod(variant, PHOTOS.size()));
            drawCover(g, photo, w, h);
        }

        // Warm tint: terracotta at the top fading into deep coffee at the bottom.
        // The variant nudges the top hue so different screens feel distinct.
        Color warmTop = warmTopFor(variant);
        g.setPaint(new GradientPaint(0, 0, warmTop, 0, h, new Color(43, 27, 18, 130)));
        g.fillRect(0, 0, w, h);

        // Overall dimming so foreground UI reads clearly.
        g.setColor(new Color(20, 13, 9, 96));
        g.fillRect(0, 0, w, h);

        // Vignette to draw the eye toward the centre.
        RadialGradientPaint vignette = new RadialGradientPaint(
                new Point(w / 2, h / 2),
                Math.max(w, h) * 0.72f,
                new float[]{0f, 1f},
                new Color[]{new Color(0, 0, 0, 0), new Color(0, 0, 0, 165)},
                MultipleGradientPaint.CycleMethod.NO_CYCLE);
        g.setPaint(vignette);
        g.fillRect(0, 0, w, h);

        g.dispose();
        return out;
    }

    private static Color warmTopFor(int variant) {
        switch (Math.floorMod(variant, 3)) {
            case 1:  return new Color(150, 104, 34, 80);  // gold-leaning
            case 2:  return new Color(120, 60, 30, 85);   // deep rust
            default: return new Color(180, 74, 34, 78);   // terracotta
        }
    }

    /** Scales the photo to fill (cover) the target area, centre-cropped. */
    private static void drawCover(Graphics2D g, BufferedImage img, int w, int h) {
        double scale = Math.max((double) w / img.getWidth(), (double) h / img.getHeight());
        int sw = (int) Math.ceil(img.getWidth() * scale);
        int sh = (int) Math.ceil(img.getHeight() * scale);
        int x = (w - sw) / 2;
        int y = (h - sh) / 2;
        g.drawImage(img, x, y, sw, sh, null);
    }
}
