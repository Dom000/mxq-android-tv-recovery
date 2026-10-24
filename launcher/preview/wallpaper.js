/* Two image slots shared by bundled artwork and the optional online catalogue. */
(() => {
  let scenes = window.WALLPAPERS || [];
  const host = document.querySelector('.movie-background');
  const label = document.querySelector('.wallpaper-label');
  const toggle = document.querySelector('[data-action="motion"]');
  const reduced = matchMedia('(prefers-reduced-motion: reduce)');
  let index = 0, paused = false, timer, busy = false;
  const images = [new Image(), new Image()];
  images.forEach(image => { image.alt = ''; image.setAttribute('aria-hidden','true'); host.append(image); });
  let front = 0;
  function schedule() {
    clearTimeout(timer);
    if (!paused && !reduced.matches && !document.hidden && scenes.length > 1)
      timer = setTimeout(() => show((index + 1) % scenes.length), 15000);
  }
  function show(next, first = false) {
    if (!scenes.length || busy) return;
    busy = true; clearTimeout(timer);
    const back = first ? front : 1 - front, image = images[back];
    const previous = images[front];
    image.onload = () => {
      image.onload = image.onerror = null;
      if (!first) previous.classList.remove('visible');
      image.classList.add('visible'); front = back; index = next;
      label.textContent = 'WALLPAPER · ' + scenes[index].title;
      host.dataset.scene = String(index);
      host.dataset.provider = scenes[index].provider || 'bundled';
      setTimeout(() => { busy = false; schedule(); }, reduced.matches || first || paused ? 0 : 950);
    };
    image.onerror = () => { image.onload = image.onerror = null; busy = false; schedule(); };
    image.src = scenes[next].file;
  }
  window.nextWallpaper = () => show((index + 1) % scenes.length);
  let catalogueVersion = 0;
  window.setWallpaperCatalogue = rows => {
    if (!rows.length) return;
    const version = ++catalogueVersion;
    function apply() {
      if (version !== catalogueVersion) return;
      if (busy) { setTimeout(apply, 1000); return; }
      scenes = rows; show(0);
    }
    apply();
  };
  window.toggleWallpaperMotion = () => {
    paused = !paused; host.classList.toggle('paused', paused);
    toggle.textContent = paused ? 'Resume wallpaper slideshow' : 'Pause wallpaper slideshow';
    schedule();
  };
  document.addEventListener('visibilitychange',schedule);
  reduced.addEventListener('change',schedule);
  show(0, true);
})();
