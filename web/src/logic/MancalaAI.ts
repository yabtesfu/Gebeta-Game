import { MancalaState, type MoveTrace } from './MancalaState';

type Bound = 'exact' | 'lower' | 'upper';

interface TableEntry {
  depth: number;
  value: number;
  bound: Bound;
  bestMove: number;
}

interface OrderedMove {
  pit: number;
  child: MancalaState;
  priority: number;
}

export interface SearchStats {
  completedDepth: number;
  nodes: number;
  cacheHits: number;
  cutoffs: number;
  elapsedMs: number;
  timedOut: boolean;
}

class SearchTimeout extends Error {}

/** Iterative-deepening alpha-beta AI with ordering and a transposition table. */
export class MancalaAI {
  private static readonly WIN_BONUS = 10_000;
  private static readonly MAX_TABLE_ENTRIES = 250_000;
  private readonly maxDepth: number;
  private readonly aiPlayer: number;
  private readonly timeBudgetMs: number;
  private readonly table = new Map<string, TableEntry>();
  private deadline = 0;
  private nodes = 0;
  private cacheHits = 0;
  private cutoffs = 0;
  private stats: SearchStats = {
    completedDepth: 0,
    nodes: 0,
    cacheHits: 0,
    cutoffs: 0,
    elapsedMs: 0,
    timedOut: false,
  };

  constructor(maxDepth: number, aiPlayer: number, timeBudgetMs = MancalaAI.defaultBudget(maxDepth)) {
    this.maxDepth = Math.max(1, maxDepth);
    this.aiPlayer = aiPlayer;
    this.timeBudgetMs = Math.max(1, timeBudgetMs);
  }

  private static defaultBudget(depth: number): number {
    if (depth <= 2) return 120;
    if (depth <= 6) return 700;
    return 1_800;
  }

  chooseMove(state: MancalaState): number {
    const legal = state.legalMoves();
    if (legal.length === 0) {
      this.stats = { completedDepth: 0, nodes: 0, cacheHits: 0, cutoffs: 0, elapsedMs: 0, timedOut: false };
      return -1;
    }

    const started = performance.now();
    this.deadline = started + this.timeBudgetMs;
    this.nodes = 0;
    this.cacheHits = 0;
    this.cutoffs = 0;
    this.table.clear();

    let bestMove = legal[0];
    let completedDepth = 0;
    let timedOut = false;
    for (let depth = 1; depth <= this.maxDepth; depth += 1) {
      try {
        bestMove = this.searchRoot(state, depth, bestMove);
        completedDepth = depth;
      } catch (error) {
        if (!(error instanceof SearchTimeout)) throw error;
        timedOut = true;
        break;
      }
    }

    this.stats = {
      completedDepth,
      nodes: this.nodes,
      cacheHits: this.cacheHits,
      cutoffs: this.cutoffs,
      elapsedMs: performance.now() - started,
      timedOut,
    };
    return bestMove;
  }

  lastSearchStats(): Readonly<SearchStats> {
    return { ...this.stats };
  }

  private searchRoot(state: MancalaState, depth: number, preferredMove: number): number {
    this.checkDeadline();
    const moves = this.orderedMoves(state, preferredMove);
    const maximizing = state.currentPlayer() === this.aiPlayer;
    let bestMove = moves[0].pit;
    let bestValue = maximizing ? Number.NEGATIVE_INFINITY : Number.POSITIVE_INFINITY;
    let alpha = Number.NEGATIVE_INFINITY;
    let beta = Number.POSITIVE_INFINITY;

    for (const move of moves) {
      const value = this.minimax(move.child, depth - 1, alpha, beta);
      if ((maximizing && value > bestValue) || (!maximizing && value < bestValue)) {
        bestValue = value;
        bestMove = move.pit;
      }
      if (maximizing) alpha = Math.max(alpha, bestValue);
      else beta = Math.min(beta, bestValue);
    }
    this.putEntry(this.key(state), { depth, value: bestValue, bound: 'exact', bestMove });
    return bestMove;
  }

  private minimax(state: MancalaState, depth: number, alpha: number, beta: number): number {
    this.checkDeadline();
    this.nodes += 1;
    if (state.isGameOver() || depth === 0) return this.evaluate(state);

    const key = this.key(state);
    const cached = this.table.get(key);
    const originalAlpha = alpha;
    const originalBeta = beta;
    let preferredMove = -1;
    if (cached) {
      preferredMove = cached.bestMove;
      if (cached.depth >= depth) {
        this.cacheHits += 1;
        if (cached.bound === 'exact') return cached.value;
        if (cached.bound === 'lower') alpha = Math.max(alpha, cached.value);
        else beta = Math.min(beta, cached.value);
        if (alpha >= beta) return cached.value;
      }
    }

    const moves = this.orderedMoves(state, preferredMove);
    if (moves.length === 0) return this.evaluate(state);
    const maximizing = state.currentPlayer() === this.aiPlayer;
    let best = maximizing ? Number.NEGATIVE_INFINITY : Number.POSITIVE_INFINITY;
    let bestMove = moves[0].pit;

    for (const move of moves) {
      const value = this.minimax(move.child, depth - 1, alpha, beta);
      if ((maximizing && value > best) || (!maximizing && value < best)) {
        best = value;
        bestMove = move.pit;
      }
      if (maximizing) alpha = Math.max(alpha, best);
      else beta = Math.min(beta, best);
      if (alpha >= beta) {
        this.cutoffs += 1;
        break;
      }
    }

    const bound: Bound = best <= originalAlpha ? 'upper' : best >= originalBeta ? 'lower' : 'exact';
    this.putEntry(key, { depth, value: best, bound, bestMove });
    return best;
  }

  private orderedMoves(state: MancalaState, preferredMove: number): OrderedMove[] {
    const player = state.currentPlayer();
    const scoreBefore = state.scoreOf(player);
    return state
      .legalMoves()
      .map((pit): OrderedMove => {
        const child = state.copy();
        const trace = child.applyMoveTraced(pit) as MoveTrace;
        let priority = (child.scoreOf(player) - scoreBefore) * 1_000;
        if (trace.extraTurn) priority += 600;
        if (trace.captured) priority += trace.capturedTotal * 120;
        if (trace.gameOver) priority += 2_000;
        if (pit === preferredMove) priority += 1_000_000;
        priority += player === 0 ? pit : 12 - pit;
        return { pit, child, priority };
      })
      .sort((left, right) => right.priority - left.priority);
  }

  private key(state: MancalaState): string {
    return `${state.toBoard().join(',')}|${state.currentPlayer()}`;
  }

  private putEntry(key: string, entry: TableEntry): void {
    const existing = this.table.get(key);
    if (existing && existing.depth > entry.depth) return;
    if (existing || this.table.size < MancalaAI.MAX_TABLE_ENTRIES) this.table.set(key, entry);
  }

  private checkDeadline(): void {
    if (performance.now() >= this.deadline) throw new SearchTimeout();
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
    for (let index = 0; index < MancalaState.SIZE; index += 1) {
      if (MancalaState.isStore(index)) continue;
      if (MancalaState.ownerOf(index) === this.aiPlayer) mySide += state.stones(index);
      else opponentSide += state.stones(index);
    }
    return value + mySide - opponentSide;
  }
}
