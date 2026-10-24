/* Browser review adapter. Native cache/lifecycle lives in OnlineWallpapers.java. */
(() => {
  const source = document.querySelector('[data-action="online-source"]');
  const refresh = document.querySelector('[data-action="online-refresh"]');
  const status = document.querySelector('#online-status');
  let enabled = false, controller, rows = [];
  function valid(image) {
    const original = image.resolutions?.original;
    if (image.type !== 'background' || !original) return false;
    const {width, height, url} = original, ratio = width / height;
    try {
      const u = new URL(url);
      return u.protocol === 'https:' && u.hostname === 'static.tvmaze.com' && !u.username && !u.password
        && (!u.port || u.port === '443') && !u.search && !u.hash
        && /^\/uploads\/images\/[a-zA-Z0-9_/-]+\.(jpg|jpeg|png|webp)$/.test(u.pathname)
        && width >= 960 && height >= 540 && width <= 8192 && height <= 8192
        && width * height <= 20000000 && ratio >= 1.4 && ratio <= 2;
    } catch { return false; }
  }
  async function load() {
    if (!enabled || controller) return;
    const request = new AbortController(); controller = request; refresh.disabled = true;
    status.textContent = 'Checking TVMaze…';
    const timeout = setTimeout(() => request.abort(), 20000);
    try {
      const groups = [];
      for (const show of window.TVMAZE_SHOWS) {
        const response = await fetch(`https://api.tvmaze.com/shows/${show.id}/images`, {signal:request.signal});
        if (!response.ok) throw new Error('TVMaze unavailable');
        const text = await response.text(); if (text.length > 1048576) throw new Error('Response too large');
        const group = JSON.parse(text).filter(valid).map(image => ({title:show.title,
          file:image.resolutions.original.url, sourcePage:show.sourcePage, provider:'tvmaze'}));
        // Shuffle the public gallery before selecting the small rotation.
        for (let i=group.length-1;i>0;i--) {const j=Math.floor(Math.random()*(i+1));[group[i],group[j]]=[group[j],group[i]];}
        groups.push(group);
      }
      if (!enabled || request.signal.aborted) return;
      const next = [];
      for (let round=0;round<8 && next.length<8;round++)
        for (const group of groups) if (group[round] && next.length<8) next.push(group[round]);
      if (!next.length) throw new Error('No landscape images');
      rows = next; window.setWallpaperCatalogue(rows);
      status.textContent = `TVMaze · ${rows.length} online backgrounds. APK refreshes daily and keeps an offline cache.`;
    } catch {
      if (enabled) status.textContent = 'TVMaze unavailable; previous backgrounds kept. Try Refresh.';
    } finally {
      clearTimeout(timeout); if (controller === request) controller = null;
      refresh.disabled = !enabled || !!controller;
    }
  }
  window.toggleOnlineWallpapers = () => {
    enabled = !enabled;
    source.textContent = enabled ? 'Use bundled movie wallpapers' : 'Use online TVMaze wallpapers';
    refresh.disabled = !enabled;
    if (enabled) load();
    else { if (controller) controller.abort(); controller = null;
      window.setWallpaperCatalogue(window.WALLPAPERS); status.textContent = 'Bundled movie wallpapers'; }
  };
  window.refreshOnlineWallpapers = load;
})();
