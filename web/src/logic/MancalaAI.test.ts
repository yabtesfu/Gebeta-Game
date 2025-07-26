import { describe, expect, it } from 'vitest';
import { MancalaAI } from './MancalaAI';
import { MancalaState } from './MancalaState';

describe('MancalaAI search', () => {
  it('completes iterative depths and returns a deterministic legal move', () => {
    const state = new MancalaState();
    const first = new MancalaAI(6, 0, 5_000);
    const second = new MancalaAI(6, 0, 5_000);
    const move = first.chooseMove(state);
    expect(state.isLegalMove(move)).toBe(true);
    expect(second.chooseMove(state)).toBe(move);
    expect(first.lastSearchStats().completedDepth).toBe(6);
    expect(first.lastSearchStats().cutoffs).toBeGreaterThan(0);
  });

  it('uses cached transpositions during deeper search', () => {
    const ai = new MancalaAI(8, 0, 5_000);
    ai.chooseMove(new MancalaState());
    expect(ai.lastSearchStats().cacheHits).toBeGreaterThan(0);
  });

  it('keeps a legal completed result when its deadline expires', () => {
    const state = new MancalaState();
    const ai = new MancalaAI(40, 0, 20);
    const move = ai.chooseMove(state);
    expect(state.isLegalMove(move)).toBe(true);
    expect(ai.lastSearchStats().timedOut).toBe(true);
    expect(ai.lastSearchStats().completedDepth).toBeGreaterThanOrEqual(1);
    expect(ai.lastSearchStats().elapsedMs).toBeLessThan(1_000);
  });
});
