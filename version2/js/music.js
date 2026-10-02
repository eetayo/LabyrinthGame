// Musica de fondo generativa, pensada para escucharse mucho rato sin cansar:
//   - progresion de 4 acordes que se repite, con notas que cambian poco de un acorde a otro
//   - pad calido (senos y triangulos suaves), bajo sereno, arpegios tipo caja de musica y una
//     melodia escasa con sonido de campana
//   - todo pasa por un filtro paso bajo y una reverberacion, asi que no hay agudos ni ataques duros
//   - nada de ondas cuadradas ni de notas fuera de la tonalidad
// Cada mundo tiene su tonalidad, su tempo y su color. Se puede usar con un AudioContext normal o
// con un OfflineAudioContext (para pruebas).

// Acordes en semitonos sobre la tonica, ordenados para que las voces se muevan lo minimo.
const MAJOR = {                       // I - V - vi - IV
  scale: [0, 2, 4, 7, 9],             // pentatonica mayor
  chords: [[0, 4, 7], [-1, 2, 7], [0, 4, 9], [0, 5, 9]],
  bass: [0, -5, -3, -7],
};
const BRIGHT = {                      // I - vi - IV - V, mas alegre
  scale: [0, 2, 4, 7, 9],
  chords: [[0, 4, 7], [0, 4, 9], [0, 5, 9], [-1, 2, 7]],
  bass: [0, -3, -7, -5],
};
const MINOR = {                       // i - VI - III - VII, serio pero luminoso
  scale: [0, 3, 5, 7, 10],            // pentatonica menor
  chords: [[0, 3, 7], [-4, 0, 3], [-2, 3, 7], [-2, 2, 5]],
  bass: [0, -4, -9, -2],
};
const DREAMY = {                      // con septimas mayores, sonido de cristal
  scale: [0, 2, 4, 7, 9],
  chords: [[0, 4, 7, 11], [0, 4, 5, 9], [0, 4, 7, 9], [-1, 2, 7, 11]],
  bass: [0, -7, -3, -5],
};

export const STYLES = {
  menu: { ...MAJOR, tonic: 130.81, bpm: 72 },     // do mayor
  madera: { ...MAJOR, tonic: 196.0, bpm: 76 },    // sol mayor
  verde: { ...BRIGHT, tonic: 146.83, bpm: 88 },   // re mayor
  piedra: { ...MINOR, tonic: 146.83, bpm: 62 },   // re menor
  hielo: { ...DREAMY, tonic: 164.81, bpm: 58 },   // mi mayor
  mercado: { ...BRIGHT, tonic: 174.61, bpm: 92 }, // fa mayor
};

// Dibujos del arpegio: indices sobre las notas del acorde (mas su octava) en 8 corcheas.
const ARP_PATTERNS = [[0, 1, 2, 3, 2, 1, 2, 1], [0, 2, 1, 3, 1, 2, 3, 2], [0, 1, 2, 1, 3, 2, 1, 2]];

function mulberry32(seed) {
  let a = seed >>> 0;
  return () => {
    a = (a + 0x6d2b79f5) >>> 0;
    let t = a;
    t = Math.imul(t ^ (t >>> 15), t | 1);
    t ^= t + Math.imul(t ^ (t >>> 7), t | 61);
    return ((t ^ (t >>> 14)) >>> 0) / 4294967296;
  };
}

// Reverberacion sintetica: ruido con caida exponencial, oscurecido para que sea calida.
function makeImpulse(ac, seconds = 1.8) {
  const len = Math.floor(ac.sampleRate * seconds);
  const buf = ac.createBuffer(2, len, ac.sampleRate);
  const rnd = mulberry32(7);
  for (let ch = 0; ch < 2; ch++) {
    const d = buf.getChannelData(ch);
    let lp = 0;
    for (let i = 0; i < len; i++) {
      lp += ((rnd() * 2 - 1) - lp) * 0.32;
      d[i] = lp * Math.pow(1 - i / len, 2.6);
    }
  }
  return buf;
}

// dest: nodo de destino. opts.seed: semilla del azar (por defecto, distinta cada vez).
export function createMusic(ac, dest, opts = {}) {
  const rnd = mulberry32(opts.seed ?? (Date.now() & 0xffffff));
  const log = opts.log || null;   // si se pasa una lista, anota cada nota (para pruebas)
  const pick = list => list[Math.floor(rnd() * list.length)];

  // cadena comun: filtro que se abre y se cierra despacio, y reverberacion
  const tone = ac.createBiquadFilter();
  tone.type = 'lowpass';
  tone.frequency.value = 2800;
  tone.Q.value = 0.3;
  const lfo = ac.createOscillator();
  const lfoGain = ac.createGain();
  lfo.frequency.value = 0.05;
  lfoGain.gain.value = 500;
  lfo.connect(lfoGain).connect(tone.frequency);
  lfo.start();
  const dry = ac.createGain();
  dry.gain.value = 0.8;
  const wet = ac.createGain();
  wet.gain.value = 0.5;
  const reverb = ac.createConvolver();
  reverb.buffer = makeImpulse(ac);
  tone.connect(dry).connect(dest);
  tone.connect(reverb).connect(wet).connect(dest);

  let key = 'menu';
  let session = null;      // ganancia de la sesion actual; al cambiar de mundo se sustituye
  let running = false;
  let nextBar = 0;
  let barIndex = 0;
  let melodyIdx = 3;
  let queue = [];          // notas ya decididas que aun no se han creado; se crean poco a poco

  const newSession = () => {
    const g = ac.createGain();
    const t = ac.currentTime;
    g.gain.setValueAtTime(0.0001, t);
    g.gain.linearRampToValueAtTime(1, t + 1.2);
    g.connect(tone);
    return g;
  };
  const fadeOut = (g, secs) => {
    const t = ac.currentTime;
    g.gain.cancelScheduledValues(t);
    g.gain.setValueAtTime(g.gain.value, t);
    g.gain.linearRampToValueAtTime(0.0001, t + secs);
    if (typeof setTimeout === 'function') setTimeout(() => { try { g.disconnect(); } catch { /* ya desconectado */ } }, (secs + 0.3) * 1000);
  };

  const freq = semis => STYLES[key].tonic * Math.pow(2, semis / 12);
  const panner = pan => {
    if (!ac.createStereoPanner) return session;
    const p = ac.createStereoPanner();
    p.pan.value = pan;
    p.connect(session);
    return p;
  };
  const note = (voice, semis, t) => { if (log) log.push({ voice, semis, t }); };

  // Nota larga y suave: dos osciladores muy poco desafinados, entrada lenta y salida larga.
  function pad(semis, t, dur, peak) {
    note('pad', semis, t);
    const g = ac.createGain();
    g.gain.setValueAtTime(0, t);
    g.gain.linearRampToValueAtTime(peak, t + 0.9);
    g.gain.setValueAtTime(peak, t + dur);
    g.gain.linearRampToValueAtTime(0, t + dur + 1.6);
    g.connect(session);
    [['sine', 0, 1], ['triangle', 5, 0.45]].forEach(([type, cents, amp]) => {
      const o = ac.createOscillator();
      const og = ac.createGain();
      o.type = type;
      o.frequency.value = freq(semis);
      o.detune.value = cents;
      og.gain.value = amp;
      o.connect(og).connect(g);
      o.start(t);
      o.stop(t + dur + 1.7);
    });
  }

  // Nota pulsada tipo caja de musica o campana: seno con un parcial y decaimiento largo.
  function pluck(semis, t, peak, decay, out) {
    note('pluck', semis, t);
    const f = freq(semis);
    [[1, 1, 1], [2, 0.24, 0.35]].forEach(([mult, amp, dmul]) => {
      const o = ac.createOscillator();
      const g = ac.createGain();
      o.type = 'sine';
      o.frequency.value = f * mult;
      g.gain.setValueAtTime(0.0001, t);
      g.gain.exponentialRampToValueAtTime(peak * amp, t + 0.008);
      g.gain.exponentialRampToValueAtTime(0.0001, t + decay * dmul);
      o.connect(g).connect(out);
      o.start(t);
      o.stop(t + decay * dmul + 0.05);
    });
  }

  function bass(semis, t, peak, decay) {
    note('bass', semis, t);
    const o = ac.createOscillator();
    const o2 = ac.createOscillator();
    const g = ac.createGain();
    o.type = 'sine';
    o2.type = 'sine';
    o.frequency.value = freq(semis);
    o2.frequency.value = freq(semis) * 2;   // el segundo armonico se oye en altavoces pequenos
    const g2 = ac.createGain();
    g2.gain.value = 0.3;
    g.gain.setValueAtTime(0.0001, t);
    g.gain.exponentialRampToValueAtTime(peak, t + 0.04);
    g.gain.exponentialRampToValueAtTime(0.0001, t + decay);
    o.connect(g);
    o2.connect(g2).connect(g);
    g.connect(session);
    o.start(t); o2.start(t);
    o.stop(t + decay + 0.05); o2.stop(t + decay + 0.05);
  }

  function scheduleBar(t) {
    const st = STYLES[key];
    const beat = 60 / st.bpm;
    const bar = beat * 4;
    const q = (time, fn) => queue.push({ time, fn });
    const ci = barIndex % 4;
    const chord = st.chords[ci];
    const arpBase = 12;
    const melBase = st.tonic > 150 ? 12 : 24;

    // pad: el acorde entero, con un ligero arpegiado al entrar, y su octava aguda muy suave
    chord.forEach((s, i) => q(t + i * 0.07, () => pad(s, t + i * 0.07, bar, 0.03)));
    q(t + chord.length * 0.07, () => pad(chord[0] + 12, t + chord.length * 0.07, bar, 0.012));

    // bajo: en el primer tiempo y, a veces, en el tercero
    q(t, () => bass(st.bass[ci], t, 0.11, beat * 3.2));
    if (rnd() < 0.55) q(t + beat * 2, () => bass(st.bass[ci], t + beat * 2, 0.07, beat * 1.9));

    // arpegio: corcheas sueltas; se omiten algunas para que respire
    const tones = [...chord.map(s => s + arpBase), chord[0] + arpBase + 12];
    const pattern = pick(ARP_PATTERNS);
    const eighth = beat / 2;
    pattern.forEach((idx, i) => {
      if (rnd() > (i % 2 === 0 ? 0.92 : 0.55)) return;
      const s = tones[idx % tones.length];
      const jitter = (rnd() - 0.5) * 0.014;
      const at = t + i * eighth + jitter, pk = (i === 0 ? 0.058 : 0.042) * (0.85 + rnd() * 0.3), pan = (rnd() - 0.5) * 0.9;
      q(at, () => pluck(s, at, pk, 1.5, panner(pan)));
    });

    // melodia: pocas notas, siempre de la pentatonica; en los tiempos fuertes, del acorde
    if (rnd() > 0.22) {
      const ladder = [...st.scale.map(s => s + melBase), ...st.scale.slice(0, 3).map(s => s + melBase + 12)];
      const chordClasses = chord.map(s => ((s % 12) + 12) % 12);
      const slots = [0, 1.5, 2, 3, 3.5].filter(() => rnd() < 0.62);
      if (!slots.includes(0) && rnd() < 0.6) slots.unshift(0);
      slots.forEach(b => {
        melodyIdx = Math.max(0, Math.min(ladder.length - 1, melodyIdx + pick([-2, -1, -1, 0, 1, 1, 2])));
        if (Number.isInteger(b) && b % 2 === 0) {
          // tiempo fuerte: la nota de acorde mas cercana
          let best = melodyIdx, dist = 99;
          ladder.forEach((s, i) => {
            const d = Math.abs(i - melodyIdx);
            if (chordClasses.includes(((s % 12) + 12) % 12) && d < dist) { best = i; dist = d; }
          });
          melodyIdx = best;
        }
        const note_ = ladder[melodyIdx], at = t + b * beat + (rnd() - 0.5) * 0.02, pk = 0.07 * (0.85 + rnd() * 0.3), pan = (rnd() - 0.5) * 0.9;
        q(at, () => pluck(note_, at, pk, 2.6, panner(pan)));
      });
    }
    barIndex++;
  }

  return {
    get running() { return running; },
    get key() { return key; },
    start() {
      if (running) return;
      running = true;
      session = newSession();
      nextBar = ac.currentTime + 0.2;
      barIndex = 0;
    },
    stop() {
      if (!running) return;
      running = false;
      queue = [];
      fadeOut(session, 0.4);
    },
    // Cambia de mundo. Se cierra la sesion anterior y se abre otra, para que dos tonalidades
    // distintas no suenen a la vez.
    setKey(k) {
      if (!STYLES[k] || k === key) return;
      key = k;
      if (!running) return;
      fadeOut(session, 0.8);
      session = newSession();
      nextBar = ac.currentTime + 0.5;
      barIndex = 0;
      melodyIdx = 3;
      queue = [];
    },
    // Programa los compases que empiezan antes de `until` (segundos del reloj del contexto).
    schedule(until) {
      while (running && nextBar < until) {
        scheduleBar(nextBar);
        nextBar += (60 / STYLES[key].bpm) * 4;
      }
      // crea solo las notas que suenan en la proxima fraccion de segundo: asi no se crean decenas
      // de nodos de golpe y el movil no se atasca
      if (queue.length) {
        queue.sort((a, b) => a.time - b.time);
        let n = 0;
        while (n < queue.length && queue[n].time < until - 0.9) n++;
        queue.splice(0, n).forEach(e => e.fn());
      }
    },
  };
}
