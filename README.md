# Gebeta - Traditional Ethiopian Mancala Game

A Java GUI implementation of the traditional Ethiopian board game Gebeta (Mancala), created as an Object-Oriented Programming project.

## About the Developer

**Made by:** Yabetse Tesfaye  
**Institution:** Addis Ababa Institute of Technology  
**Program:** Software Engineer 
**Year:** 5th Year Student  
**ID:** UGR/31352/15

## Game Features

- **Traditional Gebeta Rules:** Complete implementation of the classic Gebeta game mechanics
- **Two game modes:**
  - **Two Players** — local hotseat play
  - **Play vs Computer** — an AI opponent with **Easy / Medium / Hard** difficulty
- **AI opponent:** A computer player built on the **minimax algorithm with alpha-beta pruning**.
  It looks several moves ahead, correctly handles Gebeta's "extra turn" rule, and gets
  stronger as difficulty increases (search depth 1 → 5 → 9). The AI runs on a background
  thread so the interface stays responsive.
- **Beautiful GUI:** Aesthetic board design with colorful stones and intuitive interface
- **Multiple Panels:**
  - Intro page with background image
  - Game board with full functionality
  - About page with developer information
  - Help page with game instructions and tutorial video link
- **Enhanced User Experience:** Hover effects, "Computer is thinking…" status, responsive design
- **Game Management:** New game, reset, and navigation features
- **Unit-tested rules engine:** A dependency-free test suite verifies the game rules

## How to Compile and Run

### Prerequisites

- Java Development Kit (JDK) 8 or higher
- The background image file: `Background Image.png` (must be in the same directory)

### Compilation

```bash
javac *.java
```

### Running the Game

```bash
java Gebeta
```

## Game Rules

1. **Setup:** Each small pit starts with 4 stones
2. **Turns:** Players take turns picking up all stones from one of their pits
3. **Distribution:** Stones are distributed one by one into subsequent pits counter-clockwise
4. **Extra Turns:** If the last stone lands in your store, you get another turn
5. **Capture:** If the last stone lands in an empty pit on your side, you capture that stone and all stones in the opposite pit
6. **Game End:** The game ends when one player has no stones left in their small pits
7. **Winner:** The player with the most stones in their store wins

## Controls

- **Mouse Click:** Click on your pits to make moves
- **New Game:** Start a fresh game
- **Help:** Access game instructions and tutorial video
- **Back to Menu:** Return to the main menu

## Running the Tests

The rules engine has its own test suite that needs no external libraries — just a JDK:

```bash
javac MancalaState.java MancalaStateTest.java
java MancalaStateTest
```

It exercises sowing, captures, extra turns, illegal-move rejection, and end-of-game
collection, and exits non-zero if any check fails (so it works as a CI gate).

## File Structure

**Rules & AI (pure logic, no UI dependency):**
- `MancalaState.java` - The complete game rules as a plain `int[14]` board. No Swing
  imports, which is what makes it testable and lets the AI simulate positions freely.
- `MancalaAI.java` - Computer opponent using minimax with alpha-beta pruning.
- `MancalaStateTest.java` - Dependency-free unit tests for the rules engine.

**User interface (Swing):**
- `Gebeta.java` - Main application class and screen navigation
- `IntroPanel.java` - Introduction screen and mode/difficulty selection
- `GamePanel.java` - Game interface, input handling, and AI turn orchestration
- `AboutPanel.java` - Developer information
- `HelpPanel.java` - Game instructions and help
- `GameBoard.java` - Bridges the rules engine to the on-screen board and stones
- `Pit.java` - Visual pit (geometry + drawing)
- `Stone.java` - Stone object for visual representation
- `Background Image.png` - Background image for the intro screen

## Architecture

The game logic is fully decoupled from the user interface. All rules live in the pure
`MancalaState` class, which has no dependency on Swing. This separation is what enables
two things that would otherwise be impossible:

1. **Testability** — the rules can be unit-tested directly (see `MancalaStateTest`).
2. **AI search** — `MancalaAI` copies the state and simulates thousands of hypothetical
   moves via minimax without ever touching the rendered board.

`GameBoard` keeps the visual `Pit`/`Stone` objects in sync with the `MancalaState` after
every move, so the display always mirrors the single source of truth for the rules.

## Object-Oriented Design

The project demonstrates proper OOP principles:

- **Encapsulation:** Each class manages its own data and behavior
- **Inheritance:** Proper use of Java Swing component hierarchy
- **Polymorphism:** Different panel types implementing common interfaces
- **Abstraction:** Clean separation of game logic from UI components

## Tutorial Video

For additional help, watch the tutorial video: [Gebeta Tutorial](https://www.youtube.com/watch?v=o5HaaipZ3EA)

Enjoy playing Gebeta!
