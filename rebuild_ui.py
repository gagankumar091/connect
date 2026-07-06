import os

base_dir = "/home/randomx/Videos/mitron/backend/public"
os.makedirs(base_dir, exist_ok=True)

# Replace style.css with the globals.css variables from Kulhad
style_css = """
@tailwind base;
@tailwind components;
@tailwind utilities;

@layer base {
  :root {
    --background: 0 0% 100%;
    --foreground: 240 10% 3.9%;
    --card: 0 0% 100%;
    --card-foreground: 240 10% 3.9%;
    --border: 240 5.9% 90%;
    --primary: 240 5.9% 10%;
    --primary-foreground: 0 0% 98%;
  }
}

.bg-grid-pattern {
  background-image: linear-gradient(rgba(0, 0, 0, 0.1) 1px, transparent 1px),
                    linear-gradient(90deg, rgba(0, 0, 0, 0.1) 1px, transparent 1px);
  background-size: 20px 20px;
}

body { font-family: ui-sans-serif, system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif; }

/* Custom Tabs Logic */
.tab-content { display: none; opacity: 0; transform: translateY(10px); transition: 0.3s ease; }
.tab-content.active { display: block; opacity: 1; transform: translateY(0); }
.tab-btn.active { border-bottom: 2px solid black; color: black; font-weight: 600; }
"""

index_html = """<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Mitron Dashboard</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <link rel="stylesheet" href="style.css">
</head>
<body class="min-h-screen bg-gradient-to-br from-gray-50 via-blue-50/30 to-purple-50/30 text-gray-900 antialiased">
    
    <!-- Header identical to Kulhad style -->
    <header class="border-b bg-white/50 backdrop-blur-md sticky top-0 z-50">
        <div class="flex h-16 items-center px-4 max-w-7xl mx-auto w-full gap-4">
            <h2 class="text-xl font-bold tracking-tight">Mitron Admin Dashboard</h2>
            <div class="ml-auto flex items-center space-x-4">
                <span class="text-sm text-gray-500">Admin Session</span>
                <div class="h-8 w-8 rounded-full bg-blue-600 flex items-center justify-center text-white font-bold text-sm">A</div>
            </div>
        </div>
    </header>

    <main class="relative min-h-[calc(100vh-4rem)]">
        <!-- Background Pattern -->
        <div class="absolute inset-0 bg-grid-pattern opacity-5"></div>

        <div class="relative container mx-auto px-4 sm:px-6 py-8 max-w-7xl">
            
            <!-- Shadcn-like Tabs -->
            <div class="w-full mb-8 border-b border-gray-200">
                <nav class="-mb-px flex space-x-8" aria-label="Tabs">
                    <button onclick="showTab('dashboard', this)" class="tab-btn active whitespace-nowrap py-4 px-1 border-b-2 font-medium text-sm text-gray-500 hover:text-gray-700">Overview</button>
                    <button onclick="showTab('events', this)" class="tab-btn whitespace-nowrap py-4 px-1 border-b-2 border-transparent font-medium text-sm text-gray-500 hover:text-gray-700">Broadcast Event</button>
                    <button onclick="showTab('users', this)" class="tab-btn whitespace-nowrap py-4 px-1 border-b-2 border-transparent font-medium text-sm text-gray-500 hover:text-gray-700">Live Users</button>
                </nav>
            </div>

            <!-- DASHBOARD TAB -->
            <div id="dashboard-tab" class="tab-content active space-y-8">
                <!-- Overview Cards (Kulhad Style) -->
                <div class="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
                    <div class="rounded-xl border bg-white text-card-foreground shadow-sm">
                        <div class="p-6 flex flex-row items-center justify-between space-y-0 pb-2">
                            <h3 class="tracking-tight text-sm font-medium">Total Users</h3>
                            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="h-4 w-4 text-muted-foreground"><path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"></path><circle cx="9" cy="7" r="4"></circle><path d="M22 21v-2a4 4 0 0 0-3-3.87"></path><path d="M16 3.13a4 4 0 0 1 0 7.75"></path></svg>
                        </div>
                        <div class="p-6 pt-0">
                            <div class="text-2xl font-bold" id="stat-users">0</div>
                            <p class="text-xs text-green-500 font-medium">+12% from last month</p>
                        </div>
                    </div>
                    
                    <div class="rounded-xl border bg-white text-card-foreground shadow-sm">
                        <div class="p-6 flex flex-row items-center justify-between space-y-0 pb-2">
                            <h3 class="tracking-tight text-sm font-medium">Live Active Now</h3>
                            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="h-4 w-4 text-emerald-500"><polyline points="22 12 18 12 15 21 9 3 6 12 2 12"></polyline></svg>
                        </div>
                        <div class="p-6 pt-0">
                            <div class="text-2xl font-bold" id="stat-active">0</div>
                            <p class="text-xs text-gray-500">Currently interacting</p>
                        </div>
                    </div>
                    
                    <div class="rounded-xl border bg-white text-card-foreground shadow-sm">
                        <div class="p-6 flex flex-row items-center justify-between space-y-0 pb-2">
                            <h3 class="tracking-tight text-sm font-medium">Total Events</h3>
                            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="h-4 w-4 text-muted-foreground"><rect width="18" height="18" x="3" y="4" rx="2" ry="2"></rect><line x1="16" x2="16" y1="2" y2="6"></line><line x1="8" x2="8" y1="2" y2="6"></line><line x1="3" x2="21" y1="10" y2="10"></line></svg>
                        </div>
                        <div class="p-6 pt-0">
                            <div class="text-2xl font-bold" id="stat-events">0</div>
                            <p class="text-xs text-gray-500">+2 upcoming this week</p>
                        </div>
                    </div>
                </div>

                <!-- Kulhad Gradient Card Style for Quick Actions -->
                <div class="relative overflow-hidden rounded-xl border bg-gradient-to-br from-white/95 to-gray-50/95 border-white/20 shadow-2xl">
                    <div class="absolute inset-0 bg-gradient-to-br from-emerald-50/30 via-teal-50/20 to-cyan-50/30"></div>
                    <div class="relative p-6">
                        <div class="flex items-center gap-3 mb-2">
                            <div class="p-3 rounded-2xl bg-gradient-to-r from-emerald-500 to-teal-500 shadow-lg">
                                <svg class="h-6 w-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z"></path></svg>
                            </div>
                            <div>
                                <h3 class="text-xl font-bold text-gray-900">System Status</h3>
                                <p class="text-sm text-gray-600">All backend services are fully operational.</p>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- EVENTS TAB -->
            <div id="events-tab" class="tab-content space-y-6">
                <div class="relative overflow-hidden rounded-xl border bg-gradient-to-br from-white/95 to-gray-50/95 border-white/20 shadow-2xl">
                    <div class="absolute inset-0 bg-gradient-to-br from-purple-50/30 via-pink-50/20 to-rose-50/30"></div>
                    <div class="relative p-6">
                        <h3 class="text-xl font-bold text-gray-900 mb-6">Broadcast New Event</h3>
                        <form id="event-form" class="space-y-4 max-w-2xl">
                            <div>
                                <label class="block text-sm font-medium text-gray-700 mb-1">Event Title</label>
                                <input type="text" id="ev-title" required class="flex h-10 w-full rounded-md border border-gray-300 bg-white px-3 py-2 text-sm placeholder:text-gray-400 focus:outline-none focus:ring-2 focus:ring-purple-500" placeholder="e.g. Founder Mixer">
                            </div>
                            <div>
                                <label class="block text-sm font-medium text-gray-700 mb-1">Location</label>
                                <input type="text" id="ev-loc" required class="flex h-10 w-full rounded-md border border-gray-300 bg-white px-3 py-2 text-sm placeholder:text-gray-400 focus:outline-none focus:ring-2 focus:ring-purple-500" placeholder="e.g. Moscone Center, SF">
                            </div>
                            <div>
                                <label class="block text-sm font-medium text-gray-700 mb-1">Date & Time</label>
                                <input type="text" id="ev-date" required class="flex h-10 w-full rounded-md border border-gray-300 bg-white px-3 py-2 text-sm placeholder:text-gray-400 focus:outline-none focus:ring-2 focus:ring-purple-500" placeholder="e.g. Oct 15 - 17, 2026">
                            </div>
                            <div>
                                <label class="block text-sm font-medium text-gray-700 mb-1">Description</label>
                                <textarea id="ev-desc" rows="3" required class="flex w-full rounded-md border border-gray-300 bg-white px-3 py-2 text-sm placeholder:text-gray-400 focus:outline-none focus:ring-2 focus:ring-purple-500" placeholder="Details about the event..."></textarea>
                            </div>
                            <button type="submit" class="inline-flex items-center justify-center rounded-md text-sm font-medium transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-purple-500 disabled:pointer-events-none disabled:opacity-50 bg-gray-900 text-white hover:bg-gray-800 h-10 px-4 py-2 mt-4 shadow-md">
                                Broadcast to Nearby Users
                            </button>
                        </form>
                    </div>
                </div>
            </div>

            <!-- USERS TAB -->
            <div id="users-tab" class="tab-content space-y-6">
                <div class="rounded-xl border bg-white shadow-sm overflow-hidden">
                    <div class="p-6 border-b">
                        <h3 class="text-lg font-bold">Registered Users</h3>
                        <p class="text-sm text-gray-500">Manage and view all platform accounts.</p>
                    </div>
                    <div class="overflow-x-auto">
                        <table class="w-full text-sm text-left">
                            <thead class="text-xs text-gray-500 uppercase bg-gray-50 border-b">
                                <tr>
                                    <th class="px-6 py-3">Name</th>
                                    <th class="px-6 py-3">Email</th>
                                    <th class="px-6 py-3">Status</th>
                                    <th class="px-6 py-3">Last Active</th>
                                </tr>
                            </thead>
                            <tbody id="users-table-body" class="divide-y">
                                <!-- Populated by JS -->
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>

        </div>
    </main>

    <script>
        function showTab(tabId, btnElement) {
            document.querySelectorAll('.tab-content').forEach(el => el.classList.remove('active'));
            document.querySelectorAll('.tab-btn').forEach(el => {
                el.classList.remove('active', 'border-black', 'text-black');
                el.classList.add('border-transparent', 'text-gray-500');
            });
            
            document.getElementById(tabId + '-tab').classList.add('active');
            btnElement.classList.add('active', 'border-black', 'text-black');
            btnElement.classList.remove('border-transparent', 'text-gray-500');

            if(tabId === 'dashboard') loadStats();
            if(tabId === 'users') loadUsers();
        }

        async function loadStats() {
            try {
                const res = await fetch('/api/stats');
                const data = await res.json();
                document.getElementById('stat-users').innerText = data.users;
                document.getElementById('stat-events').innerText = data.events;
                document.getElementById('stat-active').innerText = data.activeNow;
            } catch(e) { console.error(e); }
        }

        async function loadUsers() {
            try {
                const res = await fetch('/api/users');
                const users = await res.json();
                const tbody = document.getElementById('users-table-body');
                tbody.innerHTML = '';
                users.forEach(u => {
                    const tr = document.createElement('tr');
                    tr.className = "bg-white hover:bg-gray-50";
                    tr.innerHTML = `
                        <td class="px-6 py-4 font-medium text-gray-900">${u.name}</td>
                        <td class="px-6 py-4 text-gray-500">${u.email}</td>
                        <td class="px-6 py-4"><span class="bg-green-100 text-green-800 text-xs font-medium px-2.5 py-0.5 rounded-full border border-green-200">${u.status}</span></td>
                        <td class="px-6 py-4 text-gray-500">${new Date(u.last_login).toLocaleString()}</td>
                    `;
                    tbody.appendChild(tr);
                });
            } catch(e) { console.error(e); }
        }

        document.getElementById('event-form').addEventListener('submit', async (e) => {
            e.preventDefault();
            const btn = e.target.querySelector('button');
            btn.innerText = 'Broadcasting...';
            
            const payload = {
                title: document.getElementById('ev-title').value,
                location: document.getElementById('ev-loc').value,
                date: document.getElementById('ev-date').value,
                description: document.getElementById('ev-desc').value,
            };

            try {
                const res = await fetch('/api/events', {
                    method: 'POST',
                    headers: {'Content-Type': 'application/json'},
                    body: JSON.stringify(payload)
                });
                const data = await res.json();
                if(data.success) {
                    btn.innerText = 'Broadcast Successful!';
                    btn.classList.add('bg-green-600', 'hover:bg-green-700');
                    btn.classList.remove('bg-gray-900', 'hover:bg-gray-800');
                    e.target.reset();
                    setTimeout(() => { 
                        btn.innerText = 'Broadcast to Nearby Users';
                        btn.classList.remove('bg-green-600', 'hover:bg-green-700');
                        btn.classList.add('bg-gray-900', 'hover:bg-gray-800');
                    }, 3000);
                    loadStats();
                }
            } catch(err) {
                alert("Failed to broadcast");
                btn.innerText = 'Broadcast to Nearby Users';
            }
        });

        // Init
        loadStats();
    </script>
</body>
</html>
"""

with open(os.path.join(base_dir, "style.css"), "w") as f: f.write(style_css)
with open(os.path.join(base_dir, "index.html"), "w") as f: f.write(index_html)
print("Updated to Kulhad Tailwind UI!")
