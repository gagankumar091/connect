const mysql = require('mysql2/promise');
async function migrate() {
    const pool = mysql.createPool({ 
        host: '51.79.143.65', user: 'mitron_user', password: 'MitronSecure2026!', database: 'mitron_db'
    });
    try { await pool.query('ALTER TABLE users MODIFY COLUMN password VARCHAR(255)'); } catch(e) {}
    console.log("Done"); process.exit(0);
}
migrate();
