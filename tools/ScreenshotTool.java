import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.JPanel;

/** Dev tool: renders each screen headlessly to a PNG so the visuals can be reviewed. */
public class ScreenshotTool {
    private static int W = 1200;
    private static int H = 800;

    public static void main(String[] args) throws Exception {
        System.setProperty("java.awt.headless", "true");
        String dir = args.length > 0 ? args[0] : ".";
        if (args.length >= 3) {
            W = Integer.parseInt(args[1]);
            H = Integer.parseInt(args[2]);
        }

        shoot(new IntroPanel(null), dir + "/intro.png");

        GamePanel game = new GamePanel(null);
        game.startGame(true, 5);
        shoot(game, dir + "/game.png");

        shoot(new AboutPanel(null), dir + "/about.png");
        shoot(new HelpPanel(null), dir + "/help.png");
        System.out.println("Screenshots written to " + dir);
    }

    private static void shoot(JPanel panel, String path) throws Exception {
        panel.setSize(W, H);
        layout(panel);
        BufferedImage img = new BufferedImage(W, H, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        panel.paint(g);
        g.dispose();
        File out = new File(path);
        if (out.getParentFile() != null) {
            out.getParentFile().mkdirs();
        }
        ImageIO.write(img, "png", out);
    }

    private static void layout(Component c) {
        if (c instanceof Container) {
            Container ct = (Container) c;
            ct.doLayout();
            for (Component child : ct.getComponents()) {
                layout(child);
            }
        }
    }
}
