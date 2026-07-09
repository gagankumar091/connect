require('dotenv').config();
const mysql = require('mysql2/promise');

async function run() {
    const pool = mysql.createPool(process.env.DATABASE_URL);
    try {
        await pool.query('ALTER TABLE users ADD COLUMN phone VARCHAR(50)');
        console.log("Added phone column");
    } catch (e) {
        if(e.code === 'ER_DUP_FIELDNAME') console.log("Column already exists");
        else console.error(e);
    }
    process.exit();
}
run();
