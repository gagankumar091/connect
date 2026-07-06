const mysql = require('mysql2/promise');
require('dotenv').config();

async function run() {
    try {
        const pool = await mysql.createConnection(process.env.DATABASE_URL);
        try {
            await pool.query('ALTER TABLE events ADD COLUMN attendee_ids TEXT;');
            console.log("Column attendee_ids added to events.");
        } catch(e) {
            console.log("Column might already exist or error: ", e.message);
        }
        await pool.end();
    } catch(e) {
        console.error(e);
    }
}
run();
