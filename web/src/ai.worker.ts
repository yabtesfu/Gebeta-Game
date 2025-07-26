import { MancalaAI } from './logic/MancalaAI';
import { MancalaState } from './logic/MancalaState';

interface SearchRequest {
  requestId: number;
  board: number[];
  currentPlayer: number;
  maxDepth: number;
  aiPlayer: number;
}

interface WorkerScope {
  onmessage: ((event: MessageEvent<SearchRequest>) => void) | null;
  postMessage(message: unknown): void;
}

const worker = self as unknown as WorkerScope;

worker.onmessage = (event) => {
  const request = event.data;
  const state = MancalaState.fromBoard(request.board, request.currentPlayer);
  const ai = new MancalaAI(request.maxDepth, request.aiPlayer);
  const move = ai.chooseMove(state);
  worker.postMessage({ requestId: request.requestId, move, stats: ai.lastSearchStats() });
};
