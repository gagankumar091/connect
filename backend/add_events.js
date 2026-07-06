const mysql = require('mysql2/promise');
require('dotenv').config();

async function addEvents() {
    try {
        const connection = await mysql.createConnection(process.env.DATABASE_URL);
        console.log("Adding live events to database...");
        
        // Clear old events first just to be clean
        await connection.query(`DELETE FROM events`);

        const events = [
            {
                id: 'evt_1',
                title: 'Bangalore Tech Summit 2026',
                location: 'Bangalore Palace Grounds',
                description: 'The largest tech gathering in India, featuring AI and Web3 panels.',
                date: 'Oct 15, 2026',
                lat: 13.0035, // Bangalore
                lng: 77.5891
            },
            {
                id: 'evt_2',
                title: 'Startup Mixer India',
                location: 'Koramangala, Bangalore',
                description: 'Meet local founders and investors over coffee.',
                date: 'Oct 18, 2026',
                lat: 12.9352, // Koramangala
                lng: 77.6245
            }
        ];

        for (const ev of events) {
            await connection.query(
                'INSERT INTO events (id, title, location, description, date, lat, lng) VALUES (?, ?, ?, ?, ?, ?, ?)',
                [ev.id, ev.title, ev.location, ev.description, ev.date, ev.lat, ev.lng]
            );
        }
        
        console.log("Successfully added events near your location!");
        await connection.end();
    } catch (error) { console.error("Failed to add events:", error); }
}
addEvents();
