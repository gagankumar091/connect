const mysql = require('mysql2/promise');
require('dotenv').config();

async function fixDb() {
    try {
        const connection = await mysql.createConnection(process.env.DATABASE_URL);
        console.log("Applying missing tables...");
        
        await connection.query(`CREATE TABLE IF NOT EXISTS notifications (id VARCHAR(36) PRIMARY KEY, user_id VARCHAR(36), title VARCHAR(255), description TEXT, type VARCHAR(50), action_id VARCHAR(36), is_read BOOLEAN DEFAULT FALSE, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP);`);
        
        await connection.query(`CREATE TABLE IF NOT EXISTS events (id VARCHAR(36) PRIMARY KEY, title VARCHAR(255), location VARCHAR(255), date VARCHAR(255), description TEXT, lat DOUBLE, lng DOUBLE, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP);`);
        
        // Ensure events have lat/lng
        try {
            await connection.query(`ALTER TABLE events ADD COLUMN lat DOUBLE`);
            await connection.query(`ALTER TABLE events ADD COLUMN lng DOUBLE`);
        } catch(e) {}
        
        // Seed event
        await connection.query(`DELETE FROM events`);
        await connection.query(`INSERT INTO events (id, title, location, date, description, lat, lng) VALUES ('evt_1', 'AI Summit 2026', 'San Francisco', '2026-08-15', 'The biggest AI meetup in the valley.', 37.7755, -122.4170)`);
        
        await connection.end();
        console.log("DB Fixed successfully.");
    } catch(e) {
        console.error("DB Fix error:", e);
    }
}
fixDb();
