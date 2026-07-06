import os

backend_dir = "/home/randomx/Videos/mitron/backend"

# 1. Update setup.js with full schema
setup_js_content = """const mysql = require('mysql2/promise');
require('dotenv').config();

async function setupDatabase() {
    try {
        const connection = await mysql.createConnection(process.env.DATABASE_URL);
        console.log("Connected to Aiven MySQL. Building full Mitron schema...");
        
        // 1. Users (Contacts)
        await connection.query(`
            CREATE TABLE IF NOT EXISTS users (
                id VARCHAR(36) PRIMARY KEY,
                name VARCHAR(255) NOT NULL,
                email VARCHAR(255),
                title VARCHAR(255),
                company VARCHAR(255),
                score INT DEFAULT 100,
                status VARCHAR(50) DEFAULT 'Online',
                last_login TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            );
        `);

        // 2. Companies
        await connection.query(`
            CREATE TABLE IF NOT EXISTS companies (
                id VARCHAR(36) PRIMARY KEY,
                name VARCHAR(255) NOT NULL,
                descriptor TEXT,
                funding VARCHAR(50),
                employees INT,
                open_deals INT,
                recent_news TEXT
            );
        `);

        // 3. Events
        await connection.query(`
            CREATE TABLE IF NOT EXISTS events (
                id VARCHAR(36) PRIMARY KEY,
                title VARCHAR(255) NOT NULL,
                location VARCHAR(255),
                date VARCHAR(255),
                description TEXT,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            );
        `);

        // 4. Chats
        await connection.query(`
            CREATE TABLE IF NOT EXISTS chats (
                id VARCHAR(36) PRIMARY KEY,
                user_id VARCHAR(36),
                contact_id VARCHAR(36),
                last_message TEXT,
                unread_count INT DEFAULT 0,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            );
        `);

        // 5. Connections
        await connection.query(`
            CREATE TABLE IF NOT EXISTS connections (
                id VARCHAR(36) PRIMARY KEY,
                sender_id VARCHAR(36),
                receiver_id VARCHAR(36),
                status VARCHAR(50) DEFAULT 'PENDING',
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            );
        `);
        
        // Seed some data
        await connection.query(`DELETE FROM users`);
        await connection.query(`DELETE FROM companies`);
        await connection.query(`DELETE FROM events`);
        
        await connection.query(`
            INSERT INTO users (id, name, email, title, company, score) VALUES 
            ('user_stark', 'Tony Stark', 'tony@stark.com', 'CEO', 'Stark Industries', 95),
            ('user_richard', 'Richard Hendricks', 'richard@piedpiper.com', 'Founder', 'Pied Piper', 88),
            ('user_gagan', 'Gagan Mitron', 'gagan@mitron.app', 'Admin', 'Mitron', 100)
        `);

        await connection.query(`
            INSERT INTO companies (id, name, descriptor, funding, employees, open_deals, recent_news) VALUES 
            ('comp_stark', 'Stark Industries', 'Global tech conglomerate.', '$10B+', 50000, 12, 'Arc Reactor 2.0 announced.'),
            ('comp_pied', 'Pied Piper', 'Middle-out compression.', 'Series B', 50, 2, 'New internet launched.')
        `);

        await connection.query(`
            INSERT INTO events (id, title, location, date, description) VALUES 
            (UUID(), 'TechCrunch Disrupt', 'SF', 'Oct 15 - 17, 2026', 'Startup battlefield.'),
            (UUID(), 'Stark Expo 2026', 'Flushing Meadows', 'Nov 1 - 5, 2026', 'Technology showcase.')
        `);
        
        await connection.end();
        console.log("Full Mitron schema and seed data deployed successfully!");
    } catch (error) {
        console.error("Failed:", error);
    }
}
setupDatabase();
"""

# 2. Update index.js with full endpoints
index_js_content = """const express = require('express');
const mysql = require('mysql2/promise');
const cors = require('cors');
const path = require('path');
require('dotenv').config();

const app = express();
app.use(cors());
app.use(express.json());
app.use(express.static(path.join(__dirname, 'public')));

const pool = mysql.createPool({
    uri: process.env.DATABASE_URL,
    waitForConnections: true,
    connectionLimit: 10,
    queueLimit: 0
});

// Admin Dashboard Route
app.get('/', (req, res) => {
    res.sendFile(path.join(__dirname, 'public', 'index.html'));
});

// Admin Stats
app.get('/api/stats', async (req, res) => {
    try {
        const [[{ userCount }]] = await pool.query('SELECT COUNT(*) as userCount FROM users');
        const [[{ eventCount }]] = await pool.query('SELECT COUNT(*) as eventCount FROM events');
        res.json({ users: userCount, events: eventCount, activeNow: Math.floor(userCount / 2) + 1 });
    } catch(e) { res.status(500).json({error: e.message}) }
});

// ---------------- ANDROID API ENDPOINTS ---------------- //

// GET Users (Contacts)
app.get('/api/users', async (req, res) => {
    try {
        const [rows] = await pool.query('SELECT * FROM users ORDER BY last_login DESC');
        res.json(rows);
    } catch(e) { res.status(500).json({error: e.message}) }
});

// GET Companies
app.get('/api/companies', async (req, res) => {
    try {
        const [rows] = await pool.query('SELECT * FROM companies');
        res.json(rows);
    } catch(e) { res.status(500).json({error: e.message}) }
});

// GET Events
app.get('/api/events', async (req, res) => {
    try {
        const [rows] = await pool.query('SELECT * FROM events ORDER BY created_at DESC');
        res.json(rows);
    } catch(e) { res.status(500).json({error: e.message}) }
});

// GET Chats for a user
app.get('/api/chats/:userId', async (req, res) => {
    try {
        const [rows] = await pool.query(`
            SELECT c.*, u.name as contact_name, u.company as contact_company
            FROM chats c
            JOIN users u ON c.contact_id = u.id
            WHERE c.user_id = ?
        `, [req.params.userId]);
        res.json(rows);
    } catch(e) { res.status(500).json({error: e.message}) }
});

// POST Connection Request (creates connection + chat)
app.post('/api/connections', async (req, res) => {
    const { senderId, receiverId } = req.body;
    try {
        await pool.query(
            'INSERT INTO connections (id, sender_id, receiver_id, status) VALUES (UUID(), ?, ?, ?)',
            [senderId, receiverId, 'PENDING']
        );
        await pool.query(
            'INSERT INTO chats (id, user_id, contact_id, last_message) VALUES (UUID(), ?, ?, ?)',
            [senderId, receiverId, 'Connected recently']
        );
        res.json({ success: true });
    } catch(e) { res.status(500).json({error: e.message}) }
});

// Admin Post Event
app.post('/api/events', async (req, res) => {
    try {
        const { title, location, date, description } = req.body;
        await pool.query(
            'INSERT INTO events (id, title, location, date, description) VALUES (UUID(), ?, ?, ?, ?)',
            [title, location, date, description]
        );
        res.json({ success: true });
    } catch(e) { res.status(500).json({error: e.message}) }
});

const PORT = process.env.PORT || 3000;
if (require.main === module) {
    app.listen(PORT, () => console.log(`Server running on http://localhost:${PORT}`));
}
module.exports = app;
"""

with open(os.path.join(backend_dir, "setup.js"), "w") as f: f.write(setup_js_content)
with open(os.path.join(backend_dir, "index.js"), "w") as f: f.write(index_js_content)

print("Backend API expanded for full Android migration!")
