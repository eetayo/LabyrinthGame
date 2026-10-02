// Progreso y opciones en localStorage (equivalente a SaveGame/Score del original).
import { LEVELS } from './levels.js';
import { starsFor, LEVELS_PER_WORLD, WORLDS } from './engine.js';

const KEY = 'labyrinth.v2';
// best: menos movimientos sin ayuda. stars: mejor numero de estrellas de cada nivel superado.
const DEFAULTS = { best: {}, stars: {}, settings: { sound: true, music: true, vibration: true }, tutorialDone: false };

function load() {
  try {
    const raw = JSON.parse(localStorage.getItem(KEY));
    if (raw) return { ...DEFAULTS, ...raw, settings: { ...DEFAULTS.settings, ...raw.settings } };
  } catch { /* almacenamiento no disponible */ }
  return structuredClone(DEFAULTS);
}

export const state = load();

export function save() {
  try { localStorage.setItem(KEY, JSON.stringify(state)); } catch { /* modo privado */ }
}

export function bestOf(n) {
  return state.best[n] ?? null;
}

export function starsOf(n) {
  if (state.stars[n] != null) return state.stars[n];
  // partidas guardadas antes de que existieran las ayudas: se deducen de los movimientos
  const b = bestOf(n);
  return b == null ? 0 : starsFor(b, LEVELS[n - 1].record);
}

// Guarda el resultado de superar un nivel. Las estrellas solo suben; los movimientos solo
// cuentan como marca si se jugo sin ayuda. Devuelve true si mejora la marca de movimientos.
export function recordResult(n, moves, stars, unaided = true) {
  const prevStars = starsOf(n);
  state.stars[n] = Math.max(prevStars, stars);
  const prev = bestOf(n);
  const better = unaided && (prev == null || moves < prev);
  if (better) state.best[n] = moves;
  save();
  return better;
}

export const completed = n => state.stars[n] != null || bestOf(n) != null;

// Igual que en 2011: un nivel se abre al superar el anterior.
export const levelUnlocked = n => n === 1 || completed(n) || completed(n - 1);

// Un mundo se abre al superar el ultimo nivel del mundo anterior (niveles 20, 40, 60, 80).
export const worldUnlocked = w => w === 0 || completed(w * LEVELS_PER_WORLD);

export function worldStars(w) {
  let s = 0;
  for (let i = 1; i <= LEVELS_PER_WORLD; i++) s += starsOf(w * LEVELS_PER_WORLD + i);
  return s;
}

export function totalStars() {
  let s = 0;
  for (let w = 0; w < WORLDS; w++) s += worldStars(w);
  return s;
}

export function resetProgress() {
  state.best = {};
  state.stars = {};
  state.tutorialDone = false;
  save();
}
