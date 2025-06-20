# Gebeta - Traditional Ethiopian Mancala Game

A Java GUI implementation of the traditional Ethiopian board game Gebeta (Mancala), created as an Object-Oriented Programming project.

## About the Developer

**Made by:** Yabetse Tesfaye  
**Institution:** American College of Technology  
**Program:** Computer Science  
**Year:** 2nd Year Student  
**ID:** 177/BSC-B6/2023

## Game Features

- **Traditional Gebeta Rules:** Complete implementation of the classic Gebeta game mechanics
- **Beautiful GUI:** Aesthetic board design with colorful stones and intuitive interface
- **Multiple Panels:**
  - Intro page with background image
  - Game board with full functionality
  - About page with developer information
  - Help page with game instructions and tutorial video link
- **Enhanced User Experience:** Hover effects, smooth animations, and responsive design
- **Game Management:** New game, reset, and navigation features

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

## File Structure

- `Gebeta.java` - Main application class
- `IntroPanel.java` - Introduction screen with navigation
- `GamePanel.java` - Main game interface
- `AboutPanel.java` - Developer information
- `HelpPanel.java` - Game instructions and help
- `GameBoard.java` - Game logic and board management
- `Pit.java` - Individual pit representation
- `Stone.java` - Stone object for visual representation
- `Background Image.png` - Background image for the intro screen

## Object-Oriented Design

The project demonstrates proper OOP principles:

- **Encapsulation:** Each class manages its own data and behavior
- **Inheritance:** Proper use of Java Swing component hierarchy
- **Polymorphism:** Different panel types implementing common interfaces
- **Abstraction:** Clean separation of game logic from UI components

## Tutorial Video

For additional help, watch the tutorial video: [Gebeta Tutorial](https://www.youtube.com/watch?v=o5HaaipZ3EA)

Enjoy playing Gebeta!
