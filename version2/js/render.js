// Interior del tablero en canvas, encajado dentro del marco pintado del diseno.
//
// Geometria (en casillas): el radio de la bola mas medio grosor de pared suman media
// casilla, asi que una bola parada contra una pared la toca de verdad. Contra el marco o
// contra otra bola se desplaza dentro de su casilla hasta tocarlos: las bolas chocan.
//
// Profundidad: el tablero se ve ligeramente desde el sur. Las paredes muestran su cara
// frontal y las bolas se apoyan sobre su sombra. Paredes y bolas se pintan ordenadas de
// fondo a frente: una bola delante de una pared la tapa, y detras queda tapada por ella.
import { SIZE } from './engine.js';
import { roundRect, TAU, rng } from './themes.js';

const clamp01 = t => Math.max(0, Math.min(1, t));
const easeOutBack = t => 1 + 2.4 * Math.pow(t - 1, 3) + 1.4 * Math.pow(t - 1, 2);
function easeOutBounce(t) {
  const n = 7.5625, d = 2.75;
  if (t < 1 / d) return n * t * t;
  if (t < 2 / d) { t -= 1.5 / d; return n * t * t + 0.75; }
  if (t < 2.5 / d) { t -= 2.25 / d; return n * t * t + 0.9375; }
  t -= 2.625 / d;
  return n * t * t + 0.984375;
}

const R = 0.45;          // radio de bola
const T = 0.5 - R;       // medio grosor de pared a efectos de contacto
const WALL = 0.07;       // medio grosor dibujado (algo mayor, para que la pared se vea)
const DEPTH = 0.22;      // altura aparente de las paredes
const LIFT = 0.07;       // cuanto se eleva la bola sobre su punto de apoyo
const HOLE = 0.42;       // radio del agujero
const MARGIN = 0.24;     // margen del canvas alrededor del suelo, para pintar sobre el marco
const ACCEL = 80;        // casillas/s^2: todas las bolas aceleran igual, como al inclinar
const VEC = { up: [0, -1], down: [0, 1], left: [-1, 0], right: [1, 0] };
const ARROWS = { up: 0, right: Math.PI / 2, down: Math.PI, left: -Math.PI / 2 };   // giro de la flecha
const CONFETTI = ['#ff5a5a', '#ffd23a', '#5ed26a', '#4aa8ff', '#ff8be0', '#ffffff'];

export const BOARD_MARGIN = MARGIN;

// Rebote tras un choque: sale despedida, vuelve y da un segundo bote mas corto.
function bounceAt(ms) {
  if (ms < 150) return Math.sin((Math.PI * ms) / 150);
  if (ms < 250) return 0.34 * Math.sin((Math.PI * (ms - 150)) / 100);
  return 0;
}

function sparkle(ctx, x, y, s, alpha) {
  if (s <= 0 || alpha <= 0) return;
  ctx.save();
  ctx.globalAlpha *= alpha;
  ctx.fillStyle = '#fff';
  ctx.beginPath();
  for (let k = 0; k < 8; k++) {
    const a = (k * TAU) / 8, rr = k % 2 ? s * 0.22 : s;
    ctx.lineTo(x + Math.cos(a) * rr, y + Math.sin(a) * rr);
  }
  ctx.fill();
  ctx.restore();
}

export class BoardView {
  constructor(canvas) {
    this.canvas = canvas;
    this.ctx = canvas.getContext('2d');
    this.particles = [];
    this.rings = [];
    this.anims = null;
    this.sink0 = null;
    this.label = '';
    this.onImpact = null;   // (dist, 'wall' | 'edge' | 'ball', fuerza 0..1)
    this.onLand = null;     // una bola aterriza en la entrada del nivel
    this.onSettled = null;
  }

  setLevel(board, theme, seed = 1, label = '') {
    this.board = board;
    this.theme = theme;
    this.seed = seed;
    this.label = label;
    this.anims = null;
    this.particles = [];
    this.rings = [];
    this.sink0 = null;
    this.rays = null;
    this.intro = performance.now();
    // x, y: punto de apoyo de la bola, en casillas (el centro de la casilla c es c + 0.5)
    this.display = board.balls.map((b, i) => ({
      id: b.id, x: b.c + 0.5, y: b.r + 0.5, rot: 0, squash: null, bounce: null,
      landed: false, glintAt: this.intro + 1800 + i * 900 + Math.random() * 1500, glint0: 0,
    }));
    this.resize(true);
  }

  resize(force) {
    const css = this.canvas.clientWidth;
    const dpr = Math.min(window.devicePixelRatio || 1, 3);
    if (!force && css === this.css && dpr === this.dpr) return;
    if (!css) return;
    this.css = css;
    this.dpr = dpr;
    // el canvas es cuadrado; el CSS lo ajusta al hueco del marco (casi cuadrado)
    this.canvas.width = Math.round(css * dpr);
    this.canvas.height = Math.round(css * dpr);
    this.cs = css / (SIZE + MARGIN * 2);
    this.o = this.cs * MARGIN; // origen del suelo dentro del canvas
    this.S = this.cs * SIZE;
    this.buildStatic();
  }

  // casillas -> px del canvas
  px(x, y) {
    return [this.o + x * this.cs, this.o + y * this.cs];
  }

  // Paredes como rectangulos [x, y, w, h] sobre el suelo (px relativos al suelo); las
  // contiguas en linea se unen y se recortan al borde para no pisar el marco por los lados.
  wallRects() {
    const { board, cs, S } = this;
    const wt = cs * WALL * 2;
    const rects = [];
    const add = (x, y, w, h) => {
      const x0 = Math.max(0, x), x1 = Math.min(S, x + w);
      const y0 = Math.max(0, y), y1 = Math.min(S, y + h);
      rects.push([x0, y0, x1 - x0, y1 - y0]);
    };
    for (let r = 0; r < SIZE - 1; r++) {
      for (let q = 0; q < SIZE; q++) {
        if (!board.h.has(r * SIZE + q)) continue;
        let end = q;
        while (end + 1 < SIZE && board.h.has(r * SIZE + end + 1)) end++;
        add(q * cs - wt / 2, (r + 1) * cs - wt / 2, (end - q + 1) * cs + wt, wt);
        q = end;
      }
    }
    for (let q = 0; q < SIZE - 1; q++) {
      for (let r = 0; r < SIZE; r++) {
        if (!board.v.has(r * SIZE + q)) continue;
        let end = r;
        while (end + 1 < SIZE && board.v.has((end + 1) * SIZE + q)) end++;
        add((q + 1) * cs - wt / 2, r * cs - wt / 2, wt, (end - r + 1) * cs + wt);
        r = end;
      }
    }
    return rects;
  }

  // ------------------------------------------------------------ capas estaticas
  buildStatic() {
    const { dpr, cs, o, S, theme, board } = this;
    const floor = document.createElement('canvas');
    floor.width = this.canvas.width;
    floor.height = this.canvas.height;
    const c = floor.getContext('2d');
    c.scale(dpr, dpr);
    c.translate(o, o);
    c.save();
    roundRect(c, 0, 0, S, S, S * (theme.radius || 0));
    c.clip();
    theme.floor(c, S, this.seed * 7919 + 13);

    // vineta y sombra que proyecta el marco (mas marcada arriba y a la izquierda)
    const vg = c.createRadialGradient(S / 2, S / 2, S * 0.35, S / 2, S / 2, S * 0.78);
    vg.addColorStop(0, 'rgba(0,0,0,0)');
    vg.addColorStop(1, 'rgba(0,0,0,0.22)');
    c.fillStyle = vg;
    c.fillRect(0, 0, S, S);
    const band = (x0, y0, x1, y1, a) => {
      const g = c.createLinearGradient(x0, y0, x1, y1);
      g.addColorStop(0, `rgba(0,0,0,${a})`);
      g.addColorStop(1, 'rgba(0,0,0,0)');
      c.fillStyle = g;
      c.fillRect(0, 0, S, S);
    };
    band(0, 0, 0, cs * 0.4, 0.5);
    band(0, 0, cs * 0.24, 0, 0.3);
    band(S, 0, S - cs * 0.1, 0, 0.12);
    band(0, S, 0, S - cs * 0.08, 0.1);

    // rotulo grabado con el nivel
    if (this.label) {
      c.font = `700 ${cs * 0.17}px Fredoka, system-ui, sans-serif`;
      c.textAlign = 'center';
      c.textBaseline = 'middle';
      c.letterSpacing = `${cs * 0.02}px`;
      c.fillStyle = 'rgba(255,255,255,0.45)';
      c.fillText(this.label, S / 2, cs * 0.17 + cs * 0.012);
      c.fillStyle = theme.label;
      c.fillText(this.label, S / 2, cs * 0.17);
    }

    // agujeros
    for (const g of board.goals) {
      const x = (g.c + 0.5) * cs, y = (g.r + 0.5) * cs, rad = cs * HOLE;
      // bisel: borde superior en sombra, inferior iluminado
      c.beginPath();
      c.arc(x, y, rad * 1.09, 0, TAU);
      const bevel = c.createLinearGradient(x, y - rad, x, y + rad);
      bevel.addColorStop(0, 'rgba(0,0,0,0.45)');
      bevel.addColorStop(0.5, 'rgba(0,0,0,0.15)');
      bevel.addColorStop(1, theme.hole.rim);
      c.fillStyle = bevel;
      c.fill();
      // interior: arriba se ve la pared del fondo, hacia abajo se pierde en negro
      c.save();
      c.beginPath();
      c.arc(x, y, rad, 0, TAU);
      c.clip();
      c.fillStyle = '#080605';
      c.fillRect(x - rad, y - rad, rad * 2, rad * 2);
      const inner = c.createRadialGradient(x, y + rad * 0.62, rad * 0.55, x, y + rad * 0.15, rad * 1.15);
      inner.addColorStop(0, 'rgba(0,0,0,0)');
      inner.addColorStop(0.3, 'rgba(0,0,0,0)');
      inner.addColorStop(1, theme.hole.wall);
      c.fillStyle = inner;
      c.fillRect(x - rad, y - rad, rad * 2, rad * 2);
      c.restore();
      c.beginPath();
      c.arc(x, y, rad, 0, TAU);
      c.strokeStyle = 'rgba(0,0,0,0.6)';
      c.lineWidth = cs * 0.02;
      c.stroke();
    }

    // sombras de las paredes: largas, segun su altura
    const rects = this.wallRects();
    const d = cs * DEPTH;
    c.shadowColor = 'rgba(0,0,0,0.55)';
    c.shadowBlur = cs * 0.09 * dpr;
    c.shadowOffsetX = cs * 0.12 * dpr;
    c.shadowOffsetY = cs * 0.16 * dpr;
    c.fillStyle = 'rgba(0,0,0,0.6)';
    for (const [x, y, w, h] of rects) { roundRect(c, x, y, w, h, cs * 0.03); c.fill(); }
    c.restore();
    this.floorLayer = floor;

    // cada pared en su propia imagen, para intercalarlas con las bolas al pintar
    const r2 = rng(this.seed + 5);
    const pad = Math.ceil(cs * 0.06);
    this.walls = rects.map(([x, y, w, h], i) => {
      const sp = document.createElement('canvas');
      sp.width = Math.ceil((w + pad * 2) * dpr);
      sp.height = Math.ceil((h + d + pad * 2) * dpr);
      const wc = sp.getContext('2d');
      wc.scale(dpr, dpr);
      wc.translate(pad - x, pad - (y - d));
      theme.wall(wc, x, y, w, h, d, Math.floor(r2() * 1e6));
      return {
        img: sp, x: o + x - pad, y: o + y - d - pad, w: w + pad * 2, h: h + d + pad * 2,
        key: o + y + h, rect: [o + x, o + y, w, h], order: i, shake: null,
      };
    });
  }

  // Flecha de pista (y del tutorial): dorada, por encima de todo, empujando hacia su lado.
  arrow(c, dir, now) {
    const { cs, o } = this;
    const rot = ARROWS[dir];
    const s = cs * 0.42;
    const beat = (now / 620) % 1;
    const push = (Math.sin(beat * Math.PI) - 0.3) * cs * 0.3;
    c.save();
    c.translate(o + this.S / 2, o + this.S / 2);   // en el centro del tablero
    c.rotate(rot);
    c.translate(0, -push);
    c.lineJoin = 'round';
    c.beginPath();
    c.moveTo(0, -s * 1.15);
    c.lineTo(s * 1.1, -s * 0.05);
    c.lineTo(s * 0.5, -s * 0.05);
    c.lineTo(s * 0.5, s * 1.05);
    c.lineTo(-s * 0.5, s * 1.05);
    c.lineTo(-s * 0.5, -s * 0.05);
    c.lineTo(-s * 1.1, -s * 0.05);
    c.closePath();
    c.globalAlpha = 0.78 + 0.22 * Math.sin(beat * Math.PI);
    c.shadowColor = 'rgba(0,0,0,0.5)';
    c.shadowBlur = cs * 0.08 * this.dpr;
    c.shadowOffsetY = cs * 0.04 * this.dpr;
    const g = c.createLinearGradient(0, -s * 1.15, 0, s * 1.05);
    g.addColorStop(0, '#fff6a8');
    g.addColorStop(0.5, '#ffd23a');
    g.addColorStop(1, '#ff9f12');
    c.fillStyle = g;
    c.fill();
    c.shadowColor = 'transparent';
    c.strokeStyle = '#9a5a00';
    c.lineWidth = cs * 0.03;
    c.stroke();
    c.restore();
  }

  // ------------------------------------------------------------ animacion
  // steps llega del motor ordenado de la bola mas adelantada a la mas atrasada, asi que el
  // destino de la bola que frena a otra ya esta calculado cuando le toca a la de detras.
  animateMove(steps, dir) {
    const now = performance.now();
    const vec = VEC[dir];
    const horiz = vec[0] !== 0;
    const sign = vec[0] || vec[1];
    const target = {};
    let longest = 0;
    this.anims = steps.map(s => {
      const d = this.display.find(b => b.id === s.id);
      d.bounce = null;
      const cell = (horiz ? s.to.c : s.to.r) + 0.5;
      let along;
      if (s.by === 'wall') along = cell + sign * (0.5 - T - R);
      else if (s.by === 'edge') along = cell + sign * (0.5 - R);
      else along = target[s.by] - sign * R * 2;
      target[s.id] = along;
      const from = horiz ? d.x : d.y;
      const len = Math.max(0, (along - from) * sign);
      // al rodar, la bola vuelve al centro de su carril; si no se mueve, se queda donde esta
      const perpFrom = horiz ? d.y : d.x;
      const perpTo = s.dist > 0 ? (horiz ? s.to.r : s.to.c) + 0.5 : perpFrom;
      const dur = Math.sqrt((2 * len) / ACCEL) * 1000;
      longest = Math.max(longest, dur);
      return { id: s.id, by: s.by, horiz, sign, vec, from, len, perpFrom, perpTo, t0: now, dur, done: false };
    });
    // ninguna bola puede avanzar: se aprietan contra lo que las frena
    if (!steps.moved) for (const s of steps) this.squash(s.id, vec, 0.22, now);
    this.animEnd = now + longest + 130;
    return longest;
  }

  squash(id, vec, amount, t0) {
    const d = this.display.find(b => b.id === id);
    d.squash = { vec, amount, t0 };
  }

  get busy() {
    return this.anims != null || this.sink0 != null;
  }

  // La bola ganadora cae al agujero; las demas lo celebran con un salto.
  sink() {
    const now = performance.now();
    this.sink0 = now;
    for (const d of this.display) {
      const b = this.board.balls.find(k => k.id === d.id);
      d.bounce = null;
      if (!this.board.isGoal(b.r, b.c)) { d.hop0 = now + 120; continue; }
      d.sink = { x: d.x, y: d.y, tx: b.c + 0.5, ty: b.r + 0.5 };
      const [x, y] = this.px(d.sink.tx, d.sink.ty);
      this.rays = { x, y, t0: now };
      this.rings.push({ x, y, t0: now, dur: 700, r0: this.cs * 0.3, r1: this.cs * 1.7, color: '255,226,110', width: this.cs * 0.09 });
      this.rings.push({ x, y, t0: now + 160, dur: 700, r0: this.cs * 0.3, r1: this.cs * 1.3, color: '255,255,255', width: this.cs * 0.05 });
      this.burst(x, y, 36, 'rgba(255,214,80,0.95)', 1.8);
      this.confetti(x, y, 70);
    }
  }

  burst(x, y, n, color, power = 1) {
    for (let i = 0; i < n; i++) {
      const a = Math.random() * TAU;
      const sp = (0.4 + Math.random()) * this.cs * 0.05 * power;
      this.particles.push({ x, y, vx: Math.cos(a) * sp, vy: Math.sin(a) * sp, g: 0.02, life: 1, decay: 0.012 + Math.random() * 0.02, size: this.cs * (0.03 + Math.random() * 0.05), color, kind: Math.random() > 0.5 ? 'star' : 'dot' });
    }
  }

  confetti(x, y, n) {
    for (let i = 0; i < n; i++) {
      const a = -Math.PI / 2 + (Math.random() - 0.5) * 2.6;
      const sp = this.cs * (0.06 + Math.random() * 0.12);
      this.particles.push({
        x, y, vx: Math.cos(a) * sp, vy: Math.sin(a) * sp, g: this.cs * 0.0035, life: 1, decay: 0.006 + Math.random() * 0.006,
        size: this.cs * (0.05 + Math.random() * 0.05), color: CONFETTI[i % CONFETTI.length], kind: 'rect',
        rot: Math.random() * TAU, vr: (Math.random() - 0.5) * 0.4,
      });
    }
  }

  dust(x, y, vec, n = 10, spread = 2.4) {
    for (let i = 0; i < n; i++) {
      const a = Math.atan2(-vec[1], -vec[0]) + (Math.random() - 0.5) * spread;
      const sp = this.cs * (0.012 + Math.random() * 0.034);
      this.particles.push({ x, y, vx: Math.cos(a) * sp, vy: Math.sin(a) * sp, g: 0.02, life: 1, decay: 0.04 + Math.random() * 0.03, size: this.cs * (0.035 + Math.random() * 0.05), color: this.theme.dust, kind: 'dot' });
    }
  }

  update(now) {
    if (this.anims) {
      for (const a of this.anims) {
        const d = this.display.find(b => b.id === a.id);
        const t = Math.max(0, now - a.t0) / 1000;
        const run = Math.min(a.len, 0.5 * ACCEL * t * t);
        const pos = a.from + a.sign * run;
        const perp = a.perpFrom + (a.perpTo - a.perpFrom) * clamp01((now - a.t0) / 130);
        const prev = a.horiz ? d.x : d.y;
        d.rot += (pos - prev) / R * 0.9;
        if (a.horiz) { d.x = pos; d.y = perp; } else { d.y = pos; d.x = perp; }
        // estela de polvo mientras rueda, mas densa cuanto mas rapido va
        if (run < a.len && Math.random() < Math.min(0.55, ACCEL * t * 0.03)) {
          const [tx, ty] = this.px(d.x - a.vec[0] * R * 0.8, d.y - a.vec[1] * R * 0.8 + R * 0.45);
          this.particles.push({ x: tx + (Math.random() - 0.5) * this.cs * 0.3, y: ty, vx: -a.vec[0] * this.cs * 0.008, vy: -a.vec[1] * this.cs * 0.008 - this.cs * 0.004, g: 0, life: 0.7, decay: 0.035, size: this.cs * (0.03 + Math.random() * 0.04), color: this.theme.dust, kind: 'dot' });
        }
        if (!a.done && run >= a.len) {
          a.done = true;
          if (a.len > 0.02) this.impact(a, d, now);
        }
      }
      if (now >= this.animEnd) {
        this.anims = null;
        this.onSettled?.();
      }
    }
    for (const p of this.particles) {
      p.x += p.vx;
      p.y += p.vy;
      p.vx *= 0.96;
      p.vy = p.vy * 0.96 + p.g;
      if (p.vr) p.rot += p.vr;
      p.life -= p.decay;
    }
    this.particles = this.particles.filter(p => p.life > 0);
    this.rings = this.rings.filter(r => now < r.t0 + r.dur);
  }

  impact(a, d, now) {
    const speed = Math.sqrt(2 * ACCEL * a.len);          // casillas/s al chocar
    const strength = clamp01(speed / 24);
    const ball = typeof a.by === 'number';
    this.squash(a.id, a.vec, 0.1 + strength * 0.22, now);
    d.bounce = { vec: a.vec, amp: (ball ? 0.07 : 0.045) + strength * (ball ? 0.13 : 0.08), t0: now };
    // punto de contacto: el borde delantero de la bola
    const [cx, cy] = this.px(d.x + a.vec[0] * R, d.y + a.vec[1] * R);
    if (ball) {
      // choque contra otra bola: ella acusa el golpe con un empujon y se deforma
      const other = this.display.find(b => b.id === a.by);
      this.squash(a.by, a.vec, 0.08 + strength * 0.16, now);
      other.jolt = { vec: a.vec, amp: 0.025 + strength * 0.04, t0: now };
      const y = cy - this.cs * LIFT;
      this.rings.push({ x: cx, y, t0: now, dur: 320, r0: this.cs * 0.05, r1: this.cs * (0.35 + strength * 0.35), color: '255,255,255', width: this.cs * 0.05 });
      this.burst(cx, y, 9 + Math.round(strength * 8), 'rgba(255,244,190,0.95)', 0.7 + strength * 0.7);
    } else {
      this.dust(cx, cy, a.vec, 7 + Math.round(strength * 9));
      this.rings.push({ x: cx, y: cy, t0: now, dur: 260, r0: this.cs * 0.04, r1: this.cs * (0.22 + strength * 0.2), color: '255,255,255', width: this.cs * 0.03 });
      // la pared golpeada tiembla
      if (a.by === 'wall') {
        const px = cx + a.vec[0] * this.cs * 0.06, py = cy + a.vec[1] * this.cs * 0.06;
        const w = this.walls.find(k => px >= k.rect[0] - 2 && px <= k.rect[0] + k.rect[2] + 2 && py >= k.rect[1] - 2 && py <= k.rect[1] + k.rect[3] + 2);
        if (w) w.shake = { vec: a.vec, amp: this.cs * (0.02 + strength * 0.035), t0: now };
      }
    }
    this.onImpact?.(a.len, ball ? 'ball' : a.by, strength);
  }

  // ------------------------------------------------------------ pintado
  draw(now) {
    const { ctx, dpr, cs, css } = this;
    if (!this.floorLayer) return;
    this.update(now);
    ctx.setTransform(dpr, 0, 0, dpr, 0, 0);
    ctx.clearRect(0, 0, css, css);
    ctx.drawImage(this.floorLayer, 0, 0, css, css);

    // agujeros: aro de luz que respira y destellos girando por el borde
    for (const [i, g] of this.board.goals.entries()) {
      const [x, y] = this.px(g.c + 0.5, g.r + 0.5);
      const rad = cs * HOLE;
      const pulse = 0.5 + 0.5 * Math.sin(now / 520 + i);
      ctx.beginPath();
      ctx.arc(x, y, rad * (1.13 + pulse * 0.05), 0, TAU);
      ctx.strokeStyle = `rgba(255,255,255,${0.1 + pulse * 0.2})`;
      ctx.lineWidth = cs * 0.035;
      ctx.stroke();
      for (let k = 0; k < 3; k++) {
        const a = now / 1100 + i * 1.3 + (k * TAU) / 3;
        const tw = 0.5 + 0.5 * Math.sin(now / 230 + k * 2.1);
        sparkle(ctx, x + Math.cos(a) * rad * 1.12, y + Math.sin(a) * rad * 1.12, cs * (0.05 + tw * 0.06), 0.35 + tw * 0.6);
      }
    }

    // rayos de luz al ganar
    if (this.rays) {
      const t = (now - this.rays.t0) / 1300;
      if (t >= 1) this.rays = null;
      else {
        ctx.save();
        ctx.globalCompositeOperation = 'lighter';
        ctx.translate(this.rays.x, this.rays.y);
        ctx.rotate(now / 900);
        const len = cs * (0.8 + t * 1.6), a = Math.sin(Math.PI * Math.min(1, t * 1.15)) * 0.5;
        const g = ctx.createRadialGradient(0, 0, cs * 0.2, 0, 0, len);
        g.addColorStop(0, `rgba(255,236,150,${a})`);
        g.addColorStop(1, 'rgba(255,236,150,0)');
        ctx.fillStyle = g;
        for (let k = 0; k < 10; k++) {
          ctx.rotate(TAU / 10);
          ctx.beginPath();
          ctx.moveTo(0, 0);
          ctx.lineTo(len, -len * 0.1);
          ctx.lineTo(len, len * 0.1);
          ctx.fill();
        }
        ctx.restore();
      }
    }

    const rad = cs * R;
    const balls = this.display.map((d, i) => {
      let sx = 1, sy = 1, x = d.x, y = d.y, up = 0, alpha = 1, dark = 0, scale = 1;
      if (d.squash) {
        const t = Math.max(0, (now - d.squash.t0) / 280);
        if (t >= 1) d.squash = null;
        else {
          const k = d.squash.amount * Math.exp(-t * 4.5) * Math.cos(t * 13);
          const horiz = d.squash.vec[0] !== 0;
          sx = horiz ? 1 - k : 1 + k * 0.6;
          sy = horiz ? 1 + k * 0.6 : 1 - k;
        }
      }
      // rebote tras el choque y empujon recibido de otra bola
      if (d.bounce) {
        const ms = Math.max(0, now - d.bounce.t0);
        if (ms > 250) d.bounce = null;
        else { const k = bounceAt(ms) * d.bounce.amp; x -= d.bounce.vec[0] * k; y -= d.bounce.vec[1] * k; }
      }
      if (d.jolt) {
        const ms = Math.max(0, now - d.jolt.t0);
        if (ms > 170) d.jolt = null;
        else { const k = Math.sin((Math.PI * ms) / 170) * d.jolt.amp; x += d.jolt.vec[0] * k; y += d.jolt.vec[1] * k; }
      }
      // entrada del nivel: las bolas caen de arriba y botan
      const ti = clamp01((now - this.intro - 220 - i * 120) / 620);
      if (ti < 1) {
        up = (1 - easeOutBounce(ti)) * cs * 1.5;
        alpha = clamp01(ti * 5);
        if (!d.landed && ti >= 1 / 2.75) {
          d.landed = true;
          const [lx, ly] = this.px(d.x, d.y + R * 0.5);
          this.dust(lx, ly, [0, -1], 8, 6.3);
          this.onLand?.();
        }
      }
      // salto de celebracion de las bolas que no han entrado
      if (d.hop0 && now > d.hop0) {
        const t = (now - d.hop0) / 760;
        if (t < 1) up += Math.abs(Math.sin(t * Math.PI * 2)) * cs * 0.32 * (1 - t);
      }
      if (d.sink && this.sink0 && !this.anims) {
        const t = clamp01((now - this.sink0) / 620);
        const e = t * t * (3 - 2 * t);
        // cae en espiral hacia el centro del agujero
        const sw = (1 - e) * 0.12 * Math.sin(Math.PI * t), ang = t * 9;
        x = d.sink.x + (d.sink.tx - d.sink.x) * e + Math.cos(ang) * sw;
        y = d.sink.y + (d.sink.ty - d.sink.y) * e + Math.sin(ang) * sw;
        if (t < 1) d.rot += 0.25;
        scale = 1 - e * 0.45;
        dark = e * 0.6;
        alpha = 1 - e * 0.15;
      }
      const [gx, gy] = this.px(x, y);
      return { d, gx, gy, sx, sy, scale, alpha, dark, up };
    });

    // sombras de las bolas sobre el suelo: alargada hacia abajo-derecha y oscura en el apoyo
    for (const b of balls) {
      const air = clamp01(b.up / (cs * 1.5));
      const k = b.scale * (1 - b.dark) * (1 - air * 0.55);
      const a = b.alpha * (1 - air * 0.6);
      ctx.save();
      ctx.globalAlpha = a;
      ctx.translate(b.gx + rad * (0.3 + air * 0.5), b.gy + rad * (0.34 + air * 0.4));
      ctx.rotate(0.5);
      ctx.scale(b.sx * k * 1.12, b.sy * k * 0.7);
      const sh = ctx.createRadialGradient(0, 0, rad * 0.15, 0, 0, rad * 1.1);
      sh.addColorStop(0, 'rgba(0,0,0,0.55)');
      sh.addColorStop(0.55, 'rgba(0,0,0,0.34)');
      sh.addColorStop(1, 'rgba(0,0,0,0)');
      ctx.fillStyle = sh;
      ctx.beginPath();
      ctx.arc(0, 0, rad * 1.1, 0, TAU);
      ctx.fill();
      ctx.restore();
      if (air < 0.05) {
        ctx.save();
        ctx.globalAlpha = a;
        ctx.translate(b.gx, b.gy + rad * 0.5);
        ctx.scale(k, k * 0.45);
        const ao = ctx.createRadialGradient(0, 0, 0, 0, 0, rad * 0.8);
        ao.addColorStop(0, 'rgba(0,0,0,0.5)');
        ao.addColorStop(1, 'rgba(0,0,0,0)');
        ctx.fillStyle = ao;
        ctx.beginPath();
        ctx.arc(0, 0, rad * 0.8, 0, TAU);
        ctx.fill();
        ctx.restore();
      }
    }

    // paredes y bolas, de fondo a frente
    const items = [
      ...this.walls.map(w => ({ key: w.key, wall: w })),
      ...balls.map(b => ({ key: b.gy, ball: b })),
    ].sort((a, b) => a.key - b.key);
    for (const it of items) {
      if (it.wall) this.drawWall(it.wall, now);
      else this.drawBall(it.ball, rad, now);
    }

    // ondas de choque
    for (const r of this.rings) {
      const t = (now - r.t0) / r.dur;
      if (t < 0) continue;
      const e = 1 - Math.pow(1 - t, 2);
      ctx.beginPath();
      ctx.arc(r.x, r.y, r.r0 + (r.r1 - r.r0) * e, 0, TAU);
      ctx.strokeStyle = `rgba(${r.color},${(1 - t) * 0.85})`;
      ctx.lineWidth = r.width * (1 - t * 0.7);
      ctx.stroke();
    }

    for (const p of this.particles) {
      ctx.globalAlpha = Math.max(0, Math.min(1, p.life));
      ctx.fillStyle = p.color;
      const s = p.size * (0.5 + Math.min(1, p.life) * 0.5);
      if (p.kind === 'rect') {
        ctx.save();
        ctx.translate(p.x, p.y);
        ctx.rotate(p.rot);
        const flip = Math.cos(p.rot * 2);
        ctx.fillRect(-s, -s * 0.5 * flip, s * 2, s * flip);
        ctx.restore();
        continue;
      }
      ctx.beginPath();
      if (p.kind === 'star') {
        for (let k = 0; k < 10; k++) {
          const a = (k * TAU) / 10 - Math.PI / 2, rr = k % 2 ? s * 0.5 : s * 1.2;
          ctx.lineTo(p.x + Math.cos(a) * rr, p.y + Math.sin(a) * rr);
        }
      } else ctx.arc(p.x, p.y, s, 0, TAU);
      ctx.fill();
    }
    ctx.globalAlpha = 1;

    // flecha de pista o del tutorial
    if (this.tutorialDir) this.arrow(ctx, this.tutorialDir, now);
  }

  drawWall(w, now) {
    const { ctx } = this;
    let dx = 0, dy = 0;
    if (w.shake) {
      const t = Math.max(0, (now - w.shake.t0) / 300);
      if (t >= 1) w.shake = null;
      else {
        const k = w.shake.amp * Math.exp(-t * 4) * Math.cos(t * 26);
        dx = w.shake.vec[0] * k;
        dy = w.shake.vec[1] * k;
      }
    }
    // entrada del nivel: las paredes se levantan del suelo, una tras otra
    const ti = clamp01((now - this.intro - w.order * 35) / 380);
    if (ti < 1) {
      const k = Math.max(0.02, easeOutBack(ti));
      ctx.globalAlpha = clamp01(ti * 4);
      ctx.drawImage(w.img, w.x, w.y + w.h * (1 - k), w.w, w.h * k);
      ctx.globalAlpha = 1;
    } else ctx.drawImage(w.img, w.x + dx, w.y + dy, w.w, w.h);
  }

  drawBall(b, rad, now) {
    const { ctx, cs, theme } = this;
    const d = b.d;
    ctx.save();
    ctx.globalAlpha = b.alpha;
    ctx.translate(b.gx, b.gy - cs * LIFT * (1 - b.dark * 2) - b.up);
    ctx.scale(b.sx * b.scale, b.sy * b.scale);
    theme.balls[d.id % theme.balls.length](ctx, rad, d.rot);
    if (b.dark) {
      ctx.beginPath();
      ctx.arc(0, 0, rad * 1.02, 0, TAU);
      ctx.fillStyle = `rgba(0,0,0,${b.dark})`;
      ctx.fill();
    }
    // destello ocasional en el brillo de la bola
    if (!this.sink0 && now > d.glintAt) {
      d.glint0 = now;
      d.glintAt = now + 2600 + Math.random() * 4200;
    }
    const gt = (now - d.glint0) / 520;
    if (d.glint0 && gt >= 0 && gt < 1) sparkle(ctx, -rad * 0.42, -rad * 0.46, rad * 0.5 * Math.sin(Math.PI * gt), 0.95);
    ctx.restore();
  }

  // Direccion segun el triangulo pulsado (mismo esquema que devolverZonaImpacto del original).
  zoneAt(clientX, clientY) {
    const r = this.canvas.getBoundingClientRect();
    const dx = (clientX - (r.left + r.width / 2)) / r.width;
    const dy = (clientY - (r.top + r.height / 2)) / r.height;
    if (Math.abs(dx) > Math.abs(dy)) return dx > 0 ? 'right' : 'left';
    return dy > 0 ? 'down' : 'up';
  }
}
