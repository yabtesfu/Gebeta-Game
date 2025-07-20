import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class GameBoard {
    private List<Pit> pits;
    private int currentPlayer; // 0 for player 1, 1 for player 2
    private boolean gameOver;
    private String winner;
    
    public GameBoard() {
        pits = new ArrayList<>();
        currentPlayer = 0;
        gameOver = false;
        initializeBoard();
    }
    
    private void initializeBoard() {
        pits.clear();

        for (int i = 0; i < 6; i++) {
            pits.add(new Pit(200 + i * 120, 400, 100, 100, false, 0));
        }

        
        pits.add(new Pit(1070, 200, 80, 300, true, 0));

      
        for (int i = 5; i >= 0; i--) {
            pits.add(new Pit(200 + i * 120, 200, 100, 100, false, 1));
        }

        pits.add(new Pit(50, 200, 80, 300, true, 1));

        
        for (Pit pit : pits) {
            if (!pit.isStore()) {
                for (int i = 0; i < 4; i++) {
                    pit.addStone(new Stone(0, 0));
                }
            }
        }
    }
    
    public boolean makeMove(int pitIndex) {
        if (gameOver) return false;
        Pit selectedPit = pits.get(pitIndex);
        if (selectedPit.getPlayer() != currentPlayer || selectedPit.isEmpty() || selectedPit.isStore()) {
            return false;
        }

        int opponentStoreIndex = (currentPlayer == 0) ? 13 : 6;
        List<Stone> stones = selectedPit.removeAllStones();
        int stoneCount = stones.size();
        int currentIndex = pitIndex;

       
        for (int i = 0; i < stoneCount; i++) {
            currentIndex = (currentIndex + 1) % 14;
            if (currentIndex == opponentStoreIndex) {
                currentIndex = (currentIndex + 1) % 14;
            }
        }
        
        
        int dropIndex = pitIndex;
        for (Stone stone : stones) {
            dropIndex = (dropIndex + 1) % 14;
            if (dropIndex == opponentStoreIndex) {
                dropIndex = (dropIndex + 1) % 14;
            }
            pits.get(dropIndex).addStone(stone);
        }

        Pit lastPit = pits.get(dropIndex);

        
        if (!lastPit.isStore() && lastPit.getPlayer() == currentPlayer && lastPit.getStoneCount() == 1) {
            int oppositeIndex = getOppositePitIndex(dropIndex);
            if (oppositeIndex != -1) {
                Pit oppositePit = pits.get(oppositeIndex);
                if (!oppositePit.isEmpty()) {
                    List<Stone> capturedStones = oppositePit.removeAllStones();
                    List<Stone> capturingStones = lastPit.removeAllStones();
                    Pit store = getPlayerStore(currentPlayer);
                    for (Stone stone : capturedStones) store.addStone(stone);
                    for (Stone stone : capturingStones) store.addStone(stone);
                }
            }
        }

        
        boolean extraTurn = lastPit.isStore() && lastPit.getPlayer() == currentPlayer;
        checkGameOver();
        if (!gameOver && !extraTurn) {
            currentPlayer = (currentPlayer + 1) % 2;
        }
        return true;
    }
    
    private int getNextPitIndex(int currentIndex) {
        return (currentIndex + 1) % 14;
    }
    
    private int getOppositePitIndex(int pitIndex) {
        if (pitIndex >= 0 && pitIndex <= 5) {
            return 12 - pitIndex;
        } else if (pitIndex >= 7 && pitIndex <= 12) { 
            return 12 - pitIndex;
        }
        return -1;
    }
    
    private Pit getPlayerStore(int player) {
        return pits.get(player == 0 ? 6 : 13);
    }
    
    private void checkGameOver() {
        boolean player1HasStones = false;
        boolean player2HasStones = false;
        
        for (Pit pit : pits) {
            if (!pit.isStore()) {
                if (pit.getPlayer() == 0 && !pit.isEmpty()) {
                    player1HasStones = true;
                } else if (pit.getPlayer() == 1 && !pit.isEmpty()) {
                    player2HasStones = true;
                }
            }
        }
        
        if (!player1HasStones || !player2HasStones) {
            gameOver = true;
            
            // Collect remaining stones
            for (Pit pit : pits) {
                if (!pit.isStore()) {
                    List<Stone> remainingStones = pit.removeAllStones();
                    Pit store = getPlayerStore(pit.getPlayer());
                    for (Stone stone : remainingStones) {
                        store.addStone(stone);
                    }
                }
            }
            
           
            int player1Score = getPlayerStore(0).getStoneCount();
            int player2Score = getPlayerStore(1).getStoneCount();
            
            if (player1Score > player2Score) {
                winner = "ተጫዋች 1 አሸንፏል!";
            } else if (player2Score > player1Score) {
                winner = "ተጫዋች 2 አሸንፏል!";
            } else {
                winner = "እኩል ናቸው!";
            }
        }
    }
    
    public void draw(Graphics2D g2d) {
        
        g2d.setColor(new Color(245, 245, 220));
        g2d.fillRect(0, 0, 1200, 800);
        
        
        g2d.setColor(new Color(139, 69, 19));
        g2d.setStroke(new BasicStroke(5));
        g2d.drawRect(40, 180, 1120, 390);
        
        for (Pit pit : pits) {
            pit.draw(g2d);
        }
        
       
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Nyala", Font.BOLD, 16));
        FontMetrics fm = g2d.getFontMetrics();

      
        Pit player1Store = getPlayerStore(0);
        String p1Score = "ውጤት: " + player1Store.getStoneCount();
        int p1x = player1Store.getX() + (player1Store.getWidth() - fm.stringWidth(p1Score)) / 2;
        int p1y = player1Store.getY() + player1Store.getHeight() / 2;
        g2d.drawString(p1Score, p1x, p1y);

        
        Pit player2Store = getPlayerStore(1);
        String p2Score = "ውጤት: " + player2Store.getStoneCount();
        int p2x = player2Store.getX() + (player2Store.getWidth() - fm.stringWidth(p2Score)) / 2;
        int p2y = player2Store.getY() + player2Store.getHeight() / 2;
        g2d.drawString(p2Score, p2x, p2y);
        
       
        if (gameOver) {
            g2d.setColor(new Color(0, 0, 0, 150));
            g2d.fillRect(0, 0, 1200, 800);
            
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Nyala", Font.BOLD, 36));
            int textX = 600 - fm.stringWidth(winner) / 2;
            int textY = 400;
            g2d.drawString(winner, textX, textY);
        }
    }
    
    public Pit getPitAt(int x, int y) {
        for (Pit pit : pits) {
            if (pit.contains(x, y)) {
                return pit;
            }
        }
        return null;
    }
    
    public int getPitIndex(Pit pit) {
        return pits.indexOf(pit);
    }
    
    public boolean isGameOver() {
        return gameOver;
    }
    
    public String getWinner() {
        return winner;
    }
    
    public int getCurrentPlayer() {
        return currentPlayer;
    }
    
    public void resetGame() {
        pits.clear();
        currentPlayer = 0;
        gameOver = false;
        winner = null;
        initializeBoard();
    }
} 