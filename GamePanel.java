import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class GamePanel extends JPanel {
    private Gebeta parent;
    private GameBoard gameBoard;
    private JButton backButton;
    private JButton newGameButton;
    private JButton helpButton;
    
    public GamePanel(Gebeta parent) {
        this.parent = parent;
        this.gameBoard = new GameBoard();
        setLayout(null);
        setBackground(new Color(245, 245, 220)); // Beige background
        setupComponents();
        setupMouseListener();
    }
    
    private void setupComponents() {
        // Back to Menu Button
        backButton = createStyledButton("Back to Menu", new Color(139, 69, 19));
        backButton.setBounds(50, 50, 150, 40);
        backButton.addActionListener(e -> parent.showPanel("INTRO"));
        add(backButton);
        
        // New Game Button
        newGameButton = createStyledButton("New Game", new Color(34, 139, 34));
        newGameButton.setBounds(220, 50, 150, 40);
        newGameButton.addActionListener(e -> {
            gameBoard.resetGame();
            repaint();
        });
        add(newGameButton);
        
        // Help Button
        helpButton = createStyledButton("Help", new Color(70, 130, 180));
        helpButton.setBounds(390, 50, 100, 40);
        helpButton.addActionListener(e -> parent.showPanel("HELP"));
        add(helpButton);
    }
    
    private void setupMouseListener() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (gameBoard.isGameOver()) {
                    return;
                }
                
                Pit clickedPit = gameBoard.getPitAt(e.getX(), e.getY());
                if (clickedPit != null) {
                    int pitIndex = gameBoard.getPitIndex(clickedPit);
                    boolean moveMade = gameBoard.makeMove(pitIndex);
                    
                    if (moveMade) {
                        repaint();
                        
                        // Check if game is over after the move
                        if (gameBoard.isGameOver()) {
                            SwingUtilities.invokeLater(() -> {
                                int choice = JOptionPane.showConfirmDialog(
                                    GamePanel.this,
                                    gameBoard.getWinner() + "\nWould you like to play again?",
                                    "Game Over",
                                    JOptionPane.YES_NO_OPTION
                                );
                                if (choice == JOptionPane.YES_OPTION) {
                                    gameBoard.resetGame();
                                    repaint();
                                }
                            });
                        }
                    } else {
                        // Invalid move
                        JOptionPane.showMessageDialog(
                            GamePanel.this,
                            "Invalid move! Please select one of your non-empty pits.",
                            "Invalid Move",
                            JOptionPane.WARNING_MESSAGE
                        );
                    }
                }
            }
        });
    }
    
    private JButton createStyledButton(String text, Color backgroundColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 14));
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
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Draw the game board
        gameBoard.draw(g2d);
        
        // Draw additional UI elements
        drawGameInfo(g2d);
    }
    
    private void drawGameInfo(Graphics2D g2d) {
        // Draw game title
        g2d.setColor(new Color(139, 69, 19)); // Saddle Brown
        g2d.setFont(new Font("Arial Black", Font.BOLD, 28));
        g2d.drawString("GEBETA - Traditional Mancala", 400, 30);
        
        // Draw current player with enhanced styling
        int currentPlayer = gameBoard.getCurrentPlayer();
        String playerText = "Current Player: " + (currentPlayer + 1);
        Color playerColor = currentPlayer == 0 ? new Color(34, 139, 34) : new Color(70, 130, 180);
        g2d.setColor(playerColor);
        g2d.setFont(new Font("Arial", Font.BOLD, 20));
        g2d.drawString(playerText, 800, 60);
        
        // Draw player turn indicator
        g2d.setColor(playerColor);
        g2d.fillOval(780, 45, 15, 15);
    }
    
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(1200, 800);
    }
} 