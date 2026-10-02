// Da vida a los fondos pintados sin cambiar el dibujo: un shader WebGL vuelve a pintar la
// misma imagen desplazando un poco los pixeles de algunas zonas:
//   - llamas: ruido que sube, como fuego vivo, y parpadeo de luz
//   - enredaderas y plantas: las hojas se mecen con el viento
//   - bandera del torreon: onda que recorre la tela desde el mastil
// Que zonas tiene cada imagen se define en SCENES, en px del diseno (941x1672).
//
// Las hojas se mueven filtrando por color: solo se desplazan los pixeles verdes. Asi las zonas
// pueden ser generosas y, aunque cubran madera, botones o piedra, esos siguen quietos.
// Si no hay WebGL, se queda la imagen fija.

const MAX_FIRES = 8;
const MAX_PLANTS = 16;

// fires: [x, y de la base de la llama, tamano (1 = antorcha del menu), fase]
// plants: [centro x, centro y, radio x, radio y, fase, fuerza, ancla]
//   ancla 0: sujeta por abajo (plantas del suelo) · 1: colgante, sujeta por arriba ·
//   2: racimo de hojas, se mueve mas cuanto mas lejos del centro
// leafGrid: hoja repetida en rejilla [x, y del centro de la primera, paso x, paso y,
//   columnas, filas, radio x, radio y] (la hojita de la esquina de cada casilla de nivel)
// flag: [x del mastil, y arriba, y abajo, largo de la tela]
const HUD_LEAVES = [   // enredadera de la barra superior de los tableros
  [100, 80, 95, 55, 0.4, 1, 2], [30, 30, 50, 45, 1.3, 1, 2], [30, 180, 32, 38, 2.1, 1, 2],
  [795, 98, 75, 46, 3.0, 1, 2], [800, 255, 72, 46, 3.9, 1, 2], [915, 195, 40, 52, 4.8, 1, 2],
  [890, 45, 60, 55, 5.6, 1, 2], [135, 290, 80, 26, 0.9, 1, 2],
];

export const SCENES = {
  'img/menu.webp': {
    fires: [[42, 1192, 1, 0], [898, 1216, 1, 2.7]],
    plants: [
      // plantas de la parte baja y arbustos tras las antorchas
      [70, 1400, 115, 115, 0, 1, 0], [228, 1530, 85, 80, 1.9, 1, 0], [862, 1575, 125, 135, 3.4, 1, 0],
      [770, 1278, 55, 45, 4.6, 1, 0], [165, 1278, 45, 50, 5.5, 1, 0],
      [18, 1030, 70, 95, 0.8, 0.6, 0], [915, 1040, 70, 100, 2.4, 0.6, 0],
      // enredadera del logo
      [205, 185, 75, 58, 0.3, 1, 2], [95, 410, 52, 125, 1.4, 0.9, 2], [185, 540, 80, 62, 2.2, 1, 2],
      [740, 540, 100, 62, 3.1, 1, 2], [850, 480, 38, 48, 4.0, 0.9, 2],
    ],
    flag: [820, 90, 139, 30],
  },
  'img/levels.webp': {
    fires: [[30, 1380, 1, 0], [905, 1384, 1, 2.7]],
    plants: [
      [85, 108, 62, 78, 0.2, 1, 2], [335, 130, 32, 32, 1.1, 1, 2],                 // boton Menu
      [690, 208, 100, 66, 2.0, 1, 2], [172, 255, 48, 42, 2.9, 1, 2],               // placa del titulo
      [128, 388, 42, 72, 3.8, 0.9, 2], [215, 410, 75, 46, 4.6, 1, 2],
      [785, 405, 62, 52, 5.4, 1, 2], [745, 245, 38, 42, 0.9, 1, 2],
      [60, 1570, 120, 120, 1.7, 1, 0], [860, 1595, 120, 110, 2.6, 1, 0],           // plantas del suelo
    ],
    leafGrid: [107, 475, 201.3, 187.5, 4, 5, 46, 44],
  },
  // Madera
  'img/world1.webp': {
    plants: [
      ...HUD_LEAVES, [10, 545, 32, 72, 1.8, 0.8, 2],
      [100, 1300, 115, 82, 2.7, 1, 0], [60, 1585, 105, 110, 3.6, 1, 0],
      [880, 1430, 72, 72, 4.4, 1, 0], [845, 1262, 72, 46, 5.2, 1, 0],
    ],
  },
  // Verde
  'img/world2.webp': {
    plants: [
      ...HUD_LEAVES,
      [55, 1300, 70, 70, 2.3, 1, 0], [60, 1595, 105, 100, 3.1, 1, 0],
      [880, 1440, 80, 80, 4.1, 1, 0], [870, 1605, 100, 75, 5.0, 1, 0],
    ],
  },
  // Piedra: antorchas del fondo (desenfocadas) y velas de los lados
  'img/world3.webp': {
    fires: [[170, 50, 0.4, 0], [770, 50, 0.4, 1.3], [18, 1274, 0.38, 2.1], [925, 1274, 0.38, 3.9], [268, 1322, 0.45, 4.4], [668, 1322, 0.45, 5.6]],
  },
  // Hielo: antorchas del fondo
  'img/world4.webp': {
    fires: [[100, 60, 0.36, 0], [225, 56, 0.36, 1.7], [745, 62, 0.36, 3.1], [295, 1312, 0.48, 4.2], [700, 1312, 0.48, 5.3]],
  },
  // Mercado: la vegetacion es menos trepadora, asi que se mece con menos fuerza
  'img/world5.webp': {
    plants: [
      [100, 88, 95, 70, 0.6, 1, 2], [890, 300, 60, 50, 1.7, 0.8, 2],
      [110, 1570, 150, 120, 2.5, 0.8, 0], [860, 1595, 110, 90, 3.4, 0.8, 0],
      [160, 1285, 52, 72, 4.3, 0.7, 0], [605, 1300, 52, 62, 5.2, 0.7, 0], [855, 1290, 85, 65, 0.9, 0.7, 0],
    ],
  },
};

const VERT = `
attribute vec2 a_pos;
varying vec2 v_uv;
void main() {
  v_uv = vec2(a_pos.x * 0.5 + 0.5, 0.5 - a_pos.y * 0.5);
  gl_Position = vec4(a_pos, 0.0, 1.0);
}`;

const FRAG = `
#ifdef GL_FRAGMENT_PRECISION_HIGH
precision highp float;
#else
precision mediump float;
#endif
uniform sampler2D u_tex;
uniform float u_t;
uniform float u_debug;
uniform vec4 u_fire[${MAX_FIRES}];      // x, y base, tamano, fase (tamano 0 = sin usar)
uniform vec4 u_plant[${MAX_PLANTS}];    // centro, radios (radio 0 = sin usar)
uniform vec3 u_plantFx[${MAX_PLANTS}];  // fase, fuerza, ancla
uniform vec4 u_lg;                      // rejilla de hojas: x, y del primero, paso x, paso y
uniform vec4 u_lg2;                     // columnas, filas (0 = sin rejilla), radios
uniform vec4 u_flag;                    // x mastil, y arriba, y abajo, largo (0 = sin bandera)
varying vec2 v_uv;
const vec2 ART = vec2(941.0, 1672.0);

float hash(vec2 p) { return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453); }
float noise(vec2 p) {
  vec2 i = floor(p), f = fract(p);
  f = f * f * (3.0 - 2.0 * f);
  return mix(mix(hash(i), hash(i + vec2(1.0, 0.0)), f.x),
             mix(hash(i + vec2(0.0, 1.0)), hash(i + vec2(1.0, 1.0)), f.x), f.y);
}
float fbm(vec2 p) { return noise(p) * 0.6 + noise(p * 2.1 + 7.3) * 0.3 + noise(p * 4.3 + 1.7) * 0.1; }

// 1 en el centro de la elipse, 0 fuera, con borde suave para que no se note la costura
float blob(vec2 p, vec2 c, vec2 r) { return 1.0 - smoothstep(0.45, 1.0, length((p - c) / r)); }

// Cuanto de "hoja" es un color: verde por encima de rojo y azul. Madera, arena, piedra,
// botones azules y cielo dan 0.
float leaf(vec3 c) { return smoothstep(0.02, 0.09, c.g - max(c.r, c.b)); }

float mask = 0.0;   // solo para depurar: zonas animadas
float glow = 0.0;   // luz extra de las llamas

// Llama: f.xy = punto donde nace. El ruido sube y la deforma mas cuanto mas alto.
vec2 fire(vec2 p, vec4 f) {
  float s = f.z, ph = f.w;
  float m = blob(p, f.xy - vec2(0.0, 52.0 * s), vec2(64.0, 100.0) * s);
  if (m <= 0.0) return vec2(0.0);
  float h = clamp((f.y - p.y) / (105.0 * s), 0.0, 1.0);
  vec2 q = (p - f.xy) * vec2(0.034, 0.026) / s + vec2(ph * 3.1, u_t * 2.6);
  float nx = fbm(q) - 0.5;
  float ny = fbm(q + vec2(5.2, 1.3)) - 0.5;
  float pulse = sin(u_t * 9.0 + ph * 3.0) * 0.5 + sin(u_t * 5.3 + ph) * 0.5;
  mask = max(mask, m);
  glow += m * (0.55 + 0.45 * pulse) * (0.4 + 0.6 * fbm(q * 1.7));
  return vec2(nx * 50.0 * h, ny * 34.0 * h + pulse * 9.0 * h * h) * m * s;
}

// Viento en las hojas: dos ondas que dependen de la posicion, para que cada hoja vaya a lo
// suyo, mas una racha lenta comun. h = cuanto se mueve cada punto segun el ancla.
vec2 wind(vec2 p, float ph, float h) {
  float w1 = sin(u_t * 1.5 + ph + p.x * 0.05 + p.y * 0.03);
  float w2 = sin(u_t * 2.7 + ph * 1.9 + p.y * 0.06 - p.x * 0.04);
  float gust = 0.7 + 0.3 * sin(u_t * 0.55 + ph);
  return vec2(w1 * 8.0 + w2 * 2.5, w2 * 5.0 + sin(u_t * 1.1 + ph) * 2.0) * gust * h;
}

vec2 vine(vec2 p, vec4 e, vec3 fx) {
  float m = blob(p, e.xy, e.zw);
  if (m <= 0.0) return vec2(0.0);
  float h;
  if (fx.z < 0.5) h = clamp((e.y + e.w - p.y) / (2.0 * e.w), 0.0, 1.0);          // sujeta por abajo
  else if (fx.z < 1.5) h = clamp((p.y - (e.y - e.w)) / (2.0 * e.w), 0.0, 1.0);   // colgante
  else h = 0.4 + 0.6 * clamp(length((p - e.xy) / e.zw), 0.0, 1.0);                 // racimo
  mask = max(mask, m * h);
  return wind(p, fx.x, h) * m * fx.y;
}

// Hojita repetida en rejilla (esquina de cada casilla de nivel), cada una con su fase.
vec2 leafGrid(vec2 p) {
  vec2 c = floor((p - u_lg.xy) / u_lg.zw + 0.5);
  if (c.x < 0.0 || c.y < 0.0 || c.x >= u_lg2.x || c.y >= u_lg2.y) return vec2(0.0);
  float m = blob(p, u_lg.xy + c * u_lg.zw, u_lg2.zw);
  if (m <= 0.0) return vec2(0.0);
  mask = max(mask, m);
  return wind(p, hash(c) * 6.283, 0.9) * m * 0.8;
}

// Bandera: la onda crece al alejarse del mastil.
vec2 flag(vec2 p, vec4 f) {
  float x0 = f.x, len = f.w;
  float m = smoothstep(x0 - 3.0, x0 + 3.0, p.x) * (1.0 - smoothstep(x0 + len, x0 + len + 16.0, p.x))
          * smoothstep(f.y - 6.0, f.y + 6.0, p.y) * (1.0 - smoothstep(f.z - 7.0, f.z + 7.0, p.y));
  float w = clamp((p.x - x0) / len, 0.0, 1.0);
  float a = (p.x - x0) * 0.24 - u_t * 5.5;
  mask = max(mask, m);
  return vec2(cos(a) * 1.6, sin(a) * 5.0 + sin(u_t * 2.1) * 1.5) * w * m;
}

void main() {
  vec2 p = v_uv * ART;
  vec2 d = vec2(0.0);    // fuego y bandera: desplazan todos los pixeles de su zona
  vec2 dp = vec2(0.0);   // hojas y plantas: solo desplazan los pixeles verdes
  for (int i = 0; i < ${MAX_FIRES}; i++) if (u_fire[i].z > 0.0) d += fire(p, u_fire[i]);
  if (u_flag.w > 0.0) d += flag(p, u_flag);
  for (int i = 0; i < ${MAX_PLANTS}; i++) if (u_plant[i].z > 0.0) dp += vine(p, u_plant[i], u_plantFx[i]);
  if (u_lg2.x > 0.0) dp += leafGrid(p);

  vec3 col = texture2D(u_tex, (p - d) / ART).rgb;
  float moved = 0.0;
  if (dot(dp, dp) > 0.0001) {
    // el pixel pasa a ser hoja si lo era antes o si una hoja se mueve hasta el; si no, no cambia
    vec3 c1 = texture2D(u_tex, (p - d - dp) / ART).rgb;
    moved = max(leaf(col), leaf(c1));
    col = mix(col, c1, moved);
  }
  col += vec3(1.0, 0.55, 0.16) * glow * 0.13;
  // depuracion: 1 = zonas animadas, 2 = pixeles que realmente se mueven
  if (u_debug > 1.5) col = mix(col, vec3(1.0, 0.0, 0.3), moved * 0.75);
  else if (u_debug > 0.5) col = mix(col, vec3(1.0, 0.0, 0.3), mask * 0.6);
  gl_FragColor = vec4(col, 1.0);
}`;

// Crea el lienzo de efectos de una pantalla. setScene(src) elige la imagen (y sus zonas de
// SCENES); si la imagen no tiene zonas animadas, el lienzo se oculta y se ve el fondo fijo.
export function createSceneFx(host) {
  if (matchMedia('(prefers-reduced-motion: reduce)').matches) return null;
  const canvas = document.createElement('canvas');
  canvas.className = 'scene-fx';
  canvas.hidden = true;
  canvas.setAttribute('aria-hidden', 'true');
  const gl = canvas.getContext('webgl', { antialias: false, alpha: false });
  if (!gl) return null;

  const compile = (type, code) => {
    const s = gl.createShader(type);
    gl.shaderSource(s, code);
    gl.compileShader(s);
    if (!gl.getShaderParameter(s, gl.COMPILE_STATUS)) throw new Error(gl.getShaderInfoLog(s));
    return s;
  };
  let prog;
  try {
    prog = gl.createProgram();
    gl.attachShader(prog, compile(gl.VERTEX_SHADER, VERT));
    gl.attachShader(prog, compile(gl.FRAGMENT_SHADER, FRAG));
    gl.linkProgram(prog);
    if (!gl.getProgramParameter(prog, gl.LINK_STATUS)) throw new Error(gl.getProgramInfoLog(prog));
  } catch (e) {
    console.warn('scenefx desactivado:', e.message);
    return null;
  }
  gl.useProgram(prog);
  gl.bindBuffer(gl.ARRAY_BUFFER, gl.createBuffer());
  gl.bufferData(gl.ARRAY_BUFFER, new Float32Array([-1, -1, 1, -1, -1, 1, 1, 1]), gl.STATIC_DRAW);
  const loc = gl.getAttribLocation(prog, 'a_pos');
  gl.enableVertexAttribArray(loc);
  gl.vertexAttribPointer(loc, 2, gl.FLOAT, false, 0, 0);
  const u = name => gl.getUniformLocation(prog, name);
  const uT = u('u_t'), uDebug = u('u_debug'), uFire = u('u_fire'), uPlant = u('u_plant'), uPlantFx = u('u_plantFx');
  const uLg = u('u_lg'), uLg2 = u('u_lg2'), uFlag = u('u_flag');
  host.prepend(canvas);

  const textures = {};   // src -> textura ya cargada
  const fx = { canvas, ready: false, src: null };

  fx.setScene = src => {
    const scene = SCENES[src];
    fx.src = src;
    fx.ready = false;
    canvas.hidden = true;
    if (!scene) return;
    const fires = new Float32Array(MAX_FIRES * 4);
    (scene.fires || []).forEach((f, i) => fires.set(f, i * 4));
    const plants = new Float32Array(MAX_PLANTS * 4), plantFx = new Float32Array(MAX_PLANTS * 3);
    const list = scene.plants || [];
    if (list.length > MAX_PLANTS) console.warn(`scenefx: ${src} tiene ${list.length} plantas y el maximo es ${MAX_PLANTS}`);
    list.slice(0, MAX_PLANTS).forEach((p, i) => { plants.set(p.slice(0, 4), i * 4); plantFx.set(p.slice(4, 7), i * 3); });
    const g = scene.leafGrid;
    const apply = () => {
      if (fx.src !== src) return;   // se cambio de escena mientras cargaba la imagen
      gl.bindTexture(gl.TEXTURE_2D, textures[src]);
      gl.uniform4fv(uFire, fires);
      gl.uniform4fv(uPlant, plants);
      gl.uniform3fv(uPlantFx, plantFx);
      gl.uniform4fv(uLg, g ? [g[0], g[1], g[2], g[3]] : [0, 0, 1, 1]);
      gl.uniform4fv(uLg2, g ? [g[4], g[5], g[6], g[7]] : [0, 0, 0, 0]);
      gl.uniform4fv(uFlag, scene.flag || [0, 0, 0, 0]);
      fx.ready = true;
    };
    if (textures[src]) { apply(); return; }
    const img = new Image();
    img.onload = () => {
      const tex = gl.createTexture();
      gl.bindTexture(gl.TEXTURE_2D, tex);
      gl.texImage2D(gl.TEXTURE_2D, 0, gl.RGB, gl.RGB, gl.UNSIGNED_BYTE, img);
      // la imagen no es potencia de 2: sin mipmaps y sin repeticion
      gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MIN_FILTER, gl.LINEAR);
      gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MAG_FILTER, gl.LINEAR);
      gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_WRAP_S, gl.CLAMP_TO_EDGE);
      gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_WRAP_T, gl.CLAMP_TO_EDGE);
      textures[src] = tex;
      apply();
    };
    img.src = src;
  };

  // ms: tiempo de requestAnimationFrame. debug: 1 pinta en rojo las zonas animadas y 2 los
  // pixeles que realmente se mueven.
  fx.draw = (ms, debug = 0) => {
    if (!fx.ready) return;
    canvas.hidden = false;   // se muestra con el primer fotograma, no antes
    const dpr = Math.min(window.devicePixelRatio || 1, 2);
    const w = Math.round(canvas.clientWidth * dpr), h = Math.round(canvas.clientHeight * dpr);
    if (!w || !h) return;
    if (canvas.width !== w || canvas.height !== h) { canvas.width = w; canvas.height = h; }
    gl.viewport(0, 0, w, h);
    gl.uniform1f(uT, (ms / 1000) % 600);
    gl.uniform1f(uDebug, +debug);
    gl.drawArrays(gl.TRIANGLE_STRIP, 0, 4);
  };
  return fx;
}
