import os

backend_dir = "/home/randomx/Videos/mitron/backend"

# --- 1. SETUP.JS ---
setup_js_content = """const mysql = require('mysql2/promise');
require('dotenv').config();

async function setupDatabase() {
    try {
        const connection = await mysql.createConnection(process.env.DATABASE_URL);
        console.log("Building full Mitron schema (Patched)...");
        
        // Users, Companies, Events, Chats, Connections...
        await connection.query(`CREATE TABLE IF NOT EXISTS users (id VARCHAR(36) PRIMARY KEY, name VARCHAR(255), email VARCHAR(255), title VARCHAR(255), company VARCHAR(255), score INT DEFAULT 100, status VARCHAR(50) DEFAULT 'Online', last_login TIMESTAMP DEFAULT CURRENT_TIMESTAMP);`);
        await connection.query(`CREATE TABLE IF NOT EXISTS companies (id VARCHAR(36) PRIMARY KEY, name VARCHAR(255), descriptor TEXT, funding VARCHAR(50), employees INT, open_deals INT, recent_news TEXT);`);
        await connection.query(`CREATE TABLE IF NOT EXISTS events (id VARCHAR(36) PRIMARY KEY, title VARCHAR(255), location VARCHAR(255), date VARCHAR(255), description TEXT, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP);`);
        await connection.query(`CREATE TABLE IF NOT EXISTS chats (id VARCHAR(36) PRIMARY KEY, user_id VARCHAR(36), contact_id VARCHAR(36), last_message TEXT, unread_count INT DEFAULT 0, updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP);`);
        await connection.query(`CREATE TABLE IF NOT EXISTS connections (id VARCHAR(36) PRIMARY KEY, sender_id VARCHAR(36), receiver_id VARCHAR(36), status VARCHAR(50) DEFAULT 'PENDING', created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP);`);
        
        // Missing Data Tables
        await connection.query(`CREATE TABLE IF NOT EXISTS timeline_events (id VARCHAR(36) PRIMARY KEY, contact_id VARCHAR(36), title VARCHAR(255), description TEXT, icon_name VARCHAR(50), created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP);`);
        await connection.query(`CREATE TABLE IF NOT EXISTS meeting_summaries (id VARCHAR(36) PRIMARY KEY, contact_id VARCHAR(36), summary TEXT, key_takeaways TEXT, action_items TEXT);`);
        await connection.query(`CREATE TABLE IF NOT EXISTS briefing_items (id VARCHAR(36) PRIMARY KEY, title VARCHAR(255), description TEXT, type VARCHAR(50), priority VARCHAR(20));`);

        // Seed Data
        await connection.query(`DELETE FROM users`);
        await connection.query(`INSERT INTO users (id, name, email, title, company, score) VALUES 
            ('user_gagan', 'Gagan Mitron', 'gagan@mitron.app', 'Admin', 'Mitron', 100),
            ('user_stark', 'Tony Stark', 'tony@stark.com', 'CEO', 'Stark Industries', 95)`);
            
        await connection.query(`DELETE FROM briefing_items`);
        await connection.query(`INSERT INTO briefing_items (id, title, description, type, priority) VALUES 
            (UUID(), 'Prepare for Tony Stark', 'Review Arc Reactor tech before meeting.', 'MEETING', 'HIGH'),
            (UUID(), 'Follow up with Richard', 'He sent a connection request 2 days ago.', 'ACTION', 'MEDIUM')`);
            
        await connection.end();
        console.log("Database Patched Successfully!");
    } catch (error) { console.error("Failed:", error); }
}
setupDatabase();
"""

# --- 2. INDEX.JS ---
index_js_content = """const express = require('express');
const mysql = require('mysql2/promise');
const cors = require('cors');
const path = require('path');
require('dotenv').config();

const app = express();
app.use(cors());
app.use(express.json());
app.use(express.static(path.join(__dirname, 'public')));

const pool = mysql.createPool({ uri: process.env.DATABASE_URL, waitForConnections: true, connectionLimit: 10 });

// SECURITY HOLE PATCH: API Key Middleware
const API_KEY = "MITRON_SECURE_KEY_2026";
const authenticate = (req, res, next) => {
    const authHeader = req.headers.authorization;
    if (!authHeader || authHeader !== `Bearer ${API_KEY}`) {
        return res.status(401).json({ error: "Unauthorized. Invalid API Key." });
    }
    next();
};

app.get('/', (req, res) => { res.sendFile(path.join(__dirname, 'public', 'index.html')); });

// Android API Endpoints (Secured)
app.get('/api/users', authenticate, async (req, res) => {
    try { const [rows] = await pool.query('SELECT * FROM users'); res.json(rows); } 
    catch(e) { res.status(500).json({error: e.message}) }
});

app.get('/api/companies', authenticate, async (req, res) => {
    try { const [rows] = await pool.query('SELECT * FROM companies'); res.json(rows); } 
    catch(e) { res.status(500).json({error: e.message}) }
});

app.get('/api/events', authenticate, async (req, res) => {
    try { const [rows] = await pool.query('SELECT * FROM events'); res.json(rows); } 
    catch(e) { res.status(500).json({error: e.message}) }
});

app.get('/api/chats/:userId', authenticate, async (req, res) => {
    try {
        const [rows] = await pool.query(`SELECT c.*, u.name as contact_name, u.company as contact_company FROM chats c JOIN users u ON c.contact_id = u.id WHERE c.user_id = ?`, [req.params.userId]);
        res.json(rows);
    } catch(e) { res.status(500).json({error: e.message}) }
});

// MISSING DATA HOLES PATCHED:
app.get('/api/timeline/:contactId', authenticate, async (req, res) => {
    try { const [rows] = await pool.query('SELECT * FROM timeline_events WHERE contact_id = ?', [req.params.contactId]); res.json(rows); } 
    catch(e) { res.status(500).json({error: e.message}) }
});

app.get('/api/briefings', authenticate, async (req, res) => {
    try { const [rows] = await pool.query('SELECT * FROM briefing_items'); res.json(rows); } 
    catch(e) { res.status(500).json({error: e.message}) }
});

const PORT = process.env.PORT || 3000;
if (require.main === module) { app.listen(PORT, () => console.log(`Server running on ${PORT}`)); }
module.exports = app;
"""

with open(os.path.join(backend_dir, "setup.js"), "w") as f: f.write(setup_js_content)
with open(os.path.join(backend_dir, "index.js"), "w") as f: f.write(index_js_content)
print("Backend holes patched.")
