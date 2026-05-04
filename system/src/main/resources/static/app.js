/* ══════════════════════════════════════════
   ORDERING SYSTEM — Core JS
══════════════════════════════════════════ */

const API_BASE = 'http://localhost:8080/api';

/* ── Auth ──────────────────────────────── */
const Auth = {
  token:      () => localStorage.getItem('os_token'),
  user:       () => JSON.parse(localStorage.getItem('os_user') || 'null'),
  save:       (d) => {
    localStorage.setItem('os_token', d.token);
    localStorage.setItem('os_user', JSON.stringify({ username: d.username, email: d.email, fullName: d.fullName, role: d.role }));
  },
  clear:      () => { localStorage.removeItem('os_token'); localStorage.removeItem('os_user'); },
  loggedIn:   () => !!localStorage.getItem('os_token'),
  hasRole:    (r) => { const u = Auth.user(); return u && (Array.isArray(r) ? r.includes(u.role) : u.role === r); },
  guard:      () => { if (!Auth.loggedIn()) { window.location.href = '../index.html'; throw 0; } },
  guardRole:  (r) => { Auth.guard(); if (!Auth.hasRole(r)) { Toast.show('Access denied', 'error'); throw 0; } },
};

/* ── HTTP ──────────────────────────────── */
const API = {
  async req(method, path, body) {
    const h = { 'Content-Type': 'application/json' };
    const t = Auth.token();
    if (t) h['Authorization'] = `Bearer ${t}`;
    const r = await fetch(API_BASE + path, { method, headers: h, body: body ? JSON.stringify(body) : undefined });
    const d = await r.json().catch(() => ({}));
    if (!r.ok) throw new Error(d.message || `Error ${r.status}`);
    return d;
  },
  get:    (p)    => API.req('GET',    p),
  post:   (p, b) => API.req('POST',   p, b),
  put:    (p, b) => API.req('PUT',    p, b),
  patch:  (p, b) => API.req('PATCH',  p, b),
  del:    (p)    => API.req('DELETE', p),
};

/* ── Toast ─────────────────────────────── */
const Toast = {
  _shelf: null,
  _init() {
    if (!this._shelf) {
      this._shelf = document.createElement('div');
      this._shelf.className = 'toast-shelf';
      document.body.appendChild(this._shelf);
    }
  },
  show(msg, type = 'info', ms = 3200) {
    this._init();
    const icons = { success: '✓', error: '✕', info: '●' };
    const el = document.createElement('div');
    el.className = `toast ${type}`;
    el.innerHTML = `<span style="font-weight:800;color:var(--${type === 'success' ? 'accent' : type === 'error' ? 'danger' : 'info'})">${icons[type]}</span><span>${msg}</span>`;
    this._shelf.appendChild(el);
    setTimeout(() => {
      el.style.transition = '0.3s'; el.style.opacity = '0'; el.style.transform = 'translateX(40px)';
      setTimeout(() => el.remove(), 320);
    }, ms);
  }
};

/* ── Modal ─────────────────────────────── */
const Modal = {
  open:  (id) => { document.getElementById(id)?.classList.add('open'); },
  close: (id) => { document.getElementById(id)?.classList.remove('open'); },
  all:   ()   => document.querySelectorAll('.modal-bg').forEach(m => m.classList.remove('open')),
};

/* ── Confirm ───────────────────────────── */
function confirmDlg(msg, title = 'Confirm Action') {
  return new Promise(res => {
    const el = document.createElement('div');
    el.className = 'confirm-bg';
    el.innerHTML = `
      <div class="confirm-box">
        <h3>${title}</h3>
        <p>${msg}</p>
        <div class="confirm-actions">
          <button class="btn btn-ghost" id="cc">Cancel</button>
          <button class="btn btn-danger" id="cy">Confirm</button>
        </div>
      </div>`;
    document.body.appendChild(el);
    el.querySelector('#cc').onclick = () => { el.remove(); res(false); };
    el.querySelector('#cy').onclick = () => { el.remove(); res(true); };
  });
}

/* ── Format ────────────────────────────── */
const Fmt = {
  peso:    (n) => '₱ ' + parseFloat(n || 0).toLocaleString('en-PH', { minimumFractionDigits: 2 }),
  date:    (s) => s ? new Date(s).toLocaleDateString('en-PH', { year: 'numeric', month: 'short', day: 'numeric' }) : '—',
  dt:      (s) => s ? new Date(s).toLocaleString('en-PH',    { dateStyle: 'medium', timeStyle: 'short' }) : '—',
  badge:   (v) => {
    const m = {
      ADMIN:'badge-admin', CO_ADMIN:'badge-coadmin', STAFF:'badge-staff',
      PENDING:'badge-pending', CONFIRMED:'badge-confirmed', IN_PROGRESS:'badge-inprogress',
      COMPLETED:'badge-completed', CANCELLED:'badge-cancelled',
      true:'badge-active', false:'badge-inactive',
    };
    const label = v === true ? 'Active' : v === false ? 'Inactive' : String(v).replace(/_/g,' ');
    return `<span class="badge ${m[v] || 'badge-staff'}">${label}</span>`;
  },
  initials:(s) => (s || '?').split(' ').map(w => w[0]).join('').slice(0,2).toUpperCase(),
};

/* ── Sidebar builder ───────────────────── */
function buildSidebar(active) {
  const u = Auth.user();
  const isManager = Auth.hasRole(['ADMIN','CO_ADMIN']);
  const items = [
    { id:'dashboard', icon:'⊞', label:'Dashboard',       href:'dashboard.html' },
    { id:'orders',    icon:'◉', label:'Orders',          href:'orders.html'    },
    { id:'items',     icon:'◧', label:'Items',           href:'items.html'     },
    { id:'users',     icon:'◕', label:'User Management', href:'users.html',   admin:true },
    { id:'labor',     icon:'◈', label:'Labor',           href:'labor.html',   admin:true },
  ];

  const navHtml = items
    .filter(i => !i.admin || isManager)
    .map(i => `
      <a class="nav-item ${active===i.id?'active':''}" href="${i.href}">
        <span class="nav-icon">${i.icon}</span>${i.label}
      </a>`).join('');

  document.getElementById('sidebar').innerHTML = `
    <div class="sidebar-logo">
      <div class="logo-lockup">
        <div class="logo-gem">O</div>
        <div>
          <div class="logo-text">Order<span style="color:var(--accent)">Sys</span></div>
          <div class="logo-sub">Management Platform</div>
        </div>
      </div>
    </div>
    <nav class="sidebar-nav">
      <div class="nav-label">Menu</div>
      ${navHtml}
    </nav>
    <div class="sidebar-footer">
      <div class="user-chip">
        <div class="user-av">${Fmt.initials(u?.fullName||u?.username)}</div>
        <div class="user-av-info">
          <div class="u-name">${u?.fullName||u?.username||'User'}</div>
          <div class="u-role">${(u?.role||'').replace('_','-')}</div>
        </div>
      </div>
      <button class="btn btn-ghost btn-full btn-sm" id="logout-btn">Sign Out</button>
    </div>`;

  document.getElementById('logout-btn').onclick = () => { Auth.clear(); window.location.href='../index.html'; };
}

function mountPage(id, title) {
  Auth.guard();
  buildSidebar(id);
  const el = document.getElementById('page-title');
  if (el) el.textContent = title;
}

/* close modal on backdrop click */
document.addEventListener('click', e => {
  if (e.target.classList.contains('modal-bg')) Modal.all();
});