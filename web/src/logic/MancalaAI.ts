import { MancalaState } from './MancalaState';

/** Minimax with alpha-beta pruning, matching the Java desktop opponent. */
export class MancalaAI {
  private static readonly WIN_BONUS = 10_000;
  private readonly maxDepth: number;
  private readonly aiPlayer: number;

  constructor(maxDepth: number, aiPlayer: number) {
    this.maxDepth = Math.max(1, maxDepth);
    this.aiPlayer = aiPlayer;
  }

  chooseMove(state: MancalaState): number {
    const moves = state.legalMoves();
    if (moves.length === 0) return -1;

    let bestMove = moves[0];
    let bestValue = Number.NEGATIVE_INFINITY;
    let alpha = Number.NEGATIVE_INFINITY;

    for (const move of moves) {
      const child = state.copy();
      child.applyMove(move);
      const value = this.minimax(child, this.maxDepth - 1, alpha, Number.POSITIVE_INFINITY);
      if (value > bestValue) {
        bestValue = value;
        bestMove = move;
      }
      alpha = Math.max(alpha, bestValue);
    }
    return bestMove;
  }

  private minimax(state: MancalaState, depth: number, alpha: number, beta: number): number {
    if (state.isGameOver() || depth === 0) return this.evaluate(state);
    const moves = state.legalMoves();
    if (moves.length === 0) return this.evaluate(state);

    if (state.currentPlayer() === this.aiPlayer) {
      let best = Number.NEGATIVE_INFINITY;
      for (const move of moves) {
        const child = state.copy();
        child.applyMove(move);
        best = Math.max(best, this.minimax(child, depth - 1, alpha, beta));
        alpha = Math.max(alpha, best);
        if (beta <= alpha) break;
      }
      return best;
    }

    let best = Number.POSITIVE_INFINITY;
    for (const move of moves) {
      const child = state.copy();
      child.applyMove(move);
      best = Math.min(best, this.minimax(child, depth - 1, alpha, beta));
      beta = Math.min(beta, best);
      if (beta <= alpha) break;
    }
    return best;
  }

  private evaluate(state: MancalaState): number {
    const opponent = 1 - this.aiPlayer;
    const myScore = state.scoreOf(this.aiPlayer);
    const opponentScore = state.scoreOf(opponent);
    let value = (myScore - opponentScore) * 100;

    if (state.isGameOver()) {
      if (myScore > opponentScore) value += MancalaAI.WIN_BONUS;
      else if (opponentScore > myScore) value -= MancalaAI.WIN_BONUS;
      return value;
    }

    let mySide = 0;
    let opponentSide = 0;
    for (let i = 0; i < MancalaState.SIZE; i += 1) {
      if (MancalaState.isStore(i)) continue;
      if (MancalaState.ownerOf(i) === this.aiPlayer) mySide += state.stones(i);
      else opponentSide += state.stones(i);
    }
    return value + mySide - opponentSide;
  }
}
