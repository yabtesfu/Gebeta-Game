# Gebeta - Traditional Ethiopian Mancala Game

[![CI](https://github.com/yabtesfu/Gebeta-Game/actions/workflows/ci.yml/badge.svg)](https://github.com/yabtesfu/Gebeta-Game/actions/workflows/ci.yml)

A Java GUI implementation of the traditional Ethiopian board game Gebeta (Mancala), featuring an AI opponent (minimax with alpha-beta pruning), a fully unit-tested rules engine, and a Gradle build with continuous integration. The interface is themed around Habesha (Ethiopian) culture — warm coffee-and-gold tones over a backdrop of elders playing gebeta.

**[Play Gebeta in your browser](https://yabtesfu.github.io/Gebeta-Game/)** — no download or account required.

![Gameplay demo — animated stone sowing](docs/demo.gif)

> The board above is rendered directly from the game's own drawing code (see
> [tools/DemoGifGenerator.java](tools/DemoGifGenerator.java)), so it's an exact
> capture of the in-game sowing animation.

|  Main menu  |  In game  |
| :---------: | :-------: |
| ![Main menu](docs/screenshot-intro.png) | ![Game screen](docs/screenshot-game.png) |

## Download & Play

### Play online

Open the **[live web game](https://yabtesfu.github.io/Gebeta-Game/)** on a phone,
tablet, or desktop browser. It includes two-player mode, three AI difficulties,
animated sowing and captures, sound, responsive controls, and the same rules as the
tested Java engine.

### macOS app (no Java required)

Download the DMG for your Mac from the
[latest GitHub release](https://github.com/yabtesfu/Gebeta-Game/releases/latest):

- `arm64` for Apple-silicon Macs (M1, M2, M3, and newer)
- `x64` for Intel Macs

Open the DMG, drag **Gebeta** into **Applications**, and launch it. The app includes
its own Java runtime, so players do not need to install a JDK. Because the current
release is not Apple-notarized, macOS may require a right-click followed by **Open**
the first time.

### Build from source

Build a self-contained, runnable jar and launch it — no IDE required, just a JDK:

```bash
./gradlew jar
java -jar build/libs/gebeta.jar
```

Or run it straight from source with `./gradlew run`.

On macOS, build a native app and DMG locally with JDK 17 or newer:

```bash
./gradlew packageNative
open build/jpackage/installer/Gebeta-1.0.0.dmg
```

The output contains `build/jpackage/app/Gebeta.app` and the installer in
`build/jpackage/installer/`. Pushing a tag such as `v1.0.0` runs the native release
workflow and publishes both Apple-silicon and Intel DMGs to that GitHub Release.

## About the Developer

**Made by:** Yabetse Tesfaye  
**Institution:** Addis Ababa Institute of Technology  
**Program:** Software Engineer 
**Year:** 5th Year Student  
**ID:** UGR/31352/15

## Game Features

- **Traditional Gebeta Rules:** Complete implementation of the classic Gebeta game mechanics
- **Playable from a link:** A responsive TypeScript edition runs directly in the browser
  and deploys automatically to GitHub Pages.
- **Save and resume:** Both editions automatically checkpoint completed moves. Reopen the
  app or browser later and continue from the exact board and player turn.
- **Persistent records:** Versus-computer wins, losses, and draws—and local Player 1 / Player 2
  results—remain available between sessions.
- **Two game modes:**
  - **Two Players** — local hotseat play
  - **Play vs Computer** — an AI opponent with **Easy / Medium / Hard** difficulty
- **AI opponent:** A computer player built on the **minimax algorithm with alpha-beta pruning**.
  It looks several moves ahead, correctly handles Gebeta's "extra turn" rule, and gets
  stronger as difficulty increases (search depth 1 → 5 → 9). The AI runs on a background
  thread so the interface stays responsive.
- **Habesha-themed UI:** A cohesive traditional Ethiopian look — coffee-and-gold palette,
  a carved wooden board, coffee-bean stones, and a warm photographic backdrop of elders
  playing gebeta. Drop extra photos named `background1.png`, `background2.png`, … into
  `src/main/resources` and they automatically join the background rotation.
- **Animated & audible:** Stones sow one at a time, with sound effects synthesized in
  code (no audio files shipped) and a mute toggle.
- **Multiple Panels:**
  - Themed main menu over the cultural backdrop
  - Game board with full functionality
  - About page with developer information
  - Help page with game instructions and tutorial video link
- **Enhanced User Experience:** Hover effects, "Computer is thinking…" status, responsive design
- **Game Management:** New game, reset, and navigation features
- **Unit-tested rules engine:** A JUnit 5 test suite verifies the game rules, run on every push by CI

## How to Build and Run

The project uses **Gradle** via the included wrapper, so you don't need Gradle
installed — only a JDK (17 or newer).

### Run the game

```bash
./gradlew run
```

### Run the tests

```bash
./gradlew test
```

### Run the web game

The browser edition requires Node.js 20 or newer:

```bash
cd web
npm ci
npm run dev
```

Use `npm test` for the TypeScript rules parity suite and `npm run build` for the
production site. Before the first deployment, open the repository's **Settings →
Pages** and set **Source** to **GitHub Actions**. After that, pushes to `main` that
touch the web app deploy automatically.

### Build everything (compile + test + package)

```bash
./gradlew build
```

Continuous integration (GitHub Actions, see [.github/workflows/ci.yml](.github/workflows/ci.yml))
compiles the project and runs the full test suite on every push and pull request.

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

## Testing

The rules engine has a JUnit 5 suite covering sowing, captures, extra turns,
illegal-move rejection, and end-of-game collection:

```bash
./gradlew test
```

## Project Structure

```
src/
  main/
    java/            # application source
    resources/       # background.png (loaded from the classpath)
  test/
    java/            # JUnit 5 tests
web/
  src/               # TypeScript rules, AI, browser UI, and parity tests
  package.json       # Vite development and production scripts
build.gradle         # Gradle build configuration
.github/workflows/   # GitHub Actions CI
```

**Rules & AI (pure logic, no UI dependency):**
- `MancalaState.java` - The complete game rules as a plain `int[14]` board. No Swing
  imports, which is what makes it testable and lets the AI simulate positions freely.
- `MancalaAI.java` - Computer opponent using minimax with alpha-beta pruning.
- `MancalaStateTest.java` - JUnit 5 unit tests for the rules engine.
- `GamePersistence.java` - Versioned desktop save slot and win records backed by Java Preferences.
- `GamePersistenceTest.java` - In-memory tests for round trips, corruption recovery, and statistics.
- `web/src/logic/` - TypeScript ports of the same rules and minimax AI, backed by a
  matching browser-side parity suite.

**Web app:**
- `web/src/main.ts` - Browser game flow, move animation, difficulty selection, and UI state.
- `web/src/persistence/` - Defensive, versioned `localStorage` save and statistics handling.
- `web/src/styles.css` - Responsive coffee-and-gold board presentation for mobile and desktop.
- `.github/workflows/pages.yml` - Tests, builds, and publishes the game to GitHub Pages.

**Theme & presentation:**
- `Theme.java` - Central Habesha palette, fonts, and shared paint helpers (cards, dividers).
- `ThemedButton.java` - Rounded, gradient buttons with gold rims and hover feedback.
- `BackgroundManager.java` - Warm-tinted photographic backdrop, cached and with drop-in variations.
- `SoundPlayer.java` - Sound effects synthesized in code (no audio assets), with a mute toggle.

**User interface (Swing):**
- `Gebeta.java` - Main application class and screen navigation
- `IntroPanel.java` - Themed main menu and mode/difficulty selection
- `GamePanel.java` - Game HUD, input handling, animation, and AI turn orchestration
- `AboutPanel.java` - The story behind the game (culture + craft)
- `HelpPanel.java` - How to play
- `GameBoard.java` - Bridges the rules engine to the on-screen board and stones
- `Pit.java` - Visual pit (geometry + drawing)
- `Stone.java` - Stone object for visual representation
- `MoveTrace.java` - Per-move record the UI replays as the sowing animation

**Developer tools (`tools/`, not part of the game):**
- `DemoGifGenerator.java` - Renders the board's own draw code to the animated README GIF.
- `ScreenshotTool.java` - Renders each screen headlessly to a PNG for the README.

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
