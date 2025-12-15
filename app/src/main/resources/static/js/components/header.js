
// /components/header.js
export function renderHeader(role = 'GUEST') {
  const header = document.getElementById('app-header');
  header.className = 'header';
  header.innerHTML = `
    <nav class="nav">
      /Smart Clinic</a>
      <div class="links">
        ${role === 'ADMIN' ? `
          /admin.htmlAdmin Dashboard</a>
          /doctors.htmlDoctors</a>
          /settings.htmlSettings</a>
        ` : role === 'DOCTOR' ? `
          /doctor.htmlMy Appointments</a>
          /profile.htmlProfile</a>
        ` : `
          /login.htmlLogin</a>
        `}
      </div>
    </nav>
  `;
}
