import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.BasicStroke;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Window;

/**
 * Modal pop-up dialogs painted in the Habesha theme, replacing the default
 * {@link javax.swing.JOptionPane} look so every prompt matches the rest of the game:
 * a dark warm panel with a gold rim, a gold title, cream message text and
 * {@link ThemedButton}s.
 */
public final class ThemedDialog {
    private ThemedDialog() {
    }

    /** Difficulty picker. Returns the AI search depth, or -1 if cancelled. */
    public static int chooseDifficulty(Component parent) {
        final int[] result = {-1};
        JDialog dialog = base(parent, "Play vs Computer", "Choose the computer's difficulty");
        JPanel row = buttonRow();
        String[] labels = {"Easy", "Medium", "Hard"};
        int[] depths = {1, 5, 9};
        for (int i = 0; i < labels.length; i++) {
            final int depth = depths[i];
            ThemedButton button = ThemedButton.secondary(labels[i]);
            button.setPreferredSize(new Dimension(130, 48));
            button.addActionListener(e -> {
                result[0] = depth;
                dialog.dispose();
            });
            row.add(button);
        }
        finish(dialog, row);
        return result[0];
    }

    /** Yes/No style confirmation. Returns true if the affirmative button was chosen. */
    public static boolean confirm(Component parent, String title, String message,
                                  String yesText, String noText) {
        final boolean[] result = {false};
        JDialog dialog = base(parent, title, message);
        JPanel row = buttonRow();

        ThemedButton yes = ThemedButton.primary(yesText);
        yes.setPreferredSize(new Dimension(160, 48));
        yes.addActionListener(e -> {
            result[0] = true;
            dialog.dispose();
        });

        ThemedButton no = ThemedButton.subtle(noText);
        no.setPreferredSize(new Dimension(160, 48));
        no.addActionListener(e -> {
            result[0] = false;
            dialog.dispose();
        });

        row.add(yes);
        row.add(no);
        finish(dialog, row);
        return result[0];
    }

    /** Simple message dialog with a single dismiss button. */
    public static void info(Component parent, String title, String message) {
        JDialog dialog = base(parent, title, message);
        JPanel row = buttonRow();
        ThemedButton ok = ThemedButton.primary("Got it");
        ok.setPreferredSize(new Dimension(150, 48));
        ok.addActionListener(e -> dialog.dispose());
        row.add(ok);
        finish(dialog, row);
    }

    // ---- shared construction ----

    private static JDialog base(Component parent, String title, String message) {
        Window owner = parent == null ? null : SwingUtilities.getWindowAncestor(parent);
        JDialog dialog = new JDialog(owner, Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setUndecorated(true);
        dialog.setContentPane(buildContent(title, message));
        return dialog;
    }

    /** Builds the themed content panel (title, divider, message). Also used for previews. */
    static JPanel buildContent(String title, String message) {
        JPanel content = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                Theme.enableAntialias(g2);
                g2.setPaint(new GradientPaint(0, 0, Theme.NIGHT_2, 0, getHeight(), Theme.NIGHT));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setStroke(new BasicStroke(3f));
                g2.setColor(Theme.GOLD);
                g2.drawRoundRect(4, 4, getWidth() - 9, getHeight() - 9, 26, 26);
                g2.dispose();
            }
        };
        content.setOpaque(true);
        content.setBackground(Theme.NIGHT);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(30, 48, 26, 48));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(Theme.display(30));
        titleLabel.setForeground(Theme.GOLD_LIGHT);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(titleLabel);
        content.add(Box.createVerticalStrut(6));
        content.add(CardPanel.divider(320));
        content.add(Box.createVerticalStrut(10));

        JLabel messageLabel = new JLabel("<html><div style='text-align:center; width:380px;'>"
                + escapeHtml(message) + "</div></html>");
        messageLabel.setFont(Theme.body(18));
        messageLabel.setForeground(Theme.CREAM);
        messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(messageLabel);
        content.add(Box.createVerticalStrut(26));
        return content;
    }

    static JPanel buttonRow() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.CENTER_ALIGNMENT);
        return row;
    }

    private static void finish(JDialog dialog, JPanel row) {
        ((JPanel) dialog.getContentPane()).add(row);
        dialog.pack();
        dialog.setLocationRelativeTo(dialog.getOwner());
        dialog.setVisible(true); // blocks until a button disposes it
    }

    private static String escapeHtml(String s) {
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\n", "<br>");
    }
}
