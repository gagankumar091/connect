const mysql = require('mysql2/promise');
require('dotenv').config();

async function setupDatabase() {
    try {
        const connection = await mysql.createConnection(process.env.DATABASE_URL);
        console.log("Building full Mitron schema (Auth Patched)...");
        
        await connection.query(`DROP TABLE IF EXISTS users`);
        await connection.query(`CREATE TABLE users (id VARCHAR(36) PRIMARY KEY, username VARCHAR(255) UNIQUE, password VARCHAR(255), name VARCHAR(255), email VARCHAR(255), title VARCHAR(255), company VARCHAR(255), score INT DEFAULT 100, status VARCHAR(50) DEFAULT 'Online', lat DOUBLE, lng DOUBLE, last_login TIMESTAMP DEFAULT CURRENT_TIMESTAMP);`);
        
        // Seed Data with passwords
        await connection.query(`INSERT INTO users (id, username, password, name, email, title, company, score, lat, lng) VALUES 
            ('user_gagan', 'gagan', 'password123', 'Gagan Mitron', 'gagan@mitron.app', 'Admin', 'Mitron', 100, 37.7749, -122.4194),
            ('user_stark', 'tony', 'password123', 'Tony Stark', 'tony@stark.com', 'CEO', 'Stark Industries', 95, 37.7750, -122.4180)`);
            
        await connection.end();
        console.log("Database Auth Patched Successfully!");
    } catch (error) { console.error("Failed:", error); }
}
setupDatabase();
