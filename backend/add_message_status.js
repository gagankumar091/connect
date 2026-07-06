const mysql = require('mysql2/promise');
require('dotenv').config();

async function run() {
    try {
        const pool = await mysql.createConnection(process.env.DATABASE_URL);
        
        await pool.query(`ALTER TABLE messages ADD COLUMN IF NOT EXISTS is_read BOOLEAN DEFAULT FALSE`);
        await pool.query(`ALTER TABLE messages ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP`);
        
        console.log("Added is_read and created_at to messages table");
        await pool.end();
    } catch(e) {
        console.error(e);
    }
}
run();
