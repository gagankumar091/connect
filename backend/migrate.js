require('dotenv').config();
const mysql = require('mysql2/promise');

async function migrate() {
    console.log("Connecting to Aiven (Source)...");
    const sourcePool = mysql.createPool({
        uri: process.env.DATABASE_URL,
        ssl: { rejectUnauthorized: false }
    });

    console.log("Connecting to Rocky Linux (Destination)...");
    const destPool = mysql.createPool({
        host: '51.79.143.65',
        user: 'mitron_user',
        password: 'MitronSecure2026!',
        database: 'mitron_db',
        port: 3306
    });

    try {
        await destPool.query(`SET sql_mode='ANSI_QUOTES'`);
        const tables = ['users', 'companies', 'events', 'chats', 'messages', 'notifications'];
        
        for (const table of tables) {
            console.log(`Migrating table: ${table}`);
            
            // 1. Get CREATE TABLE statement
            const [createRow] = await sourcePool.query(`SHOW CREATE TABLE ${table}`);
            let createSql = createRow[0]['Create Table'];
            // Remove AUTO_INCREMENT if present to keep identical IDs
            createSql = createSql.replace(/AUTO_INCREMENT=\d+/g, '');
            
            // Create table on destination
            await destPool.query(`DROP TABLE IF EXISTS ${table}`);
            await destPool.query(createSql);
            
            // 2. Get Data
            const [rows] = await sourcePool.query(`SELECT * FROM ${table}`);
            if (rows.length > 0) {
                const keys = Object.keys(rows[0]);
                const values = rows.map(r => keys.map(k => r[k]));
                
                // Construct placeholders
                const placeholders = rows.map(() => `(${keys.map(() => '?').join(', ')})`).join(', ');
                const flatValues = values.flat();
                
                const insertSql = `INSERT INTO ${table} (${keys.join(', ')}) VALUES ${placeholders}`;
                await destPool.query(insertSql, flatValues);
            }
            console.log(`- Inserted ${rows.length} rows into ${table}`);
        }
        
        console.log("Migration completed successfully!");
    } catch (e) {
        console.error("Migration failed:", e);
    } finally {
        await sourcePool.end();
        await destPool.end();
    }
}

migrate();
