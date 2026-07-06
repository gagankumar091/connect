const mysql = require('mysql2/promise');
require('dotenv').config();

async function runFullMigration() {
    try {
        const pool = await mysql.createConnection(process.env.DATABASE_URL);
        console.log("=== MITRON FULL DB MIGRATION ===");

        // 1. USERS - add missing columns
        console.log("1/9: Migrating users table...");
        await pool.query(`ALTER TABLE users ADD COLUMN IF NOT EXISTS fcm_token TEXT`);
        await pool.query(`ALTER TABLE users ADD COLUMN IF NOT EXISTS linkedin VARCHAR(255)`);
        await pool.query(`ALTER TABLE users ADD COLUMN IF NOT EXISTS website VARCHAR(255)`);
        console.log("   -> Added fcm_token, linkedin, website columns");

        // 2. COMPANIES - create if not exists
        console.log("2/9: Ensuring companies table...");
        await pool.query(`CREATE TABLE IF NOT EXISTS companies (
            id VARCHAR(36) PRIMARY KEY,
            name VARCHAR(255) NOT NULL,
            descriptor VARCHAR(255),
            funding VARCHAR(255),
            open_deals INT DEFAULT 0,
            recent_news TEXT,
            employees INT DEFAULT 0,
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        )`);

        // 3. EVENTS - add missing columns
        console.log("3/9: Migrating events table...");
        await pool.query(`ALTER TABLE events ADD COLUMN IF NOT EXISTS attendee_ids TEXT`);
        await pool.query(`ALTER TABLE events ADD COLUMN IF NOT EXISTS lat DOUBLE`);
        await pool.query(`ALTER TABLE events ADD COLUMN IF NOT EXISTS lng DOUBLE`);

        // 4. CHATS - add updated_at
        console.log("4/9: Migrating chats table...");
        await pool.query(`ALTER TABLE chats ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP`);

        // 5. MESSAGES - add is_read
        console.log("5/9: Migrating messages table...");
        await pool.query(`ALTER TABLE messages ADD COLUMN IF NOT EXISTS is_read BOOLEAN DEFAULT FALSE`);

        // 6. NOTIFICATIONS - ensure exists
        console.log("6/9: Ensuring notifications table...");
        await pool.query(`CREATE TABLE IF NOT EXISTS notifications (
            id VARCHAR(36) PRIMARY KEY,
            user_id VARCHAR(36),
            title VARCHAR(255),
            description TEXT,
            type VARCHAR(50),
            action_id VARCHAR(36),
            is_read BOOLEAN DEFAULT FALSE,
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        )`);

        // 7. CONNECTIONS - ensure exists
        console.log("7/9: Ensuring connections table...");
        await pool.query(`CREATE TABLE IF NOT EXISTS connections (
            id VARCHAR(36) PRIMARY KEY,
            sender_id VARCHAR(36),
            receiver_id VARCHAR(36),
            status VARCHAR(50) DEFAULT 'PENDING',
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        )`);

        // 8. TIMELINE_EVENTS - create if not exists
        console.log("8/9: Ensuring timeline_events table...");
        await pool.query(`CREATE TABLE IF NOT EXISTS timeline_events (
            id VARCHAR(36) PRIMARY KEY,
            contact_id VARCHAR(255),
            user_id VARCHAR(36),
            title VARCHAR(255),
            description TEXT,
            icon_name VARCHAR(50),
            is_meeting BOOLEAN DEFAULT FALSE,
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        )`);

        // 9. BRIEFING_ITEMS - create if not exists
        console.log("9/9: Ensuring briefing_items table...");
        await pool.query(`CREATE TABLE IF NOT EXISTS briefing_items (
            id VARCHAR(36) PRIMARY KEY,
            user_id VARCHAR(36),
            title VARCHAR(255),
            description TEXT,
            type VARCHAR(50),
            priority INT DEFAULT 0,
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        )`);

        // Seed default briefing items if table is empty
        const [count] = await pool.query('SELECT COUNT(*) as cnt FROM briefing_items');
        if (count[0].cnt === 0) {
            await pool.query(`INSERT INTO briefing_items (id, title, description, type, priority) VALUES
                ('brief_default_1', 'Follow up with a contact', 'Reach out to someone you haven\'t connected with recently.', 'task', 1),
                ('brief_default_2', 'Review new companies', 'Check out recently added companies in your network.', 'task', 2),
                ('brief_default_3', 'Attend a nearby event', 'Find and register for professional events near you.', 'task', 3)
            `);
            console.log("   -> Seeded default briefing items");
        }

        console.log("\n=== MIGRATION COMPLETE ===");
        await pool.end();
        process.exit(0);
    } catch (error) {
        console.error("Migration failed:", error);
        process.exit(1);
    }
}

runFullMigration();
