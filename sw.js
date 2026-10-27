const CACHE='muhasib-v1.3';
const FILES=['./','./index.html','./manifest.webmanifest','./icon-192.png','./icon-512.png','./icon-maskable.png'];
self.addEventListener('install',e=>{e.waitUntil(caches.open(CACHE).then(c=>c.addAll(FILES)).then(()=>self.skipWaiting()))});
self.addEventListener('activate',e=>{e.waitUntil(caches.keys().then(ks=>Promise.all(ks.filter(k=>k!==CACHE).map(k=>caches.delete(k)))).then(()=>self.clients.claim()))});
self.addEventListener('fetch',e=>{
  const req=e.request;if(req.method!=='GET'||new URL(req.url).origin!==location.origin)return;
  if(req.mode==='navigate'){ // page: try the internet first (gets updates), fall back to the saved copy offline
    e.respondWith(fetch(req).then(res=>{if(res.ok){const cp=res.clone();caches.open(CACHE).then(c=>c.put('./index.html',cp))}return res})
      .catch(()=>caches.match('./index.html')));return;}
  e.respondWith(caches.match(req,{ignoreSearch:true}).then(r=>r||fetch(req).then(res=>{if(res.ok){const cp=res.clone();caches.open(CACHE).then(c=>c.put(req,cp))}return res})));
});
