
// /index.js
import { renderHeader } from '/components/header.js';

export function setRole(role) {
  localStorage.setItem('role', role);
}

export function setToken(token) {
  localStorage.setItem('token', token);
}

export function initApp() {
  const role = localStorage.getItem('role') || 'GUEST';
  renderHeader(role);
  // login handling example
  const loginForm = document.getElementById('loginForm');
  if (loginForm) {
    loginForm.addEventListener('submit', async (e) => {
      e.preventDefault();
      const fd = new FormData(loginForm);
      const payload = { username: fd.get('username'), password: fd.get('password') };
      const res = await fetch('/api/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      });
      if (!res.ok) return alert('Login failed');
      const json = await res.json();
      setToken(json.token);
      setRole(json.role);
      location.href = json.role === 'ADMIN' ? '/admin.html' : '/doctor.html';
    });
  }
}

