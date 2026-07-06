const mysql = require('mysql2/promise');
require('dotenv').config();

async function ensureChats() {
    try {
        const connection = await mysql.createConnection(process.env.DATABASE_URL);
        
        await connection.query(`
            CREATE TABLE IF NOT EXISTS chats (
                id VARCHAR(36) PRIMARY KEY,
                user_id VARCHAR(36),
                contact_id VARCHAR(36),
                last_message TEXT,
                timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            );
        `);
        console.log("Chats table ensured");
        await connection.end();
    } catch(e) { console.error(e) }
}
ensureChats();
