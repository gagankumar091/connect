const mysql = require('mysql2/promise');
require('dotenv').config();

async function migrate() {
    const pool = mysql.createPool({ uri: process.env.DATABASE_URL, waitForConnections: true, connectionLimit: 5 });
    const conn = await pool.getConnection();

    console.log('Running migration: add missing columns...');

    const migrations = [
        // Add due_date to notifications if not exists
        `ALTER TABLE notifications ADD COLUMN IF NOT EXISTS due_date DATETIME NULL`,
        
        // Add avatar_url to notifications if not exists (for reminder contact pic)
        `ALTER TABLE notifications ADD COLUMN IF NOT EXISTS avatar_url VARCHAR(1024) NULL`,
        
        // Add is_past_due computed helper (computed on API side, not stored)
        
        // Ensure companies has all the new profile columns
        `ALTER TABLE companies ADD COLUMN IF NOT EXISTS avatar_url VARCHAR(1024) NULL`,
        `ALTER TABLE companies ADD COLUMN IF NOT EXISTS website VARCHAR(255) NULL`,
        `ALTER TABLE companies ADD COLUMN IF NOT EXISTS description TEXT NULL`,
        `ALTER TABLE companies ADD COLUMN IF NOT EXISTS industry VARCHAR(100) NULL`,
        `ALTER TABLE companies ADD COLUMN IF NOT EXISTS founded VARCHAR(4) NULL`,
        `ALTER TABLE companies ADD COLUMN IF NOT EXISTS headquarters VARCHAR(255) NULL`,
        `ALTER TABLE companies ADD COLUMN IF NOT EXISTS employee_range VARCHAR(50) NULL`,
        
        // Ensure users has score and days_since_contact columns
        `ALTER TABLE users ADD COLUMN IF NOT EXISTS score INT DEFAULT 50`,
        `ALTER TABLE users ADD COLUMN IF NOT EXISTS days_since_contact INT DEFAULT 0`,
        
        // Ensure events has lat/lng for location-based sorting
        `ALTER TABLE events ADD COLUMN IF NOT EXISTS lat DOUBLE NULL`,
        `ALTER TABLE events ADD COLUMN IF NOT EXISTS lng DOUBLE NULL`,
    ];

    for (const sql of migrations) {
        try {
            await conn.query(sql);
            console.log('✅', sql.substring(0, 70) + '...');
        } catch (e) {
            console.warn('⚠️  Skipped (may already exist):', e.message.substring(0, 80));
        }
    }

    await conn.release();
    await pool.end();
    console.log('\nMigration complete!');
}

migrate().catch(console.error);
