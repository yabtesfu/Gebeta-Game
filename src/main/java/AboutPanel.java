import javax.swing.*;
import java.awt.*;

public class AboutPanel extends JPanel {
    private Gebeta parent;

    public AboutPanel(Gebeta parent) {
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
        BackgroundManager.paint(g2, getWidth(), getHeight(), 1);
        Theme.drawFooter(g2, getWidth(), getHeight());
    }

    private void setupComponents() {
        CardPanel card = new CardPanel(36, 40, 56);

        card.add(label("ገበጣ", Theme.display(40), Theme.GOLD_LIGHT));
        card.add(Box.createVerticalStrut(6));
        card.add(CardPanel.divider(440));
        card.add(Box.createVerticalStrut(10));
        card.add(label("Why I Built This Game", Theme.display(34), Theme.CREAM));
        card.add(Box.createVerticalStrut(28));

        String html = "<html><div style='width:600px; text-align:center;'>"
                + "Gebeta is one of the oldest games in the world, played across Ethiopia "
                + "and the Horn of Africa for countless generations, around markets, in "
                + "homes, and wherever people gather.<br><br>"
                + "I built this game to share our culture with the world. Gebeta carries a "
                + "piece of who we are, and I wanted anyone, anywhere, to be able to sit "
                + "down and play it.<br><br>"
                + "I also love engineering and crafting things. I enjoy taking an idea and "
                + "shaping it into something real you can play. This project is where my "
                + "heritage and my craft meet."
                + "</div></html>";
        JLabel body = new JLabel(html);
        body.setFont(Theme.body(18));
        body.setForeground(Theme.PARCHMENT);
        body.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(body);
        card.add(Box.createVerticalStrut(34));

        ThemedButton back = ThemedButton.primary("‹  Back to Menu");
        back.setAlignmentX(Component.CENTER_ALIGNMENT);
        Dimension size = new Dimension(240, 52);
        back.setPreferredSize(size);
        back.setMaximumSize(size);
        back.addActionListener(e -> parent.showPanel("INTRO"));
        card.add(back);

        add(card);
    }

    private JLabel label(String text, Font font, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(font);
        label.setForeground(color);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        return label;
    }
}
