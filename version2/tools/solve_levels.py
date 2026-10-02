#!/usr/bin/env python3
"""Comprueba que todos los niveles del juego se pueden resolver y que su record es el
minimo real de movimientos, y guarda la solucion optima de cada uno en js/solutions.js.

Lee los niveles de js/levels.js (los que usa el juego, no los .txt de 2011). Para cada uno
hace una busqueda en anchura sobre todas las posiciones posibles de las bolas, asi que la
primera solucion que encuentra es la mas corta que existe. Despues repite cada solucion
con dos motores distintos (el de la v2 y la reproduccion literal del de 2011) y comprueba
que ninguna solucion mas corta gana.

    python3 version2/tools/solve_levels.py            # solo comprobar
    python3 version2/tools/solve_levels.py --write    # comprobar y escribir js/solutions.js
"""
import json
import os
import sys
from collections import deque

from convert_levels import move, move_original

HERE = os.path.dirname(os.path.abspath(__file__))
LEVELS_JS = os.path.join(HERE, '..', 'js', 'levels.js')
OUT = os.path.join(HERE, '..', 'js', 'solutions.js')
DIRS = 'UDLR'


def load_levels():
    levels, tutorial = [], None
    for line in open(LEVELS_JS, encoding='utf-8'):
        line = line.strip()
        if line.startswith('export const TUTORIAL = '):
            tutorial = json.loads(line[len('export const TUTORIAL = '):].rstrip(';'))
        elif line.startswith('{'):
            levels.append(json.loads(line.rstrip(',')))
    return levels, tutorial


def key(balls):
    return tuple(map(tuple, balls))


def won(lv, balls):
    goals = set(map(tuple, lv['goals']))
    return any(tuple(b) in goals for b in balls)


def solve(lv):
    """Devuelve (solucion mas corta, posiciones exploradas, numero de soluciones optimas)."""
    start = key(lv['balls'])
    dist = {start: 0}
    ways = {start: 1}           # caminos minimos distintos que llegan a cada posicion
    prev = {start: None}
    queue = deque([start])
    best, n_best, first = None, 0, None
    while queue:
        s = queue.popleft()
        if best is not None and dist[s] >= best:
            break
        for d in DIRS:
            n = key(move(lv, [list(b) for b in s], d))
            if n == s:
                continue        # ninguna bola se mueve: no cuenta como movimiento
            if n not in dist:
                dist[n] = dist[s] + 1
                ways[n] = ways[s]
                prev[n] = (s, d)
                if won(lv, n):
                    if best is None:
                        best, first = dist[n], n
                else:
                    queue.append(n)
            elif dist[n] == dist[s] + 1:
                ways[n] += ways[s]
    if best is None:
        return None, len(dist), 0
    n_best = sum(w for s, w in ways.items() if dist[s] == best and won(lv, s))
    path, s = [], first
    while prev[s]:
        s, d = prev[s]
        path.append(d)
    return ''.join(reversed(path)), len(dist), n_best


def replay(lv, path, mover):
    """Repite una solucion; devuelve en que movimiento se gana (o None) y si todos mueven."""
    balls = [list(b) for b in lv['balls']]
    for i, d in enumerate(path, 1):
        nxt = mover(lv, balls, d)
        if key(nxt) == key(balls):
            return None         # movimiento que no mueve nada
        balls = nxt
        if won(lv, balls):
            return i
    return None


def main():
    levels, tutorial = load_levels()
    assert len(levels) == 100, len(levels)
    problems, solutions = [], []
    print(f'{"nivel":>5} {"record":>6} {"minimo":>6} {"posiciones":>10} {"soluciones optimas":>18}  solucion')
    for n, lv in enumerate(levels, 1):
        path, states, n_best = solve(lv)
        if path is None:
            problems.append(f'nivel {n}: NO TIENE SOLUCION')
            solutions.append('')
            continue
        if won(lv, lv['balls']):
            problems.append(f'nivel {n}: empieza ya resuelto')
        if len(path) != lv['record']:
            problems.append(f'nivel {n}: record {lv["record"]} pero el minimo es {len(path)}')
        for name, mover in (('v2', move), ('2011', move_original)):
            if replay(lv, path, mover) != len(path):
                problems.append(f'nivel {n}: la solucion no gana con el motor {name}')
        solutions.append(path)
        print(f'{n:>5} {lv["record"]:>6} {len(path):>6} {states:>10} {n_best:>18}  {path}')

    tut_path, _, _ = solve(tutorial)
    if tut_path is None or len(tut_path) != tutorial['record']:
        problems.append('tutorial: record incorrecto o sin solucion')
    print(f'tutorial: record {tutorial["record"]}, minimo {len(tut_path)}, solucion {tut_path}')

    print()
    if problems:
        print('PROBLEMAS:')
        for p in problems:
            print('  -', p)
        sys.exit(1)
    print(f'OK: los 100 niveles y el tutorial tienen solucion y su record es el minimo real.')
    print(f'Movimientos minimos en total: {sum(map(len, solutions))}. Nivel mas largo: {max(map(len, solutions))}.')

    if '--write' in sys.argv:
        with open(OUT, 'w', encoding='utf-8') as f:
            f.write('// Generado por tools/solve_levels.py. No editar a mano.\n')
            f.write('// Solucion optima (minimo de movimientos) de cada nivel de js/levels.js, hallada por\n')
            f.write('// busqueda exhaustiva. U = arriba, D = abajo, L = izquierda, R = derecha.\n')
            f.write('// SOLUTIONS[n - 1] es la solucion del nivel n; su longitud es el record del nivel.\n')
            f.write('// Muchos niveles tienen varias soluciones igual de cortas: aqui se guarda una.\n')
            f.write(f"export const TUTORIAL_SOLUTION = '{tut_path}';\n")
            f.write('export const SOLUTIONS = [\n')
            for n, p in enumerate(solutions, 1):
                f.write(f"  '{p}', // {n}: {len(p)}\n")
            f.write('];\n')
        print('escrito', os.path.relpath(OUT))


if __name__ == '__main__':
    main()
