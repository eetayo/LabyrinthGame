// Cache para jugar sin conexion. Sube VERSION al publicar cambios.
const VERSION = 'labyrinth-v2-10';
const FILES = [
  './', 'index.html', 'css/style.css', 'manifest.webmanifest', 'icons/icon-192.png', 'icons/icon-512.png', 'fonts/Fredoka.ttf',
  'js/main.js', 'js/engine.js', 'js/levels.js', 'js/themes.js', 'js/render.js',
  'js/audio.js', 'js/storage.js', 'js/scenefx.js', 'js/solver.js', 'js/music.js',
  'img/menu.webp', 'img/levels.webp', 'img/world1.webp', 'img/world2.webp',
  'img/world3.webp', 'img/world4.webp', 'img/world5.webp',
];

self.addEventListener('install', e => {
  e.waitUntil(caches.open(VERSION).then(c => c.addAll(FILES)).then(() => self.skipWaiting()));
});

self.addEventListener('activate', e => {
  e.waitUntil(caches.keys().then(keys => Promise.all(keys.filter(k => k !== VERSION).map(k => caches.delete(k)))).then(() => self.clients.claim()));
});

// Red primero para no servir versiones viejas; cache si no hay conexion.
self.addEventListener('fetch', e => {
  if (e.request.method !== 'GET') return;
  e.respondWith(
    fetch(e.request)
      .then(res => {
        if (res.ok && (e.request.url.startsWith(self.location.origin) || e.request.url.includes('fonts.g'))) {
          const copy = res.clone();
          caches.open(VERSION).then(c => c.put(e.request, copy));
        }
        return res;
      })
      .catch(() => caches.match(e.request)),
  );
});
