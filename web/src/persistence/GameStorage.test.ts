import { describe, expect, it } from 'vitest';
import { GameStorage, type StorageLike } from './GameStorage';

class MemoryStorage implements StorageLike {
  readonly values = new Map<string, string>();
  getItem(key: string): string | null { return this.values.get(key) ?? null; }
  setItem(key: string, value: string): void { this.values.set(key, value); }
  removeItem(key: string): void { this.values.delete(key); }
}

const savedGame = {
  board: [0, 5, 5, 5, 5, 4, 0, 4, 4, 4, 4, 4, 4, 0],
  currentPlayer: 1,
  mode: 'computer' as const,
  aiDepth: 5,
};

describe('GameStorage', () => {
  it('round-trips a saved game without exposing its stored board', () => {
    const persistence = new GameStorage(new MemoryStorage());
    persistence.save(savedGame);
    const loaded = persistence.load();
    expect(loaded).toEqual(savedGame);
    loaded!.board[0] = 99;
    expect(persistence.load()).toEqual(savedGame);
  });

  it('clears a resumable game', () => {
    const persistence = new GameStorage(new MemoryStorage());
    persistence.save(savedGame);
    persistence.clearSavedGame();
    expect(persistence.load()).toBeNull();
  });

  it('rejects corrupt and non-conserving boards', () => {
    const storage = new MemoryStorage();
    storage.setItem('gebeta.savedGame', JSON.stringify({ version: 1, ...savedGame, board: [48] }));
    const persistence = new GameStorage(storage);
    expect(persistence.load()).toBeNull();
    expect(storage.getItem('gebeta.savedGame')).toBeNull();
  });

  it('tracks AI and local results separately', () => {
    const persistence = new GameStorage(new MemoryStorage());
    persistence.recordResult('computer', 30, 18);
    persistence.recordResult('computer', 18, 30);
    persistence.recordResult('computer', 24, 24);
    persistence.recordResult('two-player', 30, 18);
    persistence.recordResult('two-player', 18, 30);
    persistence.recordResult('two-player', 24, 24);
    expect(persistence.stats()).toEqual({
      humanWins: 1,
      computerWins: 1,
      computerTies: 1,
      player1Wins: 1,
      player2Wins: 1,
      localTies: 1,
    });
  });

  it('survives unavailable browser storage', () => {
    const unavailable: StorageLike = {
      getItem: () => { throw new Error('blocked'); },
      setItem: () => { throw new Error('blocked'); },
      removeItem: () => { throw new Error('blocked'); },
    };
    const persistence = new GameStorage(unavailable);
    expect(() => persistence.save(savedGame)).not.toThrow();
    expect(persistence.load()).toBeNull();
    expect(persistence.stats().humanWins).toBe(0);
  });
});
