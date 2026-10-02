#!/usr/bin/env python3
"""Convierte los niveles originales (2011, formato nivelN.txt) al formato JSON de la v2
y valida cada nivel con un solver BFS.

Coordenadas originales (Y,X):
  Y impar, X 1..5  -> casilla  (fila=(Y+1)/2, col=X). Si se activa es la meta ("cuadrado").
  Y par,   X 1..5  -> pared horizontal debajo de la fila Y/2, columna X.
  Y impar, X 6..9  -> pared vertical a la derecha de la columna X-5, fila (Y+1)/2.

Formato v2 (0-indexado, fila r, columna c):
  h: [[r,c]]  pared entre (r,c) y (r+1,c)
  v: [[r,c]]  pared entre (r,c) y (r,c+1)
  goals: [[r,c]], balls: [[r,c]], record: movimientos minimos
"""
import json, os, sys
from collections import deque

SRC = os.path.join(os.path.dirname(__file__), '..', '..', 'src', 'com', 'chocodroid', 'labyrinthGame')
N = 5
DIRS = {'U': (-1, 0), 'D': (1, 0), 'L': (0, -1), 'R': (0, 1)}


def parse(path):
    lines = [l.strip() for l in open(path, encoding='latin-1') if l.strip()]
    i = 0
    h, v, goals, balls = [], [], [], []
    while lines[i] != 'FINACTIVAR':
        y, x = map(int, lines[i].split(','))
        if y % 2 == 1 and x <= 5:
            goals.append([(y - 1) // 2, x - 1])
        elif y % 2 == 0:
            h.append([y // 2 - 1, x - 1])
        else:
            v.append([(y - 1) // 2, x - 6])
        i += 1
    i += 1
    while lines[i] != 'FIN':
        _, y, x = lines[i].split(',')
        balls.append([(int(y) - 1) // 2, int(x) - 1])
        i += 1
    record = int(lines[i + 1].split(',')[1])
    return dict(h=sorted(h), v=sorted(v), goals=goals, balls=balls, record=record)


def blocked(lv, r, c, d):
    """True si desde (r,c) no se puede avanzar un paso en direccion d."""
    dr, dc = DIRS[d]
    nr, nc = r + dr, c + dc
    if not (0 <= nr < N and 0 <= nc < N):
        return True
    if d == 'D': return [r, c] in lv['h']
    if d == 'U': return [nr, c] in lv['h']
    if d == 'R': return [r, c] in lv['v']
    return [r, nc] in lv['v']


def move(lv, balls, d):
    """Motor v2: las bolas mas adelantadas se mueven primero; cada una rueda hasta
    pared, borde u otra bola."""
    dr, dc = DIRS[d]
    order = sorted(range(len(balls)), key=lambda i: -(balls[i][0] * dr + balls[i][1] * dc))
    pos = [list(b) for b in balls]
    for i in order:
        r, c = pos[i]
        while not blocked(lv, r, c, d) and [r + dr, c + dc] not in pos:
            r, c = r + dr, c + dc
        pos[i] = [r, c]
    return pos


def move_original(lv, balls, d):
    """Reproduccion literal del algoritmo de Pelota.moverPelota (2011) para comparar."""
    dr, dc = DIRS[d]
    order = sorted(range(len(balls)), key=lambda i: -(balls[i][0] * dr + balls[i][1] * dc))
    pos = [list(b) for b in balls]
    for i in order:
        r, c = pos[i]
        # rueda ignorando las otras bolas
        while not blocked(lv, r, c, d):
            r, c = r + dr, c + dc
        others = [p for j, p in enumerate(pos) if j != i]
        # si choca con una bola, retrocede hasta 2 veces
        for _ in range(2):
            if [r, c] in others:
                r, c = r - dr, c - dc
        pos[i] = [r, c]
    return pos


def solve(lv, mover):
    start = tuple(map(tuple, lv['balls']))
    goals = set(map(tuple, lv['goals']))
    seen = {start: 0}
    q = deque([start])
    while q:
        s = q.popleft()
        if any(b in goals for b in s):
            return seen[s], len(seen)
        for d in 'UDLR':
            n = tuple(map(tuple, mover(lv, [list(b) for b in s], d)))
            if n not in seen:
                seen[n] = seen[s] + 1
                q.append(n)
    return None, len(seen)


def main():
    levels, report = [], []
    generados = json.load(open(os.path.join(os.path.dirname(__file__), 'new_levels.json')))
    for n in range(1, 101):
        if n >= 70:
            # 70-100 eran copias del nivel 1 en 2011: se usan los de generate_levels.py
            lv = generados[str(n)]
        else:
            lv = parse(os.path.join(SRC, f'nivel{n}.txt'))
        if n == 66:
            # errata del original: la meta "5,1" estaba bajo la bola B (nivel ganado sin mover)
            lv['goals'].remove([2, 0])
        opt, states = solve(lv, move)
        opt_orig, _ = solve(lv, move_original)
        report.append((n, lv['record'], opt, opt_orig, len(lv['balls']), len(lv['goals'])))
        # el record pasa a ser el minimo real (en el nivel 7 el original decia 7 y son 5)
        lv['record'] = opt
        levels.append(lv)
    tut = parse(os.path.join(SRC, 'tutorial.txt'))
    tut['record'] = solve(tut, move)[0]

    for n, rec, opt, oo, nb, ng in report:
        flag = ''
        if opt != oo: flag += '  !! motor distinto del original'
        print(f'nivel {n:3}: record={rec:3} optimo={opt} orig={oo} bolas={nb} metas={ng}{flag}')
    print('tutorial', tut)

    # duplicados (niveles de relleno)
    seen = {}
    for i, lv in enumerate(levels, 1):
        key = json.dumps([lv['h'], lv['v'], lv['goals'], lv['balls']])
        if key in seen:
            print(f'nivel {i} es identico al nivel {seen[key]}')
        else:
            seen[key] = i

    if '--write' in sys.argv:
        out = os.path.join(os.path.dirname(__file__), '..', 'js', 'levels.js')
        with open(out, 'w') as f:
            f.write('// Generado por tools/convert_levels.py a partir de los niveles originales de 2011.\n')
            f.write('// h: paredes bajo (r,c) | v: paredes a la derecha de (r,c) | record: minimo de movimientos\n')
            f.write('export const TUTORIAL = ' + json.dumps(tut, separators=(',', ':')) + ';\n')
            f.write('export const LEVELS = [\n')
            for lv in levels:
                f.write('  ' + json.dumps(lv, separators=(',', ':')) + ',\n')
            f.write('];\n')
        print('escrito', out)


if __name__ == '__main__':
    main()
