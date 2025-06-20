import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;

public class IntroPanel extends JPanel {
    private Gebeta parent;
    private BufferedImage backgroundImage;
    
    public IntroPanel(Gebeta parent) {
        this.parent = parent;
        setLayout(null);
        loadBackgroundImage();
        setupComponents();
    }
    
    private void loadBackgroundImage() {
        try {
            backgroundImage = ImageIO.read(new File("Background Image.png"));
        } catch (Exception e) {
            System.err.println("Error loading background image: " + e.getMessage());
        }
    }
    
    private void setupComponents() {
        // Title
        JLabel titleLabel = new JLabel("GEBETA");
        titleLabel.setFont(new Font("Arial Black", Font.BOLD, 72));
        titleLabel.setForeground(new Color(139, 69, 19)); // Saddle Brown
        titleLabel.setBounds(400, 150, 400, 100);
        add(titleLabel);
        
        
        // Play Game Button
        JButton playButton = createStyledButton("Play Game", new Color(34, 139, 34));
        playButton.setBounds(450, 350, 200, 50);
        playButton.addActionListener(e -> parent.showPanel("GAME"));
        add(playButton);
        
        // About Me Button
        JButton aboutButton = createStyledButton("About Me", new Color(70, 130, 180));
        aboutButton.setBounds(450, 420, 200, 50);
        aboutButton.addActionListener(e -> parent.showPanel("ABOUT"));
        add(aboutButton);
        
        // Help Button
        JButton helpButton = createStyledButton("Help", new Color(218, 165, 32));
        helpButton.setBounds(450, 490, 200, 50);
        helpButton.addActionListener(e -> parent.showPanel("HELP"));
        add(helpButton);
        
        // Exit Button
        JButton exitButton = createStyledButton("Exit", new Color(220, 20, 60));
        exitButton.setBounds(450, 560, 200, 50);
        exitButton.addActionListener(e -> System.exit(0));
        add(exitButton);
    }
    
    private JButton createStyledButton(String text, Color backgroundColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 18));
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
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        } else {
            // Fallback background
            g.setColor(new Color(245, 245, 220)); // Beige
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }
} 