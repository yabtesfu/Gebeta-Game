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
        setLayout(new BorderLayout());
        setBackground(new Color(245, 245, 220));
        setupComponents();
        setupMouseListener();
    }
    
    private void setupComponents() {
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 20, 10));
        topPanel.setBackground(new Color(245, 245, 220));
        
        backButton = createStyledButton("Back to Menu", new Color(139, 69, 19));
        backButton.setPreferredSize(new Dimension(150, 40));
        backButton.addActionListener(e -> parent.showPanel("INTRO"));
        topPanel.add(backButton);
        
        newGameButton = createStyledButton("New Game", new Color(34, 139, 34));
        newGameButton.setPreferredSize(new Dimension(150, 40));
        newGameButton.addActionListener(e -> {
            gameBoard.resetGame();
            repaint();
        });
        topPanel.add(newGameButton);
        
        helpButton = createStyledButton("Help", new Color(70, 130, 180));
        helpButton.setPreferredSize(new Dimension(100, 40));
        helpButton.addActionListener(e -> parent.showPanel("HELP"));
        topPanel.add(helpButton);
        
        add(topPanel, BorderLayout.NORTH);
        
        JPanel gameBoardPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int panelWidth = getWidth();
                int panelHeight = getHeight();
                double scaleX = (double) panelWidth / 1200.0;
                double scaleY = (double) panelHeight / 800.0;
                double scale = Math.min(scaleX, scaleY) * 0.9;
                g2d.scale(scale, scale);
                int offsetX = (int) ((panelWidth / scale - 1200) / 2);
                int offsetY = (int) ((panelHeight / scale - 800) / 2);
                g2d.translate(offsetX, offsetY);
                gameBoard.draw(g2d);
                drawGameInfo(g2d);
            }
        };
        gameBoardPanel.setBackground(new Color(245, 245, 220));
        gameBoardPanel.setPreferredSize(new Dimension(1200, 800));
        gameBoardPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (gameBoard.isGameOver()) {
                    return;
                }
                Point convertedPoint = convertMouseToGameCoordinates(e.getPoint(), gameBoardPanel);
                if (convertedPoint != null) {
                    Pit clickedPit = gameBoard.getPitAt(convertedPoint.x, convertedPoint.y);
                    if (clickedPit != null) {
                        int pitIndex = gameBoard.getPitIndex(clickedPit);
                        boolean moveMade = gameBoard.makeMove(pitIndex);
                        if (moveMade) {
                            repaint();
                            if (gameBoard.isGameOver()) {
                                SwingUtilities.invokeLater(() -> {
                                    int choice = JOptionPane.showConfirmDialog(
                                        GamePanel.this,
                                        gameBoard.getWinner() + "\nDo you want to play again?",
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
                            JOptionPane.showMessageDialog(
                                GamePanel.this,
                                "Invalid move! Please select a valid pit.",
                                "Invalid Move",
                                JOptionPane.WARNING_MESSAGE
                            );
                        }
                    }
                }
            }
        });
        add(gameBoardPanel, BorderLayout.CENTER);
    }
    
    private void setupMouseListener() {
    }
    
    private Point convertMouseToGameCoordinates(Point mousePoint, JPanel gameBoardPanel) {
        int panelWidth = gameBoardPanel.getWidth();
        int panelHeight = gameBoardPanel.getHeight();
        if (panelWidth <= 0 || panelHeight <= 0) return null;
        double scaleX = (double) panelWidth / 1200.0;
        double scaleY = (double) panelHeight / 800.0;
        double scale = Math.min(scaleX, scaleY) * 0.9;
        int offsetX = (int) ((panelWidth / scale - 1200) / 2);
        int offsetY = (int) ((panelHeight / scale - 800) / 2);
        int gameX = (int) ((mousePoint.x - offsetX * scale) / scale);
        int gameY = (int) ((mousePoint.y - offsetY * scale) / scale);
        if (gameX < 0 || gameX > 1200 || gameY < 0 || gameY > 800) {
            return null;
        }
        return new Point(gameX, gameY);
    }
    
    private JButton createStyledButton(String text, Color backgroundColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 14));
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
    
    private void drawGameInfo(Graphics2D g2d) {
        g2d.setColor(new Color(139, 69, 19));
        g2d.setFont(new Font("Arial", Font.BOLD, 28));
        int currentPlayer = gameBoard.getCurrentPlayer();
        String playerText = "Current Player: " + (currentPlayer + 1);
        Color playerColor = currentPlayer == 0 ? new Color(34, 139, 34) : new Color(70, 130, 180);
        g2d.setColor(playerColor);
        g2d.setFont(new Font("Arial", Font.BOLD, 20));
        g2d.drawString(playerText, 800, 60);
        g2d.setColor(playerColor);
        g2d.fillOval(780, 45, 15, 15);
    }
    
    public void resetGame() {
        gameBoard.resetGame();
        repaint();
    }
    
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(1200, 800);
    }
} 