
// /pages/doctorDashboard.js
import { renderHeader } from '/components/header.js';
import { openModal, closeModal, bindModalClose } from '/components/modal.js';
import { doctorService } from '/services/doctorService.js';
import { patientService } from '/services/patientService.js';
import { debounce, formatDateTime } from '/utils/common.js';

const state = {
  doctorId: null,
  appointments: [],
  filtered: [],
  filters: {
    searchPatient: '',
    date: ''
  }
};

async function init() {
  renderHeader('DOCTOR');
  bindControls();
  bindModalClose('#prescriptionsModal');

  // derive doctorId from token / profile
  state.doctorId = await doctorService.getMyProfileId();
  await loadAppointments();
  applyFiltersAndRender();
}

function bindControls() {
  const searchEl = document.getElementById('appointmentSearchInput');
  const dateEl = document.getElementById('dateFilter');

  searchEl.addEventListener('input', debounce((e) => {
    state.filters.searchPatient = e.target.value.trim().toLowerCase();
    applyFiltersAndRender();
  }, 200));

  dateEl.addEventListener('change', (e) => {
    state.filters.date = e.target.value; // "YYYY-MM-DD"
    applyFiltersAndRender();
  });
}

async function loadAppointments(force = false) {
  state.appointments = await doctorService.getAppointments(state.doctorId, { force });
}

function applyFiltersAndRender() {
  const { searchPatient, date } = state.filters;
  let list = [...state.appointments];

  if (searchPatient) {
    list = list.filter(a => (a.patient?.fullName || a.patient?.name || '')
      .toLowerCase().includes(searchPatient));
  }
  if (date) {
    list = list.filter(a => (a.appointmentTime || '').slice(0, 10) === date);
  }

  state.filtered = list;
  renderAppointments(list);
}

function renderAppointments(list) {
  const container = document.getElementById('appointmentsList');
  container.innerHTML = '';
  if (!list.length) {
    container.innerHTML = `<p class="empty">No appointments found.</p>`;
    return;
  }
  list.forEach(a => {
    const item = document.createElement('div');
    item.className = 'appointment-item';
    item.innerHTML = `
      <div class="left">
        <div class="patient">${a.patient?.fullName || 'Unknown Patient'}</div>
        <div class="time">${formatDateTime(a.appointmentTime)}</div>
      </div>
      <div class="right">
        <button class="btn" data-view-presc>View Prescriptions</button>
      </div>
    `;
    item.querySelector('[data-view-presc]').addEventListener('click', () => viewPrescriptions(a.patient?.id));
    container.appendChild(item);
  });
}

async function viewPrescriptions(patientId) {
  if (!patientId) return;
  const prescriptions = await patientService.getPrescriptions(patientId);
  const body = document.getElementById('prescriptionsBody');
  body.innerHTML = '';

  if (!prescriptions.length) {
    body.innerHTML = `<p class="empty">No prescriptions found.</p>`;
  } else {
    prescriptions.forEach(p => {
      const div = document.createElement('div');
      div.className = 'prescription-card';
      div.innerHTML = `
        <div><strong>Medication:</strong> ${p.medication}</div>
        <div><strong>Dosage:</strong> ${p.dosage || '-'}</div>
        <div><strong>Notes:</strong> ${p.doctorNotes || '-'}</div>
        <div><strong>Appointment #:</strong> ${p.appointmentId}</div>
      `;
      body.appendChild(div);
    });
  }
  openModal('#prescriptionsModal');
}

