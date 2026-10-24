/* Run with PLAYWRIGHT_MODULE pointing to an installed playwright module. */
const { chromium } = require(process.env.PLAYWRIGHT_MODULE || 'playwright');
const assert = require('node:assert/strict');
const path = require('node:path');
const fs = require('node:fs');
const { pathToFileURL } = require('node:url');

(async () => {
  const browser = await chromium.launch({headless:true,
    ...(process.env.CHROMIUM_PATH ? {executablePath:process.env.CHROMIUM_PATH} : {})});
  try {
    const page = await browser.newPage({viewport:{width:1280,height:800}, reducedMotion:'reduce'});
    const errors=[]; page.on('pageerror',e=>errors.push(e.message));
    const passed=[];
    const mark=description=>passed.push(description);
    await page.goto(pathToFileURL(path.join(__dirname,'index.html')).href);
    await page.waitForFunction(()=>document.activeElement.dataset.launch==='YouTube');
    await page.waitForFunction(()=>document.querySelector('.movie-background').dataset.scene==='0');
    await page.waitForFunction(()=>[...document.querySelectorAll('#favorites .real-icon img')].length===6&&[...document.querySelectorAll('#favorites .real-icon img')].every(i=>i.complete&&i.naturalWidth>0));
    assert.equal(await page.locator('#favorites .tile').count(),6);
    assert.match(await page.getByRole('button',{name:'Wi-Fi connected. Open Wi-Fi settings',exact:true}).innerText(),/connected/);
    await page.getByRole('button',{name:'Preview disconnected Wi-Fi',exact:true}).click();
    assert.equal(await page.locator('.wifi-connection').getAttribute('data-connected'),'false');
    assert.equal(await page.locator('.wifi-connection .slash').evaluate(e=>getComputedStyle(e).display),'block');
    await page.getByRole('button',{name:'Wi-Fi disconnected. Open Wi-Fi settings',exact:true}).click();
    assert.match(await page.getByRole('status').innerText(),/Wi-Fi settings/);
    await page.getByRole('button',{name:'Preview connected Wi-Fi',exact:true}).click();
    await page.waitForTimeout(2700);
    await page.locator('#favorites .tile').first().focus();
    mark('Connected/disconnected Wi-Fi label, slash icon and settings action');
    assert.equal(await page.locator('#music-bar').isVisible(),false);
    assert.equal(await page.locator('.tv').innerText().then(t=>/Make yourself|YOUR SPACE|D-pad to move|WALLPAPER/.test(t)),false);
    await page.locator('.frame').screenshot({path:path.join(__dirname,'evening-home.png')});
    await page.getByRole('button',{name:'Preview music controls',exact:true}).click();
    assert.equal(await page.locator('#music-bar').isVisible(),true);
    const before=await page.locator('#favorites').evaluate(e=>e.offsetTop);
    await page.getByRole('button',{name:'Pause music',exact:true}).click();
    assert.equal(await page.getByRole('button',{name:'Play music',exact:true}).count(),1);
    assert.equal(await page.locator('#music-bar').isVisible(),true);
    await page.getByRole('button',{name:'Next track',exact:true}).click();
    assert.match(await page.getByRole('status').innerText(),/next track requested/);
    await page.waitForTimeout(2700);
    await page.locator('.frame').screenshot({path:path.join(__dirname,'evening-music.png')});
    await page.getByRole('button',{name:'Hide music controls',exact:true}).click();
    assert.equal(await page.locator('#music-bar').isVisible(),false);
    assert.equal(await page.evaluate(()=>document.activeElement.dataset.launch),'YouTube');
    const after=await page.locator('#favorites').evaluate(e=>e.offsetTop);
    assert.equal(before,after);
    mark('Clean Home; simulated music visibility, pause/resume label, Next, hide/focus and stable app row');

    mark('Home: six real installed-app icons, initial focus and 1280×720 screenshot');
    await page.keyboard.press('ArrowRight');
    assert.notEqual(await page.evaluate(()=>document.activeElement.dataset.launch),'YouTube');
    mark('Arrow navigation changes focus');
    await page.locator('#favorites').evaluate(e=>e.style.width='600px');
    await page.locator('#favorites').hover();
    await page.mouse.wheel(0,320);
    await page.waitForFunction(()=>document.querySelector('#favorites').scrollLeft>0);
    await page.locator('#favorites').evaluate(e=>{e.style.width='';e.scrollLeft=0;});
    mark('Pointer/mouse-wheel scrolling works on an overflowing favorite rail');
    await page.getByRole('button',{name:'Apps',exact:true}).click();
    assert.equal(await page.locator('#grid .tile').count(),6);
    assert.equal(await page.locator('#apps-tab').getAttribute('aria-current'),'page');
    await page.locator('.frame').screenshot({path:path.join(__dirname,'evening-apps.png')});
    await page.keyboard.press('Escape');
    assert.equal(await page.evaluate(()=>document.activeElement.dataset.launch),'YouTube');
    mark('Apps navigation, six installed-app icons, Back/Escape restoration');
    await page.getByRole('button',{name:'Edit favorites',exact:true}).click();
    for(const item of await page.locator('#pin-choices input').all()) await item.uncheck();
    await page.getByRole('button',{name:'Save favorites',exact:true}).click();
    assert.equal(await page.locator('#favorites .tile').count(),0);
    assert.match(await page.locator('#favorites').innerText(),/Choose Edit/);
    mark('Saving zero favorites shows a usable empty state');
    await page.getByRole('button',{name:'Edit favorites',exact:true}).click();
    await page.getByLabel('Brave',{exact:true}).check();
    await page.getByRole('button',{name:'Cancel',exact:true}).click();
    assert.equal(await page.locator('#favorites .tile').count(),0);
    assert.equal(await page.evaluate(()=>document.activeElement.textContent),'Edit');
    mark('Cancel discards favorite draft and restores trigger focus');
    await page.getByRole('button',{name:'Edit favorites',exact:true}).click();
    await page.getByLabel('Brave',{exact:true}).check();
    await page.getByLabel('YouTube',{exact:true}).check();
    await page.getByRole('button',{name:'Save favorites',exact:true}).click();
    assert.equal(await page.locator('#favorites .tile').count(),2);
    await page.getByRole('button',{name:'Open Brave',exact:true}).click();
    assert.match(await page.getByRole('status').innerText(),/Preview: Brave/);
    mark('Favorite selection and honest app-preview feedback');
    await page.getByRole('button',{name:'Settings',exact:true}).click();
    assert.equal(await page.locator('#settings').evaluate(e=>e.open),true);
    await page.keyboard.press('Escape');
    assert.equal(await page.evaluate(()=>document.activeElement.textContent),'Settings');
    mark('Settings dialog opens and Escape returns focus');
    await page.getByRole('button',{name:'Recents',exact:true}).click();
    assert.equal(await page.locator('#recents').evaluate(e=>e.open),true);
    assert.match(await page.locator('#recents').innerText(),/cannot show or close Android tasks/);
    await page.keyboard.press('Escape');
    assert.equal(await page.evaluate(()=>document.activeElement.textContent),'Recents');
    await page.getByRole('button',{name:'Recents',exact:true}).click();
    await page.getByRole('button',{name:'Done',exact:true}).click();
    assert.equal(await page.evaluate(()=>document.activeElement.textContent),'Recents');
    const headerFits=await page.locator('header').evaluate(h=>[...h.children].every(c=>c.getBoundingClientRect().right<=h.getBoundingClientRect().right+1));
    assert.equal(headerFits,true,'Header controls must fit the TV safe area');
    mark('Recents preview explains native handoff, closes by Escape/Done, restores focus and fits the header');
    // Real bundled images, manual next, automatic advance and reduced-motion behavior.
    await page.reload();
    await page.waitForFunction(()=>document.querySelector('.movie-background').dataset.scene==='0');
    await page.getByRole('button',{name:'Next scene',exact:true}).click();
    await page.waitForFunction(()=>document.querySelector('.movie-background').dataset.scene==='1');
    assert.match(await page.locator('.wallpaper-label').textContent(),/Avengers/);
    assert.equal(await page.locator('.movie-background img').count(),2);
    mark('Next scene changes real artwork, preserves two image slots and updates the title');
    await page.emulateMedia({reducedMotion:'no-preference'});
    await page.waitForFunction(()=>document.querySelector('.movie-background').dataset.scene==='2',{},{timeout:18000});
    await page.waitForTimeout(1000);
    await page.locator('.frame').screenshot({path:path.join(__dirname,'evening-spiderverse.png')});
    mark('Slideshow advances after 15 seconds with a fade');
    await page.getByRole('button',{name:'Next scene',exact:true}).click();
    await page.waitForFunction(()=>document.querySelector('.movie-background').dataset.scene==='3');
    await page.waitForTimeout(1000);
    await page.locator('.frame').screenshot({path:path.join(__dirname,'evening-cartoons.png')});
    await page.getByRole('button',{name:'Settings',exact:true}).click();
    await page.getByRole('button',{name:'Pause wallpaper slideshow',exact:true}).click();
    assert.equal(await page.locator('.movie-background').evaluate(e=>e.classList.contains('paused')),true);
    await page.getByRole('button',{name:'Done',exact:true}).click();
    await page.waitForTimeout(16100);
    assert.equal(await page.locator('.movie-background').getAttribute('data-scene'),'3');
    mark('Pause prevents automatic wallpaper advancement');
    // Deterministic online-provider tests: a real Arcane image/metadata fixture,
    // other galleries empty, and explicit server failures. No live-service claim.
    const onlinePage=await browser.newPage({viewport:{width:1280,height:800},reducedMotion:'reduce'});
    const fixture=JSON.parse(fs.readFileSync(path.join(__dirname,'fixtures/tvmaze-image.json'),'utf8'));
    let failOnline=false, rejectedRequests=0;
    onlinePage.on('request',request=>{if(request.url().startsWith('https://127.0.0.1'))rejectedRequests++;});
    await onlinePage.route('https://api.tvmaze.com/**',async route=>{
      if(failOnline)return route.fulfill({status:503,body:'unavailable'});
      const valid=route.request().url().includes('/55138/') ? [fixture] : [];
      const malicious={...fixture,resolutions:{original:{...fixture.resolutions.original,url:'https://127.0.0.1/private.jpg'}}};
      await route.fulfill({contentType:'application/json',body:JSON.stringify([...valid,{...fixture,type:'poster'},malicious])});
    });
    await onlinePage.route(fixture.resolutions.original.url,route=>route.fulfill({path:path.join(__dirname,'fixtures/tvmaze-arcane.jpg'),contentType:'image/jpeg'}));
    await onlinePage.goto(pathToFileURL(path.join(__dirname,'index.html')).href);
    await onlinePage.waitForFunction(()=>document.querySelector('.movie-background').dataset.provider==='bundled');
    await onlinePage.getByRole('button',{name:'Settings',exact:true}).click();
    await onlinePage.getByRole('button',{name:'Use online TVMaze wallpapers',exact:true}).click();
    await onlinePage.waitForFunction(()=>document.querySelector('.movie-background').dataset.provider==='tvmaze');
    assert.match(await onlinePage.locator('#online-status').innerText(),/1 online backgrounds/);
    assert.equal(await onlinePage.locator('.movie-background img').count(),2);
    assert.equal(rejectedRequests,0);
    failOnline=true;
    await onlinePage.getByRole('button',{name:'Refresh online backgrounds',exact:true}).click();
    await onlinePage.waitForFunction(()=>document.querySelector('#online-status').textContent.includes('unavailable'));
    assert.equal(await onlinePage.locator('.movie-background').getAttribute('data-provider'),'tvmaze');
    await onlinePage.getByRole('button',{name:'Use bundled movie wallpapers',exact:true}).click();
    await onlinePage.waitForFunction(()=>document.querySelector('.movie-background').dataset.provider==='bundled');
    assert.equal(await onlinePage.getByRole('button',{name:'Refresh online backgrounds',exact:true}).isDisabled(),true);
    await onlinePage.close();
    mark('TVMaze adapter fixtures: online artwork, poster/untrusted-URL rejection, two image slots, failure retention and bundled fallback');
    await page.emulateMedia({reducedMotion:'reduce'});
    // Check every visible TV control is reachable through the arrow-key graph.
    await page.reload(); await page.waitForFunction(()=>document.activeElement.dataset.launch==='YouTube');
    await page.getByRole('button',{name:'Preview music controls',exact:true}).click();
    await page.locator('#favorites .tile').first().focus();
    const reachable=await page.evaluate(()=>{
      const visible=[...document.querySelectorAll('.tv button')].filter(b=>b.offsetWidth&&b.offsetHeight);
      const seen=new Set([document.activeElement]), queue=[document.activeElement];
      while(queue.length){const from=queue.shift(); for(const key of ['ArrowLeft','ArrowRight','ArrowUp','ArrowDown']){
        from.focus(); document.dispatchEvent(new KeyboardEvent('keydown',{key,bubbles:true,cancelable:true}));
        const to=document.activeElement;if(!seen.has(to)){seen.add(to);queue.push(to);}
      }}
      return {total:visible.length, reachable:visible.filter(v=>seen.has(v)).length,
        missing:visible.filter(v=>!seen.has(v)).map(v=>v.textContent)};
    });
    assert.equal(reachable.reachable,reachable.total, JSON.stringify(reachable));
    mark(`Home arrow-key graph reaches all ${reachable.total} visible controls`);
    for(const width of [1920,960,640]){
      await page.setViewportSize({width,height:900});
      await page.waitForFunction(()=>Math.abs(document.querySelector('.tv').getBoundingClientRect().width-document.querySelector('.frame').clientWidth)<1);
      const geometry=await page.locator('.tv').boundingBox();
      const availableWidth=await page.locator('.frame').evaluate(e=>e.clientWidth);
      assert.ok(Math.abs(geometry.width-availableWidth)<1);
      assert.ok(Math.abs(geometry.height-availableWidth*9/16)<1);
    }
    mark('16:9 preview frame fits widths 1920, 960 and 640 without clipping');
    assert.deepEqual(errors,[]); mark('No browser JavaScript errors');
    fs.writeFileSync(path.join(__dirname,'verification.json'),JSON.stringify({
      scope:'Desktop illustration only; not native Android runtime verification',passed,
      browser:browser.version(), timestamp:new Date().toISOString()
    },null,2)+'\n');
    console.log(passed.join('\n'));
  } finally {await browser.close();}
})().catch(e=>{console.error(e);process.exitCode=1;});
