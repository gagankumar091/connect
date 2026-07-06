const mysql = require('mysql2/promise');
require('dotenv').config();

async function run() {
    try {
        const pool = await mysql.createConnection(process.env.DATABASE_URL);
        await pool.query(`ALTER TABLE users ADD COLUMN IF NOT EXISTS fcm_token TEXT`);
        console.log("Added fcm_token column to users table");
        await pool.end();
    } catch(e) {
        console.error(e);
    }
}
run();
