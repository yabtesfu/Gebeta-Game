import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.JPanel;

/** Dev tool: renders a themed dialog's content to a PNG so it can be reviewed. */
public class DialogPreview {
    public static void main(String[] args) throws Exception {
        System.setProperty("java.awt.headless", "true");
        String path = args.length > 0 ? args[0] : "dialog.png";

        JPanel content = ThemedDialog.buildContent("Play vs Computer",
                "Choose the computer's difficulty");
        JPanel row = ThemedDialog.buttonRow();
        for (String s : new String[]{"Easy", "Medium", "Hard"}) {
            ThemedButton b = ThemedButton.secondary(s);
            b.setPreferredSize(new Dimension(130, 48));
            row.add(b);
        }
        content.add(row);

        Dimension d = content.getPreferredSize();
        content.setSize(d);
        layout(content);

        BufferedImage img = new BufferedImage(d.width, d.height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        content.paint(g);
        g.dispose();
        ImageIO.write(img, "png", new File(path));
        System.out.println("Wrote " + d.width + "x" + d.height + " to " + path);
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
