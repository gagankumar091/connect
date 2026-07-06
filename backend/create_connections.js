const mysql = require('mysql2/promise');
require('dotenv').config();

async function createConnectionsTable() {
    try {
        const connection = await mysql.createConnection(process.env.DATABASE_URL);
        
        await connection.query(`
            CREATE TABLE IF NOT EXISTS connections (
                id VARCHAR(36) PRIMARY KEY,
                sender_id VARCHAR(36),
                receiver_id VARCHAR(36),
                status VARCHAR(50) DEFAULT 'PENDING',
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            );
        `);
        
        console.log("Successfully created connections table!");
        await connection.end();
    } catch (error) { console.error("Failed to create table:", error); }
}
createConnectionsTable();
