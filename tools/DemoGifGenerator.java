import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageOutputStream;

/**
 * Build-time tool (NOT part of the game) that renders the real board via
 * {@link GameBoard#draw} into off-screen frames and encodes them as an animated GIF
 * for the README. Because it uses the game's own drawing code, the GIF is an exact
 * capture of the on-screen animation — no screen recording involved.
 *
 * Run from the project root after a build:
 * <pre>
 *   javac -cp build/classes/java/main -d tools tools/DemoGifGenerator.java
 *   java  -cp build/classes/java/main:tools DemoGifGenerator docs/demo.gif
 * </pre>
 */
public class DemoGifGenerator {
    private static final int SRC_W = 1200;
    private static final int SRC_H = 800;
    private static final int OUT_W = 600;
    private static final int OUT_H = 400;

    public static void main(String[] args) throws Exception {
        System.setProperty("java.awt.headless", "true");
        String outPath = args.length > 0 ? args[0] : "docs/demo.gif";

        GameBoard board = new GameBoard();

        // Play a short, deterministic opening so the demo move sows a lively number
        // of stones rather than the flat opening position.
        int[] opening = {2, 9, 1, 10, 5};
        for (int move : opening) {
            if (board.getStateCopy().isLegalMove(move)) {
                board.makeMove(move);
                board.syncVisuals();
            }
        }

        // Choose the current player's legal move with the most stones (most to watch).
        MancalaState state = board.getStateCopy();
        int demoMove = -1;
        int bestStones = -1;
        for (int m : state.legalMoves()) {
            if (state.stones(m) > bestStones) {
                bestStones = state.stones(m);
                demoMove = m;
            }
        }

        List<BufferedImage> frames = new ArrayList<>();
        List<Integer> delaysMs = new ArrayList<>();

        // Hold on the starting position.
        addFrame(frames, delaysMs, render(board), 900);

        MoveTrace trace = board.makeMove(demoMove);
        board.setAnimating(true);
        board.visualPickUp(trace.source);
        addFrame(frames, delaysMs, render(board), 260);

        for (int idx : trace.drops) {
            board.visualDrop(idx);
            addFrame(frames, delaysMs, render(board), 200);
        }

        if (trace.captured) {
            board.visualCapture(trace.captureLandingPit, trace.captureOppositePit,
                    trace.captureStore, trace.capturedTotal);
            addFrame(frames, delaysMs, render(board), 500);
        }

        board.syncVisuals();
        board.setAnimating(false);
        // Hold on the final position before the GIF loops.
        addFrame(frames, delaysMs, render(board), 1400);

        File out = new File(outPath);
        if (out.getParentFile() != null) {
            out.getParentFile().mkdirs();
        }
        writeAnimatedGif(frames, delaysMs, out);
        System.out.println("Wrote " + frames.size() + " frames to " + out.getPath()
                + " (demo move: pit " + demoMove + ", " + bestStones + " stones)");
    }

    private static void addFrame(List<BufferedImage> frames, List<Integer> delays,
                                 BufferedImage img, int delayMs) {
        frames.add(img);
        delays.add(delayMs);
    }

    /** Renders the board through its real draw code, scaled down for a compact GIF. */
    private static BufferedImage render(GameBoard board) {
        BufferedImage full = new BufferedImage(SRC_W, SRC_H, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = full.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        board.draw(g);
        g.dispose();

        BufferedImage scaled = new BufferedImage(OUT_W, OUT_H, BufferedImage.TYPE_INT_RGB);
        Graphics2D gs = scaled.createGraphics();
        gs.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        gs.drawImage(full, 0, 0, OUT_W, OUT_H, null);
        gs.dispose();
        return scaled;
    }

    /** Encodes the frames as an infinitely-looping animated GIF with per-frame delays. */
    private static void writeAnimatedGif(List<BufferedImage> frames, List<Integer> delaysMs,
                                         File file) throws Exception {
        ImageWriter writer = ImageIO.getImageWritersBySuffix("gif").next();
        try (ImageOutputStream ios = ImageIO.createImageOutputStream(file)) {
            writer.setOutput(ios);
            writer.prepareWriteSequence(null);
            for (int i = 0; i < frames.size(); i++) {
                BufferedImage frame = frames.get(i);
                ImageWriteParam params = writer.getDefaultWriteParam();
                IIOMetadata meta = writer.getDefaultImageMetadata(
                        new javax.imageio.ImageTypeSpecifier(frame), params);
                configureFrame(meta, delaysMs.get(i), i == 0);
                writer.writeToSequence(new IIOImage(frame, null, meta), params);
            }
            writer.endWriteSequence();
        } finally {
            writer.dispose();
        }
    }

    private static void configureFrame(IIOMetadata meta, int delayMs, boolean first)
            throws Exception {
        String format = meta.getNativeMetadataFormatName();
        IIOMetadataNode root = (IIOMetadataNode) meta.getAsTree(format);

        IIOMetadataNode gce = child(root, "GraphicControlExtension");
        gce.setAttribute("disposalMethod", "none");
        gce.setAttribute("userInputFlag", "FALSE");
        gce.setAttribute("transparentColorFlag", "FALSE");
        gce.setAttribute("delayTime", Integer.toString(Math.round(delayMs / 10f))); // centiseconds
        gce.setAttribute("transparentColorIndex", "0");

        if (first) {
            // Netscape application extension -> loop forever.
            IIOMetadataNode appExts = child(root, "ApplicationExtensions");
            IIOMetadataNode appNode = new IIOMetadataNode("ApplicationExtension");
            appNode.setAttribute("applicationID", "NETSCAPE");
            appNode.setAttribute("authenticationCode", "2.0");
            appNode.setUserObject(new byte[]{0x1, 0x0, 0x0}); // loop count 0 = infinite
            appExts.appendChild(appNode);
        }

        meta.setFromTree(format, root);
    }

    private static IIOMetadataNode child(IIOMetadataNode root, String name) {
        for (int i = 0; i < root.getLength(); i++) {
            if (root.item(i).getNodeName().equalsIgnoreCase(name)) {
                return (IIOMetadataNode) root.item(i);
            }
        }
        IIOMetadataNode node = new IIOMetadataNode(name);
        root.appendChild(node);
        return node;
    }
}
