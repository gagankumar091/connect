require('dotenv').config();
const mysql = require('mysql2/promise');

async function main() {
    const pool = mysql.createPool({ uri: process.env.DATABASE_URL, waitForConnections: true, connectionLimit: 2 });
    try {
        await pool.query('ALTER TABLE users ADD COLUMN avatar_url LONGTEXT NULL');
        console.log("Added avatar_url column successfully!");
    } catch (e) {
        if (e.code === 'ER_DUP_FIELDNAME') {
            console.log("Column avatar_url already exists, making sure it's LONGTEXT.");
            await pool.query('ALTER TABLE users MODIFY COLUMN avatar_url LONGTEXT NULL');
            console.log("Modified avatar_url to LONGTEXT successfully!");
        } else {
            console.error("Error:", e);
        }
    } finally {
        await pool.end();
    }
}
main();
