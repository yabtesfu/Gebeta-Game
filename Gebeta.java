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
        setTitle("ገበጣ - ባህላዊ የጨዋታ ሰሌዳ");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setResizable(true);
        
        UIManager.put("OptionPane.messageFont", new Font("Nyala", Font.PLAIN, 18));
        UIManager.put("OptionPane.buttonFont", new Font("Nyala", Font.BOLD, 16));
        
        
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        
        introPanel = new IntroPanel(this);
        gamePanel = new GamePanel(this);
        aboutPanel = new AboutPanel(this);
        helpPanel = new HelpPanel(this);
        
       
        mainPanel.add(introPanel, "INTRO");
        mainPanel.add(gamePanel, "GAME");
        mainPanel.add(aboutPanel, "ABOUT");
        mainPanel.add(helpPanel, "HELP");
        
        add(mainPanel);
        
    
        cardLayout.show(mainPanel, "INTRO");
    }
    
    public void showPanel(String panelName) {
        cardLayout.show(mainPanel, panelName);
        if (panelName.equals("GAME")) {
            gamePanel.resetGame();
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Gebeta game = new Gebeta();
            game.setVisible(true);
        });
    }
} 