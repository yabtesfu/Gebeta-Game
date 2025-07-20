import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.URI;
import java.awt.Desktop;

public class HelpPanel extends JPanel {
    private Gebeta parent;
    
    public HelpPanel(Gebeta parent) {
        this.parent = parent;
        setLayout(new BorderLayout());
        setBackground(new Color(245, 245, 220)); 
        setupComponents();
    }
    
    private void setupComponents() {
        
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(new Color(245, 245, 220));
        
      
        contentPanel.add(Box.createVerticalStrut(50));
        
    
        JLabel titleLabel = new JLabel("How to Play Gebeta");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 27));
        titleLabel.setForeground(new Color(139, 69, 19)); // Saddle Brown
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(titleLabel);
        
       
        contentPanel.add(Box.createVerticalStrut(30));
        
        JPanel instructionsPanel = new JPanel();
        instructionsPanel.setLayout(new BoxLayout(instructionsPanel, BoxLayout.Y_AXIS));
        instructionsPanel.setBackground(new Color(255, 250, 240)); // Floral White
        instructionsPanel.setBorder(BorderFactory.createLineBorder(new Color(139, 69, 19), 3));
        
        
        String[] instructions = {
            "Gebeta is a traditional Ethiopian board game.",
            "",
            "Game Rules:",
            "• The game is played on 12 small pits (6 for each player) and 2 large stores.",
            "• Each small pit starts with 4 stones.",
            "• Players take turns picking up all stones from one of their pits.",
            "• Stones are distributed one by one in consecutive pits in counter-clockwise direction.",
            "• If the last stone lands in your store, you get another turn.",
            "• If the last stone lands in an empty pit on your side, you capture that stone and all stones in the opposite pit.",
            "• The game ends when one player has no stones in their small pits.",
            "• The player with the most stones in their store wins!",
            "",
            "Strategy Tips:",
            "• Try to land stones in your store to get extra turns.",
            "• Plan moves that allow you to capture your opponent's stones.",
            "• Pay attention to stone distribution to predict your opponent's moves."
        };
        
        for (String instruction : instructions) {
            JLabel label = new JLabel(instruction);
            label.setFont(new Font("Arial", Font.PLAIN, 16));
            label.setForeground(new Color(70, 130, 180));
            label.setAlignmentX(Component.CENTER_ALIGNMENT);
            instructionsPanel.add(label);
            instructionsPanel.add(Box.createVerticalStrut(8));
        }
        
        
        contentPanel.add(instructionsPanel);
        
        
        contentPanel.add(Box.createVerticalStrut(30));
        
       
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setOpaque(false);
        
        
        JButton videoButton = createStyledButton("Watch Tutorial Video", new Color(255, 0, 0));
        videoButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        videoButton.setMaximumSize(new Dimension(250, 50));
        videoButton.setPreferredSize(new Dimension(250, 50));
        videoButton.addActionListener(e -> openYouTubeVideo());
        buttonPanel.add(videoButton);
        buttonPanel.add(Box.createVerticalStrut(20));
        
        
        JButton backButton = createStyledButton("Back to Menu", new Color(139, 69, 19));
        backButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        backButton.setMaximumSize(new Dimension(200, 50));
        backButton.setPreferredSize(new Dimension(200, 50));
        backButton.addActionListener(e -> parent.showPanel("INTRO"));
        buttonPanel.add(backButton);
        
        contentPanel.add(buttonPanel);
        
       
        contentPanel.add(Box.createVerticalGlue());
        
       
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);
        centerPanel.add(contentPanel, BorderLayout.CENTER);
        
        add(centerPanel, BorderLayout.CENTER);
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