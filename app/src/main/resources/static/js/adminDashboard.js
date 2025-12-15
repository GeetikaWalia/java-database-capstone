
// /pages/adminDashboard.js
import { renderHeader } from '/components/header.js';
import { openModal, closeModal, bindModalClose } from '/components/modal.js';
import { createDoctorCard } from '/components/doctorCard.js';
import { doctorService } from '/services/doctorService.js';
import { debounce, inTimeWindow } from '/utils/common.js';

const state = {
  doctors: [],
  filtered: [],
  filters: {
    search: '',
    specialty: '',
    timeWindow: '' // '', 'morning'|'afternoon'|'evening'
  }
};

async function init() {
  renderHeader('ADMIN');

  bindControls();
  await loadDoctors();
  applyFiltersAndRender();
  bindModalClose('#addDoctorModal');
}

function bindControls() {
  const searchEl = document.getElementById('doctorSearchInput');
  const specialtyEl = document.getElementById('specialtyFilter');
  const timeEl = document.getElementById('timeFilter');
  const addBtn = document.getElementById('addDoctorBtn');

  searchEl.addEventListener('input', debounce((e) => {
    state.filters.search = e.target.value.trim().toLowerCase();
    applyFiltersAndRender();
  }, 200));

  specialtyEl.addEventListener('change', (e) => {
    state.filters.specialty = e.target.value;
    applyFiltersAndRender();
  });

  timeEl.addEventListener('change', (e) => {
    state.filters.timeWindow = e.target.value;
    applyFiltersAndRender();
  });

  addBtn.addEventListener('click', () => openModal('#addDoctorModal'));

  // Modal submit
  const form = document.getElementById('addDoctorForm');
  form.addEventListener('submit', async (e) => {
    e.preventDefault();
    const fd = new FormData(form);
    const payload = {
      fullName: fd.get('fullName'),
      specialty: fd.get('specialty'),
      email: fd.get('email'),
      phone: fd.get('phone')
    };
    try {
      await doctorService.createDoctor(payload);
      closeModal('#addDoctorModal');
      form.reset();
      await loadDoctors(true);
      applyFiltersAndRender();
    } catch (err) {
      console.error(err);
      alert('Failed to add doctor.');
    }
  });
}

async function loadDoctors(force = false) {
  state.doctors = await doctorService.getAll(force);
}

function applyFiltersAndRender() {
  const { search, specialty, timeWindow } = state.filters;
  let list = [...state.doctors];

  if (search) {
    list = list.filter(d => (d.fullName || d.name || '')
      .toLowerCase().includes(search));
  }
  if (specialty) {
    list = list.filter(d => (d.specialty || '') === specialty);
  }
  if (timeWindow) {
    // Assuming doctor.availableTimes: [{ weekday, startTime, endTime }]
    list = list.filter(d => {
      const slots = d.availableTimes || [];
      return slots.some(slot => inTimeWindow(slot.startTime, slot.endTime, timeWindow));
    });
  }

  state.filtered = list;
  renderDoctors(list);
}

function renderDoctors(list) {
  const grid = document.getElementById('doctorsGrid');
  grid.innerHTML = '';
  if (!list.length) {
    grid.innerHTML = `<p class="empty">No doctors found.</p>`;
    return;
  }
  list.forEach(d => {
    grid.appendChild(createDoctorCard(d, { onSelect: () => {} }));
  });
}

