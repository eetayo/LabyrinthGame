// Solucion mas corta desde la posicion actual, por busqueda en anchura. Los niveles son
// pequenos (como mucho unos miles de posiciones), asi que se calcula al instante y sirve
// para dar pistas desde donde este el jugador, no solo desde el principio.
import { Board } from './engine.js';

const DIRS = ['up', 'down', 'left', 'right'];
const key = balls => balls.map(b => b.r * 5 + b.c).join(',');

// Devuelve la lista de movimientos que quedan ([] si ya esta resuelto) o null si desde
// esta posicion el nivel no tiene salida.
export function solveFrom(board) {
  if (board.won) return [];
  const probe = new Board(board.level);
  const start = board.balls.map(b => ({ ...b }));
  const seen = new Set([key(start)]);
  let frontier = [{ balls: start, path: [] }];
  while (frontier.length) {
    const next = [];
    for (const node of frontier) {
      for (const dir of DIRS) {
        probe.balls = node.balls.map(b => ({ ...b }));
        probe.won = false;
        const steps = probe.move(dir);
        if (!steps.moved) continue;
        const path = [...node.path, dir];
        if (probe.won) return path;
        const k = key(probe.balls);
        if (seen.has(k)) continue;
        seen.add(k);
        next.push({ balls: probe.balls, path });
      }
    }
    frontier = next;
  }
  return null;
}
