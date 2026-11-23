const APP_ROOT = document.currentScript.src.replace(/\/static\/script\.js(\?.*)?$/, '');
const API_URL = `${APP_ROOT}/points`;
const X_ALLOWED = [-2, -1.5, -1, -0.5, 0, 0.5, 1, 1.5, 2];

const $ = id => document.getElementById(id);
const yInput = $('y-input');
const err = $('error'), body = $('results-body');
const ctx = $('area').getContext('2d');
const CX = 150, CY = 150, U = 90;
let R = null;
let points = typeof POINTS !== 'undefined' ? POINTS : [];

function onRChange(changed) {
    if (changed.checked) {
        document.querySelectorAll('#r-checkboxes input[type="checkbox"]').forEach(cb => {
            if (cb !== changed) cb.checked = false;
        });
        R = Number(changed.value);
    } else {
        R = null;
    }
    draw();
    redraw();
}

function draw() {
    ctx.clearRect(0, 0, 300, 300);
    ctx.fillStyle = '#4a90e2';

    ctx.beginPath();
    ctx.moveTo(CX, CY);
    ctx.arc(CX, CY, U, -Math.PI / 2, -Math.PI, true);
    ctx.fill();

    ctx.fillRect(CX - U / 2, CY, U / 2, U);

    ctx.beginPath();
    ctx.moveTo(CX, CY);
    ctx.lineTo(CX + U, CY);
    ctx.lineTo(CX, CY + U / 2);
    ctx.fill();

    ctx.strokeStyle = '#000';
    ctx.beginPath();
    ctx.moveTo(0, CY); ctx.lineTo(300, CY);
    ctx.moveTo(CX, 0); ctx.lineTo(CX, 300);
    ctx.stroke();

    ctx.fillStyle = '#000';
    ctx.font = '11px sans-serif';
    [['R', 1], ['R/2', 0.5], ['-R/2', -0.5], ['-R', -1]].forEach(([t, p]) => {
        ctx.fillText(t, CX + p * U - 8, CY + 14);
        ctx.fillText(t, CX + 4, CY - p * U + 4);
    });
    ctx.font = 'bold 12px sans-serif';
    ctx.fillText('x', 290, CY - 4);
    ctx.fillText('y', CX + 6, 12);
}

function drawPoint(x, y, r, hit) {
    ctx.fillStyle = hit ? 'green' : 'red';
    ctx.beginPath();
    ctx.arc(CX + x / r * U, CY - y / r * U, 4, 0, 2 * Math.PI);
    ctx.fill();
}

function parseDecimal(text) {
    const trimmed = text.trim();
    const dotIndex = trimmed.indexOf(".");

    if (dotIndex === -1) {
        return Number(trimmed);
    }

    const truncated = trimmed.slice(0, dotIndex + 11);
    return Number(truncated);
}

function nearestAllowedX(value) {
    return X_ALLOWED.reduce((best, cur) => Math.abs(cur - value) < Math.abs(best - value) ? cur : best);
}

function redraw() {
    if (R !== null) points.filter(it => it.r === R).forEach(it => drawPoint(it.x, it.y, it.r, it.hit));
}

const fmt = new Intl.DateTimeFormat('ru-RU', {
    year: 'numeric', month: '2-digit', day: '2-digit',
    hour: '2-digit', minute: '2-digit', second: '2-digit',
    timeZoneName: 'short'
});

function render() {
    body.replaceChildren();
    points.slice().reverse().forEach(it => {
        const tr = document.createElement('tr');
        tr.className = it.hit ? 'hit' : 'miss';
        [it.x, it.y, it.r, it.hit ? 'Попадание' : 'Промах', fmt.format(new Date(it.requestTime))].forEach(v => {
            const td = document.createElement('td');
            td.textContent = v;
            tr.appendChild(td);
        });
        body.appendChild(tr);
    });
}

function readResultFromHtml(html) {
    const doc = new DOMParser().parseFromString(html, 'text/html');
    const marker = doc.getElementById('check-result');
    if (!marker || marker.dataset.status !== 'ok') return null;
    return {
        x: Number(marker.dataset.x),
        y: Number(marker.dataset.y),
        r: Number(marker.dataset.r),
        hit: marker.dataset.hit === 'true',
        requestTime: Number(marker.dataset.requestTime)
    };
}

async function checkPoint(x, y, r) {
    const res = await fetch(`${API_URL}?x=${x}&y=${y}&r=${r}`);
    if (!res.ok) {
        err.textContent = `Ошибка сервера: ${res.status}`;
        return;
    }
    const html = await res.text();
    const point = readResultFromHtml(html);
    if (!point) {
        err.textContent = 'Не удалось определить результат проверки';
        return;
    }
    err.textContent = '';
    points.push(point);
    render();
    draw();
    redraw();
}

$('point-form').addEventListener('submit', e => {
    err.textContent = '';
    const xChecked = document.querySelector('input[name="x"]:checked');
    const yRaw = yInput.value.trim().replace(',', '.');
    const y = parseDecimal(yRaw);

    if (!xChecked) { e.preventDefault(); err.textContent = 'Выберите X'; return; }
    if (yRaw === '' || isNaN(y) || y <= -3 || y >= 5) { e.preventDefault(); err.textContent = 'Y — число от -3 до 5'; return; }
    if (R === null) { e.preventDefault(); err.textContent = 'Выберите R'; return; }
});

$('area').addEventListener('click', e => {
    if (R === null) {
        err.textContent = 'Невозможно определить координаты: выберите радиус R';
        return;
    }
    err.textContent = '';
    const rect = $('area').getBoundingClientRect();
    const clickX = e.clientX - rect.left;
    const clickY = e.clientY - rect.top;
    const rawX = (clickX - CX) / U * R;
    const rawY = (CY - clickY) / U * R;
    const x = nearestAllowedX(rawX);
    const y = Math.max(-2.99, Math.min(4.99, Math.round(rawY * 100) / 100));
    checkPoint(x, y, R);
});

draw();
render();
redraw();
