#!/usr/bin/env python3
"""Prepara el arte del juego a partir de los disenos de design/.

Los disenos llevan "horneado" contenido que en el juego es dinamico (movimientos, numero y
estrellas de cada nivel, nombre del mundo). Este script lo borra para que el juego pinte
encima el valor real. Requiere: pip install pillow numpy opencv-python-headless
"""
import os
import cv2
import numpy as np

HERE = os.path.dirname(os.path.abspath(__file__))
SRC = os.path.join(HERE, '..', 'design')
OUT = os.path.join(HERE, '..', 'img')

# Casillas de nivel en niveles_madera.webp (px del diseno, 941x1672)
TILE_X = [(83, 260), (284, 460), (483, 661), (687, 864)]
TILE_Y = [(467, 633), (653, 820), (844, 1007), (1029, 1193), (1217, 1383)]


def load(name):
    return cv2.imread(os.path.join(SRC, name), cv2.IMREAD_COLOR)


def save(img, name, q=90):
    cv2.imwrite(os.path.join(OUT, name), img, [cv2.IMWRITE_WEBP_QUALITY, q])
    print('escrito', name, os.path.getsize(os.path.join(OUT, name)) // 1024, 'KB')


def erase_dark_text(img, rect, ratio=0.9, grow=7, radius=7):
    """Borra texto oscuro sobre una placa clara (marcador de movimientos)."""
    x0, y0, x1, y1 = rect
    roi = img[y0:y1, x0:x1]
    gray = cv2.cvtColor(roi, cv2.COLOR_BGR2GRAY).astype(np.float32)
    base = cv2.medianBlur(cv2.cvtColor(roi, cv2.COLOR_BGR2GRAY), 51).astype(np.float32)
    mask = (gray < base * ratio).astype(np.uint8) * 255
    mask = cv2.dilate(mask, np.ones((grow, grow), np.uint8))
    full = np.zeros(img.shape[:2], np.uint8)
    full[y0:y1, x0:x1] = mask
    return cv2.inpaint(img, full, radius, cv2.INPAINT_TELEA)


def erase_rows(img, rect, margin=5):
    """Rellena un rectangulo interpolando cada fila entre sus bordes izquierdo y derecho."""
    x0, y0, x1, y1 = rect
    left = img[y0:y1, x0 - margin:x0].astype(np.float32).mean(axis=1)
    right = img[y0:y1, x1:x1 + margin].astype(np.float32).mean(axis=1)
    # suaviza verticalmente para no arrastrar ruido de los bordes
    k = np.ones(9, np.float32) / 9
    for side in (left, right):
        for c in range(3):
            side[:, c] = np.convolve(np.pad(side[:, c], 4, mode='edge'), k, mode='valid')
    t = np.linspace(0, 1, x1 - x0, dtype=np.float32)[None, :, None]
    fill = left[:, None, :] * (1 - t) + right[:, None, :] * t
    noise = np.random.default_rng(7).normal(0, 1.2, fill.shape).astype(np.float32)
    img[y0:y1, x0:x1] = np.clip(fill + noise, 0, 255).astype(np.uint8)
    return img


def clean_lip(img, floor, depths, win=181):
    """Limpia el borde interior del marco: en los disenos hay paredes y bolas que pisan el
    marco. Cada pixel del borde se sustituye por la mediana a lo largo del liston, que borra
    esos restos y conserva el sombreado del canto. depths = (arriba, derecha, abajo, izquierda)."""
    from numpy.lib.stride_tricks import sliding_window_view
    x, y, w, h = floor
    top, right, bottom, left = depths

    def fix(strip, axis, inner_first):
        # strip: banda con el eje largo en `axis`; la mezcla va de 0 (exterior) a 1 (interior)
        a = np.moveaxis(strip.astype(np.float32), axis, 0)            # (largo, fondo, 3)
        pad = np.pad(a, ((win // 2, win // 2), (0, 0), (0, 0)), mode='reflect')
        med = np.median(sliding_window_view(pad, win, axis=0), axis=-1)
        depth = a.shape[1]
        ramp = np.clip(np.linspace(0, 1, depth) * 3, 0, 1)
        if inner_first:
            ramp = ramp[::-1]
        out = a * (1 - ramp[None, :, None]) + med * ramp[None, :, None]
        return np.moveaxis(out, 0, axis).astype(np.uint8)

    if top:
        img[y - top:y, x:x + w] = fix(img[y - top:y, x:x + w], 1, False)
    if bottom:
        img[y + h:y + h + bottom, x:x + w] = fix(img[y + h:y + h + bottom, x:x + w], 1, True)
    if left:
        img[y:y + h, x - left:x] = np.swapaxes(fix(np.swapaxes(img[y:y + h, x - left:x], 0, 1), 1, False), 0, 1)
    if right:
        img[y:y + h, x + w:x + w + right] = np.swapaxes(fix(np.swapaxes(img[y:y + h, x + w:x + w + right], 0, 1), 1, True), 0, 1)
    return img


# hueco del tablero (x, y, ancho, alto) y fondo a limpiar en cada lado; igual que js/themes.js
WORLDS = [
    ('mundo1_madera', (81, 374, 782, 783), (24, 12, 16, 16)),
    ('mundo2_verde', (93, 375, 768, 768), (0, 0, 0, 0)),
    ('mundo3_piedra', (88, 370, 765, 787), (24, 24, 24, 24)),
    ('mundo4_hielo', (78, 370, 780, 793), (12, 12, 48, 14)),
    ('mundo5_mercado', (90, 374, 761, 772), (12, 20, 16, 18)),
]


def build_worlds():
    for i, (n, floor, depths) in enumerate(WORLDS, 1):
        img = load(n + '.webp')
        img = erase_dark_text(img, (334, 116, 704, 230))
        img = clean_lip(img, floor, depths)
        save(img, f'world{i}.webp')


def build_levels():
    img = load('niveles_madera.webp')
    # numero y estrellas de cada casilla
    for (x0, x1) in TILE_X:
        for (y0, y1) in TILE_Y:
            img = erase_rows(img, (x0 + 46, y0 + 24, x1 - 16, y0 + 92))   # numero (deja la hoja)
            img = erase_rows(img, (x0 + 14, y0 + 88, x1 - 14, y0 + 148))  # estrellas
    # titulo "Madera" y "34 / 60 estrellas": letras doradas/blancas sobre piedra azul
    x0, y0, x1, y1 = 235, 200, 712, 402
    roi = img[y0:y1, x0:x1].astype(np.int32)
    b, g, r = roi[..., 0], roi[..., 1], roi[..., 2]
    warm = (r > 150) & (g > 110) & (r >= g * 0.95)          # dorado y blanco, no hojas verdes
    mask = cv2.dilate(warm.astype(np.uint8) * 255, np.ones((17, 17), np.uint8))
    full = np.zeros(img.shape[:2], np.uint8)
    full[y0:y1, x0:x1] = mask
    img = cv2.inpaint(img, full, 9, cv2.INPAINT_TELEA)
    # puntos de paginacion
    full[:] = 0
    for cx in (387, 432, 476, 520, 563):
        cv2.circle(full, (cx, 1435), 21, 255, -1)
    img = cv2.inpaint(img, full, 7, cv2.INPAINT_TELEA)
    save(img, 'levels.webp')


def main():
    os.makedirs(OUT, exist_ok=True)
    save(load('menu.webp'), 'menu.webp')
    build_worlds()
    build_levels()


if __name__ == '__main__':
    main()
