# Logic Labyrinth · Versión 2

Remake del juego Android de Chocodroid (2011) como web app instalable (PWA), con gráficos,
animaciones y sonido nuevos, y las mismas reglas del original.

## Reglas (idénticas a 2011)

- Tablero de 5×5 con paredes entre casillas.
- Todas las bolas se mueven a la vez en la dirección elegida. Cada una rueda hasta chocar con una
  pared, con el borde o con otra bola.
- Se gana cuando una bola se detiene sobre un agujero.
- Estrellas: 3 si igualas el récord, 2 hasta 1,5× el récord y 1 con cualquier otro resultado.
  En 2011 se podían sacar 0 estrellas; aquí un nivel superado da siempre al menos 1.
- 100 niveles en 5 mundos: Madera, Verde, Piedra, Hielo y Mercado. Cada nivel se abre al superar
  el anterior, y cada mundo al superar el último nivel del mundo anterior.

## Ayudas

El botón «Ayuda», bajo el tablero, ofrece dos opciones. Las dos calculan la solución más
corta desde la posición en la que esté el jugador (`js/solver.js`), no desde el principio.

| Ayuda | Qué hace | Estrellas de ese intento |
|---|---|---|
| Pista | Una flecha indica el siguiente movimiento y dice cuántos quedan | Máximo 2 |
| Resolver | El juego termina el nivel solo | 1 |

El nivel queda superado en los dos casos y se puede repetir sin ayuda para subir de estrellas;
las estrellas guardadas nunca bajan. Si desde la posición actual no hay salida, avisa de que
hay que reiniciar y no penaliza. El botón late cuando el jugador parece atascado: lleva el
doble de movimientos que el récord o ha reiniciado tres veces seguidas.

## Controles

- Deslizar el dedo sobre el tablero.
- Tocar un lado del tablero: se divide en 4 triángulos por las diagonales, como en el original.
- Flechas o WASD. `R` reinicia y `Esc` pausa.

## Ejecutar

Usa módulos ES, así que necesita un servidor HTTP (no funciona abriendo el fichero directamente):

```bash
python3 version2/tools/serve.py
```

Después abre <http://localhost:8642>. Se puede desplegar tal cual en cualquier hosting estático
(GitHub Pages, Netlify…). Desde el móvil se instala con «Añadir a pantalla de inicio» y funciona
sin conexión gracias al service worker. Al publicar cambios, sube `VERSION` en `sw.js`.

## Niveles

`js/levels.js` se genera con `tools/convert_levels.py` a partir de los `nivelN.txt` originales
de `src/`. El script incluye un solver BFS que:

- comprueba que el motor nuevo da exactamente los mismos resultados que `Pelota.moverPelota` de
  2011 en todos los niveles;
- fija como récord el mínimo real de movimientos de cada nivel.

Cambios respecto a los datos originales:

| Nivel | Qué pasaba en 2011 | Versión 2 |
|---|---|---|
| 7 | Récord 7, pero se resuelve en 5 | Récord 5 |
| 66 | Una meta de más (errata) bajo la bola B: se ganaba sin mover | Quitada; el mínimo vuelve a ser 22, el récord original |
| 70–100 | Copias del nivel 1 (relleno) | 31 niveles nuevos de `tools/generate_levels.py`, verificados con el solver |
| Tutorial | La secuencia guiada (abajo, izquierda, abajo, abajo) no lo resolvía | Solución real: izquierda, abajo, izquierda, arriba |

```bash
python3 version2/tools/generate_levels.py            # regenera tools/new_levels.json (semilla fija)
python3 version2/tools/convert_levels.py --write     # informe + js/levels.js
```

### Soluciones

`js/solutions.js` guarda la solución óptima de cada nivel (`SOLUTIONS[n - 1]`, con `U`, `D`,
`L`, `R`) y la del tutorial. La genera `tools/solve_levels.py`, que además comprueba que los
100 niveles tienen solución y que su récord es el mínimo real: explora todas las posiciones
posibles de cada nivel y repite cada solución con dos motores distintos. Si algo falla,
termina con error y no escribe nada.

```bash
python3 version2/tools/solve_levels.py --write       # comprobar + js/solutions.js
```

El juego no usa este fichero: las ayudas calculan la solución en el momento. Sirve como
referencia y como prueba de que todos los niveles se pueden resolver.

## Arte

El aspecto sale de los diseños de `design/` (menú, selección de niveles y un tablero por mundo).
El juego es un escenario de proporción fija 941×1672, la de los diseños, y cada elemento se
coloca en píxeles del diseño, así que cae exactamente sobre el arte a cualquier tamaño.

- **Del diseño, tal cual:** fondos, logo, botones del menú, barra superior con sus botones y
  el marco de cada tablero. Los botones pintados llevan encima una zona pulsable invisible.
- **Limpiado por `tools/build_assets.py`:** lo que en el diseño es un valor de ejemplo y en el
  juego cambia. Borra el texto del marcador, el número y las estrellas de las 20 casillas, el
  título del mundo y los puntos de paginación, y repara el canto interior de los marcos donde
  el diseño tenía paredes o bolas pisándolo. El resultado va a `img/`.
- **Dibujado por código (`js/themes.js`, `js/render.js`):** el interior del tablero, porque
  depende del nivel: suelo, paredes con volumen y sombra, bolas, agujeros, flechas y rótulo.

```bash
pip install pillow numpy opencv-python-headless
python3 version2/tools/build_assets.py     # design/*.webp -> img/*.webp
```

Si cambia un diseño, hay que revisar las coordenadas que dependen de él: `floorRect` en
`js/themes.js` y `WORLDS` en `tools/build_assets.py` (hueco del tablero), `TILE_X`/`TILE_Y`
(casillas de nivel) y los `--x/--y/--w/--h` de los botones en `index.html`.

### Fondos animados

`js/scenefx.js` vuelve a pintar la misma imagen con un shader y mueve solo algunas zonas:
el fuego de las antorchas, la bandera del torreón y las hojas de las enredaderas y plantas
(menú, niveles y los tableros de Madera, Verde y Mercado). Las hojas se mueven filtrando por
color: solo se desplazan los píxeles verdes, así que aunque una zona cubra madera, botones o
piedra, eso no se mueve. Para depurar, `sceneFx.<pantalla>.draw(ms, 1)` pinta en rojo las zonas
y `draw(ms, 2)` los píxeles que realmente se mueven. Con movimiento reducido o sin WebGL, el
fondo queda fijo.

En pantallas que no son 9:16 el escenario se centra: a los lados se rellena con el mismo arte
desenfocado y, arriba y abajo, con el arte reflejado y desenfocado.

## Música

`js/music.js` la genera en el momento, sin ficheros de audio. Cada mundo tiene su tonalidad, su
tempo y su carácter (Madera y el menú en mayor, Verde y Mercado más alegres, Piedra en menor,
Hielo con séptimas mayores). Una progresión de cuatro acordes con las voces muy juntas, un pad
cálido, un bajo sereno, arpegios tipo caja de música y una melodía escasa, todo pasando por un
filtro paso bajo y una reverberación. Solo usa senos y triángulos, y todas las notas pertenecen
a la tonalidad. Al cambiar de mundo se corta la sesión anterior con un fundido, para que dos
tonalidades no suenen a la vez.

## Estructura

| Fichero | Contenido |
|---|---|
| `design/` | Diseños originales (fuente del arte) |
| `img/` | Arte listo para el juego, generado por `tools/build_assets.py` |
| `js/engine.js` | Reglas: movimiento, victoria y estrellas |
| `js/levels.js` | Los 100 niveles y el tutorial |
| `js/solver.js` | Solución más corta desde cualquier posición; la usan las ayudas |
| `js/solutions.js` | Solución óptima de cada nivel, generada por `tools/solve_levels.py` |
| `js/themes.js` | Los 5 mundos: suelo, paredes y bolas de cada uno, y dónde encaja el tablero |
| `js/render.js` | Tablero en canvas: capas, animación de rodar, choques y partículas |
| `js/scenefx.js` | Fondos animados (WebGL): llamas, enredaderas, plantas y bandera de la propia imagen; las zonas de cada fondo están en `SCENES` |
| `js/music.js` | Música de fondo generativa: acordes, pad, bajo, arpegios y melodía, con un carácter distinto por mundo |
| `js/audio.js` | Efectos y música generativa con Web Audio (sin ficheros de audio) |
| `js/storage.js` | Progreso y opciones en `localStorage` |
| `js/main.js` | Pantallas, entrada, tutorial y flujo de juego |
