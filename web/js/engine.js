/* Motor del puzle, trasladado de Pelota.java, Cuadrado.java y GeneradorNiveles.java. */
(function (root) {
  var DENSITY = 1.5;

  function dp(value) {
    return Math.trunc(value * DENSITY);
  }

  var THEMES = {
    madera: {
      id: "madera",
      name: "Madera",
      prefix: "madera",
      fondo: "madera_fondo",
      marco: "madera_marco_juego",
      ball: "madera_pelota",
      ballHit: "madera_pelota_colision"
    },
    verde: {
      id: "verde",
      name: "Verde",
      prefix: "verde",
      fondo: "verde_fondo",
      marco: "verde_marco_juego",
      ball: "verde_pelota",
      ballHit: "verde_pelota_colision",
      balls: ["verde_pelota", "verde_futbol"],
      ballHits: ["verde_pelota_colision", "verde_futbol"]
    },
    piedra: {
      id: "piedra",
      name: "Piedra",
      prefix: "piedra",
      fondo: "piedra_fondo",
      marco: "piedra_marco_juego",
      ball: "piedra_pelota",
      ballHit: "piedra_pelota_colision"
    },
    agua: {
      id: "agua",
      name: "Hielo",
      prefix: "ice",
      fondo: "agua_fondo",
      marco: "agua_marco_juego",
      ball: "hielo_verde",
      ballHit: "hielo_verde",
      balls: ["hielo_verde", "hielo_rosa"],
      ballHits: ["hielo_verde", "hielo_rosa"]
    },
    mercado: {
      id: "mercado",
      name: "Mercado",
      prefix: "mercado",
      fondo: "mercado_fondo",
      marco: "mercado_marco_juego",
      ball: "mercado_sandia",
      ballHit: "mercado_sandia"
    }
  };

  var BASE_SPRITE = {
    bordeVertical1: "vertical_fila_1",
    bordeVertical: "vertical_fila_2_3_4",
    bordeVertical5: "vertical_fila_5",
    bordeHorizontal1: "horizontal_columna1",
    bordeHorizontal: "horizontal_columna2_3_4",
    bordeHorizontal5: "horizontal_columna5"
  };

  function themeForLevel(level) {
    if (level < 21) return THEMES.madera;
    if (level < 41) return THEMES.verde;
    if (level < 61) return THEMES.piedra;
    if (level < 81) return THEMES.agua;
    return THEMES.mercado;
  }

  function worldForLevel(level) {
    return Math.floor((level - 1) / 20) + 1;
  }

  function parseBoard(text) {
    var cells = new Map();
    var lines = text.split(/\r?\n/);
    for (var i = 0; i < lines.length; i++) {
      var line = lines[i].trim();
      if (!line || line === "FIN") break;
      var parts = line.split(",");
      var y = +parts[0];
      var x = +parts[1];
      var tipo = parts[2];
      cells.set(x + "," + y, {
        x: x,
        y: y,
        tipo: tipo,
        px: dp(+parts[3]),
        py: dp(+parts[4]),
        kind: tipo === "cuadrado" ? "meta" : "pared"
      });
    }
    return cells;
  }

  function parseLevel(text) {
    var lines = text.split(/\r?\n/);
    var clean = [];
    for (var i = 0; i < lines.length; i++) {
      var line = lines[i].trim();
      if (line) clean.push(line);
    }
    var walls = [];
    var balls = [];
    var record = 1;
    var phase = "walls";
    for (var j = 0; j < clean.length; j++) {
      var row = clean[j];
      if (row === "FINACTIVAR") {
        phase = "balls";
        continue;
      }
      if (row === "FIN") {
        phase = "record";
        continue;
      }
      if (phase === "walls") {
        var wall = row.split(",");
        walls.push({ y: +wall[0], x: +wall[1] });
      } else if (phase === "balls") {
        var ball = row.split(",");
        balls.push({ name: ball[0], y: +ball[1], x: +ball[2] });
      } else if (row.indexOf("RECORD") === 0) {
        record = +row.split(",")[1];
      }
    }
    return { walls: walls, balls: balls, record: record };
  }

  function activated(cells, x, y) {
    var cell = cells.get(x + "," + y);
    return !!(cell && cell.activated);
  }

  function spriteFor(cell, cells, prefix) {
    if (cell.kind === "meta") return "casilla_final";
    var base = prefix + "_" + BASE_SPRITE[cell.tipo];
    if (cell.tipo === "bordeVertical" || cell.tipo === "bordeVertical5") {
      if (cell.y > 1 && activated(cells, cell.x, cell.y - 2)) {
        if (cell.y === 9) return prefix + "_vertical_fila_5_continuacion";
        return prefix + "_vertical_fila_2_3_4_continuacion";
      }
    }
    if (cell.tipo === "bordeHorizontal1") {
      if (!activated(cells, cell.x + 5, cell.y - 1) &&
          !activated(cells, cell.x + 5, cell.y + 1) &&
          activated(cells, cell.x + 1, cell.y)) {
        return prefix + "_horizontal_columna1_continuacion";
      }
    } else if (cell.tipo === "bordeHorizontal") {
      var leftClear = !activated(cells, cell.x + 4, cell.y - 1) && !activated(cells, cell.x + 4, cell.y + 1);
      var rightClear = !activated(cells, cell.x + 5, cell.y - 1) && !activated(cells, cell.x + 5, cell.y + 1);
      var joinLeft = activated(cells, cell.x - 1, cell.y);
      var joinRight = activated(cells, cell.x + 1, cell.y);
      if (leftClear && rightClear && joinLeft && joinRight) {
        return prefix + "_horizontal_columna2_3_4_continuacion_izq_y_drcha";
      }
      if (leftClear && joinLeft) return prefix + "_horizontal_columna2_3_4_continuacion_izq";
      if (rightClear && joinRight) return prefix + "_horizontal_columna2_3_4_continuacion_dcha";
    } else if (cell.tipo === "bordeHorizontal5") {
      if (!activated(cells, cell.x + 4, cell.y - 1) &&
          !activated(cells, cell.x + 4, cell.y + 1) &&
          activated(cells, cell.x - 1, cell.y)) {
        return prefix + "_horizontal_columna5_continuacion";
      }
    }
    return base;
  }

  function placeBall(ball, cells) {
    var cell = cells.get(ball.x + "," + ball.y);
    if (!cell) return;
    ball.px = cell.px;
    ball.py = cell.py;
  }

  function occupied(ball, balls) {
    for (var i = 0; i < balls.length; i++) {
      var other = balls[i];
      if (other !== ball && other.x === ball.x && other.y === ball.y) return true;
    }
    return false;
  }

  function nearestWall(cells, predicate, closer) {
    var found = null;
    cells.forEach(function (cell) {
      if (!cell.activated || cell.kind !== "pared") return;
      if (!predicate(cell)) return;
      if (!found || closer(cell, found)) found = cell;
    });
    return found;
  }

  function retreat(ball, cells, dx, dy) {
    var nextX = ball.x + dx;
    var nextY = ball.y + dy;
    if (!cells.has(nextX + "," + nextY)) return;
    ball.x = nextX;
    ball.y = nextY;
  }

  function stepBall(ball, balls, cells, dir) {
    var startX = ball.x;
    var startY = ball.y;
    if (dir === "arriba" && ball.y > 1) {
      var up = nearestWall(cells, function (cell) {
        return cell.x === ball.x && cell.y < ball.y;
      }, function (cell, best) {
        return (ball.y - cell.y) < (ball.y - best.y);
      });
      ball.y = up ? up.y + 1 : 1;
      if (occupied(ball, balls)) retreat(ball, cells, 0, 2);
      if (occupied(ball, balls)) retreat(ball, cells, 0, 2);
    } else if (dir === "abajo" && ball.y < 9) {
      var down = nearestWall(cells, function (cell) {
        return cell.x === ball.x && cell.y > ball.y;
      }, function (cell, best) {
        return (cell.y - ball.y) < (best.y - ball.y);
      });
      ball.y = down ? down.y - 1 : 9;
      if (occupied(ball, balls)) retreat(ball, cells, 0, -2);
      if (occupied(ball, balls)) retreat(ball, cells, 0, -2);
    } else if (dir === "izquierda" && ball.x > 1) {
      var left = nearestWall(cells, function (cell) {
        return cell.y === ball.y && cell.x < ball.x + 5;
      }, function (cell, best) {
        return (ball.x + 5 - cell.x) < (ball.x + 5 - best.x);
      });
      ball.x = left ? left.x - 4 : 1;
      if (occupied(ball, balls)) retreat(ball, cells, 1, 0);
      if (occupied(ball, balls)) retreat(ball, cells, 1, 0);
    } else if (dir === "derecha" && ball.x < 5) {
      var right = nearestWall(cells, function (cell) {
        return cell.y === ball.y && cell.x >= ball.x + 5;
      }, function (cell, best) {
        return (cell.x - (ball.x + 5)) < (best.x - (ball.x + 5));
      });
      ball.x = right ? right.x - 5 : 5;
      if (occupied(ball, balls)) retreat(ball, cells, -1, 0);
      if (occupied(ball, balls)) retreat(ball, cells, -1, 0);
    }
    placeBall(ball, cells);
    return ball.x !== startX || ball.y !== startY;
  }

  function sortBalls(balls, dir) {
    return balls.slice().sort(function (a, b) {
      if (dir === "arriba") return a.py - b.py;
      if (dir === "abajo") return b.py - a.py;
      if (dir === "izquierda") return a.px - b.px;
      return b.px - a.px;
    });
  }

  function markHits(balls, cells) {
    for (var i = 0; i < balls.length; i++) balls[i].hit = false;
    for (var a = 0; a < balls.length; a++) {
      for (var b = 0; b < balls.length; b++) {
        if (a === b) continue;
        var p = balls[a];
        var q = balls[b];
        if (p.y === q.y) {
          if (p.x === q.x - 1 && !activated(cells, p.x + 5, p.y)) p.hit = true;
          if (p.x === q.x + 1 && !activated(cells, p.x + 4, p.y)) p.hit = true;
        }
        if (p.x === q.x) {
          if (p.y === q.y + 2 && !activated(cells, p.x, p.y - 1)) p.hit = true;
          if (p.y === q.y - 2 && !activated(cells, p.x, p.y + 1)) p.hit = true;
        }
      }
    }
  }

  function isWon(balls, cells) {
    for (var i = 0; i < balls.length; i++) {
      var ball = balls[i];
      var cell = cells.get(ball.x + "," + ball.y);
      if (cell && cell.activated && cell.kind === "meta") return true;
    }
    return false;
  }

  function starsFor(score, record) {
    if (score === record) return 3;
    if (score <= record * 1.5) return 2;
    if (score <= record * 2) return 1;
    return 0;
  }

  function createLevel(template, levelData, theme) {
    var cells = new Map();
    template.forEach(function (cell, key) {
      cells.set(key, {
        x: cell.x,
        y: cell.y,
        tipo: cell.tipo,
        px: cell.px,
        py: cell.py,
        kind: cell.kind,
        activated: false,
        sprite: null
      });
    });
    for (var i = 0; i < levelData.walls.length; i++) {
      var wall = levelData.walls[i];
      var target = cells.get(wall.x + "," + wall.y);
      if (target) target.activated = true;
    }
    cells.forEach(function (cell) {
      if (!cell.activated) return;
      cell.sprite = spriteFor(cell, cells, theme.prefix);
    });
    var balls = levelData.balls.map(function (ball) {
      var placed = { name: ball.name, x: ball.x, y: ball.y, px: 0, py: 0, hit: false };
      placeBall(placed, cells);
      return placed;
    });
    markHits(balls, cells);
    return {
      cells: cells,
      balls: balls,
      record: levelData.record,
      theme: theme,
      start: levelData.balls.map(function (ball) {
        return { name: ball.name, x: ball.x, y: ball.y };
      })
    };
  }

  function resetLevel(level) {
    for (var i = 0; i < level.balls.length; i++) {
      var ball = level.balls[i];
      var origin = level.start[i];
      ball.x = origin.x;
      ball.y = origin.y;
      placeBall(ball, level.cells);
    }
    markHits(level.balls, level.cells);
  }

  function moveLevel(level, dir) {
    var order = sortBalls(level.balls, dir);
    var moved = false;
    for (var i = 0; i < order.length; i++) {
      if (stepBall(order[i], level.balls, level.cells, dir)) moved = true;
    }
    markHits(level.balls, level.cells);
    return { moved: moved, won: moved && isWon(level.balls, level.cells) };
  }

  function zoneAt(x, y) {
    var top = dp(86);
    var side = dp(320);
    var mid = dp(160);
    function inside(px, py, x1, y1, x2, y2, x3, y3) {
      function orient(ax, ay, bx, by) {
        var value = ((bx - ax) * (py - ay)) - ((px - ax) * (by - ay));
        if (value > 0) return 1;
        if (value < 0) return -1;
        return 0;
      }
      var o1 = orient(x1, y1, x2, y2);
      var o2 = orient(x2, y2, x3, y3);
      var o3 = orient(x3, y3, x1, y1);
      return o1 === o2 && o2 === o3;
    }
    if (inside(x, y, 0, top, side, top, mid, mid + top)) return "arriba";
    if (inside(x, y, 0, side + top, mid, mid + top, side, side + top)) return "abajo";
    if (inside(x, y, 0, top, mid, mid + top, 0, side + top)) return "izquierda";
    if (inside(x, y, side, top, mid, mid + top, side, side + top)) return "derecha";
    return null;
  }

  root.Labyrinth = {
    dp: dp,
    THEMES: THEMES,
    themeForLevel: themeForLevel,
    worldForLevel: worldForLevel,
    parseBoard: parseBoard,
    parseLevel: parseLevel,
    createLevel: createLevel,
    resetLevel: resetLevel,
    moveLevel: moveLevel,
    starsFor: starsFor,
    zoneAt: zoneAt,
    BOARD_TOP: dp(86),
    FRAME_TOP: dp(71),
    GOAL_NUDGE: dp(10.3),
    HIT_NUDGE: 5
  };
})(typeof window !== "undefined" ? window : globalThis);
