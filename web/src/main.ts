import './styles.css';
import { MancalaAI } from './logic/MancalaAI';
import { MancalaState, type MoveTrace } from './logic/MancalaState';

type GameMode = 'computer' | 'two-player';

const app = document.querySelector<HTMLDivElement>('#app');
if (!app) throw new Error('Missing #app');

app.innerHTML = `
  <main class="app-shell">
    <section class="landing screen" id="landing-screen" aria-labelledby="landing-title">
      <div class="landing-card">
        <div class="flag-mark" aria-hidden="true"><i></i><i></i><i></i></div>
        <p class="eyebrow">A traditional Ethiopian strategy game</p>
        <h1 id="landing-title"><span lang="am">ገበጣ</span><strong>GEBETA</strong></h1>
        <p class="intro-copy">Sow with purpose. Capture with foresight. Bring every stone home.</p>
        <div class="landing-actions">
          <button class="button primary" id="computer-button">Play vs Computer</button>
          <button class="button secondary" id="two-player-button">Two Players</button>
          <button class="text-button" id="landing-help-button">How to play <span aria-hidden="true">→</span></button>
        </div>
        <p class="no-install">No download · No account · Plays in your browser</p>
      </div>
    </section>

    <section class="game screen hidden" id="game-screen" aria-labelledby="game-title">
      <header class="game-header">
        <div class="brand-lockup">
          <h1 id="game-title"><span lang="am">ገበጣ</span> Gebeta</h1>
          <p>Sow counterclockwise</p>
        </div>
        <nav class="game-actions" aria-label="Game controls">
          <button class="small-button" id="new-game-button">New game</button>
          <button class="small-button" id="help-button">How to play</button>
          <button class="small-button icon-button" id="sound-button" aria-pressed="true">♪ <span>Sound on</span></button>
          <button class="small-button" id="menu-button">‹ Menu</button>
        </nav>
      </header>

      <div class="game-content">
        <section class="score-row" aria-label="Scores and turn">
          <article class="player-card opponent" id="p1-card">
            <span class="avatar" id="p1-avatar">AI</span>
            <span><strong id="p1-name">Computer</strong><small><b id="p1-score">0</b> banked</small></span>
          </article>
          <div class="status-pill" id="status" role="status" aria-live="polite">Your turn</div>
          <article class="player-card player" id="p0-card">
            <span class="avatar" id="p0-avatar">You</span>
            <span><strong id="p0-name">You</strong><small><b id="p0-score">0</b> banked</small></span>
          </article>
        </section>

        <section class="board-frame" aria-label="Gebeta board">
          <div class="board" id="board">
            <div class="store store-left" data-slot="13" aria-label="Player 2 store"></div>
            <div class="pit-row top-row" id="top-row"></div>
            <div class="direction" aria-hidden="true"><span>↺</span> sowing direction <span>↻</span></div>
            <div class="pit-row bottom-row" id="bottom-row"></div>
            <div class="store store-right" data-slot="6" aria-label="Player 1 store"></div>
          </div>
        </section>

        <p class="board-tip" id="board-tip">Pick any glowing pit on your side.</p>
      </div>
      <footer>© 2026 Yabetse · Traditional play, thoughtfully rebuilt</footer>
    </section>
  </main>

  <dialog class="modal" id="difficulty-dialog" aria-labelledby="difficulty-title">
    <form method="dialog" class="modal-card">
      <button class="close-button" value="cancel" aria-label="Close">×</button>
      <p class="eyebrow">Choose your challenge</p>
      <h2 id="difficulty-title">Play vs Computer</h2>
      <div class="difficulty-list">
        <button value="2" class="difficulty"><b>Easy</b><span>A relaxed first game</span><i>Depth 2</i></button>
        <button value="5" class="difficulty recommended"><b>Medium</b><span>Plans several moves ahead</span><i>Depth 5</i></button>
        <button value="8" class="difficulty"><b>Hard</b><span>A patient, tactical opponent</span><i>Depth 8</i></button>
      </div>
    </form>
  </dialog>

  <dialog class="modal" id="help-dialog" aria-labelledby="help-title">
    <form method="dialog" class="modal-card help-card">
      <button class="close-button" value="close" aria-label="Close">×</button>
      <p class="eyebrow">The essentials</p>
      <h2 id="help-title">How to play Gebeta</h2>
      <ol>
        <li><b>Choose a pit.</b> Pick up every stone from one of the six pits on your side.</li>
        <li><b>Sow the stones.</b> Drop them one at a time counterclockwise, skipping your opponent’s store.</li>
        <li><b>Earn another turn.</b> Land your final stone in your own store to play again.</li>
        <li><b>Capture.</b> Land in an empty pit on your side to take that stone and the stones opposite it.</li>
        <li><b>Win.</b> When either side is empty, remaining stones are collected. The fuller store wins.</li>
      </ol>
      <button class="button primary" value="close">Ready to play</button>
    </form>
  </dialog>

  <dialog class="modal" id="game-over-dialog" aria-labelledby="result-title">
    <form method="dialog" class="modal-card result-card">
      <p class="eyebrow">Game over</p>
      <h2 id="result-title">You win!</h2>
      <p id="result-score">Final score 30–18</p>
      <div class="result-actions">
        <button class="button primary" value="again">Play again</button>
        <button class="button secondary" value="menu">Main menu</button>
      </div>
    </form>
  </dialog>
`;

const required = <T extends Element>(selector: string): T => {
  const element = document.querySelector<T>(selector);
  if (!element) throw new Error(`Missing element: ${selector}`);
  return element;
};

const landingScreen = required<HTMLElement>('#landing-screen');
const gameScreen = required<HTMLElement>('#game-screen');
const boardElement = required<HTMLElement>('#board');
const topRow = required<HTMLElement>('#top-row');
const bottomRow = required<HTMLElement>('#bottom-row');
const statusElement = required<HTMLElement>('#status');
const boardTip = required<HTMLElement>('#board-tip');
const difficultyDialog = required<HTMLDialogElement>('#difficulty-dialog');
const helpDialog = required<HTMLDialogElement>('#help-dialog');
const gameOverDialog = required<HTMLDialogElement>('#game-over-dialog');

const pitMarkup = (index: number): string =>
  `<button class="pit" data-slot="${index}" type="button" aria-label="Pit ${index}"></button>`;
topRow.innerHTML = [12, 11, 10, 9, 8, 7].map(pitMarkup).join('');
bottomRow.innerHTML = [0, 1, 2, 3, 4, 5].map(pitMarkup).join('');

let state = new MancalaState();
let visualBoard = state.toBoard();
let mode: GameMode = 'computer';
let aiDepth = 5;
let ai: MancalaAI | null = new MancalaAI(aiDepth, 1);
let animating = false;
let aiThinking = false;
let muted = false;
let sequence = 0;
let lastSlot = -1;

const prefersReducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)');
const pause = (milliseconds: number): Promise<void> =>
  new Promise((resolve) => window.setTimeout(resolve, prefersReducedMotion.matches ? 0 : milliseconds));

function playerName(player: number): string {
  if (mode === 'computer') return player === 0 ? 'You' : 'Computer';
  return `Player ${player + 1}`;
}

function stoneMarkup(count: number, slot: number): string {
  const visible = Math.min(count, 14);
  const stones = Array.from({ length: visible }, (_, index) => {
    const angle = ((index * 137.5 + slot * 29) * Math.PI) / 180;
    const radius = 8 + (index % 4) * 7;
    const x = 50 + Math.cos(angle) * radius;
    const y = 48 + Math.sin(angle) * radius;
    return `<i class="stone stone-${(index + slot) % 4}" style="left:${x}%;top:${y}%"></i>`;
  }).join('');
  return `${stones}<b class="count">${count}</b>`;
}

function statusText(): string {
  if (state.isGameOver()) return 'Game over';
  if (aiThinking) return 'Computer is thinking…';
  if (mode === 'computer') return state.currentPlayer() === 0 ? 'Your turn' : "Computer's turn";
  return `${playerName(state.currentPlayer())}'s turn`;
}

function canHumanPlay(index: number): boolean {
  return (
    !animating &&
    !aiThinking &&
    !state.isGameOver() &&
    !(mode === 'computer' && state.currentPlayer() === 1) &&
    state.isLegalMove(index)
  );
}

function render(): void {
  required<HTMLElement>('#p0-name').textContent = playerName(0);
  required<HTMLElement>('#p1-name').textContent = playerName(1);
  required<HTMLElement>('#p0-avatar').textContent = mode === 'computer' ? 'You' : '1';
  required<HTMLElement>('#p1-avatar').textContent = mode === 'computer' ? 'AI' : '2';
  required<HTMLElement>('#p0-score').textContent = String(visualBoard[MancalaState.P0_STORE]);
  required<HTMLElement>('#p1-score').textContent = String(visualBoard[MancalaState.P1_STORE]);
  required<HTMLElement>('#p0-card').classList.toggle('active', state.currentPlayer() === 0 && !state.isGameOver());
  required<HTMLElement>('#p1-card').classList.toggle('active', state.currentPlayer() === 1 && !state.isGameOver());
  statusElement.textContent = statusText();
  statusElement.classList.toggle('thinking', aiThinking);

  if (state.isGameOver()) boardTip.textContent = 'All stones are home. Check the final score.';
  else if (aiThinking) boardTip.textContent = 'The computer is weighing its options.';
  else if (mode === 'computer' && state.currentPlayer() === 1) boardTip.textContent = 'The computer has the board.';
  else boardTip.textContent = 'Pick any glowing pit on your side.';

  document.querySelectorAll<HTMLElement>('[data-slot]').forEach((slotElement) => {
    const index = Number(slotElement.dataset.slot);
    const count = visualBoard[index];
    slotElement.innerHTML = stoneMarkup(count, index);
    slotElement.classList.toggle('last', index === lastSlot);
    slotElement.setAttribute('aria-label', `${MancalaState.isStore(index) ? playerName(MancalaState.ownerOf(index)) + ' store' : 'Pit'}: ${count} stones`);

    if (slotElement instanceof HTMLButtonElement) {
      const playable = canHumanPlay(index);
      slotElement.disabled = !playable;
      slotElement.classList.toggle('playable', playable);
      slotElement.classList.toggle('free-turn', playable && state.landsInOwnStore(index));
      slotElement.title = playable && state.landsInOwnStore(index) ? 'Free turn' : '';
    }
  });
}

function showGame(): void {
  landingScreen.classList.add('hidden');
  gameScreen.classList.remove('hidden');
  window.scrollTo({ top: 0, behavior: 'smooth' });
}

function showMenu(): void {
  sequence += 1;
  animating = false;
  aiThinking = false;
  gameScreen.classList.add('hidden');
  landingScreen.classList.remove('hidden');
}

function resetGame(): void {
  sequence += 1;
  state = new MancalaState();
  visualBoard = state.toBoard();
  animating = false;
  aiThinking = false;
  lastSlot = -1;
  render();
}

function startGame(selectedMode: GameMode, depth = 0): void {
  mode = selectedMode;
  aiDepth = depth;
  ai = mode === 'computer' ? new MancalaAI(aiDepth, 1) : null;
  resetGame();
  showGame();
}

function playTone(frequency: number, duration = 0.055): void {
  if (muted) return;
  const AudioContextClass = window.AudioContext;
  if (!AudioContextClass) return;
  const context = new AudioContextClass();
  const oscillator = context.createOscillator();
  const gain = context.createGain();
  oscillator.frequency.value = frequency;
  oscillator.type = 'sine';
  gain.gain.setValueAtTime(0.035, context.currentTime);
  gain.gain.exponentialRampToValueAtTime(0.001, context.currentTime + duration);
  oscillator.connect(gain).connect(context.destination);
  oscillator.start();
  oscillator.stop(context.currentTime + duration);
  oscillator.addEventListener('ended', () => void context.close(), { once: true });
}

async function animateTrace(trace: MoveTrace, before: number[], run: number): Promise<boolean> {
  visualBoard = [...before];
  visualBoard[trace.source] = 0;
  lastSlot = trace.source;
  render();
  playTone(180);
  await pause(150);

  for (const drop of trace.drops) {
    if (run !== sequence) return false;
    visualBoard[drop] += 1;
    lastSlot = drop;
    render();
    playTone(260 + (drop % 4) * 22);
    await pause(105);
  }

  if (trace.captured) {
    if (run !== sequence) return false;
    visualBoard[trace.captureLandingPit] = 0;
    visualBoard[trace.captureOppositePit] = 0;
    visualBoard[trace.captureStore] += trace.capturedTotal;
    lastSlot = trace.captureStore;
    render();
    playTone(520, 0.12);
    await pause(260);
  }

  visualBoard = state.toBoard();
  render();
  return true;
}

async function makeMove(index: number): Promise<void> {
  if (!canHumanPlay(index) && !(mode === 'computer' && state.currentPlayer() === 1 && aiThinking)) return;
  const before = state.toBoard();
  const trace = state.applyMoveTraced(index);
  if (!trace) return;

  const run = sequence;
  animating = true;
  aiThinking = false;
  render();
  const completed = await animateTrace(trace, before, run);
  if (!completed || run !== sequence) return;
  animating = false;
  render();
  await afterMove(run);
}

async function afterMove(run: number): Promise<void> {
  if (state.isGameOver()) {
    playTone(660, 0.18);
    await pause(220);
    if (run === sequence) showGameOver();
    return;
  }
  if (mode !== 'computer' || state.currentPlayer() !== 1 || !ai) return;

  aiThinking = true;
  render();
  await pause(420);
  if (run !== sequence) return;
  const move = ai.chooseMove(state.copy());
  if (run !== sequence || move < 0) return;
  await makeMove(move);
}

function showGameOver(): void {
  const p0 = state.scoreOf(0);
  const p1 = state.scoreOf(1);
  let title = "It's a tie!";
  if (p0 > p1) title = mode === 'computer' ? 'You win!' : 'Player 1 wins!';
  if (p1 > p0) title = mode === 'computer' ? 'Computer wins' : 'Player 2 wins!';
  required<HTMLElement>('#result-title').textContent = title;
  required<HTMLElement>('#result-score').textContent = `Final score ${p0}–${p1}`;
  gameOverDialog.showModal();
}

boardElement.addEventListener('click', (event) => {
  const pit = (event.target as HTMLElement).closest<HTMLButtonElement>('.pit');
  if (pit) void makeMove(Number(pit.dataset.slot));
});

required<HTMLButtonElement>('#computer-button').addEventListener('click', () => difficultyDialog.showModal());
required<HTMLButtonElement>('#two-player-button').addEventListener('click', () => startGame('two-player'));
required<HTMLButtonElement>('#landing-help-button').addEventListener('click', () => helpDialog.showModal());
required<HTMLButtonElement>('#help-button').addEventListener('click', () => helpDialog.showModal());
required<HTMLButtonElement>('#new-game-button').addEventListener('click', resetGame);
required<HTMLButtonElement>('#menu-button').addEventListener('click', showMenu);

required<HTMLButtonElement>('#sound-button').addEventListener('click', (event) => {
  muted = !muted;
  const button = event.currentTarget as HTMLButtonElement;
  button.setAttribute('aria-pressed', String(!muted));
  button.querySelector('span')!.textContent = muted ? 'Sound off' : 'Sound on';
  if (!muted) playTone(440);
});

difficultyDialog.addEventListener('close', () => {
  const depth = Number(difficultyDialog.returnValue);
  if (depth > 0) startGame('computer', depth);
});

gameOverDialog.addEventListener('close', () => {
  if (gameOverDialog.returnValue === 'again') resetGame();
  if (gameOverDialog.returnValue === 'menu') showMenu();
});

render();
