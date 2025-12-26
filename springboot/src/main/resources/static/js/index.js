const base = window.location.origin;

function showCopyStatus(el, msg) {
    const s = el;
    s.textContent = msg;
    s.style.visibility = 'visible';
    clearTimeout(window.__copyStatusTimer);
    window.__copyStatusTimer = setTimeout(() => {
        s.style.visibility = 'hidden';
    }, 3000);
}

async function copyText(text) {
    try {
        if (navigator.clipboard && navigator.clipboard.writeText) {
            await navigator.clipboard.writeText(text);
            return true;
        }
    } catch (e) { /* fallback */
    }
    try {
        const ta = document.createElement('textarea');
        ta.value = text;
        ta.setAttribute('readonly', '');
        ta.style.position = 'fixed';
        ta.style.top = '-1000px';
        document.body.appendChild(ta);
        ta.select();
        const ok = document.execCommand('copy');
        ta.remove();
        return ok;
    } catch (e) {
        return false;
    }
}

document.getElementById('newBtn').onclick = async () => {
    const res = await fetch('/api/new', {method: 'POST'});
    const data = await res.json();
    const url = base + data.url;
    const a = document.getElementById('mapLink');
    a.href = data.url;
    a.textContent = url;
    document.getElementById('dialog').style.display = 'block';
    const statusEl = document.getElementById('copyStatus');
    document.getElementById('copyBtn').onclick = async () => {
        const ok = await copyText(url);
        showCopyStatus(statusEl, ok ? 'Link copied' : 'Copy failed');
    };
};
document.getElementById('joinBtn').onclick = () => {
    const id = document.getElementById('joinId').value.trim();
    if (id) location.href = '/' + id;
};
