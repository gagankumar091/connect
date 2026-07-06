import os

html_content = """<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Mitron Admin Dashboard</title>
    <style>
        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; margin: 0; background-color: #f4f7f6; color: #333; }
        header { background-color: #2c3e50; color: white; padding: 20px; text-align: center; }
        .container { max-width: 1200px; margin: 20px auto; padding: 20px; background: white; border-radius: 8px; box-shadow: 0 4px 6px rgba(0,0,0,0.1); }
        .tabs { display: flex; border-bottom: 2px solid #ddd; margin-bottom: 20px; }
        .tab { padding: 10px 20px; cursor: pointer; font-weight: bold; color: #7f8c8d; }
        .tab.active { color: #2c3e50; border-bottom: 3px solid #3498db; }
        .tab-content { display: none; }
        .tab-content.active { display: block; }
        table { width: 100%; border-collapse: collapse; margin-bottom: 20px; }
        th, td { padding: 12px; text-align: left; border-bottom: 1px solid #ddd; }
        th { background-color: #ecf0f1; }
        button { padding: 8px 16px; border: none; border-radius: 4px; cursor: pointer; color: white; }
        .btn-delete { background-color: #e74c3c; }
        .btn-add { background-color: #2ecc71; margin-bottom: 10px; }
    </style>
</head>
<body>
    <header><h1>Mitron Admin Dashboard</h1></header>
    <div class="container">
        <div class="tabs">
            <div class="tab active" onclick="switchTab('users')">Users</div>
            <div class="tab" onclick="switchTab('companies')">Companies</div>
            <div class="tab" onclick="switchTab('events')">Events</div>
        </div>
        
        <div id="users" class="tab-content active">
            <h2>Users Management</h2>
            <table>
                <thead><tr><th>ID</th><th>Name</th><th>Email</th><th>Company</th><th>Action</th></tr></thead>
                <tbody id="users-tbody"></tbody>
            </table>
        </div>

        <div id="companies" class="tab-content">
            <h2>Companies Management</h2>
            <button class="btn-add" onclick="addCompany()">+ Add Company</button>
            <table>
                <thead><tr><th>ID</th><th>Name</th><th>Funding</th><th>Employees</th><th>Action</th></tr></thead>
                <tbody id="companies-tbody"></tbody>
            </table>
        </div>

        <div id="events" class="tab-content">
            <h2>Events Management</h2>
            <button class="btn-add" onclick="addEvent()">+ Add Event</button>
            <table>
                <thead><tr><th>ID</th><th>Title</th><th>Location</th><th>Date</th><th>Action</th></tr></thead>
                <tbody id="events-tbody"></tbody>
            </table>
        </div>
    </div>

    <script>
        const API_KEY = 'MITRON_SECURE_KEY_2026';
        const headers = { 'Authorization': `Bearer ${API_KEY}`, 'Content-Type': 'application/json' };

        function switchTab(tabId) {
            document.querySelectorAll('.tab').forEach(t => t.classList.remove('active'));
            document.querySelectorAll('.tab-content').forEach(c => c.classList.remove('active'));
            event.target.classList.add('active');
            document.getElementById(tabId).classList.add('active');
            if(tabId === 'users') loadUsers();
            if(tabId === 'companies') loadCompanies();
            if(tabId === 'events') loadEvents();
        }

        async function loadUsers() {
            const res = await fetch('/api/users', { headers });
            const data = await res.json();
            document.getElementById('users-tbody').innerHTML = data.map(u => `
                <tr><td>${u.id}</td><td>${u.name}</td><td>${u.email}</td><td>${u.company || '-'}</td>
                <td><button class="btn-delete" onclick="deleteEntity('users', '${u.id}')">Delete</button></td></tr>
            `).join('');
        }

        async function loadCompanies() {
            const res = await fetch('/api/companies', { headers });
            const data = await res.json();
            document.getElementById('companies-tbody').innerHTML = data.map(c => `
                <tr><td>${c.id}</td><td>${c.name}</td><td>${c.funding || '-'}</td><td>${c.employees || '-'}</td>
                <td><button class="btn-delete" onclick="deleteEntity('companies', '${c.id}')">Delete</button></td></tr>
            `).join('');
        }

        async function loadEvents() {
            const res = await fetch('/api/events', { headers });
            const data = await res.json();
            document.getElementById('events-tbody').innerHTML = data.map(e => `
                <tr><td>${e.id}</td><td>${e.title}</td><td>${e.location || '-'}</td><td>${e.date || '-'}</td>
                <td><button class="btn-delete" onclick="deleteEntity('events', '${e.id}')">Delete</button></td></tr>
            `).join('');
        }

        async function deleteEntity(type, id) {
            if(!confirm("Delete this?")) return;
            await fetch(`/api/crud/${type}/${id}`, { method: 'DELETE', headers });
            if(type === 'users') loadUsers();
            if(type === 'companies') loadCompanies();
            if(type === 'events') loadEvents();
        }

        async function addCompany() {
            const name = prompt("Company Name:");
            if(!name) return;
            await fetch('/api/crud/companies', { method: 'POST', headers, body: JSON.stringify({ name }) });
            loadCompanies();
        }

        async function addEvent() {
            const title = prompt("Event Title:");
            if(!title) return;
            await fetch('/api/events', { method: 'POST', headers, body: JSON.stringify({ title, location: 'TBD', date: 'TBD', description: '' }) });
            loadEvents();
        }

        loadUsers();
    </script>
</body>
</html>
"""

import os
with open("/home/randomx/Videos/mitron/backend/public/dashboard.html", "w") as f:
    f.write(html_content)

print("Dashboard created.")
