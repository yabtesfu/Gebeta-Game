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
        
    
        JLabel titleLabel = new JLabel("ገበጣ እንዴት እንደሚጫወት");
        titleLabel.setFont(new Font("Nyala", Font.BOLD, 27));
        titleLabel.setForeground(new Color(139, 69, 19)); // Saddle Brown
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(titleLabel);
        
       
        contentPanel.add(Box.createVerticalStrut(30));
        
        JPanel instructionsPanel = new JPanel();
        instructionsPanel.setLayout(new BoxLayout(instructionsPanel, BoxLayout.Y_AXIS));
        instructionsPanel.setBackground(new Color(255, 250, 240)); // Floral White
        instructionsPanel.setBorder(BorderFactory.createLineBorder(new Color(139, 69, 19), 3));
        
        
        String[] instructions = {
            "ገበጣ በኢትዮጵያ የሚጫወት ባህላዊ የጨዋታ ሰሌዳ ነው።",
            "",
            "የጨዋታ ህጎች:",
            "• ጨዋታው በ12 ትንሽ ጉድጓዶች (ለእያንዳንዱ ተጫዋች 6) እና በ2 ትልልቅ መጋዘኖች ላይ ይጫወታል።",
            "• እያንዳንዱ ትንሽ ጉድጓድ በ4 ድንጋዮች ይጀምራል።",
            "• ተጫዋቾች ከእያንዳንዱ ጉድጓዶቻቸው ሁሉንም ድንጋዮች በመውሰድ ተራ ይለዋወጣሉ።",
            "• ድንጋዮች በአንድ በአንድ በተከታታይ ጉድጓዶች ውስጥ በተቃራኒ ሰዓት አቅጣጫ ይሰራጫሉ።",
            "• የመጨረሻው ድንጋይ በመጋዘንዎ ውስጥ ከተደረገ፣ ሌላ ተራ ያገኛሉ።",
            "• የመጨረሻው ድንጋይ በባዶ ጉድጓድ በጎንዎ ላይ ከተደረገ፣ ያንን ድንጋይ እና በተቃራኒው ጉድጓድ ውስጥ ያሉ ሁሉንም ድንጋዮች ያዛሉ።",
            "• ጨዋታው አንድ ተጫዋች በትንሽ ጉድጓዶቹ ውስጥ ድንጋይ ከሌለው ያበቃል።",
            "• በመጋዘኑ ውስጥ በጣም ብዙ ድንጋዮች ያሉት ተጫዋች ያሸንፋል!",
            "",
            "የዘመቻ ምክሮች:",
            "• በመጋዘንዎ ውስጥ በመድረስ ተጨማሪ ተራዎች ለማግኘት ይሞክሩ።",
            "• የተቃዋሚውን ድንጋዮች እንዲያዙ የሚያስችሉ እንቅስቃሴዎችን ያቅዱ።",
            "• የተቃዋሚውን እንቅስቃሴዎች ለመገመት የድንጋይ ስርጭቱን ያስተውሉ።"
        };
        
        for (String instruction : instructions) {
            JLabel label = new JLabel(instruction);
            label.setFont(new Font("Nyala", Font.PLAIN, 16));
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
        
        
        JButton videoButton = createStyledButton("የማስተማሪያ ቪዲዮ ይመልከቱ", new Color(255, 0, 0));
        videoButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        videoButton.setMaximumSize(new Dimension(250, 50));
        videoButton.setPreferredSize(new Dimension(250, 50));
        videoButton.addActionListener(e -> openYouTubeVideo());
        buttonPanel.add(videoButton);
        buttonPanel.add(Box.createVerticalStrut(20));
        
        
        JButton backButton = createStyledButton("ወደ ዝርዝር ተመለስ", new Color(139, 69, 19));
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
                "ቪዲዮ ሊከፈት አልቻለም። እባክዎ ይህን ይጎብኙ:\nhttps://www.youtube.com/watch?v=o5HaaipZ3EA", 
                "ስህተት", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private JButton createStyledButton(String text, Color backgroundColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Nyala", Font.BOLD, 16));
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