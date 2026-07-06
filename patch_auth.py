import os

backend_dir = "/home/randomx/Videos/mitron/backend"

# --- 1. SETUP.JS ---
setup_js_content = """const mysql = require('mysql2/promise');
require('dotenv').config();

async function setupDatabase() {
    try {
        const connection = await mysql.createConnection(process.env.DATABASE_URL);
        console.log("Building full Mitron schema (Auth Patched)...");
        
        await connection.query(`DROP TABLE IF EXISTS users`);
        await connection.query(`CREATE TABLE users (id VARCHAR(36) PRIMARY KEY, username VARCHAR(255) UNIQUE, password VARCHAR(255), name VARCHAR(255), email VARCHAR(255), title VARCHAR(255), company VARCHAR(255), score INT DEFAULT 100, status VARCHAR(50) DEFAULT 'Online', last_login TIMESTAMP DEFAULT CURRENT_TIMESTAMP);`);
        
        // Seed Data with passwords
        await connection.query(`INSERT INTO users (id, username, password, name, email, title, company, score) VALUES 
            ('user_gagan', 'gagan', 'password123', 'Gagan Mitron', 'gagan@mitron.app', 'Admin', 'Mitron', 100),
            ('user_stark', 'tony', 'password123', 'Tony Stark', 'tony@stark.com', 'CEO', 'Stark Industries', 95)`);
            
        await connection.end();
        console.log("Database Auth Patched Successfully!");
    } catch (error) { console.error("Failed:", error); }
}
setupDatabase();
"""

# --- 2. INDEX.JS (Append Endpoints) ---
index_js_patch = """
// AUTHENTICATION ENDPOINTS
app.post('/api/register', async (req, res) => {
    try {
        const { username, password, name, email } = req.body;
        const id = 'user_' + Date.now();
        await pool.query('INSERT INTO users (id, username, password, name, email) VALUES (?, ?, ?, ?, ?)', [id, username, password, name, email]);
        res.json({ success: true, userId: id });
    } catch(e) { 
        console.error(e);
        res.status(500).json({ success: false, message: e.message }); 
    }
});

app.post('/api/login', async (req, res) => {
    try {
        const { usernameOrEmail, password } = req.body;
        const [rows] = await pool.query('SELECT id, password FROM users WHERE username = ? OR email = ?', [usernameOrEmail, usernameOrEmail]);
        if (rows.length > 0 && rows[0].password === password) {
            res.json({ success: true, userId: rows[0].id });
        } else {
            res.status(401).json({ success: false, message: 'Invalid credentials' });
        }
    } catch(e) { 
        res.status(500).json({ success: false, message: e.message }); 
    }
});
"""

with open(os.path.join(backend_dir, "setup.js"), "w") as f: f.write(setup_js_content)
with open(os.path.join(backend_dir, "index.js"), "a") as f: f.write(index_js_patch)

print("Auth backend patches applied.")
