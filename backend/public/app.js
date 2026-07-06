
const HEADERS = { 'Authorization': 'Bearer MITRON_SECURE_KEY_2026', 'Content-Type': 'application/json' };

function showTab(tabId, event) {
    document.querySelectorAll('.tab-content').forEach(el => el.classList.remove('active'));
    document.querySelectorAll('.nav-item').forEach(el => el.classList.remove('active'));
    document.getElementById(tabId + '-tab').classList.add('active');
    if(event) event.currentTarget.classList.add('active');

    if(tabId === 'users') loadUsers();
    if(tabId === 'events') loadEvents();
    if(tabId === 'companies') loadCompanies();
}

async function loadUsers() {
    const res = await fetch('/api/users', { headers: HEADERS });
    const users = await res.json();
    const tbody = document.getElementById('users-table-body');
    const attendeeSelect = document.getElementById('ev-attendees');
    tbody.innerHTML = '';
    if (attendeeSelect) attendeeSelect.innerHTML = '';
    users.forEach(u => {
        tbody.innerHTML += `<tr>
            <td><strong>${u.name}</strong></td>
            <td>${u.email}</td>
            <td>${u.company || '-'}</td>
            <td><button class="delete-btn" onclick="deleteItem('users', '${u.id}')">Delete</button></td>
        </tr>`;
        if (attendeeSelect) {
            attendeeSelect.innerHTML += `<option value="${u.id}">${u.name} (${u.company || 'No Company'})</option>`;
        }
    });
}

async function loadEvents() {
    const res = await fetch('/api/events', { headers: HEADERS });
    const events = await res.json();
    const tbody = document.getElementById('events-table-body');
    tbody.innerHTML = '';
    events.forEach(e => {
        tbody.innerHTML += `<tr>
            <td><strong>${e.title}</strong></td>
            <td>${e.location}</td>
            <td>${e.date}</td>
            <td><button class="delete-btn" onclick="deleteItem('events', '${e.id}')">Delete</button></td>
        </tr>`;
    });
}

async function loadCompanies() {
    const res = await fetch('/api/companies', { headers: HEADERS });
    const comps = await res.json();
    const tbody = document.getElementById('companies-table-body');
    tbody.innerHTML = '';
    comps.forEach(c => {
        tbody.innerHTML += `<tr>
            <td><strong>${c.name}</strong></td>
            <td>${c.descriptor}</td>
            <td>${c.employees}</td>
            <td><button class="delete-btn" onclick="deleteItem('companies', '${c.id}')">Delete</button></td>
        </tr>`;
    });
}

async function deleteItem(type, id) {
    if(!confirm('Are you sure you want to delete this item?')) return;
    const res = await fetch(`/api/crud/${type}/${id}`, { method: 'DELETE', headers: HEADERS });
    if(res.ok) {
        if(type === 'users') loadUsers();
        if(type === 'events') loadEvents();
        if(type === 'companies') loadCompanies();
    }
}

document.getElementById('event-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    const attendeeOptions = document.getElementById('ev-attendees').selectedOptions;
    const attendees = Array.from(attendeeOptions).map(opt => opt.value);

    const payload = {
        title: document.getElementById('ev-title').value,
        location: document.getElementById('ev-loc').value,
        date: document.getElementById('ev-date').value,
        lat: parseFloat(document.getElementById('ev-lat').value),
        lng: parseFloat(document.getElementById('ev-lng').value),
        description: document.getElementById('ev-desc').value,
        attendees: attendees
    };
    await fetch('/api/events', { method: 'POST', headers: HEADERS, body: JSON.stringify(payload) });
    alert("Event Added Successfully!");
    e.target.reset();
});

document.getElementById('user-form')?.addEventListener('submit', async (e) => {
    e.preventDefault();
    const payload = {
        name: document.getElementById('usr-name').value,
        email: document.getElementById('usr-email').value,
        company: document.getElementById('usr-comp').value,
        title: document.getElementById('usr-title').value,
    };
    await fetch('/api/crud/users', { method: 'POST', headers: HEADERS, body: JSON.stringify(payload) });
    alert("Employee Added Successfully!");
    e.target.reset();
});

document.getElementById('company-form')?.addEventListener('submit', async (e) => {
    e.preventDefault();
    const payload = {
        name: document.getElementById('comp-name').value,
        descriptor: document.getElementById('comp-desc').value,
        employees: document.getElementById('comp-emp').value,
    };
    await fetch('/api/crud/companies', { method: 'POST', headers: HEADERS, body: JSON.stringify(payload) });
    alert("Company Added Successfully!");
    e.target.reset();
});

loadUsers();
