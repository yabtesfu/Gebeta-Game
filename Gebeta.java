import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.URI;

public class Gebeta extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private IntroPanel introPanel;
    private GamePanel gamePanel;
    private AboutPanel aboutPanel;
    private HelpPanel helpPanel;
    
    public Gebeta() {
        setTitle("Gebeta - Traditional Mancala Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setResizable(true);
        
        // Initialize components
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        
        introPanel = new IntroPanel(this);
        gamePanel = new GamePanel(this);
        aboutPanel = new AboutPanel(this);
        helpPanel = new HelpPanel(this);
        
        // Add panels to main panel
        mainPanel.add(introPanel, "INTRO");
        mainPanel.add(gamePanel, "GAME");
        mainPanel.add(aboutPanel, "ABOUT");
        mainPanel.add(helpPanel, "HELP");
        
        add(mainPanel);
        
        // Show intro panel first
        cardLayout.show(mainPanel, "INTRO");
    }
    
    public void showPanel(String panelName) {
        cardLayout.show(mainPanel, panelName);
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Gebeta game = new Gebeta();
            game.setVisible(true);
        });
    }
} 