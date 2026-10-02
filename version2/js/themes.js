// Los 5 mundos. El marco del tablero, el fondo y la barra superior vienen pintados en
// img/worldN.webp (disenos de design/); aqui se dibuja por codigo el interior del tablero:
// suelo, paredes con volumen, bolas y agujeros. La luz viene de arriba a la izquierda y el
// tablero se ve ligeramente desde abajo, como en los disenos (las paredes muestran su cara
// frontal).

export const TAU = Math.PI * 2;
export const ART_W = 941;
export const ART_H = 1672;

export function rng(seed) {
  let s = seed >>> 0 || 1;
  return () => ((s = (s * 1664525 + 1013904223) >>> 0) / 4294967296);
}

export function roundRect(ctx, x, y, w, h, r) {
  ctx.beginPath();
  ctx.roundRect(x, y, w, h, Math.max(0, Math.min(r, w / 2, h / 2)));
}

// Bloque con volumen: cara frontal (abajo) + cara superior desplazada hacia arriba.
function block(c, x, y, w, h, d, r, { top, front, edge, shine = 0.35 }) {
  roundRect(c, x, y - d, w, h + d, r);
  c.fillStyle = front;
  c.fill();
  c.lineWidth = Math.max(1, d * 0.12);
  c.strokeStyle = edge;
  c.stroke();
  roundRect(c, x, y - d, w, h, r);
  c.fillStyle = top;
  c.fill();
  c.strokeStyle = edge;
  c.lineWidth = Math.max(0.8, d * 0.07);
  c.stroke();
  // brillo en el borde superior izquierdo
  if (shine) {
    c.save();
    roundRect(c, x, y - d, w, h, r);
    c.clip();
    c.strokeStyle = `rgba(255,255,255,${shine})`;
    c.lineWidth = d * 0.28;
    c.beginPath();
    c.moveTo(x + r * 0.6, y - d + d * 0.14);
    c.lineTo(x + w - r * 0.6, y - d + d * 0.14);
    c.moveTo(x + d * 0.14, y - d + r * 0.6);
    c.lineTo(x + d * 0.14, y - d + h - r * 0.6);
    c.stroke();
    c.restore();
  }
}

function lin(c, x0, y0, x1, y1, stops) {
  const g = c.createLinearGradient(x0, y0, x1, y1);
  stops.forEach(([o, col]) => g.addColorStop(o, col));
  return g;
}

// ---------------------------------------------------------------- bolas
function shadeSphere(c, rad, strength = 0.5) {
  const g = c.createRadialGradient(-rad * 0.3, -rad * 0.35, rad * 0.2, 0, 0, rad * 1.02);
  g.addColorStop(0, 'rgba(0,0,0,0)');
  g.addColorStop(0.65, `rgba(0,0,0,${strength * 0.25})`);
  g.addColorStop(1, `rgba(0,0,0,${strength})`);
  c.fillStyle = g;
  c.beginPath();
  c.arc(0, 0, rad, 0, TAU);
  c.fill();
}

function gloss(c, rad, a = 0.85) {
  // luz rebotada abajo
  c.save();
  c.beginPath();
  c.arc(0, 0, rad, 0, TAU);
  c.clip();
  const b = c.createRadialGradient(rad * 0.25, rad * 0.75, 0, rad * 0.25, rad * 0.75, rad * 0.7);
  b.addColorStop(0, `rgba(255,255,255,${a * 0.3})`);
  b.addColorStop(1, 'rgba(255,255,255,0)');
  c.fillStyle = b;
  c.fillRect(-rad, -rad, rad * 2, rad * 2);
  c.restore();
  // reflejo principal
  c.save();
  c.translate(-rad * 0.36, -rad * 0.44);
  c.rotate(-0.6);
  const h = c.createRadialGradient(0, 0, 0, 0, 0, rad * 0.42);
  h.addColorStop(0, `rgba(255,255,255,${a})`);
  h.addColorStop(0.6, `rgba(255,255,255,${a * 0.45})`);
  h.addColorStop(1, 'rgba(255,255,255,0)');
  c.scale(1, 0.62);
  c.fillStyle = h;
  c.beginPath();
  c.arc(0, 0, rad * 0.42, 0, TAU);
  c.fill();
  c.restore();
  c.fillStyle = `rgba(255,255,255,${a})`;
  c.beginPath();
  c.arc(-rad * 0.5, -rad * 0.5, rad * 0.07, 0, TAU);
  c.fill();
}

function outline(c, rad, color = 'rgba(0,0,0,0.35)') {
  c.beginPath();
  c.arc(0, 0, rad, 0, TAU);
  c.strokeStyle = color;
  c.lineWidth = Math.max(1, rad * 0.06);
  c.stroke();
}

const glossy = (light, base, dark) => (c, rad) => {
  const g = c.createRadialGradient(-rad * 0.32, -rad * 0.38, rad * 0.08, 0, 0, rad);
  g.addColorStop(0, light);
  g.addColorStop(0.5, base);
  g.addColorStop(1, dark);
  c.beginPath();
  c.arc(0, 0, rad, 0, TAU);
  c.fillStyle = g;
  c.fill();
  shadeSphere(c, rad, 0.34);
  outline(c, rad, dark);
  gloss(c, rad);
};

function clipBall(c, rad, rot, base, paint) {
  c.save();
  c.beginPath();
  c.arc(0, 0, rad, 0, TAU);
  c.fillStyle = base;
  c.fill();
  c.clip();
  c.rotate(rot);
  paint();
  c.restore();
}

function soccer(c, rad, rot) {
  clipBall(c, rad, rot, '#fbfbf7', () => {
    const pent = (cx, cy, s, a0) => {
      c.beginPath();
      for (let k = 0; k < 5; k++) {
        const a = a0 + (k * TAU) / 5;
        c.lineTo(cx + Math.cos(a) * s, cy + Math.sin(a) * s);
      }
      c.closePath();
      c.fill();
    };
    c.fillStyle = '#1b1b1f';
    c.strokeStyle = '#1b1b1f';
    c.lineWidth = rad * 0.05;
    pent(0, 0, rad * 0.36, -Math.PI / 2);
    for (let k = 0; k < 5; k++) {
      const a = -Math.PI / 2 + (k * TAU) / 5;
      c.beginPath();
      c.moveTo(Math.cos(a) * rad * 0.36, Math.sin(a) * rad * 0.36);
      c.lineTo(Math.cos(a) * rad * 0.7, Math.sin(a) * rad * 0.7);
      c.stroke();
      const b = a + Math.PI / 5;
      pent(Math.cos(b) * rad * 0.98, Math.sin(b) * rad * 0.98, rad * 0.34, b);
    }
  });
  shadeSphere(c, rad, 0.42);
  outline(c, rad, 'rgba(40,40,50,0.55)');
  gloss(c, rad, 0.6);
}

function golf(c, rad, rot) {
  clipBall(c, rad, rot, '#ffffff', () => {
    const s = rad * 0.26;
    for (let j = -5; j <= 5; j++) {
      for (let i = -5; i <= 5; i++) {
        const x = (i + (j % 2 ? 0.5 : 0)) * s, y = j * s * 0.87;
        c.fillStyle = 'rgba(120,130,150,0.22)';
        c.beginPath();
        c.arc(x + s * 0.06, y + s * 0.06, s * 0.36, 0, TAU);
        c.fill();
        c.fillStyle = 'rgba(255,255,255,0.9)';
        c.beginPath();
        c.arc(x - s * 0.05, y - s * 0.05, s * 0.3, 0, TAU);
        c.fill();
      }
    }
  });
  shadeSphere(c, rad, 0.36);
  outline(c, rad, 'rgba(90,100,120,0.55)');
  gloss(c, rad, 0.5);
}

function tennis(c, rad, rot) {
  clipBall(c, rad, rot, '#d7ec3a', () => {
    c.strokeStyle = '#fbfff0';
    c.lineWidth = rad * 0.13;
    for (const s of [-1, 1]) {
      c.beginPath();
      c.arc(s * rad * 1.15, 0, rad * 0.85, 0, TAU);
      c.stroke();
    }
  });
  shadeSphere(c, rad, 0.4);
  outline(c, rad, 'rgba(90,110,10,0.6)');
  gloss(c, rad, 0.45);
}

function spiked(c, rad, rot) {
  const body = rad * 0.8;
  const cone = (a, len, wid) => {
    c.save();
    c.rotate(a);
    c.beginPath();
    c.moveTo(body * 0.86, -wid);
    c.lineTo(body * 0.86 + len, 0);
    c.lineTo(body * 0.86, wid);
    c.closePath();
    c.fillStyle = lin(c, 0, -wid, 0, wid, [[0, '#e8eef5'], [0.5, '#98a6b5'], [1, '#4a5563']]);
    c.fill();
    c.strokeStyle = 'rgba(20,26,34,0.6)';
    c.lineWidth = rad * 0.035;
    c.stroke();
    c.restore();
  };
  for (let k = 0; k < 8; k++) cone(rot + (k * TAU) / 8, rad * 0.34, rad * 0.13);
  const g = c.createRadialGradient(-body * 0.3, -body * 0.35, body * 0.05, 0, 0, body);
  g.addColorStop(0, '#c9d6e2');
  g.addColorStop(0.45, '#6f8293');
  g.addColorStop(1, '#273240');
  c.beginPath();
  c.arc(0, 0, body, 0, TAU);
  c.fillStyle = g;
  c.fill();
  outline(c, body, 'rgba(15,20,28,0.7)');
  // puas de la cara frontal
  for (let k = 0; k < 4; k++) {
    const a = rot * 0.6 + (k * TAU) / 4 + 0.5;
    const x = Math.cos(a) * body * 0.45, y = Math.sin(a) * body * 0.45;
    const s = c.createRadialGradient(x - body * 0.04, y - body * 0.05, 0, x, y, body * 0.17);
    s.addColorStop(0, '#ffffff');
    s.addColorStop(0.4, '#aab8c6');
    s.addColorStop(1, '#3a4654');
    c.fillStyle = s;
    c.beginPath();
    c.arc(x, y, body * 0.16, 0, TAU);
    c.fill();
  }
  c.save();
  c.scale(0.8, 0.8);
  gloss(c, rad, 0.4);
  c.restore();
}

function watermelon(c, rad, rot) {
  clipBall(c, rad, rot, '#2f8f2f', () => {
    c.strokeStyle = '#0f4a1c';
    c.lineWidth = rad * 0.2;
    c.lineCap = 'round';
    for (let k = -3; k <= 3; k++) {
      c.beginPath();
      for (let t = -1.1; t <= 1.1; t += 0.08) {
        const bulge = Math.cos(t * 1.25);
        c.lineTo(k * rad * 0.36 * bulge + Math.sin(t * 9 + k * 2) * rad * 0.035, t * rad);
      }
      c.stroke();
    }
    c.strokeStyle = 'rgba(160,230,110,0.35)';
    c.lineWidth = rad * 0.05;
    for (let k = -3; k <= 2; k++) {
      c.beginPath();
      for (let t = -1.1; t <= 1.1; t += 0.1) c.lineTo((k + 0.5) * rad * 0.36 * Math.cos(t * 1.25), t * rad);
      c.stroke();
    }
  });
  shadeSphere(c, rad, 0.5);
  outline(c, rad, 'rgba(8,40,14,0.7)');
  gloss(c, rad, 0.5);
}

const BALL = {
  yellow: glossy('#fff6b0', '#ffc60a', '#c77700'),
  purple: glossy('#d9b8ff', '#7b3fe4', '#3b1591'),
  red: glossy('#ffc0b0', '#f0452f', '#9a1408'),
  pink: glossy('#ffd0ee', '#f23fa8', '#a3126a'),
  green: glossy('#c8ffc0', '#2fc24a', '#0d7a2a'),
  orange: glossy('#ffe2a0', '#ff9a16', '#c25a00'),
  pearl: glossy('#ffffff', '#efe9dd', '#a89f90'),
  soccer, golf, tennis, spiked, watermelon,
};

// ---------------------------------------------------------------- suelos
function planks(c, S, seed) {
  const r = rng(seed);
  c.fillStyle = lin(c, 0, 0, S, S, [[0, '#f2c58a'], [1, '#e0a765']]);
  c.fillRect(0, 0, S, S);
  const cols = [0, 0.19, 0.4, 0.6, 0.79, 1];
  for (let i = 0; i < 5; i++) {
    const x0 = cols[i] * S, x1 = cols[i + 1] * S;
    c.fillStyle = `rgba(${i % 2 ? '150,90,40' : '255,230,180'},${0.05 + r() * 0.07})`;
    c.fillRect(x0, 0, x1 - x0, S);
    // vetas: curvas largas y algun nudo
    c.save();
    c.beginPath();
    c.rect(x0, 0, x1 - x0, S);
    c.clip();
    for (let k = 0; k < 9; k++) {
      const bx = x0 + r() * (x1 - x0), amp = (x1 - x0) * (0.05 + r() * 0.18), ph = r() * 6, f = 3 + r() * 5;
      c.beginPath();
      for (let y = 0; y <= S; y += S / 40) c.lineTo(bx + Math.sin((y / S) * f + ph) * amp, y);
      c.strokeStyle = `rgba(176,108,52,${0.14 + r() * 0.18})`;
      c.lineWidth = S * (0.002 + r() * 0.005);
      c.stroke();
    }
    // veta "catedral": arcos anidados
    for (let n = 0; n < 2; n++) {
      const kx = x0 + (x1 - x0) * (0.35 + r() * 0.3), ky = S * (0.1 + r() * 0.8);
      const up = r() > 0.5 ? 1 : -1, len = S * (0.16 + r() * 0.14);
      for (let q = 1; q <= 5; q++) {
        const half = (x1 - x0) * 0.09 * q;
        c.beginPath();
        c.moveTo(kx - half, ky + up * len);
        c.quadraticCurveTo(kx, ky - up * len * 0.28 * q, kx + half, ky + up * len);
        c.strokeStyle = `rgba(168,102,50,${0.2 - q * 0.025})`;
        c.lineWidth = S * 0.0035;
        c.stroke();
      }
    }
    c.restore();
    if (i) {
      c.fillStyle = 'rgba(120,70,30,0.35)';
      c.fillRect(x0 - S * 0.0015, 0, S * 0.003, S);
      c.fillStyle = 'rgba(255,240,210,0.3)';
      c.fillRect(x0 + S * 0.0015, 0, S * 0.002, S);
    }
  }
  for (const y of [0.245 + r() * 0.02, 0.715 + r() * 0.02]) {
    const a = Math.floor(r() * 3), b = a + 2;
    c.fillStyle = 'rgba(120,70,30,0.3)';
    c.fillRect(cols[a] * S, y * S, (cols[b] - cols[a]) * S, S * 0.003);
  }
}

function grass(c, S, seed) {
  const r = rng(seed);
  c.fillStyle = lin(c, 0, 0, S, S, [[0, '#5fbf2e'], [0.5, '#4aa823'], [1, '#3f9a20']]);
  c.fillRect(0, 0, S, S);
  for (let i = 0; i < 26; i++) {
    const g = c.createRadialGradient(0, 0, 0, 0, 0, S * (0.08 + r() * 0.14));
    const dark = r() > 0.5;
    g.addColorStop(0, dark ? 'rgba(30,110,20,0.28)' : 'rgba(170,235,80,0.24)');
    g.addColorStop(1, 'rgba(0,0,0,0)');
    c.save();
    c.translate(r() * S, r() * S);
    c.fillStyle = g;
    c.fillRect(-S * 0.25, -S * 0.25, S * 0.5, S * 0.5);
    c.restore();
  }
  c.lineCap = 'round';
  for (let i = 0; i < 2600; i++) {
    const x = r() * S, y = r() * S, len = S * (0.008 + r() * 0.014), a = -Math.PI / 2 + (r() - 0.5) * 1.6;
    const v = r();
    c.strokeStyle = v > 0.6 ? `rgba(190,245,110,${0.25 + r() * 0.3})` : v > 0.3 ? `rgba(40,130,25,${0.3 + r() * 0.3})` : `rgba(110,200,50,${0.3 + r() * 0.3})`;
    c.lineWidth = S * 0.0026;
    c.beginPath();
    c.moveTo(x, y);
    c.lineTo(x + Math.cos(a) * len, y + Math.sin(a) * len);
    c.stroke();
  }
  for (let i = 0; i < 7; i++) {
    const x = S * (0.06 + r() * 0.88), y = S * (0.06 + r() * 0.88), s = S * (0.009 + r() * 0.005);
    c.save();
    c.translate(x, y);
    if (r() > 0.45) {
      c.fillStyle = 'rgba(255,255,255,0.92)';
      for (let k = 0; k < 5; k++) {
        c.beginPath();
        c.ellipse(Math.cos((k * TAU) / 5) * s, Math.sin((k * TAU) / 5) * s, s * 0.75, s * 0.5, (k * TAU) / 5, 0, TAU);
        c.fill();
      }
      c.fillStyle = '#ffd24a';
      c.beginPath();
      c.arc(0, 0, s * 0.55, 0, TAU);
      c.fill();
    } else {
      c.fillStyle = 'rgba(120,215,70,0.9)';
      for (let k = 0; k < 3; k++) {
        c.beginPath();
        c.arc(Math.cos((k * TAU) / 3) * s * 0.8, Math.sin((k * TAU) / 3) * s * 0.8, s * 0.8, 0, TAU);
        c.fill();
      }
    }
    c.restore();
  }
}

function flagstones(c, S, seed) {
  const r = rng(seed);
  c.fillStyle = '#4a4339';
  c.fillRect(0, 0, S, S);
  const rows = 8;
  const rh = S / rows;
  const gap = S * 0.0055;
  for (let j = 0; j < rows; j++) {
    let x = -r() * S * 0.08;
    while (x < S) {
      const w = S * (0.1 + r() * 0.14);
      const v = 150 + Math.floor(r() * 36);
      const bx = x + gap, by = j * rh + gap, bw = w - gap * 2, bh = rh - gap * 2;
      roundRect(c, bx, by, bw, bh, S * 0.007);
      c.fillStyle = lin(c, bx, by, bx + bw, by + bh, [[0, `rgb(${v + 22},${v + 10},${v - 14})`], [1, `rgb(${v - 10},${v - 22},${v - 46})`]]);
      c.fill();
      // bisel
      c.save();
      c.clip();
      c.lineWidth = S * 0.006;
      c.strokeStyle = 'rgba(255,250,235,0.34)';
      c.beginPath();
      c.moveTo(bx, by + bh);
      c.lineTo(bx, by);
      c.lineTo(bx + bw, by);
      c.stroke();
      c.strokeStyle = 'rgba(30,24,16,0.34)';
      c.beginPath();
      c.moveTo(bx + bw, by);
      c.lineTo(bx + bw, by + bh);
      c.lineTo(bx, by + bh);
      c.stroke();
      c.restore();
      if (r() > 0.55) {
        let px = x + w * r(), py = j * rh + rh * r();
        c.beginPath();
        c.moveTo(px, py);
        for (let k = 0; k < 4; k++) { px += (r() - 0.5) * w * 0.4; py += (r() - 0.3) * rh * 0.3; c.lineTo(px, py); }
        c.strokeStyle = 'rgba(50,42,30,0.45)';
        c.lineWidth = S * 0.0022;
        c.stroke();
      }
      x += w;
    }
  }
  for (let i = 0; i < 1600; i++) {
    c.fillStyle = r() > 0.5 ? `rgba(255,255,255,${r() * 0.08})` : `rgba(0,0,0,${r() * 0.1})`;
    c.fillRect(r() * S, r() * S, S * 0.004, S * 0.004);
  }
}

function ice(c, S, seed) {
  const r = rng(seed);
  c.fillStyle = lin(c, 0, 0, S, S, [[0, '#a5dcfb'], [0.5, '#86c9f4'], [1, '#6fb6ea']]);
  c.fillRect(0, 0, S, S);
  for (let i = 0; i < 10; i++) {
    const g = c.createRadialGradient(0, 0, 0, 0, 0, S * (0.12 + r() * 0.2));
    g.addColorStop(0, `rgba(255,255,255,${0.12 + r() * 0.14})`);
    g.addColorStop(1, 'rgba(255,255,255,0)');
    c.save();
    c.translate(r() * S, r() * S);
    c.fillStyle = g;
    c.fillRect(-S * 0.35, -S * 0.35, S * 0.7, S * 0.7);
    c.restore();
  }
  c.lineCap = 'round';
  for (let i = 0; i < 46; i++) {
    let x = r() * S, y = r() * S;
    const a = r() * TAU, len = S * (0.04 + r() * 0.2);
    c.beginPath();
    c.moveTo(x, y);
    const segs = 2 + Math.floor(r() * 3);
    for (let k = 0; k < segs; k++) {
      x += Math.cos(a + (r() - 0.5) * 0.5) * len / segs;
      y += Math.sin(a + (r() - 0.5) * 0.5) * len / segs;
      c.lineTo(x, y);
    }
    c.strokeStyle = `rgba(255,255,255,${0.25 + r() * 0.4})`;
    c.lineWidth = S * (0.001 + r() * 0.0025);
    c.stroke();
  }
  for (let i = 0; i < 260; i++) {
    c.fillStyle = `rgba(255,255,255,${0.2 + r() * 0.5})`;
    c.beginPath();
    c.arc(r() * S, r() * S, S * (0.0008 + r() * 0.0016), 0, TAU);
    c.fill();
  }
}

function slabs(c, S, seed) {
  const r = rng(seed);
  c.fillStyle = '#b98f52';
  c.fillRect(0, 0, S, S);
  const n = 5, cs = S / n, gap = S * 0.004;
  for (let j = 0; j < n; j++) {
    for (let i = 0; i < n; i++) {
      const v = r() * 18;
      roundRect(c, i * cs + gap, j * cs + gap, cs - gap * 2, cs - gap * 2, S * 0.008);
      c.fillStyle = lin(c, i * cs, j * cs, (i + 1) * cs, (j + 1) * cs, [[0, `rgb(${236 - v},${198 - v},${140 - v})`], [1, `rgb(${220 - v},${178 - v},${118 - v})`]]);
      c.fill();
      c.strokeStyle = 'rgba(255,240,200,0.4)';
      c.lineWidth = S * 0.0025;
      c.stroke();
      if (r() > 0.5) {
        let px = (i + r()) * cs, py = (j + r()) * cs;
        c.beginPath();
        c.moveTo(px, py);
        for (let k = 0; k < 4; k++) { px += (r() - 0.5) * cs * 0.35; py += (r() - 0.5) * cs * 0.35; c.lineTo(px, py); }
        c.strokeStyle = 'rgba(140,96,44,0.4)';
        c.lineWidth = S * 0.002;
        c.stroke();
      }
    }
  }
  for (let i = 0; i < 1200; i++) {
    c.fillStyle = r() > 0.5 ? `rgba(255,245,215,${r() * 0.12})` : `rgba(120,80,30,${r() * 0.1})`;
    c.fillRect(r() * S, r() * S, S * 0.004, S * 0.004);
  }
}

// ---------------------------------------------------------------- paredes
function woodWall(c, x, y, w, h, d, seed) {
  const r = rng(seed);
  const rad = Math.min(w, h) * 0.16;
  block(c, x, y, w, h, d, rad, {
    top: lin(c, x, y - d, x + w, y - d + h, [[0, '#f0bd7e'], [1, '#d99a58']]),
    front: lin(c, 0, y + h - d, 0, y + h, [[0, '#b97838'], [1, '#8f5523']]),
    edge: '#7a4518',
  });
  c.save();
  roundRect(c, x, y - d, w, h, rad);
  c.clip();
  const vertical = h > w;
  for (let i = 0; i < 4; i++) {
    const t = 0.2 + i * 0.2 + (r() - 0.5) * 0.08;
    c.beginPath();
    for (let s = 0; s <= 1.001; s += 0.1) {
      const wob = Math.sin(s * 7 + i * 2) * Math.min(w, h) * 0.05;
      if (vertical) c.lineTo(x + w * t + wob, y - d + h * s);
      else c.lineTo(x + w * s, y - d + h * t + wob);
    }
    c.strokeStyle = 'rgba(150,88,36,0.4)';
    c.lineWidth = Math.max(0.8, Math.min(w, h) * 0.035);
    c.stroke();
  }
  c.restore();
}

function hedgeWall(c, x, y, w, h, d, seed) {
  const r = rng(seed);
  const rad = Math.min(w, h) * 0.2;
  block(c, x, y, w, h, d, rad, {
    top: '#2f9a2a',
    front: lin(c, 0, y + h - d, 0, y + h, [[0, '#a8672f'], [1, '#7c4718']]),
    edge: '#5a3210',
    shine: 0,
  });
  c.save();
  roundRect(c, x, y - d, w, h, rad);
  c.clip();
  const s = Math.min(w, h) * 0.3;
  const n = Math.ceil((w * h) / (s * s) * 2.4);
  for (let i = 0; i < n; i++) {
    const lx = x + r() * w, ly = y - d + r() * h, a = r() * TAU;
    const v = r();
    c.save();
    c.translate(lx, ly);
    c.rotate(a);
    c.fillStyle = v > 0.66 ? '#7fdc4a' : v > 0.33 ? '#46b835' : '#1f7f24';
    c.beginPath();
    c.moveTo(-s * 0.6, 0);
    c.quadraticCurveTo(0, -s * 0.5, s * 0.6, 0);
    c.quadraticCurveTo(0, s * 0.5, -s * 0.6, 0);
    c.fill();
    c.strokeStyle = 'rgba(10,70,15,0.45)';
    c.lineWidth = s * 0.07;
    c.stroke();
    c.restore();
  }
  c.restore();
  roundRect(c, x, y - d, w, h, rad);
  c.strokeStyle = 'rgba(12,70,18,0.8)';
  c.lineWidth = Math.max(1, d * 0.1);
  c.stroke();
}

function ironWall(c, x, y, w, h, d) {
  const rad = Math.min(w, h) * 0.12;
  block(c, x, y, w, h, d, rad, {
    top: lin(c, x, y - d, x + w, y - d + h, [[0, '#7d7986'], [0.5, '#5f5b69'], [1, '#4b4755']]),
    front: lin(c, 0, y + h - d, 0, y + h, [[0, '#3c3844'], [1, '#232029']]),
    edge: '#1b1820',
    shine: 0.28,
  });
  const vertical = h > w;
  const len = vertical ? h : w, th = vertical ? w : h;
  const n = Math.max(2, Math.round(len / (th * 1.15)));
  for (let i = 0; i < n; i++) {
    const t = (i + 0.5) / n;
    const cx = vertical ? x + w / 2 : x + len * t;
    const cy = (vertical ? y + len * t : y + h / 2) - d;
    const rr = th * 0.2;
    c.fillStyle = 'rgba(0,0,0,0.4)';
    c.beginPath();
    c.arc(cx + rr * 0.2, cy + rr * 0.3, rr, 0, TAU);
    c.fill();
    const g = c.createRadialGradient(cx - rr * 0.35, cy - rr * 0.4, 0, cx, cy, rr);
    g.addColorStop(0, '#f1eef5');
    g.addColorStop(0.45, '#9b97a6');
    g.addColorStop(1, '#3e3a48');
    c.fillStyle = g;
    c.beginPath();
    c.arc(cx, cy, rr, 0, TAU);
    c.fill();
  }
}

function iceWall(c, x, y, w, h, d, seed) {
  const r = rng(seed);
  const rad = Math.min(w, h) * 0.18;
  block(c, x, y, w, h, d, rad, {
    top: lin(c, x, y - d, x + w, y - d + h, [[0, '#e8f7ff'], [0.5, '#b9e2fb'], [1, '#8fcbf3']]),
    front: lin(c, 0, y + h - d, 0, y + h, [[0, '#6fb2e6'], [1, '#3f86c9']]),
    edge: '#3b7fc2',
    shine: 0.7,
  });
  c.save();
  roundRect(c, x, y - d, w, h, rad);
  c.clip();
  const vertical = h > w;
  const len = vertical ? h : w, th = vertical ? w : h;
  const n = Math.max(2, Math.round(len / (th * 0.9)));
  for (let i = 0; i < n; i++) {
    const a0 = (i / n) * len, a1 = ((i + 1) / n) * len, mid = (a0 + a1) / 2 + (r() - 0.5) * th * 0.3;
    const P = (u, v) => (vertical ? [x + v * th, y - d + u] : [x + u, y - d + v * th]);
    const tri = (pts, col) => { c.beginPath(); pts.forEach(p => c.lineTo(...p)); c.closePath(); c.fillStyle = col; c.fill(); };
    tri([P(a0, 0), P(mid, 0.5), P(a0, 1)], `rgba(255,255,255,${0.2 + r() * 0.3})`);
    tri([P(a0, 1), P(mid, 0.5), P(a1, 1)], `rgba(60,140,210,${0.18 + r() * 0.2})`);
    tri([P(a1, 0), P(mid, 0.5), P(a1, 1)], `rgba(120,190,240,${0.15 + r() * 0.2})`);
  }
  c.restore();
  // nieve en el extremo superior
  c.fillStyle = '#ffffff';
  c.beginPath();
  if (vertical) c.ellipse(x + w / 2, y - d + w * 0.2, w * 0.52, w * 0.3, 0, 0, TAU);
  else c.ellipse(x + w * 0.5, y - d + h * 0.08, w * 0.46, h * 0.22, 0, Math.PI, TAU);
  c.fill();
}

function sandWall(c, x, y, w, h, d, seed) {
  const r = rng(seed);
  const rad = Math.min(w, h) * 0.1;
  block(c, x, y, w, h, d, rad, {
    top: lin(c, x, y - d, x + w, y - d + h, [[0, '#ffe07a'], [0.5, '#f4c448'], [1, '#e2a932']]),
    front: lin(c, 0, y + h - d, 0, y + h, [[0, '#c98b22'], [1, '#9c6512']]),
    edge: '#8a5a10',
    shine: 0.5,
  });
  c.save();
  roundRect(c, x, y - d, w, h, rad);
  c.clip();
  const vertical = h > w;
  const len = vertical ? h : w, th = vertical ? w : h;
  for (let p = th * (1.6 + r()); p < len - th; p += th * (1.8 + r())) {
    c.beginPath();
    if (vertical) { c.moveTo(x, y - d + p); c.lineTo(x + w, y - d + p + (r() - 0.5) * th * 0.2); } else { c.moveTo(x + p, y - d); c.lineTo(x + p + (r() - 0.5) * th * 0.2, y - d + h); }
    c.strokeStyle = 'rgba(150,96,20,0.55)';
    c.lineWidth = Math.max(1, th * 0.05);
    c.stroke();
  }
  c.restore();
}

// ---------------------------------------------------------------- mundos
// floorRect: [x, y, ancho, alto] del hueco del tablero en el diseno (px de 941x1672).
export const THEMES = [
  {
    id: 'madera', name: 'Madera', art: 'img/world1.webp',
    floorRect: [81, 374, 782, 783], radius: 0,
    floor: planks, wall: woodWall,
    balls: [BALL.yellow, BALL.purple, BALL.red],
    hole: { rim: '#f6d29a', wall: '#8a5526' }, dust: 'rgba(255,236,200,0.85)',
    ink: '#3d2410', inkSoft: 'rgba(61,36,16,0.62)', label: 'rgba(110,62,20,0.5)',
  },
  {
    id: 'verde', name: 'Verde', art: 'img/world2.webp',
    floorRect: [93, 375, 768, 768], radius: 0,
    floor: grass, wall: hedgeWall,
    balls: [BALL.soccer, BALL.golf, BALL.tennis],
    hole: { rim: '#8fd65a', wall: '#6b4a2a' }, dust: 'rgba(200,255,150,0.85)',
    ink: '#3d2410', inkSoft: 'rgba(61,36,16,0.62)', label: 'rgba(15,70,10,0.55)',
  },
  {
    id: 'piedra', name: 'Piedra', art: 'img/world3.webp',
    floorRect: [88, 370, 765, 787], radius: 0,
    floor: flagstones, wall: ironWall,
    balls: [BALL.spiked, BALL.pearl, BALL.spiked],
    hole: { rim: '#cfc6b2', wall: '#5a5245' }, dust: 'rgba(240,235,220,0.85)',
    ink: '#2e2a26', inkSoft: 'rgba(46,42,38,0.62)', label: 'rgba(50,44,36,0.55)',
  },
  {
    id: 'hielo', name: 'Hielo', art: 'img/world4.webp',
    floorRect: [78, 370, 780, 793], radius: 0.03,
    floor: ice, wall: iceWall,
    balls: [BALL.pink, BALL.green, BALL.pink],
    hole: { rim: '#e9f8ff', wall: '#4a86c0' }, dust: 'rgba(255,255,255,0.95)',
    ink: '#15264a', inkSoft: 'rgba(21,38,74,0.62)', label: 'rgba(40,100,170,0.6)',
  },
  {
    id: 'mercado', name: 'Mercado', art: 'img/world5.webp',
    floorRect: [90, 374, 761, 772], radius: 0,
    floor: slabs, wall: sandWall,
    balls: [BALL.watermelon, BALL.orange, BALL.watermelon],
    hole: { rim: '#f7dba6', wall: '#9a6b2c' }, dust: 'rgba(255,240,200,0.9)',
    ink: '#3d2410', inkSoft: 'rgba(61,36,16,0.62)', label: 'rgba(130,84,24,0.55)',
  },
];
