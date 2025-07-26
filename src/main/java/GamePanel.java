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
    private final GamePersistence persistence;

    // Game mode. The AI, when present, always plays as Player 1 (the top row);
    // the human is Player 0 and moves first.
    private static final int AI_PLAYER = 1;
    private boolean vsComputer;
    private int aiDepth;
    private MancalaAI ai;
    private boolean aiThinking;

    // Animation uses the Swing event thread; AI search runs in a cancellable worker so
    // painting, sound, navigation and window controls stay responsive on Hard mode.
    private static final int SOW_INTERVAL_MS = 170;
    private static final int MOVE_START_DELAY_MS = 220;
    private static final int AI_THINK_DELAY_MS = 320;
    private Timer sowTimer;
    private Timer thinkTimer;
    private SwingWorker<Integer, Void> aiWorker;
    private int aiSearchGeneration;
    private boolean animating;
    private boolean resultRecorded;

    public GamePanel(Gebeta parent, GamePersistence persistence) {
        this.parent = parent;
        this.persistence = persistence;
        this.gameBoard = new GameBoard();
        setLayout(new BorderLayout());
        setOpaque(true);
        setupComponents();
    }

    /** Retained for developer preview tools. The application injects its shared store. */
    public GamePanel(Gebeta parent) {
        this(parent, GamePersistence.transientPersistence());
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        BackgroundManager.paintGame((Graphics2D) g, getWidth(), getHeight());
    }

    private void setupComponents() {
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        topPanel.setBorder(BorderFactory.createEmptyBorder(14, 24, 4, 24));

        // Title on the left.
        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("ገበጣ  Gebeta");
        title.setFont(Theme.display(30));
        title.setForeground(Theme.CREAM);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel subtitle = new JLabel("SOW COUNTERCLOCKWISE");
        subtitle.setFont(Theme.body(12));
        subtitle.setForeground(Theme.PARCHMENT_DARK);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        titlePanel.add(title);
        titlePanel.add(Box.createVerticalStrut(4));
        titlePanel.add(subtitle);
        topPanel.add(titlePanel, BorderLayout.WEST);

        // Buttons on the right.
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 8));
        buttons.setOpaque(false);

        newGameButton = ThemedButton.primary("New Game");
        newGameButton.setPreferredSize(new Dimension(150, 44));
        newGameButton.addActionListener(e -> {
            startFreshGame();
        });
        buttons.add(newGameButton);

        helpButton = ThemedButton.subtle("How to Play");
        helpButton.setPreferredSize(new Dimension(160, 44));
        helpButton.addActionListener(e -> parent.showPanel("HELP"));
        buttons.add(helpButton);

        soundButton = ThemedButton.secondary(soundLabel());
        soundButton.setPreferredSize(new Dimension(160, 44));
        soundButton.addActionListener(e -> {
            SoundPlayer.setMuted(!SoundPlayer.isMuted());
            soundButton.setText(soundLabel());
        });
        buttons.add(soundButton);

        backButton = ThemedButton.subtle("‹  Menu");
        backButton.setPreferredSize(new Dimension(120, 44));
        backButton.addActionListener(e -> {
            stopAnimation();
            parent.showPanel("INTRO");
        });
        buttons.add(backButton);

        topPanel.add(buttons, BorderLayout.EAST);
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
            recordCompletedGame();
            SoundPlayer.playGameOver();
            showGameOverDialog();
        } else {
            persistCurrentGame();
            if (vsComputer && gameBoard.getCurrentPlayer() == AI_PLAYER) {
                triggerAiTurn();
            }
        }
    }

    /**
     * Has the computer choose and play a move. After a short "thinking" pause it picks
     * a move on a worker thread, then animates it; {@link #afterMove()} re-enters here
     * so the AI keeps playing while it holds extra turns.
     */
    private void triggerAiTurn() {
        if (ai == null) {
            return;
        }
        aiThinking = true;
        repaint();

        MancalaState snapshot = gameBoard.getStateCopy();
        int generation = ++aiSearchGeneration;

        thinkTimer = new Timer(AI_THINK_DELAY_MS, null);
        thinkTimer.setRepeats(false);
        thinkTimer.addActionListener(e -> {
            thinkTimer = null;
            if (generation != aiSearchGeneration) return;
            aiWorker = new SwingWorker<>() {
                @Override
                protected Integer doInBackground() {
                    return ai.chooseMove(snapshot);
                }

                @Override
                protected void done() {
                    if (generation != aiSearchGeneration || isCancelled()) return;
                    aiWorker = null;
                    aiThinking = false;
                    try {
                        int move = get();
                        if (move < 0 || gameBoard.isGameOver()
                                || gameBoard.getCurrentPlayer() != AI_PLAYER) {
                            repaint();
                            return;
                        }
                        MoveTrace trace = gameBoard.makeMove(move);
                        if (trace == null) {
                            repaint();
                            return;
                        }
                        animateMove(trace, GamePanel.this::afterMove);
                    } catch (Exception searchFailedOrCancelled) {
                        repaint();
                    }
                }
            };
            aiWorker.execute();
        });
        thinkTimer.start();
    }

    private void stopAnimation() {
        aiSearchGeneration++;
        if (sowTimer != null) {
            sowTimer.stop();
            sowTimer = null;
        }
        if (thinkTimer != null) {
            thinkTimer.stop();
            thinkTimer = null;
        }
        if (aiWorker != null) {
            aiWorker.cancel(true);
            aiWorker = null;
        }
        aiThinking = false;
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
            startFreshGame();
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
        // Human input is live only on the human's turn, when nothing is animating.
        boolean canInput = !animating && !aiThinking && !gameBoard.isGameOver()
                && (!vsComputer || current == 0);
        gameBoard.setInputActive(canInput);

        // Left card = Player 1 / Computer (orange); right card = Player 0 / You (green).
        String p1Name = vsComputer ? "Computer" : "Player 2";
        String p0Name = vsComputer ? "You" : "Player 1";
        drawPlayerCard(g2d, 70, p1Name, vsComputer ? "AI" : "2", gameBoard.getStoreScore(1),
                Theme.AI_ORANGE, Theme.AI_ORANGE_D, current == 1);
        drawPlayerCard(g2d, 830, p0Name, vsComputer ? "You" : "1", gameBoard.getStoreScore(0),
                Theme.YOU_GREEN, Theme.YOU_GREEN_D, current == 0);

        drawStatusPill(g2d, current, canInput);
    }

    private void drawPlayerCard(Graphics2D g2d, int x, String name, String token, int score,
                                Color accent, Color accentDark, boolean active) {
        int y = 44;
        int w = 300;
        int h = 92;
        if (active) {
            for (int i = 3; i >= 1; i--) {
                g2d.setColor(new Color(Theme.GOLD.getRed(), Theme.GOLD.getGreen(),
                        Theme.GOLD.getBlue(), 30));
                g2d.fillRoundRect(x - i * 4, y - i * 4, w + i * 8, h + i * 8, 28 + i * 4, 28 + i * 4);
            }
        }
        Theme.drawCard(g2d, x, y, w, h, 24, Theme.COFFEE);

        // Avatar token.
        int av = 56;
        int ax = x + 20;
        int ay = y + (h - av) / 2;
        g2d.setPaint(new RadialGradientPaint(new Point(ax + av / 3, ay + av / 3), av,
                new float[]{0f, 1f}, new Color[]{accent, accentDark}));
        g2d.fillOval(ax, ay, av, av);
        g2d.setStroke(new BasicStroke(2f));
        g2d.setColor(new Color(0, 0, 0, 90));
        g2d.drawOval(ax, ay, av, av);
        g2d.setColor(Theme.CREAM);
        g2d.setFont(Theme.heading(token.length() > 1 ? 18 : 22));
        Theme.drawCentered(g2d, token, ax + av / 2, ay + av / 2 + 7);

        // Name + banked score.
        int tx = ax + av + 16;
        g2d.setColor(Theme.CREAM);
        g2d.setFont(Theme.heading(21));
        g2d.drawString(name, tx, y + 40);
        g2d.setColor(Theme.GOLD);
        g2d.setFont(Theme.display(28));
        String s = String.valueOf(score);
        g2d.drawString(s, tx, y + 74);
        g2d.setColor(Theme.PARCHMENT_DARK);
        g2d.setFont(Theme.body(13));
        int sw = g2d.getFontMetrics(Theme.display(28)).stringWidth(s);
        g2d.drawString("banked", tx + sw + 8, y + 74);
    }

    private void drawStatusPill(Graphics2D g2d, int current, boolean yourTurn) {
        String status;
        if (gameBoard.isGameOver()) {
            status = "Game over";
        } else if (aiThinking) {
            status = "Computer is thinking…";
        } else if (vsComputer) {
            status = (current == AI_PLAYER) ? "Computer's turn"
                    : "Your turn, pick a glowing pit";
        } else {
            status = "Player " + (current + 1) + "'s turn";
        }

        g2d.setFont(Theme.display(20));
        int tw = g2d.getFontMetrics().stringWidth(status);
        int w = Math.max(300, tw + 56);
        int h = 54;
        int x = 600 - w / 2;
        int y = 63;

        boolean highlight = yourTurn && !gameBoard.isGameOver();
        g2d.setColor(Theme.COFFEE);
        g2d.fillRoundRect(x, y, w, h, h, h);
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.setColor(highlight ? new Color(229, 180, 90, 160) : new Color(75, 54, 32));
        g2d.drawRoundRect(x, y, w, h, h, h);

        g2d.setColor(highlight ? Theme.GOLD : new Color(201, 174, 134));
        Theme.drawCentered(g2d, status, 600, y + 35);
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
        this.resultRecorded = false;
        gameBoard.setPlayerLabels(vsComputer ? "You" : "Player 1",
                vsComputer ? "Computer" : "Player 2");
        gameBoard.resetGame();
        persistCurrentGame();
        repaint();
    }

    private void startFreshGame() {
        stopAnimation();
        resultRecorded = false;
        gameBoard.resetGame();
        persistCurrentGame();
        repaint();
    }

    /** Loads a validated snapshot and continues the AI turn when necessary. */
    public void resumeGame(GamePersistence.SavedGame saved) {
        stopAnimation();
        this.vsComputer = saved.vsComputer();
        this.aiDepth = saved.aiDepth();
        this.ai = vsComputer ? new MancalaAI(aiDepth, AI_PLAYER) : null;
        this.resultRecorded = false;
        gameBoard.setPlayerLabels(vsComputer ? "You" : "Player 1",
                vsComputer ? "Computer" : "Player 2");
        gameBoard.restoreGame(saved.board(), saved.currentPlayer());
        repaint();
        if (vsComputer && gameBoard.getCurrentPlayer() == AI_PLAYER) {
            SwingUtilities.invokeLater(this::triggerAiTurn);
        }
    }

    private void persistCurrentGame() {
        persistence.save(gameBoard.getStateCopy(), vsComputer, aiDepth);
    }

    private void recordCompletedGame() {
        if (resultRecorded) return;
        resultRecorded = true;
        MancalaState state = gameBoard.getStateCopy();
        persistence.recordResult(vsComputer, state.scoreOf(0), state.scoreOf(1));
        persistence.clearSavedGame();
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(1200, 800);
    }
}
