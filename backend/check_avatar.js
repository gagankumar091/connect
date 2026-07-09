require('dotenv').config();
const mysql = require('mysql2/promise');

async function check() {
    const pool = mysql.createPool({ uri: process.env.DATABASE_URL, waitForConnections: true, connectionLimit: 2 });
    try {
        const [rows] = await pool.query('SELECT id, username, email, LENGTH(avatar_url) as avatar_length FROM users');
        console.log("Users in DB:", rows);
    } catch (e) {
        console.error("Error:", e);
    } finally {
        await pool.end();
    }
}
check();
