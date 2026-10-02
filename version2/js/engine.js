// Motor del juego: reglas identicas a Pelota.moverPelota (2011).
// Todas las bolas se mueven a la vez; cada una rueda hasta una pared, el borde u otra bola.
// Se gana cuando una bola se detiene sobre una casilla meta.

export const SIZE = 5;
export const DIRS = {
  up: [-1, 0],
  down: [1, 0],
  left: [0, -1],
  right: [0, 1],
};

export const WORLDS = 5;
export const LEVELS_PER_WORLD = 20;

export class Board {
  constructor(level) {
    this.level = level;
    this.h = new Set(level.h.map(([r, c]) => r * SIZE + c));
    this.v = new Set(level.v.map(([r, c]) => r * SIZE + c));
    this.goals = level.goals.map(([r, c]) => ({ r, c }));
    this.reset();
  }

  reset() {
    this.balls = this.level.balls.map(([r, c], id) => ({ id, r, c }));
    this.moves = 0;
    this.won = false;
  }

  blocked(r, c, dir) {
    const [dr, dc] = DIRS[dir];
    const nr = r + dr, nc = c + dc;
    if (nr < 0 || nr >= SIZE || nc < 0 || nc >= SIZE) return true;
    if (dir === 'down') return this.h.has(r * SIZE + c);
    if (dir === 'up') return this.h.has(nr * SIZE + c);
    if (dir === 'right') return this.v.has(r * SIZE + c);
    return this.v.has(r * SIZE + nc);
  }

  // Devuelve un paso por bola, de la mas adelantada a la mas atrasada:
  // {id, from:{r,c}, to:{r,c}, dist, by} donde `by` es lo que la ha frenado: 'wall',
  // 'edge' (borde del tablero) o el id de otra bola. steps.moved indica si alguna se movio;
  // si ninguna se mueve no cuenta como movimiento (igual que en el original).
  move(dir) {
    if (this.won) return null;
    const [dr, dc] = DIRS[dir];
    const order = [...this.balls].sort((a, b) => (b.r * dr + b.c * dc) - (a.r * dr + a.c * dc));
    const steps = [];
    for (const b of order) {
      const from = { r: b.r, c: b.c };
      let by;
      for (;;) {
        const nr = b.r + dr, nc = b.c + dc;
        if (nr < 0 || nr >= SIZE || nc < 0 || nc >= SIZE) { by = 'edge'; break; }
        if (this.blocked(b.r, b.c, dir)) { by = 'wall'; break; }
        const other = this.balls.find(o => o !== b && o.r === nr && o.c === nc);
        if (other) { by = other.id; break; }
        b.r = nr;
        b.c = nc;
      }
      const dist = Math.abs(b.r - from.r) + Math.abs(b.c - from.c);
      steps.push({ id: b.id, from, to: { r: b.r, c: b.c }, dist, by });
    }
    steps.moved = steps.some(s => s.dist > 0);
    if (steps.moved) {
      this.moves++;
      this.won = this.balls.some(b => this.isGoal(b.r, b.c));
    }
    return steps;
  }

  isGoal(r, c) {
    return this.goals.some(g => g.r === r && g.c === c);
  }

  // Bolas pegadas sin pared entre ellas (en el original cambiaban de imagen al "chocar").
  touching() {
    const pairs = [];
    for (const a of this.balls) {
      for (const b of this.balls) {
        if (a.id >= b.id) continue;
        if (a.r === b.r && Math.abs(a.c - b.c) === 1 && !this.blocked(a.r, Math.min(a.c, b.c), 'right')) pairs.push([a, b]);
        if (a.c === b.c && Math.abs(a.r - b.r) === 1 && !this.blocked(Math.min(a.r, b.r), a.c, 'down')) pairs.push([a, b]);
      }
    }
    return pairs;
  }
}

// Estrellas por movimientos, como Score.devolverNumeroEstrellas del original, salvo que un
// nivel superado da siempre al menos 1 (en 2011 podia dar 0).
export function starsFor(moves, record) {
  if (moves <= record) return 3;
  if (moves <= record * 1.5) return 2;
  return 1;
}

// Estrellas finales segun la ayuda usada: con pista, maximo 2; si el juego resolvio el
// nivel, 1. Nunca menos de 1.
export function starsWithHelp(moves, record, { hinted = false, solved = false } = {}) {
  if (solved) return 1;
  return Math.max(1, Math.min(starsFor(moves, record), hinted ? 2 : 3));
}

export const worldOf = n => Math.floor((n - 1) / LEVELS_PER_WORLD);
