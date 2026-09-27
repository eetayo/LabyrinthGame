(function () {
  var L = window.Labyrinth;
  var IMG = "../res/drawable-hdpi/";
  var RAW = "../res/raw/";
  var DATA = "../src/com/chocodroid/labyrinthGame/";
  var SAVE_KEY = "logicLabyrinth.v1";

  var WORLDS = [
    { id: 1, name: "Madera", theme: "madera", bg: "mundos01", color: "linear-gradient(#c9843a, #8a4e16)" },
    { id: 2, name: "Verde", theme: "verde", bg: "mundos02", color: "linear-gradient(#3e9a45, #1d6b28)" },
    { id: 3, name: "Piedra", theme: "piedra", bg: "mundos03", color: "linear-gradient(#8d8478, #4e473f)" },
    { id: 4, name: "Hielo", theme: "agua", bg: "mundos04", color: "linear-gradient(#6eb7e0, #2d6f9a)" },
    { id: 5, name: "Mercado", theme: "mercado", bg: "mundos05", color: "linear-gradient(#d4543c, #8c2a1c)" }
  ];

  var phone = document.getElementById("phone");
  var save = loadSave();
  var catalog = { board: null, levels: {}, tutorial: null };
  var play = {
    levelNumber: 1,
    tutorial: false,
    model: null,
    moves: 0,
    busy: false,
    won: false,
    pointer: null
  };
  var worlds = { index: 0 };
  var worldSwipe = { active: false, moved: false, dx: 0, vx: 0 };

  var music = new Audio(RAW + "musica.mp3");
  music.loop = true;
  music.volume = 0.35;
  var clickSound = new Audio(RAW + "pulsacion_boton_corta.mp3");
  var hits = [new Audio(RAW + "choque_bola.mp3"), new Audio(RAW + "choque_bola.mp3")];
  var hitIndex = 0;

  function loadSave() {
    try {
      var stored = JSON.parse(localStorage.getItem(SAVE_KEY));
      if (stored && stored.scores) return stored;
    } catch (error) { /* partida nueva */ }
    return { scores: {}, sound: true, music: true, help: true, ball: 0 };
  }

  function persist() {
    localStorage.setItem(SAVE_KEY, JSON.stringify(save));
  }

  var LOGO_BALLS = [
    "../res/drawable-hdpi/madera_pelota.png",
    "../res/drawable-hdpi/verde_pelota.png",
    "img/verde_futbol.png",
    "../res/drawable-hdpi/piedra_pelota.png",
    "img/hielo_verde.png",
    "img/hielo_rosa.png",
    "img/mercado_sandia.png"
  ];
  var LOCAL_BALLS = {
    verde_futbol: true,
    hielo_verde: true,
    hielo_rosa: true,
    mercado_sandia: true
  };
  var LOGO_SCREENS = { loading: true, menu: true, help: true, options: true, worlds: true };

  function show(id) {
    var screens = document.querySelectorAll(".screen");
    for (var i = 0; i < screens.length; i++) {
      screens[i].classList.toggle("active", screens[i].id === "screen-" + id);
    }
    phone.classList.toggle("show-logo", !!LOGO_SCREENS[id]);
  }

  function paintLogoBall() {
    var index = save.ball || 0;
    if (index < 0 || index >= LOGO_BALLS.length) {
      index = 0;
      save.ball = 0;
      persist();
    }
    document.getElementById("logo-ball-img").src = LOGO_BALLS[index];
  }

  function cycleLogoBall() {
    save.ball = ((save.ball || 0) + 1) % LOGO_BALLS.length;
    persist();
    var button = document.getElementById("logo-ball");
    button.classList.remove("pop");
    void button.offsetWidth;
    button.classList.add("pop");
    paintLogoBall();
    playClick();
  }

  function playClick() {
    if (!save.sound) return;
    clickSound.currentTime = 0;
    clickSound.play().catch(function () {});
  }

  function playHit() {
    if (!save.sound) return;
    var audio = hits[hitIndex];
    hitIndex = (hitIndex + 1) % hits.length;
    audio.currentTime = 0;
    audio.play().catch(function () {});
  }

  function syncMusic() {
    if (save.music) {
      music.play().catch(function () {});
    } else {
      music.pause();
    }
  }

  function best(level) {
    var value = save.scores[String(level)];
    return value == null ? null : value;
  }

  function isUnlocked(level) {
    return level === 1 || best(level - 1) != null;
  }

  function worldStars(world) {
    var total = 0;
    var start = (world - 1) * 20 + 1;
    for (var level = start; level < start + 20; level++) {
      var score = best(level);
      var data = catalog.levels[level];
      if (score != null && data) total += L.starsFor(score, data.record);
    }
    return total;
  }

  function fit() {
    var scale = Math.min(window.innerWidth / 480, window.innerHeight / 800);
    document.getElementById("scaler").style.transform = "scale(" + scale + ")";
  }

  function fetchText(url, attempt) {
    return fetch(url).then(function (response) {
      if (!response.ok) throw new Error(url);
      return response.text();
    }).catch(function (error) {
      if ((attempt || 0) < 4) return fetchText(url, (attempt || 0) + 1);
      throw error;
    });
  }

  function loadInBatches(urls, size, onProgress) {
    var results = new Array(urls.length);
    var cursor = 0;
    function next() {
      if (cursor >= urls.length) return Promise.resolve(results);
      var start = cursor;
      var slice = urls.slice(cursor, cursor + size);
      cursor += slice.length;
      return Promise.all(slice.map(function (url, offset) {
        return fetchText(url).then(function (text) {
          results[start + offset] = text;
          onProgress(start + offset + 1, urls.length);
        });
      })).then(next);
    }
    return next();
  }

  function boot() {
    var urls = [DATA + "posiciones_tema_madera.txt", DATA + "tutorial.txt"];
    for (var level = 1; level <= 100; level++) urls.push(DATA + "nivel" + level + ".txt");
    var label = document.getElementById("loading-text");
    loadInBatches(urls, 8, function (done, total) {
      label.textContent = "Cargando niveles… " + done + "/" + total;
    }).then(function (files) {
      catalog.board = L.parseBoard(files[0]);
      catalog.tutorial = L.parseLevel(files[1]);
      for (var n = 1; n <= 100; n++) catalog.levels[n] = L.parseLevel(files[n + 1]);
      renderOptions();
      show("menu");
    }).catch(function () {
      label.textContent = "No se pudieron cargar los niveles. Abre el juego desde la carpeta del proyecto con un servidor local.";
    });
  }

  function renderWorlds() {
    var list = document.getElementById("world-list");
    list.innerHTML = "";
    WORLDS.forEach(function (world) {
      var open = world.id === 1 || isUnlocked((world.id - 1) * 20 + 1);
      var button = document.createElement("button");
      button.className = "world";
      button.style.background = world.color;
      button.disabled = !open;
      button.innerHTML = "<div><b>" + world.name + "</b><span>Niveles " + ((world.id - 1) * 20 + 1) + "–" + (world.id * 20) + "</span></div><em>" + (open ? worldStars(world.id) + " / 60 ★" : "Cerrado") + "</em>";
      button.addEventListener("click", function () {
        if (!open) return;
        playClick();
        openLevels(world.id);
      });
      list.appendChild(button);
    });
  }

  function furthestWorld() {
    var world = 1;
    for (var id = 2; id <= WORLDS.length; id++) {
      if (isUnlocked((id - 1) * 20 + 1)) world = id;
    }
    return world;
  }

  function placeWorldTrack(index, animate) {
    var track = document.getElementById("world-track");
    track.style.transition = animate ? "transform 0.32s cubic-bezier(.22,.8,.28,1)" : "none";
    track.style.transform = "translateX(" + (-index * 480) + "px)";
  }

  function updateWorldChrome(index) {
    var world = WORLDS[index];
    document.getElementById("levels-title").textContent = world.name;
    document.getElementById("levels-stars").textContent = worldStars(world.id) + " / 60 estrellas";
    var dots = document.querySelectorAll(".world-dot");
    for (var i = 0; i < dots.length; i++) dots[i].classList.toggle("on", i === index);
  }

  function renderLevelPages() {
    var track = document.getElementById("world-track");
    var dots = document.getElementById("world-dots");
    track.innerHTML = "";
    dots.innerHTML = "";
    WORLDS.forEach(function (world, worldIndex) {
      var page = document.createElement("div");
      page.className = "world-page";
      var bg = document.createElement("img");
      bg.className = "fill";
      bg.alt = "";
      bg.src = IMG + world.bg + ".png";
      page.appendChild(bg);
      var grid = document.createElement("div");
      grid.className = "level-grid";
      for (var index = 0; index < 20; index++) {
        var level = worldIndex * 20 + index + 1;
        var unlocked = isUnlocked(level);
        var button = document.createElement("button");
        button.className = "level-btn";
        button.disabled = !unlocked;
        var score = best(level);
        var stars = unlocked && score != null ? L.starsFor(score, catalog.levels[level].record) : 0;
        var art = unlocked ? "boton_nivel" + stars + "estrellas" : "boton_nivel_bloqueado_estrellas";
        button.style.backgroundImage = "url('" + IMG + art + ".png')";
        button.textContent = unlocked ? String(level) : "";
        button.addEventListener("click", function (chosen) {
          return function () {
            if (worldSwipe.moved || !isUnlocked(chosen)) return;
            playClick();
            startLevel(chosen, false);
          };
        }(level));
        grid.appendChild(button);
      }
      page.appendChild(grid);
      track.appendChild(page);

      var dot = document.createElement("button");
      dot.className = "world-dot" + (worldIndex === worlds.index ? " on" : "");
      dot.type = "button";
      dot.setAttribute("aria-label", world.name);
      dot.addEventListener("click", function (chosen) {
        return function () {
          worlds.index = chosen;
          placeWorldTrack(chosen, true);
          updateWorldChrome(chosen);
        };
      }(worldIndex));
      dots.appendChild(dot);
    });
    placeWorldTrack(worlds.index, false);
    updateWorldChrome(worlds.index);
  }

  function openLevels(worldId) {
    worlds.index = Math.max(0, Math.min(WORLDS.length - 1, worldId - 1));
    renderLevelPages();
    show("levels");
  }

  function phoneScale() {
    return phone.getBoundingClientRect().width / 480 || 1;
  }

  function renderOptions() {
    document.getElementById("opt-sound").textContent = "Sonido: " + (save.sound ? "ON" : "OFF");
    document.getElementById("opt-music").textContent = "Música: " + (save.music ? "ON" : "OFF");
    document.getElementById("opt-help").textContent = "Ayuda: " + (save.help ? "ON" : "OFF");
  }

  function startLevel(levelNumber, tutorial) {
    play.levelNumber = levelNumber;
    play.tutorial = tutorial;
    play.moves = 0;
    play.busy = false;
    play.won = false;
    var data = tutorial ? catalog.tutorial : catalog.levels[levelNumber];
    var theme = tutorial ? L.THEMES.madera : L.themeForLevel(levelNumber);
    play.model = L.createLevel(catalog.board, data, theme);
    document.getElementById("play-bg").src = IMG + theme.fondo + ".png";
    document.getElementById("play-frame").src = IMG + theme.marco + ".png";
    document.getElementById("play-help").classList.toggle("off", !save.help);
    hideOverlay();
    drawBoard();
    updateHud();
    var banner = document.getElementById("level-banner");
    banner.textContent = tutorial ? "Tutorial" : "Nivel " + levelNumber;
    banner.classList.remove("show");
    void banner.offsetWidth;
    banner.classList.add("show");
    show("play");
    syncMusic();
  }

  function drawBoard() {
    var layer = document.getElementById("pieces");
    layer.innerHTML = "";
    var theme = play.model.theme;
    play.model.cells.forEach(function (cell) {
      if (!cell.activated || !cell.sprite) return;
      var image = document.createElement("img");
      image.src = IMG + cell.sprite + ".png";
      image.alt = "";
      if (cell.kind === "meta") image.className = "goal";
      else if (cell.tipo && cell.tipo.indexOf("Horizontal") >= 0) image.className = "wall-h";
      else image.className = "wall-v";
      var nudge = cell.kind === "meta" ? L.GOAL_NUDGE : 0;
      image.style.left = (cell.px + nudge) + "px";
      image.style.top = (cell.py + L.BOARD_TOP + nudge) + "px";
      layer.appendChild(image);
    });
    play.model.balls.forEach(function (ball) {
      var image = document.createElement("img");
      image.className = "ball";
      image.alt = "";
      image.dataset.name = ball.name;
      placeBallImage(image, ball, false);
      layer.appendChild(image);
    });
  }

  function spriteUrl(name) {
    if (LOCAL_BALLS[name]) return "img/" + name + ".png";
    return IMG + name + ".png";
  }

  function ballArt(ball) {
    var theme = play.model.theme;
    var normal = theme.balls;
    if (!normal) return ball.hit ? theme.ballHit : theme.ball;
    var letter = (ball.name || "A").slice(-1).toUpperCase();
    var index = letter.charCodeAt(0) - 65;
    if (index < 0 || index > 25) index = 0;
    index = index % normal.length;
    var hits = theme.ballHits;
    if (ball.hit && hits && hits[index]) return hits[index];
    return normal[index];
  }

  function placeBallImage(image, ball, animate) {
    var nudge = ball.hit ? L.HIT_NUDGE : 0;
    var art = ballArt(ball);
    if (!animate) image.style.transition = "none";
    image.src = spriteUrl(art);
    image.style.left = (ball.px - nudge) + "px";
    image.style.top = (ball.py + L.BOARD_TOP - nudge) + "px";
    if (!animate) {
      void image.offsetWidth;
      image.style.transition = "";
    }
  }

  function updateHud() {
    var record = play.model.record;
    var previous = play.tutorial ? null : best(play.levelNumber);
    document.getElementById("move-label").innerHTML = "Movimientos: <b>" + play.moves + "</b>";
    document.getElementById("record-label").textContent = "Récord: " + record + (previous != null ? "  ·  Tu marca: " + previous : "");
  }

  function phonePoint(event) {
    var rect = phone.getBoundingClientRect();
    return {
      x: (event.clientX - rect.left) / (rect.width / 480),
      y: (event.clientY - rect.top) / (rect.height / 800)
    };
  }

  function tryMove(dir) {
    if (!dir || play.busy || play.won || !document.getElementById("screen-play").classList.contains("active")) return;
    if (!document.getElementById("overlay").classList.contains("hidden")) return;
    var before = play.model.balls.map(function (ball) {
      return { x: ball.x, y: ball.y, px: ball.px, py: ball.py };
    });
    var result = L.moveLevel(play.model, dir);
    if (!result.moved) return;
    play.busy = true;
    play.moves += 1;
    playHit();
    updateHud();
    var pending = 0;
    play.model.balls.forEach(function (ball, index) {
      var distance = Math.abs(ball.x - before[index].x) + Math.abs(ball.y - before[index].y) / 2;
      var image = document.querySelector('#pieces img[data-name="' + ball.name + '"]');
      if (distance === 0) {
        placeBallImage(image, ball, false);
        return;
      }
      pending += 1;
      var duration = Math.min(720, 90 + distance * 80);
      image.style.transitionDuration = duration + "ms";
      placeBallImage(image, ball, true);
      var finished = false;
      function done() {
        if (finished) return;
        finished = true;
        pending -= 1;
        if (pending === 0) finishMove(result.won);
      }
      image.addEventListener("transitionend", done, { once: true });
      setTimeout(done, duration + 50);
    });
    if (pending === 0) finishMove(result.won);
  }

  function finishMove(won) {
    play.model.balls.forEach(function (ball) {
      var image = document.querySelector('#pieces img[data-name="' + ball.name + '"]');
      placeBallImage(image, ball, false);
    });
    play.busy = false;
    if (won) showWin();
  }

  function showWin() {
    play.won = true;
    var record = play.model.record;
    var stars = L.starsFor(play.moves, record);
    if (!play.tutorial) {
      var previous = best(play.levelNumber);
      if (previous == null || play.moves < previous) {
        save.scores[String(play.levelNumber)] = play.moves;
        persist();
      }
    }
    var starText = "";
    for (var i = 1; i <= 3; i++) starText += i <= stars ? "★" : "<i>★</i>";
    var title = play.moves === record ? "¡Perfecto!" : "¡Superado!";
    var next = play.tutorial || play.levelNumber >= 100 ? "" : '<button class="btn" data-action="next">Siguiente</button>';
    var back = play.tutorial ? "Menú" : "Niveles";
    openCard(
      "<h2>" + title + "</h2>" +
      '<div class="stars">' + starText + "</div>" +
      "<p>" + play.moves + (play.moves === 1 ? " movimiento" : " movimientos") + " · récord " + record + "</p>" +
      next +
      '<button class="btn" data-action="restart">Reintentar</button>' +
      '<button class="btn btn-ghost" data-action="leave-win">' + back + "</button>"
    );
  }

  function showPause() {
    openCard(
      "<h2>Pausa</h2>" +
      '<button class="btn" data-action="resume">Continuar</button>' +
      '<button class="btn" data-action="restart">Reiniciar</button>' +
      '<button class="btn btn-ghost" data-action="exit-level">Salir</button>'
    );
  }

  function openCard(html) {
    var overlay = document.getElementById("overlay");
    overlay.innerHTML = '<div class="card">' + html + "</div>";
    overlay.classList.remove("hidden");
  }

  function hideOverlay() {
    var overlay = document.getElementById("overlay");
    overlay.classList.add("hidden");
    overlay.innerHTML = "";
  }

  function leaveLevel() {
    playClick();
    hideOverlay();
    if (play.tutorial) show("menu");
    else openLevels(L.worldForLevel(play.levelNumber));
  }

  var worldViewport = document.getElementById("world-viewport");
  worldViewport.addEventListener("pointerdown", function (event) {
    if (event.pointerType === "mouse" && event.button !== 0) return;
    worldSwipe.active = true;
    worldSwipe.moved = false;
    worldSwipe.dx = 0;
    worldSwipe.vx = 0;
    worldSwipe.startX = event.clientX;
    worldSwipe.startY = event.clientY;
    worldSwipe.lastX = event.clientX;
    worldSwipe.lastT = performance.now();
    worldSwipe.pointerId = event.pointerId;
    document.getElementById("world-track").style.transition = "none";
  });

  worldViewport.addEventListener("pointermove", function (event) {
    if (!worldSwipe.active) return;
    var scale = phoneScale();
    var dx = (event.clientX - worldSwipe.startX) / scale;
    var dy = (event.clientY - worldSwipe.startY) / scale;
    if (!worldSwipe.moved) {
      if (Math.abs(dx) < 12) return;
      if (Math.abs(dy) > Math.abs(dx)) return;
      worldSwipe.moved = true;
      try { worldViewport.setPointerCapture(worldSwipe.pointerId); } catch (error) { /* el deslizamiento sigue con los eventos del viewport */ }
    }
    var now = performance.now();
    var dt = now - worldSwipe.lastT;
    if (dt > 0) worldSwipe.vx = ((event.clientX - worldSwipe.lastX) / scale) / dt;
    worldSwipe.lastX = event.clientX;
    worldSwipe.lastT = now;
    var offset = dx;
    if ((worlds.index === 0 && offset > 0) || (worlds.index === WORLDS.length - 1 && offset < 0)) offset *= 0.3;
    worldSwipe.dx = offset;
    document.getElementById("world-track").style.transform = "translateX(" + ((-worlds.index * 480) + offset) + "px)";
    var shown = worlds.index + (offset < -160 ? 1 : offset > 160 ? -1 : 0);
    shown = Math.max(0, Math.min(WORLDS.length - 1, shown));
    updateWorldChrome(shown);
  });

  function endWorldSwipe() {
    if (!worldSwipe.active) return;
    worldSwipe.active = false;
    if (!worldSwipe.moved) return;
    var direction = 0;
    if (worldSwipe.dx < -42 || worldSwipe.vx < -0.4) direction = 1;
    else if (worldSwipe.dx > 42 || worldSwipe.vx > 0.4) direction = -1;
    worlds.index = Math.max(0, Math.min(WORLDS.length - 1, worlds.index + direction));
    placeWorldTrack(worlds.index, true);
    updateWorldChrome(worlds.index);
    setTimeout(function () { worldSwipe.moved = false; }, 0);
  }

  worldViewport.addEventListener("pointerup", endWorldSwipe);
  worldViewport.addEventListener("pointercancel", function () {
    worldSwipe.active = false;
    worldSwipe.moved = false;
    placeWorldTrack(worlds.index, true);
  });
  worldViewport.addEventListener("click", function (event) {
    if (!worldSwipe.moved) return;
    event.preventDefault();
    event.stopPropagation();
    worldSwipe.moved = false;
  }, true);

  document.getElementById("touch").addEventListener("pointerdown", function (event) {
    play.pointer = { x: event.clientX, y: event.clientY };
    event.currentTarget.setPointerCapture(event.pointerId);
  });

  document.getElementById("touch").addEventListener("pointerup", function (event) {
    if (!play.pointer) return;
    var dx = event.clientX - play.pointer.x;
    var dy = event.clientY - play.pointer.y;
    play.pointer = null;
    if (Math.hypot(dx, dy) > 28) {
      tryMove(Math.abs(dx) > Math.abs(dy) ? (dx > 0 ? "derecha" : "izquierda") : (dy > 0 ? "abajo" : "arriba"));
      return;
    }
    var point = phonePoint(event);
    tryMove(L.zoneAt(point.x, point.y));
  });

  window.addEventListener("keydown", function (event) {
    var map = {
      ArrowUp: "arriba", ArrowDown: "abajo", ArrowLeft: "izquierda", ArrowRight: "derecha",
      w: "arriba", s: "abajo", a: "izquierda", d: "derecha",
      W: "arriba", S: "abajo", A: "izquierda", D: "derecha"
    };
    if ((event.key === "ArrowLeft" || event.key === "ArrowRight") && document.getElementById("screen-levels").classList.contains("active")) {
      event.preventDefault();
      var step = event.key === "ArrowRight" ? 1 : -1;
      worlds.index = Math.max(0, Math.min(WORLDS.length - 1, worlds.index + step));
      placeWorldTrack(worlds.index, true);
      updateWorldChrome(worlds.index);
      return;
    }
    if (map[event.key]) {
      event.preventDefault();
      tryMove(map[event.key]);
    } else if (event.key === "Escape" && document.getElementById("screen-play").classList.contains("active")) {
      if (document.getElementById("overlay").classList.contains("hidden")) showPause();
      else if (!play.won) hideOverlay();
    } else if ((event.key === "r" || event.key === "R") && document.getElementById("screen-play").classList.contains("active") && !play.busy) {
      restartLevel();
    }
  });

  function restartLevel() {
    if (!play.model) return;
    play.moves = 0;
    play.won = false;
    play.busy = false;
    L.resetLevel(play.model);
    hideOverlay();
    drawBoard();
    updateHud();
  }

  phone.addEventListener("click", function (event) {
    var target = event.target.closest("[data-action]");
    if (!target) return;
    var action = target.dataset.action;
    if (action === "play") {
      playClick();
      syncMusic();
      renderWorlds();
      show("worlds");
    } else if (action === "help") {
      playClick();
      show("help");
    } else if (action === "options") {
      playClick();
      renderOptions();
      show("options");
    } else if (action === "menu") {
      playClick();
      show("menu");
    } else if (action === "tutorial") {
      playClick();
      startLevel(0, true);
    } else if (action === "toggle-sound") {
      save.sound = !save.sound;
      persist();
      playClick();
      renderOptions();
    } else if (action === "toggle-music") {
      save.music = !save.music;
      persist();
      playClick();
      syncMusic();
      renderOptions();
    } else if (action === "toggle-help") {
      save.help = !save.help;
      persist();
      playClick();
      renderOptions();
    } else if (action === "reset") {
      playClick();
      save.scores = {};
      persist();
      target.textContent = "Progreso borrado";
      setTimeout(renderOptions, 900);
    } else if (action === "pause") {
      playClick();
      if (!play.won) showPause();
    } else if (action === "resume") {
      playClick();
      hideOverlay();
    } else if (action === "restart") {
      playClick();
      restartLevel();
    } else if (action === "exit-level" || action === "leave-win") {
      leaveLevel();
    } else if (action === "next") {
      playClick();
      startLevel(play.levelNumber + 1, false);
    }
  });

  document.getElementById("logo-ball").addEventListener("click", function (event) {
    event.stopPropagation();
    cycleLogoBall();
  });

  window.addEventListener("resize", fit);
  fit();
  paintLogoBall();
  show("loading");
  boot();
})();
