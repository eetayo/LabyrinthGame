// Sonido sintetizado con Web Audio (sin ficheros): rodar, choque, clic y victoria. La musica va en music.js.
import { createMusic } from './music.js';

let ctx = null;
let master, sfxBus, musicBus;
let noiseBuf = null;
let musicTimer = null;
let settings = { sound: true, music: true };

function ensure() {
  if (ctx) return ctx;
  const AC = window.AudioContext || window.webkitAudioContext;
  if (!AC) return null;
  // 'playback': buffer de salida mas grande; evita los cortes de la musica en moviles con la pantalla ocupada
  try { ctx = new AC({ latencyHint: 'playback' }); } catch { ctx = new AC(); }
  master = ctx.createGain();
  master.gain.value = 0.9;
  master.connect(ctx.destination);
  sfxBus = ctx.createGain();
  sfxBus.connect(master);
  musicBus = ctx.createGain();
  musicBus.gain.value = 0.0;
  musicBus.connect(master);
  noiseBuf = ctx.createBuffer(1, ctx.sampleRate, ctx.sampleRate);
  const d = noiseBuf.getChannelData(0);
  for (let i = 0; i < d.length; i++) d[i] = Math.random() * 2 - 1;
  return ctx;
}

// Los navegadores solo permiten audio tras un gesto del usuario.
export function unlock() {
  const c = ensure();
  if (c && c.state === 'suspended') c.resume();
  if (settings.music) startMusic();
}

export function configure(s) {
  settings = { ...settings, ...s };
  if (!ctx) return;
  if (settings.music) startMusic(); else stopMusic();
}

function env(gainNode, t, a, peak, d) {
  gainNode.gain.setValueAtTime(0.0001, t);
  gainNode.gain.exponentialRampToValueAtTime(peak, t + a);
  gainNode.gain.exponentialRampToValueAtTime(0.0001, t + a + d);
}

function tone(freq, t, { type = 'sine', a = 0.005, d = 0.2, peak = 0.3, bus = sfxBus, detune = 0 } = {}) {
  const o = ctx.createOscillator();
  const g = ctx.createGain();
  o.type = type;
  o.frequency.setValueAtTime(freq, t);
  o.detune.value = detune;
  env(g, t, a, peak, d);
  o.connect(g).connect(bus);
  o.start(t);
  o.stop(t + a + d + 0.05);
  return o;
}

function noise(t, dur, { freq = 800, q = 1, peak = 0.2, type = 'bandpass' } = {}) {
  const src = ctx.createBufferSource();
  src.buffer = noiseBuf;
  const f = ctx.createBiquadFilter();
  f.type = type;
  f.frequency.value = freq;
  f.Q.value = q;
  const g = ctx.createGain();
  env(g, t, 0.01, peak, dur);
  src.connect(f).connect(g).connect(sfxBus);
  src.start(t, Math.random() * 0.5);
  src.stop(t + dur + 0.05);
  return f;
}

// Timbre del golpe segun el mundo.
const KNOCK = {
  madera: { freq: 180, noise: 1400, type: 'triangle' },
  verde: { freq: 120, noise: 500, type: 'sine' },
  piedra: { freq: 90, noise: 2400, type: 'triangle' },
  hielo: { freq: 900, noise: 5000, type: 'sine' },
  mercado: { freq: 140, noise: 900, type: 'sine' },
};

export function roll(duration, theme) {
  if (!settings.sound || !ensure()) return;
  const t = ctx.currentTime;
  const f = noise(t, duration, { freq: theme === 'hielo' ? 3000 : 500, q: 0.8, peak: 0.08, type: 'lowpass' });
  f.frequency.linearRampToValueAtTime(theme === 'hielo' ? 5000 : 1100, t + duration);
}

export function knock(theme, strength = 1) {
  if (!settings.sound || !ensure()) return;
  const k = KNOCK[theme] || KNOCK.madera;
  const t = ctx.currentTime;
  const o = tone(k.freq, t, { type: k.type, d: 0.14, peak: 0.35 * strength });
  o.frequency.exponentialRampToValueAtTime(k.freq * 0.5, t + 0.12);
  noise(t, 0.05, { freq: k.noise, q: 2, peak: 0.25 * strength });
  if (theme === 'hielo') tone(k.freq * 2.7, t, { d: 0.35, peak: 0.08 });
}

// choque entre dos bolas: un "clac" corto y agudo
export function clack(strength = 1) {
  if (!settings.sound || !ensure()) return;
  const t = ctx.currentTime;
  tone(1250, t, { type: 'triangle', d: 0.05, peak: 0.22 * strength });
  tone(2100, t, { type: 'sine', d: 0.035, peak: 0.12 * strength });
  noise(t, 0.025, { freq: 3200, q: 1.5, peak: 0.16 * strength });
}

export function click() {
  if (!settings.sound || !ensure()) return;
  const t = ctx.currentTime;
  tone(880, t, { type: 'triangle', d: 0.06, peak: 0.12 });
  tone(1320, t + 0.02, { type: 'sine', d: 0.05, peak: 0.06 });
}

export function bump() {
  if (!settings.sound || !ensure()) return;
  const t = ctx.currentTime;
  tone(110, t, { type: 'sine', d: 0.1, peak: 0.15 });
}

export function win(stars) {
  if (!settings.sound || !ensure()) return;
  const t = ctx.currentTime;
  const notes = [523.25, 659.25, 783.99, 1046.5];
  notes.forEach((f, i) => {
    tone(f, t + i * 0.09, { type: 'triangle', d: 0.35, peak: 0.18 });
    tone(f * 2, t + i * 0.09, { type: 'sine', d: 0.25, peak: 0.05 });
  });
  for (let i = 0; i < stars; i++) {
    tone(1568 + i * 220, t + 0.55 + i * 0.28, { type: 'sine', d: 0.4, peak: 0.12 });
    tone(2093 + i * 220, t + 0.55 + i * 0.28, { type: 'triangle', d: 0.2, peak: 0.05 });
  }
}

export function goalSink() {
  if (!settings.sound || !ensure()) return;
  const t = ctx.currentTime;
  const o = tone(600, t, { type: 'sine', d: 0.3, peak: 0.15 });
  o.frequency.exponentialRampToValueAtTime(200, t + 0.3);
}

// ---------------------------------------------------------------- musica
// La musica esta en music.js; aqui solo se enciende, se apaga y se cambia de mundo.
let music = null;
let musicKey = 'menu';
const MUSIC_LEVEL = 0.95;

export function setMusic(key) {
  musicKey = key;
  music?.setKey(key);
}

function startMusic() {
  if (!ctx || musicTimer) return;
  music ??= createMusic(ctx, musicBus);
  music.setKey(musicKey);
  music.start();
  const now = ctx.currentTime;
  musicBus.gain.cancelScheduledValues(now);
  musicBus.gain.setValueAtTime(musicBus.gain.value, now);
  musicBus.gain.linearRampToValueAtTime(MUSIC_LEVEL, now + 2.5);
  const tick = () => music.schedule(ctx.currentTime + 2.4);
  tick();
  musicTimer = setInterval(tick, 200);
}

function stopMusic() {
  if (!musicTimer) return;
  clearInterval(musicTimer);
  musicTimer = null;
  const now = ctx.currentTime;
  musicBus.gain.cancelScheduledValues(now);
  musicBus.gain.setValueAtTime(musicBus.gain.value, now);
  musicBus.gain.linearRampToValueAtTime(0, now + 0.5);
  music.stop();
}

export function haptic(ms = 12) {
  if (settings.vibration && navigator.vibrate && navigator.userActivation?.hasBeenActive !== false) navigator.vibrate(ms);
}
