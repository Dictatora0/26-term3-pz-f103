const statusEl = document.getElementById('device-status');
const lastUpdateEl = document.getElementById('last-update');
const tempEl = document.getElementById('temperature');
const lightEl = document.getElementById('light');
const humidityEl = document.getElementById('humidity');
const historyBodyEl = document.getElementById('history-body');
const nowDateEl = document.getElementById('now-date');
const nowTimeEl = document.getElementById('now-time');
const trendCanvas = document.getElementById('trend-canvas');
const trendCtx = trendCanvas.getContext('2d');
let latestHistory = [];

function formatNow() {
  const now = new Date();
  const weekMap = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六'];
  const y = now.getFullYear();
  const m = String(now.getMonth() + 1).padStart(2, '0');
  const d = String(now.getDate()).padStart(2, '0');
  const hh = String(now.getHours()).padStart(2, '0');
  const mm = String(now.getMinutes()).padStart(2, '0');
  const ss = String(now.getSeconds()).padStart(2, '0');
  nowDateEl.textContent = `${y}-${m}-${d} ${weekMap[now.getDay()]}`;
  nowTimeEl.textContent = `${hh}:${mm}:${ss}`;
}

function setStatus(connected, hasData) {
  if (!connected) {
    statusEl.textContent = '未连接';
    statusEl.className = 'status error';
    return;
  }

  if (!hasData) {
    statusEl.textContent = '等待数据';
    statusEl.className = 'status waiting';
    return;
  }

  statusEl.textContent = '已连接';
  statusEl.className = 'status ok';
}

function showLatest(data) {
  const hasData = data && data.message === 'ok';
  setStatus(Boolean(data && data.connected), hasData);

  if (!hasData) {
    tempEl.textContent = '--';
    lightEl.textContent = '--';
    humidityEl.textContent = '暂无数据';
    lastUpdateEl.textContent = '--';
    return;
  }

  tempEl.textContent = data.temperature == null ? '--' : Number(data.temperature).toFixed(2);
  lightEl.textContent = data.light == null ? '--' : String(data.light);
  humidityEl.textContent = data.humidity == null ? '暂未接入' : Number(data.humidity).toFixed(2);
  lastUpdateEl.textContent = data.timestamp || '--';
}

function renderHistory(items) {
  if (!items || items.length === 0) {
    historyBodyEl.innerHTML = '<tr><td colspan="4" class="placeholder">等待串口数据...</td></tr>';
    latestHistory = [];
    renderTrend([]);
    return;
  }

  const recent = items.slice(-100).reverse();
  historyBodyEl.innerHTML = recent
    .map((it) => {
      const t = it.temperature == null ? '--' : Number(it.temperature).toFixed(2);
      const l = it.light == null ? '--' : String(it.light);
      const h = it.humidity == null ? '暂未接入' : Number(it.humidity).toFixed(2);
      return `<tr><td>${it.timestamp || '--'}</td><td>${t}</td><td>${l}</td><td>${h}</td></tr>`;
    })
    .join('');

  latestHistory = items.slice(-40);
  renderTrend(latestHistory);
}

function scaleCanvasForDpr() {
  const dpr = window.devicePixelRatio || 1;
  const rect = trendCanvas.getBoundingClientRect();
  const w = Math.max(1, Math.floor(rect.width * dpr));
  const h = Math.max(1, Math.floor(rect.height * dpr));
  if (trendCanvas.width !== w || trendCanvas.height !== h) {
    trendCanvas.width = w;
    trendCanvas.height = h;
  }
  trendCtx.setTransform(dpr, 0, 0, dpr, 0, 0);
}

function renderTrend(items) {
  scaleCanvasForDpr();
  const w = trendCanvas.clientWidth;
  const h = trendCanvas.clientHeight;
  trendCtx.clearRect(0, 0, w, h);

  const pad = { left: 40, right: 16, top: 16, bottom: 28 };
  const plotW = w - pad.left - pad.right;
  const plotH = h - pad.top - pad.bottom;
  if (plotW <= 0 || plotH <= 0) return;

  trendCtx.strokeStyle = '#d5e0ea';
  trendCtx.lineWidth = 1;
  trendCtx.strokeRect(pad.left, pad.top, plotW, plotH);

  if (!items || items.length < 2) {
    trendCtx.fillStyle = '#6f7f90';
    trendCtx.font = '13px Microsoft YaHei';
    trendCtx.fillText('等待足够数据绘制趋势...', pad.left + 12, pad.top + 20);
    return;
  }

  const temps = items.map((x) => (x.temperature == null ? NaN : Number(x.temperature))).filter((x) => !Number.isNaN(x));
  const lights = items.map((x) => (x.light == null ? NaN : Number(x.light))).filter((x) => !Number.isNaN(x));

  let tMin = temps.length ? Math.min(...temps) : 0;
  let tMax = temps.length ? Math.max(...temps) : 1;
  let lMin = lights.length ? Math.min(...lights) : 0;
  let lMax = lights.length ? Math.max(...lights) : 1;

  if (tMin === tMax) { tMin -= 1; tMax += 1; }
  if (lMin === lMax) { lMin -= 1; lMax += 1; }

  const n = items.length;
  const xAt = (i) => pad.left + (i * plotW) / (n - 1);
  const yTemp = (v) => pad.top + ((tMax - v) * plotH) / (tMax - tMin);
  const yLight = (v) => pad.top + ((lMax - v) * plotH) / (lMax - lMin);

  trendCtx.strokeStyle = '#e05252';
  trendCtx.lineWidth = 2;
  trendCtx.beginPath();
  let started = false;
  items.forEach((it, i) => {
    if (it.temperature == null) return;
    const x = xAt(i);
    const y = yTemp(Number(it.temperature));
    if (!started) {
      trendCtx.moveTo(x, y);
      started = true;
    } else {
      trendCtx.lineTo(x, y);
    }
  });
  trendCtx.stroke();

  trendCtx.strokeStyle = '#0b6aa2';
  trendCtx.lineWidth = 2;
  trendCtx.beginPath();
  started = false;
  items.forEach((it, i) => {
    if (it.light == null) return;
    const x = xAt(i);
    const y = yLight(Number(it.light));
    if (!started) {
      trendCtx.moveTo(x, y);
      started = true;
    } else {
      trendCtx.lineTo(x, y);
    }
  });
  trendCtx.stroke();

  trendCtx.fillStyle = '#6f7f90';
  trendCtx.font = '12px Microsoft YaHei';
  trendCtx.fillText(`温度: ${tMin.toFixed(1)} ~ ${tMax.toFixed(1)} °C`, pad.left + 4, h - 8);
  trendCtx.fillText(`光照: ${lMin.toFixed(0)} ~ ${lMax.toFixed(0)} %`, w - 150, h - 8);
}

async function refreshLatest() {
  try {
    const resp = await fetch('/api/latest', { cache: 'no-store' });
    const data = await resp.json();
    showLatest(data);
  } catch (err) {
    setStatus(false, false);
  }
}

async function refreshHistory() {
  try {
    const resp = await fetch('/api/history', { cache: 'no-store' });
    const data = await resp.json();
    renderHistory(data.items || []);
  } catch (err) {
    renderHistory([]);
  }
}

async function tick() {
  await Promise.all([refreshLatest(), refreshHistory()]);
}

formatNow();
setInterval(formatNow, 1000);
tick();
setInterval(tick, 1000);

window.addEventListener('resize', () => renderTrend(latestHistory));
