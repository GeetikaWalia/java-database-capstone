
// /services/doctorService.js
const BASE = '/api';

function authHeader() {
  const token = localStorage.getItem('token');
  return token ? { 'Authorization': `Bearer ${token}` } : {};
}

export const doctorService = {
  cache: { doctors: null, myProfileId: null },

  async getAll(force = false) {
    if (!force && this.cache.doctors) return this.cache.doctors;
    const res = await fetch(`${BASE}/doctors`, { headers: { 'Content-Type': 'application/json', ...authHeader() } });
    if (!res.ok) throw new Error('Failed to fetch doctors');
    const json = await res.json();
    this.cache.doctors = json;
    return json;
  },

  async createDoctor(payload) {
    const res = await fetch(`${BASE}/doctors`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', ...authHeader() },
      body: JSON.stringify(payload)
    });
    if (!res.ok) throw new Error('Failed to create doctor');
    return res.json();
  },

  async getMyProfileId() {
    if (this.cache.myProfileId) return this.cache.myProfileId;
    const res = await fetch(`${BASE}/doctors/me`, { headers: { ...authHeader() } });
    if (!res.ok) throw new Error('Failed to fetch doctor profile');
    const me = await res.json();
    this.cache.myProfileId = me.id;
    return me.id;
  },

  async getAppointments(doctorId, { date, force } = {}) {
    const q = new URLSearchParams();
    q.set('doctorId', doctorId);
    if (date) q.set('date', date);
    const res = await fetch(`${BASE}/appointments?${q}`, { headers: { ...authHeader() } });
    if (!res.ok) throw new Error('Failed to fetch appointments');
    return res.json();
  }
};
