const mysql = require('mysql2/promise');
require('dotenv').config();

async function run() {
    try {
        const pool = await mysql.createConnection(process.env.DATABASE_URL);
        
        try { await pool.query('ALTER TABLE companies ADD COLUMN descriptor VARCHAR(255);'); } catch(e) {}
        try { await pool.query('ALTER TABLE companies ADD COLUMN employees INT DEFAULT 0;'); } catch(e) {}
        try { await pool.query('ALTER TABLE users ADD COLUMN title VARCHAR(255);'); } catch(e) {}
        try { await pool.query('ALTER TABLE users ADD COLUMN company VARCHAR(255);'); } catch(e) {}
        
        console.log("DB columns ensured.");
        await pool.end();
    } catch(e) {
        console.error(e);
    }
}
run();
