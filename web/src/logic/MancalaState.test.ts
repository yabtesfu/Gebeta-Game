import { describe, expect, it } from 'vitest';
import { MancalaAI } from './MancalaAI';
import { MancalaState } from './MancalaState';

describe('MancalaState Java parity suite', () => {
  it('starts with four stones per pit and Player 0 to move', () => {
    const state = new MancalaState();
    for (let i = 0; i < MancalaState.SIZE; i += 1) {
      expect(state.stones(i)).toBe(MancalaState.isStore(i) ? 0 : 4);
    }
    expect(state.currentPlayer()).toBe(0);
    expect(state.isGameOver()).toBe(false);
    expect(state.legalMoves()).toHaveLength(6);
  });

  it('sows one stone per pit and passes the turn', () => {
    const state = new MancalaState();
    state.applyMove(0);
    expect(state.stones(0)).toBe(0);
    expect(state.stones(1)).toBe(5);
    expect(state.stones(4)).toBe(5);
    expect(state.stones(5)).toBe(4);
    expect(state.currentPlayer()).toBe(1);
  });

  it('grants an extra turn when the last stone reaches the own store', () => {
    const state = new MancalaState();
    state.applyMove(2);
    expect(state.scoreOf(0)).toBe(1);
    expect(state.currentPlayer()).toBe(0);
  });

  it("skips the opponent's store", () => {
    const state = MancalaState.fromBoard([4, 4, 4, 4, 4, 4, 0, 9, 4, 4, 4, 4, 4, 0], 1);
    state.applyMove(7);
    expect(state.stones(MancalaState.P0_STORE)).toBe(0);
  });

  it('captures from the opposite pit after landing in an own empty pit', () => {
    const state = MancalaState.fromBoard([1, 0, 4, 4, 4, 4, 0, 4, 4, 4, 4, 4, 4, 0], 0);
    state.applyMove(0);
    expect(state.stones(1)).toBe(0);
    expect(state.stones(11)).toBe(0);
    expect(state.scoreOf(0)).toBe(5);
  });

  it('does not capture when the opposite pit is empty', () => {
    const state = MancalaState.fromBoard([1, 0, 4, 4, 4, 4, 0, 4, 4, 4, 4, 0, 4, 0], 0);
    state.applyMove(0);
    expect(state.stones(1)).toBe(1);
    expect(state.scoreOf(0)).toBe(0);
  });

  it('rejects illegal moves without changing the turn', () => {
    const state = new MancalaState();
    expect(state.applyMove(MancalaState.P0_STORE)).toBe(false);
    expect(state.applyMove(7)).toBe(false);
    expect(state.applyMove(99)).toBe(false);
    expect(state.currentPlayer()).toBe(0);
  });

  it('sweeps the remaining stones when one side empties', () => {
    const state = MancalaState.fromBoard([0, 0, 0, 0, 0, 1, 10, 2, 3, 0, 0, 0, 0, 5], 0);
    state.applyMove(5);
    expect(state.isGameOver()).toBe(true);
    expect(state.scoreOf(0)).toBe(11);
    expect(state.scoreOf(1)).toBe(10);
    expect(state.stones(7)).toBe(0);
    expect(state.stones(8)).toBe(0);
  });

  it('copies independently', () => {
    const original = new MancalaState();
    const copy = original.copy();
    copy.applyMove(0);
    expect(original.stones(0)).toBe(4);
  });

  it('returns a complete move trace', () => {
    const state = new MancalaState();
    const trace = state.applyMoveTraced(2);
    expect(trace?.drops).toEqual([3, 4, 5, 6]);
    expect(trace?.extraTurn).toBe(true);
    expect(trace?.captured).toBe(false);
    expect(trace?.gameOver).toBe(false);
  });

  it('records capture details', () => {
    const state = MancalaState.fromBoard([1, 0, 4, 4, 4, 4, 0, 4, 4, 4, 4, 4, 4, 0], 0);
    const trace = state.applyMoveTraced(0);
    expect(trace?.drops).toEqual([1]);
    expect(trace?.captured).toBe(true);
    expect(trace?.captureLandingPit).toBe(1);
    expect(trace?.captureOppositePit).toBe(11);
    expect(trace?.capturedTotal).toBe(5);
  });

  it('returns null traces for illegal moves', () => {
    const state = new MancalaState();
    expect(state.applyMoveTraced(MancalaState.P0_STORE)).toBeNull();
    expect(state.applyMoveTraced(7)).toBeNull();
  });

  it('conserves all 48 stones throughout play', () => {
    const state = new MancalaState();
    for (const move of [0, 7, 1, 8, 2, 9, 3, 10, 4, 11, 5, 12]) state.applyMove(move);
    expect(state.toBoard().reduce((sum, stones) => sum + stones, 0)).toBe(48);
  });

  it('has an AI that always chooses a legal move', () => {
    const state = new MancalaState();
    const ai = new MancalaAI(4, 0);
    expect(state.legalMoves()).toContain(ai.chooseMove(state));
  });
});
