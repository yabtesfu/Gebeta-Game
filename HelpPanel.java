import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.URI;
import java.awt.Desktop;

public class HelpPanel extends JPanel {
    private Gebeta parent;
    
    public HelpPanel(Gebeta parent) {
        this.parent = parent;
        setLayout(null);
        setBackground(new Color(245, 245, 220)); // Beige background
        setupComponents();
    }
    
    private void setupComponents() {
        // Title
        JLabel titleLabel = new JLabel("How to Play Gebeta");
        titleLabel.setFont(new Font("Arial Black", Font.BOLD, 27));
        titleLabel.setForeground(new Color(139, 69, 19)); // Saddle Brown
        titleLabel.setBounds(400, 50, 400, 50);
        add(titleLabel);
        
        // Instructions Panel
        JPanel instructionsPanel = new JPanel();
        instructionsPanel.setLayout(new BoxLayout(instructionsPanel, BoxLayout.Y_AXIS));
        instructionsPanel.setBackground(new Color(255, 250, 240)); // Floral White
        instructionsPanel.setBorder(BorderFactory.createLineBorder(new Color(139, 69, 19), 3));
        instructionsPanel.setBounds(100, 120, 1000, 400);
        
        // Instructions text
        String[] instructions = {
            "Gebeta (Mancala) is a traditional Ethiopian board game played with stones.",
            "",
            "Game Rules:",
            "• The game is played on a board with 12 small pits (6 for each player) and 2 large stores.",
            "• Each small pit starts with 4 stones.",
            "• Players take turns picking up all stones from one of their pits.",
            "• Stones are distributed one by one into subsequent pits in a counter-clockwise direction.",
            "• If the last stone lands in your store, you get another turn.",
            "• If the last stone lands in an empty pit on your side, you capture that stone and all stones in the opposite pit.",
            "• The game ends when one player has no stones left in their small pits.",
            "• The player with the most stones in their store wins!",
            "",
            "Strategy Tips:",
            "• Try to get extra turns by landing in your store.",
            "• Plan moves that allow you to capture opponent's stones.",
            "• Keep track of stone distribution to anticipate opponent's moves."
        };
        
        for (String instruction : instructions) {
            JLabel label = new JLabel(instruction);
            label.setFont(new Font("Arial", Font.PLAIN, 16));
            label.setForeground(new Color(70, 130, 180));
            label.setAlignmentX(Component.CENTER_ALIGNMENT);
            instructionsPanel.add(label);
            instructionsPanel.add(Box.createVerticalStrut(8));
        }
        
        add(instructionsPanel);
        
        // YouTube Video Link
        JButton videoButton = createStyledButton("Watch Tutorial Video", new Color(255, 0, 0));
        videoButton.setBounds(400, 550, 250, 50);
        videoButton.addActionListener(e -> openYouTubeVideo());
        add(videoButton);
        
        // Back to Menu Button
        JButton backButton = createStyledButton("Back to Menu", new Color(139, 69, 19));
        backButton.setBounds(500, 620, 200, 50);
        backButton.addActionListener(e -> parent.showPanel("INTRO"));
        add(backButton);
    }
    
    private void openYouTubeVideo() {
        try {
            Desktop.getDesktop().browse(new URI("https://www.youtube.com/watch?v=o5HaaipZ3EA&pp=ygUOZ2ViZXRhIGNoZXdhdGE%3D"));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Could not open video. Please visit:\nhttps://www.youtube.com/watch?v=o5HaaipZ3EA", 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private JButton createStyledButton(String text, Color backgroundColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 16));
        button.setBackground(backgroundColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        
        // Add hover effect
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(backgroundColor.brighter());
            }
            public void mouseExited(MouseEvent e) {
                button.setBackground(backgroundColor);
            }
        });
        
        return button;
    }
} 