const X_VALUES = ['-3','-2','-1','0','1','2','3','4','5'];
const R_VALUES = ['1','2','3','4','5'];
const API_URL = '/fcgi-bin/web.jar';

const $ = id => document.getElementById(id);
const xBox = $('x-buttons'), xInput = $('x-value');
const yInput = $('y-input'), rBox = $('r-radios');
const err = $('error'), body = $('results-body');
const ctx = $('area').getContext('2d');
const CX = 150, CY = 150, U = 90;
let R = null;
let points = [];

X_VALUES.forEach(v => {
    const b = document.createElement('button');
    b.type = 'button';
    b.textContent = v;
    b.onclick = () => {
        [...xBox.children].forEach(el => el.classList.remove('active'));
        b.classList.add('active');
        xInput.value = v;
    };
    xBox.appendChild(b);
});

R_VALUES.forEach(v => {
    const l = document.createElement('label');
    const r = document.createElement('input');
    r.type = 'radio';
    r.name = 'r';
    r.value = v;
    r.onchange = () => { R = Number(v); draw(); redraw(); };
    l.append(r, v);
    rBox.appendChild(l);
});

function draw() {
    ctx.clearRect(0, 0, 300, 300);
    ctx.fillStyle = '#4a90e2';
    ctx.beginPath();
    ctx.moveTo(CX, CY);
    ctx.lineTo(CX - U, CY);
    ctx.lineTo(CX, CY + U/2);
    ctx.fill();
    ctx.beginPath();
    ctx.moveTo(CX, CY);
    ctx.arc(CX, CY, U, -Math.PI, 0);
    ctx.fill();
    ctx.fillRect(CX, CY, U, -U);

    ctx.strokeStyle = '#000';
    ctx.beginPath();
    ctx.moveTo(0, CY); ctx.lineTo(300, CY);
    ctx.moveTo(CX, 0); ctx.lineTo(CX, 300);
    ctx.stroke();

    ctx.fillStyle = '#000';
    ctx.font = '11px sans-serif';
    [['R', 1], ['R/2', 0.5], ['-R/2', -0.5], ['-R', -1]].forEach(([t, p]) => {
        ctx.fillText(t, CX + p*U - 8, CY + 14);
        ctx.fillText(t, CX + 4, CY - p*U + 4);
    });
    ctx.font = 'bold 12px sans-serif';
    ctx.fillText('x', 290, CY - 4);
    ctx.fillText('y', CX + 6, 12);
}

function drawPoint(x, y, r, hit) {
    ctx.fillStyle = hit ? 'green' : 'red';
    ctx.beginPath();
    ctx.arc(CX + x/r*U, CY - y/r*U, 4, 0, 2*Math.PI);
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

async function readError(res) {
    const data = await res.json().catch(() => null);
    return data?.error ?? `Ошибка сервера: ${res.status}`;
}

async function loadPoints() {
    const res = await fetch(API_URL);
    if (!res.ok) {
        err.textContent = await readError(res);
        return;
    }
    points = await res.json();
    render();
    draw();
    redraw();
}

$('point-form').onsubmit = async e => {
    e.preventDefault();
    err.textContent = '';
    const yRaw = yInput.value.trim().replace(',', '.');
    const y = parseDecimal(yRaw);
    if (!xInput.value) { err.textContent = 'Выберите X'; return; }
    if (yRaw === '' || isNaN(y) || y <= -3 || y >= 5) { err.textContent = 'Y — число от -3 до 5'; return; }
    if (R === null) { err.textContent = 'Выберите R'; return; }
    const x = Number(xInput.value);

    const res = await fetch(`${API_URL}?x=${x}&y=${y}&r=${R}`);
    if (!res.ok) {
        err.textContent = await readError(res);
        return;
    }
    points.push(await res.json());
    render();
    draw();
    redraw();
};

draw();
loadPoints();