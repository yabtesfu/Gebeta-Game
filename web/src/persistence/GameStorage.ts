export type GameMode = 'computer' | 'two-player';

export interface StoredGame {
  board: number[];
  currentPlayer: number;
  mode: GameMode;
  aiDepth: number;
}

export interface GameStats {
  humanWins: number;
  computerWins: number;
  computerTies: number;
  player1Wins: number;
  player2Wins: number;
  localTies: number;
}

export interface StorageLike {
  getItem(key: string): string | null;
  setItem(key: string, value: string): void;
  removeItem(key: string): void;
}

const SAVE_KEY = 'gebeta.savedGame';
const STATS_KEY = 'gebeta.stats';
const VERSION = 1;
const EMPTY_STATS: GameStats = {
  humanWins: 0,
  computerWins: 0,
  computerTies: 0,
  player1Wins: 0,
  player2Wins: 0,
  localTies: 0,
};

const isNonNegativeInteger = (value: unknown): value is number =>
  typeof value === 'number' && Number.isInteger(value) && value >= 0;

/** Versioned, defensive localStorage adapter for one saved game and win records. */
export class GameStorage {
  constructor(private readonly storage: StorageLike) {}

  save(game: StoredGame): void {
    if (!this.validGame(game)) return;
    this.write(SAVE_KEY, JSON.stringify({ version: VERSION, ...game }));
  }

  load(): StoredGame | null {
    try {
      const raw = this.storage.getItem(SAVE_KEY);
      if (!raw) return null;
      const parsed: unknown = JSON.parse(raw);
      if (!this.isRecord(parsed) || parsed.version !== VERSION || !this.validGame(parsed)) {
        this.clearSavedGame();
        return null;
      }
      return {
        board: [...parsed.board],
        currentPlayer: parsed.currentPlayer,
        mode: parsed.mode,
        aiDepth: parsed.aiDepth,
      };
    } catch {
      this.clearSavedGame();
      return null;
    }
  }

  clearSavedGame(): void {
    try {
      this.storage.removeItem(SAVE_KEY);
    } catch {
      // Storage may be disabled or unavailable; gameplay should still continue.
    }
  }

  stats(): GameStats {
    try {
      const raw = this.storage.getItem(STATS_KEY);
      if (!raw) return { ...EMPTY_STATS };
      const parsed: unknown = JSON.parse(raw);
      if (!this.isRecord(parsed) || parsed.version !== VERSION) return { ...EMPTY_STATS };
      const result = { ...EMPTY_STATS };
      for (const key of Object.keys(result) as (keyof GameStats)[]) {
        const value = parsed[key];
        result[key] = isNonNegativeInteger(value) ? value : 0;
      }
      return result;
    } catch {
      return { ...EMPTY_STATS };
    }
  }

  recordResult(mode: GameMode, player0Score: number, player1Score: number): void {
    const stats = this.stats();
    if (mode === 'computer') {
      if (player0Score > player1Score) stats.humanWins += 1;
      else if (player1Score > player0Score) stats.computerWins += 1;
      else stats.computerTies += 1;
    } else {
      if (player0Score > player1Score) stats.player1Wins += 1;
      else if (player1Score > player0Score) stats.player2Wins += 1;
      else stats.localTies += 1;
    }
    this.write(STATS_KEY, JSON.stringify({ version: VERSION, ...stats }));
  }

  private validGame(value: unknown): value is StoredGame {
    if (!this.isRecord(value)) return false;
    if (!Array.isArray(value.board) || value.board.length !== 14) return false;
    if (!value.board.every(isNonNegativeInteger)) return false;
    if (value.board.reduce((sum, stones) => sum + stones, 0) !== 48) return false;
    if (value.currentPlayer !== 0 && value.currentPlayer !== 1) return false;
    if (value.mode !== 'computer' && value.mode !== 'two-player') return false;
    if (!isNonNegativeInteger(value.aiDepth)) return false;
    return value.mode !== 'computer' || value.aiDepth >= 1;
  }

  private isRecord(value: unknown): value is Record<string, any> {
    return typeof value === 'object' && value !== null;
  }

  private write(key: string, value: string): void {
    try {
      this.storage.setItem(key, value);
    } catch {
      // Quota/security errors must not interrupt a game.
    }
  }
}
