import os

base_dir = "/home/randomx/Videos/mitron/backend"

setup_path = os.path.join(base_dir, "setup.js")
with open(setup_path, "r") as f:
    setup_content = f.read()

# Add lat and lng to users and events schemas
setup_content = setup_content.replace(
    "score INT DEFAULT 100, status VARCHAR(50) DEFAULT 'Online', last_login TIMESTAMP DEFAULT CURRENT_TIMESTAMP",
    "score INT DEFAULT 100, status VARCHAR(50) DEFAULT 'Online', lat DOUBLE, lng DOUBLE, last_login TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
)
setup_content = setup_content.replace(
    'date VARCHAR(255), description TEXT, created_at TIMESTAMP',
    'date VARCHAR(255), description TEXT, lat DOUBLE, lng DOUBLE, created_at TIMESTAMP'
)

# Add coordinates to seed data
setup_content = setup_content.replace(
    "VALUES \n            ('user_gagan', 'gagan', 'password123', 'Gagan Mitron', 'gagan@mitron.app', 'Admin', 'Mitron', 100),\n            ('user_stark', 'tony', 'password123', 'Tony Stark', 'tony@stark.com', 'CEO', 'Stark Industries', 95)",
    "VALUES \n            ('user_gagan', 'gagan', 'password123', 'Gagan Mitron', 'gagan@mitron.app', 'Admin', 'Mitron', 100, 37.7749, -122.4194),\n            ('user_stark', 'tony', 'password123', 'Tony Stark', 'tony@stark.com', 'CEO', 'Stark Industries', 95, 37.7750, -122.4180)"
)

with open(setup_path, "w") as f: f.write(setup_content)


index_path = os.path.join(base_dir, "index.js")
with open(index_path, "r") as f:
    index_content = f.read()

# Modify get users/events to support lat/lng + Haversine
index_content = index_content.replace(
    "const [rows] = await pool.query('SELECT * FROM users');",
    "const { lat, lng } = req.query;\n        let query = 'SELECT * FROM users';\n        let params = [];\n        if (lat && lng) {\n            query = 'SELECT *, ( 6371 * acos( cos( radians(?) ) * cos( radians( lat ) ) * cos( radians( lng ) - radians(?) ) + sin( radians(?) ) * sin( radians( lat ) ) ) ) AS distance FROM users HAVING distance < 50 ORDER BY distance';\n            params = [lat, lng, lat];\n        }\n        const [rows] = await pool.query(query, params);"
)
index_content = index_content.replace(
    "const [rows] = await pool.query('SELECT * FROM events');",
    "const { lat, lng } = req.query;\n        let query = 'SELECT * FROM events';\n        let params = [];\n        if (lat && lng) {\n            query = 'SELECT *, ( 6371 * acos( cos( radians(?) ) * cos( radians( lat ) ) * cos( radians( lng ) - radians(?) ) + sin( radians(?) ) * sin( radians( lat ) ) ) ) AS distance FROM events HAVING distance < 50 ORDER BY distance';\n            params = [lat, lng, lat];\n        }\n        const [rows] = await pool.query(query, params);"
)

# Add POST to update location
if "/api/users/location" not in index_content:
    patch = """
app.post('/api/users/location', authenticate, async (req, res) => {
    try {
        const { userId, lat, lng } = req.body;
        await pool.query('UPDATE users SET lat = ?, lng = ? WHERE id = ?', [lat, lng, userId]);
        res.json({ success: true });
    } catch(e) { res.status(500).json({ error: e.message }); }
});
"""
    with open(index_path, "a") as f: f.write(patch)

print("Backend patched for location tracking.")
