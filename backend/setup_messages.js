const mysql = require('mysql2/promise');
require('dotenv').config();

async function run() {
    try {
        const pool = await mysql.createConnection(process.env.DATABASE_URL);
        
        await pool.query(`
            CREATE TABLE IF NOT EXISTS messages (
                id VARCHAR(255) PRIMARY KEY,
                chat_id VARCHAR(255),
                sender_id VARCHAR(255),
                text TEXT,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        `);
        console.log("Created messages table");
        
        await pool.end();
    } catch(e) {
        console.error(e);
    }
}
run();
