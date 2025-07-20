import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class AboutPanel extends JPanel {
    private Gebeta parent;
    
    public AboutPanel(Gebeta parent) {
        this.parent = parent;
        setLayout(new BorderLayout());
        setBackground(new Color(245, 245, 220)); 
        setupComponents();
    }
    
    private void setupComponents() {
        
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(new Color(245, 245, 220));
        
        
        contentPanel.add(Box.createVerticalStrut(100));
        
       
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(new Color(255, 250, 240)); // Floral White
        infoPanel.setBorder(BorderFactory.createLineBorder(new Color(139, 69, 19), 3));
        infoPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        infoPanel.setMaximumSize(new Dimension(800, 400));
        infoPanel.setPreferredSize(new Dimension(800, 400));
        
     
        JLabel nameLabel = new JLabel("Made by: Yabetse Tesfaye");
        nameLabel.setFont(new Font("Arial", Font.BOLD, 24));
        nameLabel.setForeground(new Color(70, 130, 180));
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel institutionLabel = new JLabel("Student at Addis Ababa Institute of Technology");
        institutionLabel.setFont(new Font("Arial", Font.BOLD, 20));
        institutionLabel.setForeground(new Color(70, 130, 180));
        institutionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel programLabel = new JLabel("Software Engineer");
        programLabel.setFont(new Font("Arial", Font.BOLD, 20));
        programLabel.setForeground(new Color(70, 130, 180));
        programLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel yearLabel = new JLabel("5th Year Student");
        yearLabel.setFont(new Font("Arial", Font.BOLD, 20));
        yearLabel.setForeground(new Color(70, 130, 180));
        yearLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel idLabel = new JLabel("ID: UGR/31352/15");
        idLabel.setFont(new Font("Arial", Font.BOLD, 20));
        idLabel.setForeground(new Color(70, 130, 180));
        idLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
      
        infoPanel.add(Box.createVerticalStrut(30));
        infoPanel.add(nameLabel);
        infoPanel.add(Box.createVerticalStrut(20));
        infoPanel.add(institutionLabel);
        infoPanel.add(Box.createVerticalStrut(15));
        infoPanel.add(programLabel);
        infoPanel.add(Box.createVerticalStrut(15));
        infoPanel.add(yearLabel);
        infoPanel.add(Box.createVerticalStrut(15));
        infoPanel.add(idLabel);
        infoPanel.add(Box.createVerticalStrut(30));
        
        contentPanel.add(infoPanel);
        

        contentPanel.add(Box.createVerticalStrut(50));
        
    
        JButton backButton = createStyledButton("ወደ ዝርዝር ተመለስ", new Color(139, 69, 19));
        backButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        backButton.setMaximumSize(new Dimension(200, 50));
        backButton.setPreferredSize(new Dimension(200, 50));
        backButton.addActionListener(e -> parent.showPanel("INTRO"));
        contentPanel.add(backButton);
        
       
        contentPanel.add(Box.createVerticalGlue());
        
     
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);
        centerPanel.add(contentPanel, BorderLayout.CENTER);
        
        add(centerPanel, BorderLayout.CENTER);
    }
    
    private JButton createStyledButton(String text, Color backgroundColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Nyala", Font.BOLD, 18));
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