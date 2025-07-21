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
        setLayout(new BorderLayout());
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
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);
        contentPanel.add(Box.createVerticalStrut(100));
        JLabel titleLabel = new JLabel("GEBETA");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 72));
        titleLabel.setForeground(new Color(139, 69, 19));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(titleLabel);
        contentPanel.add(Box.createVerticalStrut(100));
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setOpaque(false);
        JButton twoPlayerButton = createStyledButton("Two Players", new Color(34, 139, 34));
        twoPlayerButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        twoPlayerButton.setMaximumSize(new Dimension(200, 50));
        twoPlayerButton.setPreferredSize(new Dimension(200, 50));
        twoPlayerButton.addActionListener(e -> parent.startGame(false, 0));
        buttonPanel.add(twoPlayerButton);
        buttonPanel.add(Box.createVerticalStrut(20));

        JButton vsComputerButton = createStyledButton("Play vs Computer", new Color(178, 34, 34));
        vsComputerButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        vsComputerButton.setMaximumSize(new Dimension(200, 50));
        vsComputerButton.setPreferredSize(new Dimension(200, 50));
        vsComputerButton.addActionListener(e -> chooseDifficultyAndPlay());
        buttonPanel.add(vsComputerButton);
        buttonPanel.add(Box.createVerticalStrut(20));
        JButton aboutButton = createStyledButton("About", new Color(70, 130, 180));
        aboutButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        aboutButton.setMaximumSize(new Dimension(200, 50));
        aboutButton.setPreferredSize(new Dimension(200, 50));
        aboutButton.addActionListener(e -> parent.showPanel("ABOUT"));
        buttonPanel.add(aboutButton);
        buttonPanel.add(Box.createVerticalStrut(20));
        JButton helpButton = createStyledButton("Help", new Color(218, 165, 32));
        helpButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        helpButton.setMaximumSize(new Dimension(200, 50));
        helpButton.setPreferredSize(new Dimension(200, 50));
        helpButton.addActionListener(e -> parent.showPanel("HELP"));
        buttonPanel.add(helpButton);
        buttonPanel.add(Box.createVerticalStrut(20));
        JButton exitButton = createStyledButton("Exit", new Color(220, 20, 60));
        exitButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        exitButton.setMaximumSize(new Dimension(200, 50));
        exitButton.setPreferredSize(new Dimension(200, 50));
        exitButton.addActionListener(e -> System.exit(0));
        buttonPanel.add(exitButton);
        contentPanel.add(buttonPanel);
        contentPanel.add(Box.createVerticalGlue());
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);
        centerPanel.add(contentPanel, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);
    }
    
    /** Asks the player for a difficulty, then starts a game against the AI. */
    private void chooseDifficultyAndPlay() {
        Object[] options = {"Easy", "Medium", "Hard"};
        int choice = JOptionPane.showOptionDialog(
            this,
            "Choose the computer's difficulty:",
            "Play vs Computer",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[1]
        );
        int depth;
        switch (choice) {
            case 0: depth = 1; break;  // Easy   — greedy, one move ahead
            case 1: depth = 5; break;  // Medium — a few moves ahead
            case 2: depth = 9; break;  // Hard   — deep search
            default: return;           // dialog closed, stay on the menu
        }
        parent.startGame(true, depth);
    }

    private JButton createStyledButton(String text, Color backgroundColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 18));
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
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        } else {
            g.setColor(new Color(245, 245, 220));
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }
} 