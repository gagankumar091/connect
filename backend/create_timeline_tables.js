require('dotenv').config();
const mysql = require('mysql2/promise');

async function run() {
    const connection = await mysql.createConnection({
        uri: process.env.DATABASE_URL
    });

    await connection.query(`
        CREATE TABLE IF NOT EXISTS timeline_events (
            id VARCHAR(36) PRIMARY KEY,
            contact_id VARCHAR(36) NOT NULL,
            content VARCHAR(255) NOT NULL,
            subtitle VARCHAR(255),
            icon VARCHAR(50) DEFAULT 'LOCATION',
            color VARCHAR(50) DEFAULT 'NEUTRAL',
            is_meeting BOOLEAN DEFAULT FALSE,
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        )
    `);

    await connection.query(`
        CREATE TABLE IF NOT EXISTS meeting_summaries (
            id VARCHAR(36) PRIMARY KEY,
            contact_id VARCHAR(36) NOT NULL,
            summary TEXT,
            pain_point VARCHAR(255),
            budget VARCHAR(255),
            action_items VARCHAR(255),
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        )
    `);

    console.log("Tables created successfully.");
    
    // Check if timeline_events has subtitle column
    const [columns] = await connection.query("SHOW COLUMNS FROM timeline_events LIKE 'subtitle'");
    if (columns.length === 0) {
        await connection.query("ALTER TABLE timeline_events ADD COLUMN subtitle VARCHAR(255)");
        console.log("Added subtitle column.");
    }
    
    process.exit(0);
}

run().catch(console.error);
