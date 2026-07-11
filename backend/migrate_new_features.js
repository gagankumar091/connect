const mysql = require('mysql2/promise');
async function migrate() {
    const pool = mysql.createPool({ 
        host: '51.79.143.65', user: 'mitron_user', password: 'MitronSecure2026!', database: 'mitron_db'
    });
    try { await pool.query('ALTER TABLE users ADD FULLTEXT INDEX ft_users (name, company)'); } catch(e) {}
    try { await pool.query('ALTER TABLE companies ADD FULLTEXT INDEX ft_companies (name, industry)'); } catch(e) {}
    console.log("Done"); process.exit(0);
}
migrate();
