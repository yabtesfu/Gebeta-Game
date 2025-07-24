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
    private int bgVariant;

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
        setOpaque(true);
        setupComponents();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        BackgroundManager.paint((Graphics2D) g, getWidth(), getHeight(), bgVariant);
    }

    private void setupComponents() {
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 16, 12));
        topPanel.setOpaque(false);

        backButton = ThemedButton.subtle("‹  Menu");
        backButton.setPreferredSize(new Dimension(130, 44));
        backButton.addActionListener(e -> {
            stopAnimation();
            parent.showPanel("INTRO");
        });
        topPanel.add(backButton);

        newGameButton = ThemedButton.primary("New Game");
        newGameButton.setPreferredSize(new Dimension(150, 44));
        newGameButton.addActionListener(e -> {
            stopAnimation();
            aiThinking = false;
            gameBoard.resetGame();
            repaint();
        });
        topPanel.add(newGameButton);

        helpButton = ThemedButton.subtle("How to Play");
        helpButton.setPreferredSize(new Dimension(170, 44));
        helpButton.addActionListener(e -> parent.showPanel("HELP"));
        topPanel.add(helpButton);

        soundButton = ThemedButton.secondary(soundLabel());
        soundButton.setPreferredSize(new Dimension(175, 44));
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
                double scale = Math.min(scaleX, scaleY) * 0.94;
                g2d.scale(scale, scale);
                int offsetX = (int) ((panelWidth / scale - 1200) / 2);
                int offsetY = (int) ((panelHeight / scale - 800) / 2);
                g2d.translate(offsetX, offsetY);
                drawGameInfo(g2d);
                gameBoard.draw(g2d);
            }
        };
        gameBoardPanel.setOpaque(false);
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
            return; // not a legal pit to pick up — ignore the click quietly
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
        boolean again = ThemedDialog.confirm(
            GamePanel.this,
            "Game Over",
            gameBoard.getWinner() + "\n\nWould you like to play again?",
            "Play Again",
            "Close"
        );
        if (again) {
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
        double scale = Math.min(scaleX, scaleY) * 0.94;
        int offsetX = (int) ((panelWidth / scale - 1200) / 2);
        int offsetY = (int) ((panelHeight / scale - 800) / 2);
        int gameX = (int) ((mousePoint.x - offsetX * scale) / scale);
        int gameY = (int) ((mousePoint.y - offsetY * scale) / scale);
        if (gameX < 0 || gameX > 1200 || gameY < 0 || gameY > 800) {
            return null;
        }
        return new Point(gameX, gameY);
    }

    private String soundLabel() {
        return SoundPlayer.isMuted() ? "♪ Sound: Off" : "♪ Sound: On";
    }

    // ---- heads-up display drawn in the 1200x800 board space (scales with the board) ----

    private void drawGameInfo(Graphics2D g2d) {
        int current = gameBoard.getCurrentPlayer();
        boolean p1Active = current == 0;
        boolean p2Active = current == 1;

        String p1Name = vsComputer ? "You" : "Player 1";
        String p2Name = vsComputer ? "Computer" : "Player 2";
        drawPlayerCard(g2d, 60, 40, p1Name, gameBoard.getStoreScore(0), Theme.ETH_GREEN, p1Active);
        drawPlayerCard(g2d, 840, 40, p2Name, gameBoard.getStoreScore(1), Theme.TERRACOTTA, p2Active);

        drawStatusPill(g2d, current);
    }

    private void drawPlayerCard(Graphics2D g2d, int x, int y, String name, int score,
                                Color accent, boolean active) {
        int w = 300;
        int h = 96;
        if (active) {
            // Warm outer glow around the player whose turn it is.
            for (int i = 3; i >= 1; i--) {
                g2d.setColor(new Color(Theme.GOLD_LIGHT.getRed(), Theme.GOLD_LIGHT.getGreen(),
                        Theme.GOLD_LIGHT.getBlue(), 26));
                g2d.fillRoundRect(x - i * 4, y - i * 4, w + i * 8, h + i * 8, 30 + i * 4, 30 + i * 4);
            }
        }
        Theme.drawCard(g2d, x, y, w, h, 26, Theme.SCRIM);

        // Avatar token — a gold ring with the player's accent colour.
        int av = 64;
        int ax = x + 18;
        int ay = y + (h - av) / 2;
        g2d.setColor(accent.darker());
        g2d.fillOval(ax, ay, av, av);
        g2d.setStroke(new BasicStroke(3f));
        g2d.setColor(active ? Theme.GOLD_LIGHT : Theme.GOLD);
        g2d.drawOval(ax, ay, av, av);
        g2d.setColor(Theme.CREAM);
        g2d.setFont(Theme.display(30));
        String token = name.equals("Computer") ? "AI" : name.substring(0, 1);
        Theme.drawCentered(g2d, token, ax + av / 2, ay + av / 2 + 11);

        // Name + score.
        int tx = ax + av + 16;
        g2d.setColor(Theme.CREAM);
        g2d.setFont(Theme.heading(22));
        g2d.drawString(name, tx, y + 40);
        g2d.setColor(Theme.GOLD_LIGHT);
        g2d.setFont(Theme.display(30));
        g2d.drawString(String.valueOf(score), tx, y + 78);
        g2d.setColor(Theme.PARCHMENT_DARK);
        g2d.setFont(Theme.body(14));
        g2d.drawString("stones", tx + g2d.getFontMetrics(Theme.display(30)).stringWidth(
                String.valueOf(score)) + 8, y + 78);
    }

    private void drawStatusPill(Graphics2D g2d, int current) {
        int w = 380;
        int h = 70;
        int x = 600 - w / 2;
        int y = 44;

        String status;
        if (aiThinking) {
            status = "Computer is thinking…";
        } else if (vsComputer) {
            status = (current == AI_PLAYER) ? "Computer's turn" : "Your turn";
        } else {
            status = "Player " + (current + 1) + "'s turn";
        }

        Theme.drawCard(g2d, x, y, w, h, 34, Theme.SCRIM_LIGHT);
        g2d.setColor(Theme.CREAM);
        g2d.setFont(Theme.heading(24));
        Theme.drawCentered(g2d, status, 600, y + 34);

        String sub = vsComputer ? ("Playing the Computer  •  " + difficultyLabel())
                : "Two Players  •  Local";
        g2d.setColor(Theme.PARCHMENT_DARK);
        g2d.setFont(Theme.body(14));
        Theme.drawCentered(g2d, sub, 600, y + 56);
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
        this.bgVariant = (bgVariant + 1) % Math.max(1, BackgroundManager.variantCount() * 3);
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
