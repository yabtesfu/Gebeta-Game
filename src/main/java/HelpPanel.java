import javax.swing.*;
import java.awt.*;
import java.net.URI;
import java.awt.Desktop;

public class HelpPanel extends JPanel {
    private Gebeta parent;

    public HelpPanel(Gebeta parent) {
        this.parent = parent;
        setLayout(new GridBagLayout());
        setOpaque(true);
        setupComponents();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        Theme.enableAntialias(g2);
        BackgroundManager.paint(g2, getWidth(), getHeight(), 2);
        Theme.drawFooter(g2, getWidth(), getHeight());
    }

    private void setupComponents() {
        CardPanel card = new CardPanel(36, 32, 48);

        JLabel title = new JLabel("How to Play Gebeta");
        title.setFont(Theme.display(36));
        title.setForeground(Theme.CREAM);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(title);
        card.add(Box.createVerticalStrut(6));
        card.add(CardPanel.divider(560));
        card.add(Box.createVerticalStrut(16));

        String html = "<html><div style='width:720px;'>"
                + "<b style='font-size:15px;'>The board</b><br>"
                + "12 small pits (6 per player) and 2 large stores. Each small pit starts "
                + "with 4 stones.<br><br>"
                + "<b style='font-size:15px;'>On your turn</b><br>"
                + "• Pick up all the stones from one of your own pits.<br>"
                + "• Sow them one by one into the following pits, counterclockwise.<br>"
                + "• Land your last stone in your store to take another turn.<br>"
                + "• Land your last stone in an empty pit on your side to capture it and "
                + "all the stones directly opposite.<br><br>"
                + "<b style='font-size:15px;'>Winning</b><br>"
                + "The game ends when a player has no stones left in their pits. The player "
                + "with the most stones in their store wins.<br><br>"
                + "<b style='font-size:15px;'>Tips</b><br>"
                + "• Chain moves that end in your store for free turns.<br>"
                + "• Set up captures, and watch for your opponent's."
                + "</div></html>";
        JLabel body = new JLabel(html);
        body.setFont(Theme.body(17));
        body.setForeground(Theme.PARCHMENT);
        body.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(body);
        card.add(Box.createVerticalStrut(30));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 0));
        buttons.setOpaque(false);
        buttons.setAlignmentX(Component.CENTER_ALIGNMENT);

        ThemedButton video = ThemedButton.secondary("▶  Watch Tutorial");
        video.setPreferredSize(new Dimension(240, 52));
        video.addActionListener(e -> openYouTubeVideo());
        buttons.add(video);

        ThemedButton back = ThemedButton.primary("‹  Back to Menu");
        back.setPreferredSize(new Dimension(220, 52));
        back.addActionListener(e -> parent.showPanel("INTRO"));
        buttons.add(back);

        card.add(buttons);
        add(card);
    }

    private void openYouTubeVideo() {
        try {
            Desktop.getDesktop().browse(new URI(
                "https://www.youtube.com/watch?v=o5HaaipZ3EA&pp=ygUOZ2ViZXRhIGNoZXdhdGE%3D"));
        } catch (Exception e) {
            ThemedDialog.info(this, "Couldn't open the video",
                "Please visit\nyoutube.com/watch?v=o5HaaipZ3EA");
        }
    }
}
