export interface MoveTrace {
  source: number;
  drops: number[];
  captured: boolean;
  captureLandingPit: number;
  captureOppositePit: number;
  captureStore: number;
  capturedTotal: number;
  extraTurn: boolean;
  gameOver: boolean;
}

/** A direct TypeScript port of the tested, Swing-free Java rules engine. */
export class MancalaState {
  static readonly SIZE = 14;
  static readonly P0_STORE = 6;
  static readonly P1_STORE = 13;
  static readonly STONES_PER_PIT = 4;

  private readonly board: number[];
  private player: number;
  private gameOver: boolean;

  constructor(board?: readonly number[], currentPlayer = 0, gameOver = false) {
    if (board !== undefined) {
      if (board.length !== MancalaState.SIZE) {
        throw new Error(`board must have exactly ${MancalaState.SIZE} slots`);
      }
      if (board.some((stones) => !Number.isInteger(stones) || stones < 0)) {
        throw new Error('board slots must contain non-negative integers');
      }
      if (currentPlayer !== 0 && currentPlayer !== 1) {
        throw new Error('currentPlayer must be 0 or 1');
      }
      this.board = [...board];
      this.player = currentPlayer;
      this.gameOver = gameOver;
      return;
    }

    this.board = Array.from({ length: MancalaState.SIZE }, (_, index) =>
      MancalaState.isStore(index) ? 0 : MancalaState.STONES_PER_PIT,
    );
    this.player = 0;
    this.gameOver = false;
  }

  static fromBoard(board: readonly number[], currentPlayer: number): MancalaState {
    return new MancalaState(board, currentPlayer, false);
  }

  static isStore(index: number): boolean {
    return index === MancalaState.P0_STORE || index === MancalaState.P1_STORE;
  }

  static ownerOf(index: number): number {
    return index <= MancalaState.P0_STORE ? 0 : 1;
  }

  static storeIndex(player: number): number {
    return player === 0 ? MancalaState.P0_STORE : MancalaState.P1_STORE;
  }

  static oppositeOf(index: number): number {
    return 12 - index;
  }

  copy(): MancalaState {
    return new MancalaState(this.board, this.player, this.gameOver);
  }

  toBoard(): number[] {
    return [...this.board];
  }

  stones(index: number): number {
    return this.board[index] ?? 0;
  }

  currentPlayer(): number {
    return this.player;
  }

  isGameOver(): boolean {
    return this.gameOver;
  }

  scoreOf(player: number): number {
    return this.board[MancalaState.storeIndex(player)];
  }

  isLegalMove(pit: number): boolean {
    return (
      !this.gameOver &&
      pit >= 0 &&
      pit < MancalaState.SIZE &&
      !MancalaState.isStore(pit) &&
      MancalaState.ownerOf(pit) === this.player &&
      this.board[pit] > 0
    );
  }

  landsInOwnStore(pit: number): boolean {
    if (!this.isLegalMove(pit)) return false;
    const opponentStore = this.player === 0 ? MancalaState.P1_STORE : MancalaState.P0_STORE;
    let position = pit;
    for (let i = 0; i < this.board[pit]; i += 1) {
      position = (position + 1) % MancalaState.SIZE;
      if (position === opponentStore) position = (position + 1) % MancalaState.SIZE;
    }
    return position === MancalaState.storeIndex(this.player);
  }

  legalMoves(): number[] {
    return Array.from({ length: MancalaState.SIZE }, (_, index) => index).filter((index) =>
      this.isLegalMove(index),
    );
  }

  winnerText(): string | null {
    if (!this.gameOver) return null;
    const p0 = this.scoreOf(0);
    const p1 = this.scoreOf(1);
    if (p0 > p1) return 'Player 1 Wins!';
    if (p1 > p0) return 'Player 2 Wins!';
    return "It's a Tie!";
  }

  applyMove(pit: number): boolean {
    return this.applyMoveTraced(pit) !== null;
  }

  applyMoveTraced(pit: number): MoveTrace | null {
    if (!this.isLegalMove(pit)) return null;

    const movingPlayer = this.player;
    const opponentStore = movingPlayer === 0 ? MancalaState.P1_STORE : MancalaState.P0_STORE;
    const count = this.board[pit];
    const drops: number[] = [];
    this.board[pit] = 0;

    let index = pit;
    for (let i = 0; i < count; i += 1) {
      index = (index + 1) % MancalaState.SIZE;
      if (index === opponentStore) index = (index + 1) % MancalaState.SIZE;
      this.board[index] += 1;
      drops.push(index);
    }

    let captured = false;
    let captureLandingPit = -1;
    let captureOppositePit = -1;
    let captureStore = -1;
    let capturedTotal = 0;

    if (
      !MancalaState.isStore(index) &&
      MancalaState.ownerOf(index) === movingPlayer &&
      this.board[index] === 1
    ) {
      const opposite = MancalaState.oppositeOf(index);
      if (this.board[opposite] > 0) {
        captured = true;
        captureLandingPit = index;
        captureOppositePit = opposite;
        captureStore = MancalaState.storeIndex(movingPlayer);
        capturedTotal = this.board[opposite] + this.board[index];
        this.board[opposite] = 0;
        this.board[index] = 0;
        this.board[captureStore] += capturedTotal;
      }
    }

    const extraTurn = index === MancalaState.storeIndex(movingPlayer);
    this.checkGameOver();
    if (!this.gameOver && !extraTurn) this.player = 1 - movingPlayer;

    return {
      source: pit,
      drops,
      captured,
      captureLandingPit,
      captureOppositePit,
      captureStore,
      capturedTotal,
      extraTurn,
      gameOver: this.gameOver,
    };
  }

  private checkGameOver(): void {
    const p0HasStones = this.board.slice(0, 6).some((stones) => stones > 0);
    const p1HasStones = this.board.slice(7, 13).some((stones) => stones > 0);
    if (p0HasStones && p1HasStones) return;

    this.gameOver = true;
    for (let i = 0; i <= 5; i += 1) {
      this.board[MancalaState.P0_STORE] += this.board[i];
      this.board[i] = 0;
    }
    for (let i = 7; i <= 12; i += 1) {
      this.board[MancalaState.P1_STORE] += this.board[i];
      this.board[i] = 0;
    }
  }
}
