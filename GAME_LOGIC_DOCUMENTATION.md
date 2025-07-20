# Gebeta Game Logic Documentation

## Overview

Gebeta is a traditional Mancala game implemented in Java using Swing. This document explains the core game logic, design decisions, and implementation details.

## Game Structure

### Board Layout

The game board consists of 14 pits arranged in a specific pattern:

- **6 pits per player** (indices 0-5 for Player 1, 7-12 for Player 2)
- **2 stores** (indices 6 for Player 1, 13 for Player 2)
- **Initial setup**: 4 stones in each regular pit, stores start empty

```
Player 2 Store (13) | P2 Pits (7-12) | Player 1 Store (6)
                    |                |
                    | P1 Pits (0-5)  |
```

### Pit Types and Design Decisions

#### Regular Pits (Indices 0-5, 7-12)

- **Purpose**: Hold stones during gameplay
- **Visual Design**:
  - Wheat-colored background (`Color(245, 222, 179)`)
  - Saddle Brown border (`Color(139, 69, 19)`)
  - Stone count displayed below each pit
- **Stone Arrangement**: Grid layout (4 stones per row max) for optimal visibility
- **Interaction**: Clickable for player moves

#### Stores (Indices 6, 13)

- **Purpose**: Score collection and game end condition
- **Visual Design**:
  - Tan background (`Color(210, 180, 140)`) to distinguish from regular pits
  - Taller and narrower than regular pits (80x300 vs 100x100)
- **Stone Arrangement**: Vertical column layout for better space utilization
- **Interaction**: Non-clickable, automatic stone collection

## Core Game Logic

### 1. Move Validation

```java
// A move is valid if:
- Game is not over
- Selected pit belongs to current player
- Selected pit is not empty
- Selected pit is not a store
```

### 2. Stone Distribution Algorithm

The distribution follows these rules:

1. **Pick up all stones** from the selected pit
2. **Distribute counterclockwise**, dropping one stone per pit
3. **Skip opponent's store** during distribution
4. **Continue until all stones are distributed**

**Why this approach?**

- Ensures fair gameplay by preventing players from scoring on opponent's turn
- Maintains traditional Mancala rules
- Creates strategic depth through store positioning

### 3. Capture Rule

```java
// Capture occurs when:
- Last stone lands in current player's regular pit
- That pit was empty before the move (now has exactly 1 stone)
- Opposite pit contains stones
```

**Capture Logic:**

1. Remove stones from the landing pit
2. Remove stones from the opposite pit
3. Add all captured stones to current player's store

**Strategic Importance:**

- Rewards careful planning and counting
- Can dramatically change game balance
- Encourages defensive play

### 4. Extra Turn Rule

```java
// Extra turn is granted when:
- Last stone lands in current player's store
```

**Why this rule exists:**

- Rewards players for strategic distribution
- Can create momentum swings
- Adds complexity to game planning

### 5. Game End Conditions

The game ends when:

1. **One player has no stones** in their regular pits
2. **Remaining stones are collected** into respective stores
3. **Winner is determined** by store stone count

**End Game Logic:**

```java
// Check each player's regular pits
// If any player has no stones, game ends
// Collect all remaining stones into stores
// Compare final scores
```

## Visual Design Decisions

### Color Scheme

- **Background**: Beige (`Color(245, 245, 220)`) - Warm, traditional feel
- **Pits**: Wheat color for regular pits, tan for stores
- **Borders**: Saddle Brown for consistency
- **Stones**: Random brown variations for visual interest

### Stone Visualization

- **Size**: 8 pixels diameter - visible but not overwhelming
- **Colors**: 5 different brown shades for variety
- **Arrangement**:
  - Regular pits: Grid layout (max 4 per row)
  - Stores: Vertical column layout
- **Positioning**: Dynamic calculation based on stone count

### UI Elements

- **Buttons**: Styled with hover effects for better UX
- **Player indicators**: Color-coded (green for Player 1, blue for Player 2)
- **Score display**: Clear, centered text on stores
- **Game over overlay**: Semi-transparent with winner announcement

## Technical Implementation

### Class Architecture

#### Gebeta (Main Class)

- **Purpose**: Application entry point and navigation
- **Design Pattern**: CardLayout for panel switching
- **Responsibilities**:
  - Initialize all panels
  - Handle navigation between screens
  - Manage application lifecycle

#### GamePanel

- **Purpose**: Main game interface
- **Responsibilities**:
  - Handle mouse interactions
  - Manage game state updates
  - Display game information
  - Handle game over scenarios

#### GameBoard

- **Purpose**: Core game logic engine
- **Responsibilities**:
  - Manage pit array and game state
  - Execute move logic
  - Handle capture rules
  - Determine game end conditions

#### Pit

- **Purpose**: Individual pit representation
- **Responsibilities**:
  - Store and manage stones
  - Handle stone positioning
  - Provide collision detection
  - Render pit appearance

#### Stone

- **Purpose**: Individual stone representation
- **Responsibilities**:
  - Store position and color
- **Design**: Simple, lightweight for performance

### Key Algorithms

#### Stone Distribution

```java
// Two-pass algorithm for efficiency
// Pass 1: Calculate final landing position
// Pass 2: Actually distribute stones
```

**Why two passes?**

- Prevents issues with stone count changes during distribution
- Ensures accurate capture rule application
- Maintains game state consistency

#### Opposite Pit Calculation

```java
// Mathematical formula for finding opposite pit
// Player 1 pits (0-5): opposite = 12 - index
// Player 2 pits (7-12): opposite = 12 - index
```

**Why this formula?**

- Symmetrical board layout
- Efficient calculation
- Handles edge cases correctly

## Strategic Elements

### Opening Moves

- **Pit selection** affects stone distribution pattern
- **Store positioning** influences scoring opportunities
- **Defensive play** can prevent opponent captures

### Mid-Game Strategy

- **Counting stones** is crucial for planning
- **Capture opportunities** should be identified early
- **Store management** affects turn efficiency

### End Game Tactics

- **Stone conservation** becomes critical
- **Forced moves** may limit options
- **Final scoring** determines winner

## Performance Considerations

### Rendering Optimization

- **Antialiasing** enabled for smooth graphics
- **Efficient stone positioning** calculations
- **Minimal repaints** only when necessary

### Memory Management

- **Stone objects** are lightweight
- **Pit arrays** are fixed size
- **No memory leaks** from event listeners

## Future Enhancements

### Potential Features

1. **AI opponent** with varying difficulty levels
2. **Move history** and replay functionality
3. **Statistics tracking** (win rates, average scores)
4. **Customizable rules** (different stone counts, capture variations)
5. **Multiplayer support** over network
6. **Sound effects** and animations
7. **Save/load game states**

### Technical Improvements

1. **Database integration** for persistent statistics
2. **Configuration files** for game settings
3. **Plugin architecture** for rule variations
4. **Performance profiling** and optimization

## Conclusion

The Gebeta implementation successfully captures the essence of traditional Mancala while providing a modern, user-friendly interface. The modular design allows for easy maintenance and future enhancements, while the comprehensive game logic ensures accurate rule enforcement and engaging gameplay.
