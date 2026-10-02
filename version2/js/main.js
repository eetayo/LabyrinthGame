import { LEVELS, TUTORIAL } from './levels.js';
import { Board, starsWithHelp, worldOf, WORLDS, LEVELS_PER_WORLD } from './engine.js';
import { solveFrom } from './solver.js';
import { THEMES } from './themes.js';
import { BoardView, BOARD_MARGIN } from './render.js';
import { createSceneFx } from './scenefx.js';
import * as sfx from './audio.js';
import * as store from './storage.js';

const $ = s => document.querySelector(s);
const $$ = s => [...document.querySelectorAll(s)];

const view = new BoardView($('#board'));
// fondo animado (fuego, plantas, bandera) de cada pantalla; null si no hay WebGL
const sceneFx = { home: createSceneFx($('#home')), levels: createSceneFx($('#levels')), game: createSceneFx($('#game')) };

const game = {
  n: 1,            // nivel actual (1..100) o 0 para el tutorial
  board: null,
  queued: null,    // direccion pulsada durante la animacion
  tutorialStep: -1,
  hinted: false,   // se ha pedido pista en este intento: maximo 2 estrellas
  solved: false,   // el juego esta resolviendo (o ha resuelto) el nivel: 1 estrella
  auto: null,      // movimientos que le quedan a la resolucion automatica
  restarts: 0,     // reinicios seguidos del mismo nivel, para saber si esta atascado
};
let currentScreen = 'home';
let selectedWorld = 0;

// Casillas de nivel pintadas en img/levels.webp (px del diseno)
const TILE_X = [83, 284, 483, 687];
const TILE_Y = [467, 653, 844, 1029, 1217];
const TILE_W = 177, TILE_H = 166;

const ART = { home: 'img/menu.webp', levels: 'img/levels.webp' };
[...Object.values(ART), ...THEMES.map(t => t.art)].forEach(src => { new Image().src = src; });

// ------------------------------------------------------------ navegacion
function show(id) {
  currentScreen = id;
  $$('.screen').forEach(s => s.classList.toggle('active', s.id === id));
  if (id === 'home') { $('#home-stars').textContent = store.totalStars(); sfx.setMusic('menu'); }
  if (id === 'levels') renderLevels();
  if (id !== 'game') { game.board = null; setArt(ART[id]); }
  if (id !== 'game' && sceneFx[id]?.src !== ART[id]) sceneFx[id]?.setScene(ART[id]);
}

// arte que rellena lo que queda fuera del escenario 9:16
function setArt(src) { document.body.style.setProperty('--art', `url(${new URL(src, location.href).href})`); }

function openModal(id) { syncSettings(); $(`#${id}`).hidden = false; document.body.classList.add('dim'); }
function closeModals() { $$('.modal').forEach(m => (m.hidden = true)); document.body.classList.remove('dim'); }
const modalOpen = () => $$('.modal').some(m => !m.hidden);

function toast(msg) {
  const t = $('#toast');
  t.textContent = msg;
  t.hidden = false;
  clearTimeout(toast.timer);
  toast.timer = setTimeout(() => (t.hidden = true), 2400);
}

function setGold(el, text) {
  el.textContent = text;
  el.dataset.text = text;
}

const star = on => `<svg><use href="#i-star${on ? '' : '-off'}"/></svg>`;

// ------------------------------------------------------------ niveles
function renderLevels() {
  const w = selectedWorld;
  const t = THEMES[w];
  const open = store.worldUnlocked(w);
  sfx.setMusic(t.id);
  setGold($('#levels-title'), t.name);
  $('#levels-stars').textContent = `${store.worldStars(w)} / 60 estrellas`;

  const grid = $('#level-grid');
  grid.innerHTML = '';
  for (let i = 0; i < LEVELS_PER_WORLD; i++) {
    const n = w * LEVELS_PER_WORLD + i + 1;
    const unlocked = open && store.levelUnlocked(n);
    const tile = document.createElement('button');
    tile.className = 'level-tile' + (unlocked ? '' : ' locked') + (unlocked && !store.completed(n) ? ' current' : '');
    tile.style.cssText = `--x:${TILE_X[i % 4]};--y:${TILE_Y[Math.floor(i / 4)]};--w:${TILE_W};--h:${TILE_H};animation-delay:${i * 14}ms`;
    if (unlocked) {
      const s = store.starsOf(n);
      tile.innerHTML = `<span class="num">${n}</span><span class="stars">${[0, 1, 2].map(k => star(k < s)).join('')}</span>`;
      tile.setAttribute('aria-label', `Nivel ${n}, ${s} estrellas`);
      tile.addEventListener('click', () => { sfx.click(); startLevel(n); });
    } else {
      tile.innerHTML = `<svg class="lock"><use href="#i-lock"/></svg>`;
      tile.setAttribute('aria-label', `Nivel ${n} bloqueado`);
      tile.addEventListener('click', () => {
        sfx.bump();
        toast(open ? `Supera el nivel ${n - 1} para abrir este` : `Supera el nivel ${w * LEVELS_PER_WORLD} para abrir ${t.name}`);
      });
    }
    grid.appendChild(tile);
  }

  const dots = $('#world-dots');
  dots.innerHTML = '';
  THEMES.forEach((th, k) => {
    const d = document.createElement('button');
    d.className = k === w ? 'on' : '';
    d.setAttribute('aria-label', `Mundo ${k + 1}: ${th.name}`);
    d.addEventListener('click', () => setWorld(k));
    dots.appendChild(d);
  });
  $('[data-action=world-prev]').disabled = w === 0;
  $('[data-action=world-next]').disabled = w === WORLDS - 1;
}

function setWorld(w) {
  w = Math.max(0, Math.min(WORLDS - 1, w));
  if (w === selectedWorld) return;
  sfx.click();
  selectedWorld = w;
  renderLevels();
}

// ------------------------------------------------------------ juego
function startLevel(n) {
  closeModals();
  const tutorial = n === 0;
  const data = tutorial ? TUTORIAL : LEVELS[n - 1];
  const w = tutorial ? 0 : worldOf(n);
  const t = THEMES[w];
  game.restarts = n === game.n && currentScreen === 'game' ? game.restarts + 1 : 0;
  game.n = n;
  game.board = new Board(data);
  game.queued = null;
  game.hinted = false;
  game.solved = false;
  game.auto = null;
  clearTimeout(autoTimer);
  $('#help-btn').hidden = tutorial;
  sfx.setMusic(t.id);

  const screen = $('#game');
  screen.style.backgroundImage = `url(${t.art})`;
  screen.style.setProperty('--ink', t.ink);
  screen.style.setProperty('--ink-soft', t.inkSoft);
  setArt(t.art);
  if (sceneFx.game?.src !== t.art) sceneFx.game?.setScene(t.art);
  // el canvas sobresale un margen del hueco del marco: las bolas y paredes pegadas al
  // borde se pintan por delante del marco
  const [x, y, bw, bh] = t.floorRect;
  const mx = (bw / 5) * BOARD_MARGIN, my = (bh / 5) * BOARD_MARGIN;
  $('#board').style.cssText = `left:calc(${x - mx} * var(--u));top:calc(${y - my} * var(--u));width:calc(${bw + mx * 2} * var(--u));height:calc(${bh + my * 2} * var(--u))`;

  $('#hud-record').textContent = data.record;
  if (currentScreen !== 'game') show('game');
  view.setLevel(game.board, t, n || 999, tutorial ? 'TUTORIAL' : `NIVEL ${n}`);
  updateHud(false);
  if (tutorial) startTutorial(); else stopTutorial();
}

function updateHud(bump = true) {
  const el = $('#hud-moves');
  el.textContent = game.board.moves;
  if (bump) { el.classList.remove('bump'); void el.offsetWidth; el.classList.add('bump'); }
  // el boton de ayuda llama la atencion si el jugador parece atascado
  const stuck = game.n > 0 && !game.solved && (game.board.moves >= game.board.level.record * 2 || game.restarts >= 3);
  $('#help-btn').classList.toggle('nudge', stuck);
}

// Movimiento pedido por el jugador.
function input(dir) {
  if (currentScreen !== 'game' || !game.board || modalOpen() || game.board.won || game.auto) return;
  if (view.busy) { game.queued = dir; return; }
  if (game.tutorialStep >= 0 && !tutorialAllows(dir)) return;
  doMove(dir);
  if (game.tutorialStep >= 0) advanceTutorial(true);
}

function doMove(dir) {
  const steps = game.board.move(dir);
  if (!steps) return;
  if (game.tutorialStep < 0) view.tutorialDir = null;   // la flecha de pista vale para un movimiento
  const dur = view.animateMove(steps, dir);
  if (!steps.moved) { sfx.bump(); return; }
  sfx.roll(dur / 1000, view.theme.id);
  updateHud();
}

// ------------------------------------------------------------ ayudas
// Pista: maximo 2 estrellas. Resolver: el juego termina el nivel y da 1 estrella.
const DIR_NAME = { up: 'arriba', down: 'abajo', left: 'a la izquierda', right: 'a la derecha' };
let autoTimer = 0;

function openHelp() {
  if (!game.board || game.board.won || game.auto || game.n === 0) return;
  $('#help-note').textContent = game.hinted
    ? 'Ya has usado una pista en este intento: como máximo conseguirás 2 estrellas.'
    : 'Sin ayuda puedes conseguir las 3 estrellas.';
  openModal('help-modal');
}

// Quedan movimientos por hacer desde la posicion actual, o null si no tiene salida.
function remaining() {
  const path = solveFrom(game.board);
  if (!path) toast('Desde aquí no hay salida. Reinicia el nivel.');
  return path;
}

function giveHint() {
  closeModals();
  const path = remaining();
  if (!path) return;
  game.hinted = true;
  view.tutorialDir = path[0];
  const n = path.length;
  toast(`Mueve ${DIR_NAME[path[0]]}. ${n === 1 ? '¡Es el último movimiento!' : `Se resuelve en ${n} movimientos.`}`);
}

function solveForMe() {
  closeModals();
  const path = remaining();
  if (!path) return;
  game.solved = true;
  game.auto = path;
  game.queued = null;
  view.tutorialDir = null;
  $('#help-btn').classList.remove('nudge');
  autoStep();
}

// Un movimiento de la resolucion automatica; el siguiente lo lanza onSettled.
function autoStep() {
  clearTimeout(autoTimer);
  if (!game.auto || currentScreen !== 'game') return;
  if (modalOpen() || view.busy) { autoTimer = setTimeout(autoStep, 250); return; }
  const dir = game.auto.shift();
  if (dir) doMove(dir);
}

// kind: 'wall' | 'edge' | 'ball' (choque entre bolas)
view.onImpact = (dist, kind, strength) => {
  if (kind === 'ball') {
    sfx.clack(0.5 + strength * 0.5);
    sfx.haptic(8);
    return;
  }
  if (view._knockedThisMove) return;
  view._knockedThisMove = true;
  sfx.knock(view.theme.id, 0.45 + strength * 0.55);
  sfx.haptic(dist > 2 ? 18 : 10);
  if (dist >= 3.5) {
    const b = $('#board');
    b.classList.remove('shake');
    void b.offsetWidth;
    b.classList.add('shake');
  }
};

// entrada del nivel: cada bola suena al caer sobre el tablero
view.onLand = () => {
  if (currentScreen === 'game') sfx.knock(view.theme.id, 0.35);
};

view.onSettled = () => {
  view._knockedThisMove = false;
  if (game.board?.won) { game.auto = null; onWin(); return; }
  if (game.auto) { autoTimer = setTimeout(autoStep, 260); return; }
  if (game.queued) {
    const d = game.queued;
    game.queued = null;
    input(d);
  }
};

function onWin() {
  $('#toast').hidden = true;
  $('#help-btn').classList.remove('nudge');
  view.sink();
  sfx.goalSink();
  const b = game.board;
  if (game.n === 0) {
    store.state.tutorialDone = true;
    store.save();
    setTimeout(() => {
      stopTutorial();
      coach('<b>¡Perfecto!</b> Ya sabes jugar. Usa el mínimo de movimientos para ganar 3 estrellas.', 'Empezar', () => { selectedWorld = 0; show('levels'); });
    }, 1150);
    return;
  }
  const record = b.level.record;
  const { hinted, solved } = game;
  const stars = starsWithHelp(b.moves, record, { hinted, solved });
  const prev = store.bestOf(game.n);
  const improved = store.recordResult(game.n, b.moves, stars, !hinted && !solved) && prev != null;
  setTimeout(() => {
    $('#win-kicker').textContent = `Nivel ${game.n}`;
    const mov = `${b.moves} ${b.moves === 1 ? 'movimiento' : 'movimientos'}`;
    $('#win-title').textContent = $('#win-title').dataset.text = solved ? '¡Resuelto!' : '¡Superado!';
    $('#win-detail').textContent = solved
      ? 'Lo he resuelto por ti. Repítelo sin ayuda para ganar más estrellas.'
      : b.moves <= record ? `${mov}: ¡igualas el récord!` : `${mov} · récord ${record}`;
    // aviso si la pista ha costado una estrella
    const note = $('#win-help');
    note.hidden = !(hinted && !solved && b.moves <= record);
    $('#win-best').hidden = !improved;
    $$('#win-stars span').forEach((s, i) => {
      s.classList.toggle('got', i < stars);
      s.style.animationDelay = `${250 + i * 280}ms`;
    });
    const last = game.n === LEVELS.length;
    const nextOpensWorld = game.n % LEVELS_PER_WORLD === 0 && !last;
    $('#next-label').textContent = last ? 'Fin del juego' : nextOpensWorld ? `¡Mundo ${worldOf(game.n + 1) + 1}!` : 'Siguiente';
    openModal('win-modal');
    rainConfetti(stars);
    sfx.win(stars);
  }, 1150);
}

// Confeti que cae por detras del panel de victoria; mas cuantas mas estrellas.
function rainConfetti(stars) {
  const box = $('#win-modal .confetti');
  box.innerHTML = '';
  const colors = ['#ff5a5a', '#ffd23a', '#5ed26a', '#7fd0ff', '#ff8be0', '#ffffff'];
  for (let i = 0; i < 18 + stars * 16; i++) {
    const p = document.createElement('i');
    p.style.cssText = `left:${Math.random() * 100}%;background:${colors[i % colors.length]};` +
      `--dx:${(Math.random() - 0.5) * 30}cqw;--rot:${(Math.random() - 0.5) * 1400}deg;` +
      `animation-duration:${1.8 + Math.random() * 2.2}s;animation-delay:${Math.random() * 1.2}s`;
    box.appendChild(p);
  }
}

// ------------------------------------------------------------ tutorial
// Textos del tutorial de 2011; la secuencia final es la solucion real del nivel.
const TUTORIAL_STEPS = [
  { text: 'Hay que llevar <b>una</b> de estas pelotas al <b>agujero</b>.' },
  { text: 'Las pelotas se mueven <b>de manera conjunta</b>: todas a la vez.' },
  { text: 'Desliza el dedo, toca un lado del tablero o usa las flechas: <b>izquierda, derecha, arriba o abajo</b>.' },
  { text: 'Cada pelota avanza hasta chocar con <b>una pared</b> o con <b>otra pelota</b>.' },
  { text: 'Ahora te ayudaré a resolver este nivel.' },
  { text: 'Mueve a la <b>izquierda</b>', dir: 'left' },
  { text: 'Ahora <b>abajo</b>', dir: 'down' },
  { text: 'Otra vez a la <b>izquierda</b>', dir: 'left' },
  { text: 'Y por último, <b>arriba</b>', dir: 'up' },
];

function coach(html, button, onNext) {
  $('#coach').hidden = false;
  $('#coach-text').innerHTML = html;
  const btn = $('#coach-next');
  btn.hidden = !button;
  if (button) btn.textContent = button;
  btn.onclick = () => { sfx.click(); onNext?.(); };
}

function startTutorial() {
  game.tutorialStep = 0;
  showTutorialStep();
}

function stopTutorial() {
  game.tutorialStep = -1;
  view.tutorialDir = null;
  $('#coach').hidden = true;
}

function showTutorialStep() {
  const s = TUTORIAL_STEPS[game.tutorialStep];
  view.tutorialDir = s.dir ?? null;
  coach(s.text, s.dir ? null : 'Siguiente', () => advanceTutorial(false));
}

function tutorialAllows(dir) {
  const s = TUTORIAL_STEPS[game.tutorialStep];
  if (s?.dir === dir) return true;
  sfx.bump();
  return false;
}

function advanceTutorial(fromMove) {
  const s = TUTORIAL_STEPS[game.tutorialStep];
  if (fromMove !== !!s.dir) return;
  game.tutorialStep++;
  if (game.tutorialStep >= TUTORIAL_STEPS.length) { view.tutorialDir = null; $('#coach').hidden = true; return; }
  showTutorialStep();
}

// ------------------------------------------------------------ entrada
const KEYS = { ArrowUp: 'up', ArrowDown: 'down', ArrowLeft: 'left', ArrowRight: 'right', w: 'up', s: 'down', a: 'left', d: 'right', W: 'up', S: 'down', A: 'left', D: 'right' };
addEventListener('keydown', e => {
  sfx.unlock();
  const dir = KEYS[e.key];
  if (dir && currentScreen === 'game') { e.preventDefault(); input(dir); }
  if (dir && currentScreen === 'levels' && !modalOpen()) {
    if (dir === 'left') setWorld(selectedWorld - 1);
    if (dir === 'right') setWorld(selectedWorld + 1);
  }
  if (e.key === 'Escape') {
    if (modalOpen()) { if ($('#win-modal').hidden) closeModals(); } else if (currentScreen === 'game') openModal('pause-modal');
  }
  if ((e.key === 'r' || e.key === 'R') && currentScreen === 'game' && !modalOpen()) startLevel(game.n);
});

// Deslizar: en el juego mueve las bolas; en niveles cambia de mundo.
let touch = null;
const stage = $('#stage');
stage.addEventListener('pointerdown', e => {
  sfx.unlock();
  touch = e.target.closest('.coach, .modal') ? null : { x: e.clientX, y: e.clientY, target: e.target };
});
stage.addEventListener('pointerup', e => {
  if (!touch) return;
  const dx = e.clientX - touch.x, dy = e.clientY - touch.y;
  const from = touch.target;
  touch = null;
  const swipe = Math.hypot(dx, dy) > stage.clientWidth * 0.06;
  if (currentScreen === 'game') {
    if (swipe) input(Math.abs(dx) > Math.abs(dy) ? (dx > 0 ? 'right' : 'left') : (dy > 0 ? 'down' : 'up'));
    else if (from === $('#board')) input(view.zoneAt(e.clientX, e.clientY));
  } else if (currentScreen === 'levels' && swipe && Math.abs(dx) > Math.abs(dy) * 1.5) {
    swallowClick = true;
    setTimeout(() => (swallowClick = false), 50);
    setWorld(selectedWorld + (dx < 0 ? 1 : -1));
  }
});
stage.addEventListener('pointercancel', () => (touch = null));

// tras deslizar para cambiar de mundo, ignora el clic que cae sobre una casilla
let swallowClick = false;
stage.addEventListener('click', e => {
  if (swallowClick) { e.stopPropagation(); e.preventDefault(); }
}, true);

// Politica de privacidad publicada (Google Play exige enlazarla tambien dentro de la app)
const PRIVACY_URL = 'https://eetayo.github.io/LabyrinthGame/privacy-policy.html';

// ------------------------------------------------------------ botones
document.addEventListener('click', e => {
  const go = e.target.closest('[data-go]');
  if (go) { sfx.click(); closeModals(); show(go.dataset.go); return; }
  const act = e.target.closest('[data-action]');
  if (!act) return;
  sfx.click();
  switch (act.dataset.action) {
    case 'tutorial': startLevel(0); break;
    case 'options': openModal('options-modal'); break;
    case 'pause': openModal('pause-modal'); break;
    case 'help': openHelp(); break;
    case 'hint': giveHint(); break;
    case 'solve': solveForMe(); break;
    case 'resume': closeModals(); break;
    case 'restart': startLevel(game.n); break;
    case 'world-prev': setWorld(selectedWorld - 1); break;
    case 'world-next': setWorld(selectedWorld + 1); break;
    case 'to-levels':
      closeModals();
      if (game.n === 0) { show('home'); break; }
      selectedWorld = worldOf(game.n);
      show('levels');
      break;
    case 'next':
      closeModals();
      if (game.n >= LEVELS.length) { selectedWorld = WORLDS - 1; show('levels'); toast('¡Has completado los 100 niveles!'); break; }
      if (game.n % LEVELS_PER_WORLD === 0) { selectedWorld = worldOf(game.n + 1); show('levels'); break; }
      startLevel(game.n + 1);
      break;
    case 'privacy': window.open(PRIVACY_URL, '_blank', 'noopener'); break;
    case 'ask-reset': closeModals(); openModal('reset-modal'); break;
    case 'do-reset':
      store.resetProgress();
      closeModals();
      $('#home-stars').textContent = 0;
      toast('Progreso borrado');
      break;
    case 'close-modal': closeModals(); break;
  }
});

// ------------------------------------------------------------ opciones
function syncSettings() {
  $$('[data-setting]').forEach(i => (i.checked = !!store.state.settings[i.dataset.setting]));
}
document.addEventListener('change', e => {
  const k = e.target.dataset?.setting;
  if (!k) return;
  store.state.settings[k] = e.target.checked;
  store.save();
  syncSettings();
  sfx.configure(store.state.settings);
  if (k === 'vibration' && e.target.checked) sfx.haptic(30);
});

document.addEventListener('visibilitychange', () => {
  sfx.configure({ ...store.state.settings, music: store.state.settings.music && !document.hidden });
});

// ------------------------------------------------------------ bucle
function frame(now) {
  if (currentScreen === 'game' && view.board) { view.resize(); view.draw(now); }
  sceneFx[currentScreen]?.draw(now);
  requestAnimationFrame(frame);
}

// el rotulo del tablero usa Fredoka: se repinta cuando la fuente termina de cargar
document.fonts?.ready.then(() => { if (view.board) view.resize(true); });

sfx.configure(store.state.settings);
syncSettings();
show('home');
requestAnimationFrame(frame);

// En la app de Android los ficheros ya van dentro del paquete: no hace falta service worker
const nativeApp = !!window.Capacitor?.isNativePlatform?.();
if (!nativeApp && 'serviceWorker' in navigator && location.protocol.startsWith('http')) {
  addEventListener('load', () => navigator.serviceWorker.register('sw.js').catch(() => {}));
}

// Boton "atras" de Android (solo existe dentro de la app nativa). Cierra lo que haya abierto y
// va subiendo de pantalla; desde el menu principal sale de la app.
export function handleBack(app) {
  if (modalOpen()) {
    if (!$('#win-modal').hidden) { closeModals(); selectedWorld = worldOf(game.n); show('levels'); } else closeModals();
    return;
  }
  if (currentScreen === 'game') { if (game.n === 0) show('home'); else openModal('pause-modal'); return; }
  if (currentScreen === 'levels') { show('home'); return; }
  app?.exitApp?.();
}
window.Capacitor?.Plugins?.App?.addListener?.('backButton', () => handleBack(window.Capacitor.Plugins.App));

// Acceso para pruebas desde la consola.
window.labyrinth = { game, startLevel, input, store, view, show, sceneFx, handleBack };
