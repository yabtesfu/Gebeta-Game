import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class GamePanel extends JPanel {
    private Gebeta parent;
    private GameBoard gameBoard;
    private JButton backButton;
    private JButton newGameButton;
    private JButton helpButton;
    private JButton soundButton;

    // Game mode. The AI, when present, always plays as Player 1 (the top row);
    // the human is Player 0 and moves first.
    private static final int AI_PLAYER = 1;
    private boolean vsComputer;
    private int aiDepth;
    private MancalaAI ai;
    private boolean aiThinking;

    // Animation. Everything runs on the Swing event thread: a Timer sows one stone
    // per tick, so the UI never blocks and clicks are simply ignored while it plays.
    private static final int SOW_INTERVAL_MS = 170;
    private static final int MOVE_START_DELAY_MS = 220;
    private static final int AI_THINK_DELAY_MS = 320;
    private Timer sowTimer;
    private boolean animating;

    public GamePanel(Gebeta parent) {
        this.parent = parent;
        this.gameBoard = new GameBoard();
        setLayout(new BorderLayout());
        setBackground(new Color(245, 245, 220));
        setupComponents();
    }

    private void setupComponents() {
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 20, 10));
        topPanel.setBackground(new Color(245, 245, 220));

        backButton = createStyledButton("Back to Menu", new Color(139, 69, 19));
        backButton.setPreferredSize(new Dimension(150, 40));
        backButton.addActionListener(e -> {
            stopAnimation();
            parent.showPanel("INTRO");
        });
        topPanel.add(backButton);

        newGameButton = createStyledButton("New Game", new Color(34, 139, 34));
        newGameButton.setPreferredSize(new Dimension(150, 40));
        newGameButton.addActionListener(e -> {
            stopAnimation();
            aiThinking = false;
            gameBoard.resetGame();
            repaint();
        });
        topPanel.add(newGameButton);

        helpButton = createStyledButton("Help", new Color(70, 130, 180));
        helpButton.setPreferredSize(new Dimension(100, 40));
        helpButton.addActionListener(e -> parent.showPanel("HELP"));
        topPanel.add(helpButton);

        soundButton = createStyledButton(soundLabel(), new Color(128, 0, 128));
        soundButton.setPreferredSize(new Dimension(130, 40));
        soundButton.addActionListener(e -> {
            SoundPlayer.setMuted(!SoundPlayer.isMuted());
            soundButton.setText(soundLabel());
        });
        topPanel.add(soundButton);

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
                handleHumanClick(e, gameBoardPanel);
            }
        });
        add(gameBoardPanel, BorderLayout.CENTER);
    }

    private void handleHumanClick(MouseEvent e, JPanel gameBoardPanel) {
        // Ignore clicks while a move is animating, the computer is thinking, the game
        // is over, or it is the AI's turn.
        if (animating || aiThinking || gameBoard.isGameOver()) {
            return;
        }
        if (vsComputer && gameBoard.getCurrentPlayer() == AI_PLAYER) {
            return;
        }

        Point convertedPoint = convertMouseToGameCoordinates(e.getPoint(), gameBoardPanel);
        if (convertedPoint == null) {
            return;
        }
        Pit clickedPit = gameBoard.getPitAt(convertedPoint.x, convertedPoint.y);
        if (clickedPit == null) {
            return;
        }

        int pitIndex = gameBoard.getPitIndex(clickedPit);
        MoveTrace trace = gameBoard.makeMove(pitIndex);
        if (trace == null) {
            JOptionPane.showMessageDialog(
                GamePanel.this,
                "Invalid move! Please select a valid pit.",
                "Invalid Move",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }
        animateMove(trace, this::afterMove);
    }

    /**
     * Plays a move out one stone at a time on a Swing Timer, then runs {@code onDone}.
     * The rules engine has already applied the move; this only drives the visuals.
     */
    private void animateMove(MoveTrace trace, Runnable onDone) {
        animating = true;
        gameBoard.setAnimating(true);
        gameBoard.visualPickUp(trace.source);
        SoundPlayer.playPickup();
        repaint();

        final int[] i = {0};
        sowTimer = new Timer(SOW_INTERVAL_MS, null);
        sowTimer.setInitialDelay(MOVE_START_DELAY_MS);
        sowTimer.addActionListener(ev -> {
            if (i[0] < trace.drops.length) {
                gameBoard.visualDrop(trace.drops[i[0]]);
                SoundPlayer.playDrop();
                i[0]++;
                repaint();
                return;
            }
            // All stones sown — finish with the capture (if any) and reconcile.
            sowTimer.stop();
            sowTimer = null;
            if (trace.captured) {
                gameBoard.visualCapture(trace.captureLandingPit, trace.captureOppositePit,
                        trace.captureStore, trace.capturedTotal);
                SoundPlayer.playCapture();
            }
            gameBoard.syncVisuals(); // covers end-of-game stone collection
            gameBoard.setAnimating(false);
            animating = false;
            repaint();
            if (onDone != null) {
                onDone.run();
            }
        });
        sowTimer.start();
    }

    /** Called once a move's animation completes: end the game or hand off to the AI. */
    private void afterMove() {
        if (gameBoard.isGameOver()) {
            SoundPlayer.playGameOver();
            showGameOverDialog();
        } else if (vsComputer && gameBoard.getCurrentPlayer() == AI_PLAYER) {
            triggerAiTurn();
        }
    }

    /**
     * Has the computer choose and play a move. After a short "thinking" pause it picks
     * a move, then animates it; {@link #afterMove()} re-enters here so the AI keeps
     * playing while it holds extra turns. All of this stays on the event thread.
     */
    private void triggerAiTurn() {
        if (ai == null) {
            return;
        }
        aiThinking = true;
        repaint();

        Timer think = new Timer(AI_THINK_DELAY_MS, null);
        think.setRepeats(false);
        think.addActionListener(e -> {
            int move = ai.chooseMove(gameBoard.getStateCopy());
            aiThinking = false;
            if (move < 0) {
                repaint();
                return;
            }
            MoveTrace trace = gameBoard.makeMove(move);
            if (trace == null) {
                repaint();
                return;
            }
            animateMove(trace, this::afterMove);
        });
        think.start();
    }

    private void stopAnimation() {
        if (sowTimer != null) {
            sowTimer.stop();
            sowTimer = null;
        }
        animating = false;
        gameBoard.setAnimating(false);
    }

    private void showGameOverDialog() {
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

    private String soundLabel() {
        return SoundPlayer.isMuted() ? "Sound: Off" : "Sound: On";
    }

    private void drawGameInfo(Graphics2D g2d) {
        int currentPlayer = gameBoard.getCurrentPlayer();
        Color playerColor = currentPlayer == 0 ? new Color(34, 139, 34) : new Color(70, 130, 180);

        String playerText;
        if (aiThinking) {
            playerText = "Computer is thinking...";
        } else if (vsComputer) {
            playerText = (currentPlayer == AI_PLAYER) ? "Computer's turn" : "Your turn";
        } else {
            playerText = "Current Player: " + (currentPlayer + 1);
        }

        g2d.setColor(playerColor);
        g2d.setFont(new Font("Arial", Font.BOLD, 20));
        g2d.drawString(playerText, 800, 60);
        g2d.fillOval(780, 45, 15, 15);

        if (vsComputer) {
            g2d.setColor(new Color(139, 69, 19));
            g2d.setFont(new Font("Arial", Font.PLAIN, 16));
            g2d.drawString("Mode: vs Computer (" + difficultyLabel() + ")", 800, 90);
        }
    }

    private String difficultyLabel() {
        if (aiDepth <= 2) return "Easy";
        if (aiDepth <= 6) return "Medium";
        return "Hard";
    }

    /** Configures the mode and starts a fresh game. Called from the intro menu. */
    public void startGame(boolean vsComputer, int aiDepth) {
        stopAnimation();
        this.vsComputer = vsComputer;
        this.aiDepth = aiDepth;
        this.ai = vsComputer ? new MancalaAI(aiDepth, AI_PLAYER) : null;
        this.aiThinking = false;
        gameBoard.resetGame();
        repaint();
    }

    public void resetGame() {
        stopAnimation();
        aiThinking = false;
        gameBoard.resetGame();
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(1200, 800);
    }
}
