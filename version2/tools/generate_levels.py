#!/usr/bin/env python3
"""Genera los niveles 70-100, que en el juego de 2011 eran copias del nivel 1.

Busqueda local (hill climbing) sobre paredes, bolas y metas, puntuando con el solver BFS
de convert_levels.py. Semilla fija => resultado reproducible. Salida: new_levels.json
"""
import json, random, os
from convert_levels import solve, move, N

# (nivel, bolas, metas, movimientos minimos buscados)
PLAN = [(n, 2, 2, 22 + (n - 70) * 8 // 10) for n in range(70, 81)] + \
       [(n, 3, 1 if n % 3 else 2, 20 + (n - 81) * 16 // 19) for n in range(81, 101)]


def random_level(rng, nb, ng):
    cells = [[r, c] for r in range(N) for c in range(N)]
    rng.shuffle(cells)
    lv = dict(h=[], v=[], balls=cells[:nb], goals=cells[nb:nb + ng])
    for _ in range(rng.randint(8, 13)):
        mutate_wall(rng, lv)
    return lv


def mutate_wall(rng, lv):
    k = rng.choice('hv')
    w = [rng.randrange(N - 1), rng.randrange(N)] if k == 'h' else [rng.randrange(N), rng.randrange(N - 1)]
    if w in lv[k]:
        lv[k].remove(w)
    else:
        lv[k].append(w)


def mutate(rng, lv):
    lv = json.loads(json.dumps(lv))
    for _ in range(rng.randint(1, 2)):
        if rng.random() < 0.75:
            mutate_wall(rng, lv)
        else:
            key = rng.choice(['balls', 'goals'])
            used = lv['balls'] + lv['goals']
            free = [[r, c] for r in range(N) for c in range(N) if [r, c] not in used]
            lv[key][rng.randrange(len(lv[key]))] = rng.choice(free)
    return lv


def score(lv, target):
    opt, _ = solve(lv, move)
    if opt is None or opt == 0:
        return -999, opt
    walls = len(lv['h']) + len(lv['v'])
    return -abs(opt - target) * 10 - max(0, walls - 16), opt


def build(n, nb, ng, target, rng):
    best, (bs, bo) = None, (-10**9, None)
    for _restart in range(12):
        cur = random_level(rng, nb, ng)
        cs, co = score(cur, target)
        for _ in range(500):
            cand = mutate(rng, cur)
            s, o = score(cand, target)
            if s >= cs:
                cur, cs, co = cand, s, o
        if cs > bs:
            best, bs, bo = cur, cs, co
        if bo == target:
            break
    best['h'].sort(); best['v'].sort()
    best['record'] = bo
    print(f'nivel {n}: objetivo={target} optimo={bo} paredes={len(best["h"]) + len(best["v"])}', flush=True)
    return best


def main():
    rng = random.Random(2011)
    out = {}
    for n, nb, ng, target in PLAN:
        out[n] = build(n, nb, ng, target, rng)
    path = os.path.join(os.path.dirname(__file__), 'new_levels.json')
    json.dump(out, open(path, 'w'), indent=1)
    print('escrito', path)


if __name__ == '__main__':
    main()
