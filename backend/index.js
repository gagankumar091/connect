const express = require('express');
const mysql = require('mysql2/promise');
const cors = require('cors');
const path = require('path');
const https = require('https');
require('dotenv').config();

const app = express();
app.use(cors());
app.use(express.json());
app.use(express.static(path.join(__dirname, 'public')));

const pool = mysql.createPool({ uri: process.env.DATABASE_URL, waitForConnections: true, connectionLimit: 10 });

// SECURITY HOLE PATCH: API Key Middleware
const API_KEY = "MITRON_SECURE_KEY_2026";
const authenticate = (req, res, next) => {
    const authHeader = req.headers.authorization;
    if (!authHeader || authHeader !== `Bearer ${API_KEY}`) {
        return res.status(401).json({ error: "Unauthorized. Invalid API Key." });
    }
    next();
};

app.get('/', (req, res) => { res.sendFile(path.join(__dirname, 'public', 'index.html')); });

// Android API Endpoints (Secured)
app.get('/api/users', authenticate, async (req, res) => {
    try { 
        const { lat, lng } = req.query;
        let query = 'SELECT * FROM users';
        let params = [];
        if (lat && lng) {
            query = 'SELECT *, ( 6371 * acos( cos( radians(?) ) * cos( radians( lat ) ) * cos( radians( lng ) - radians(?) ) + sin( radians(?) ) * sin( radians( lat ) ) ) ) AS distance FROM users ORDER BY distance';
            params = [lat, lng, lat];
        }
        const [rows] = await pool.query(query, params);
        res.json(rows); 
    } catch(e) { res.status(500).json({error: e.message}) }
});

app.get('/api/companies', authenticate, async (req, res) => {
    try { const [rows] = await pool.query('SELECT * FROM companies'); res.json(rows); } 
    catch(e) { res.status(500).json({error: e.message}) }
});

app.get('/api/events', authenticate, async (req, res) => {
    try { 
        const { lat, lng } = req.query;
        let query = 'SELECT * FROM events';
        let params = [];
        if (lat && lng) {
            query = 'SELECT *, ( 6371 * acos( cos( radians(?) ) * cos( radians( lat ) ) * cos( radians( lng ) - radians(?) ) + sin( radians(?) ) * sin( radians( lat ) ) ) ) AS distance FROM events ORDER BY distance';
            params = [lat, lng, lat];
        }
        const [rows] = await pool.query(query, params);
        const mappedRows = rows.map(r => ({
            ...r,
            attendee_ids: r.attendee_ids ? r.attendee_ids.split(',') : []
        }));
        res.json(mappedRows); 
    } catch(e) { res.status(500).json({error: e.message}) }
});

app.post('/api/events', authenticate, async (req, res) => {
    try {
        const { title, location, date, description, lat, lng, attendees } = req.body;
        const id = 'evt_' + Date.now();
        const attendeeStr = (attendees && Array.isArray(attendees)) ? attendees.join(',') : null;
        await pool.query('INSERT INTO events (id, title, location, description, date, lat, lng, attendee_ids) VALUES (?, ?, ?, ?, ?, ?, ?, ?)',
            [id, title, location, description, date, lat, lng, attendeeStr]);
        res.json({ success: true, id });
    } catch(e) { res.status(500).json({ success: false, error: e.message }) }
});

app.get('/api/chats/:userId', authenticate, async (req, res) => {
    try {
        const userId = req.params.userId;
        const [rows] = await pool.query(`SELECT c.*, u.name as contact_name, u.company as contact_company FROM chats c JOIN users u ON c.contact_id = u.id WHERE c.user_id = ?`, [userId]);
        const mappedRows = await Promise.all(rows.map(async (r) => {
            const [unreadRows] = await pool.query('SELECT COUNT(*) as cnt FROM messages WHERE chat_id = ? AND sender_id != ? AND is_read = FALSE', [r.id, userId]);
            const unreadCount = unreadRows[0].cnt;
            return {
                id: r.id,
                contactId: r.contact_id,
                initials: (r.contact_name || '').substring(0, 2).toUpperCase(),
                name: r.contact_name,
                company: r.contact_company || 'No Company',
                lastMessage: r.last_message || '',
                lastMessageTime: r.updated_at ? r.updated_at.toISOString() : r.timestamp ? r.timestamp.toISOString() : new Date().toISOString(),
                unreadCount: unreadCount,
                scoreLabel: '90/100',
                color: 'PRO',
                overdue: false
            };
        }));
        res.json(mappedRows);
    } catch(e) { res.status(500).json({error: e.message}) }
});

app.get('/api/chats/:chatId/messages', authenticate, async (req, res) => {
    try {
        const userId = req.query.userId;
        const [rows] = await pool.query(`SELECT * FROM messages WHERE chat_id = ? ORDER BY created_at ASC`, [req.params.chatId]);
        const mappedRows = rows.map(r => ({
            id: r.id,
            chat_id: r.chat_id,
            content: r.text,
            from_user: r.sender_id === userId,
            is_ai_labeled: false,
            is_read: r.is_read == 1 ? 1 : 0,
            created_at: r.created_at ? r.created_at.toISOString() : new Date().toISOString()
        }));
        res.json(mappedRows);
    } catch(e) { res.status(500).json({error: e.message}) }
});

app.post('/api/chats/:chatId/messages', authenticate, async (req, res) => {
    try {
        const { senderId, text } = req.body;
        const chatId = req.params.chatId;
        const id = 'msg_' + Date.now();
        const now = new Date();
        await pool.query('INSERT INTO messages (id, chat_id, sender_id, text, is_read, created_at) VALUES (?, ?, ?, ?, ?, ?)', [id, chatId, senderId, text, false, now]);
        await pool.query('UPDATE chats SET last_message = ?, updated_at = ? WHERE id = ?', [text, now, chatId]);
        
        // Find recipient ID for this chat to trigger a notification
        const [chats] = await pool.query('SELECT contact_id, user_id FROM chats WHERE id = ?', [chatId]);
        const chat = chats[0];
        if (chat) {
            const receiverId = chat.user_id === senderId ? chat.contact_id : chat.user_id;
            
            const [users] = await pool.query('SELECT name FROM users WHERE id = ?', [senderId]);
            const senderName = users.length > 0 ? users[0].name : 'Someone';
            
            const notifId = 'notif_' + Date.now();
            await pool.query('INSERT INTO notifications (id, user_id, title, description, type, action_id, is_read) VALUES (?, ?, ?, ?, ?, ?, ?)', 
                [notifId, receiverId, `New Message from ${senderName}`, text.substring(0, 50) + (text.length > 50 ? '...' : ''), 'NEW_MESSAGE', chatId, false]);
            
            sendFcmPush(receiverId, `New Message from ${senderName}`, text.substring(0, 100), { type: 'chat', chatId: chatId, action_id: chatId });
        }

        res.json({ success: true, id, chat_id: chatId, content: text, from_user: true, created_at: now.toISOString(), is_read: 0 });
    } catch(e) { res.status(500).json({ error: e.message }); }
});

app.put('/api/users/:id', authenticate, async (req, res) => {
    try {
        const { username, email, name, title, company, linkedin, website } = req.body;
        await pool.query(
            'UPDATE users SET username = ?, email = ?, name = ?, title = ?, company = ?, linkedin = ?, website = ? WHERE id = ?',
            [username, email, name, title, company, linkedin, website, req.params.id]
        );
        res.json({ success: true });
    } catch (e) { res.status(500).json({ error: e.message }); }
});

// MISSING DATA HOLES PATCHED:
app.get('/api/timeline/:contactId', authenticate, async (req, res) => {
    try { const [rows] = await pool.query('SELECT * FROM timeline_events WHERE contact_id = ?', [req.params.contactId]); res.json(rows); } 
    catch(e) { res.status(500).json({error: e.message}) }
});

app.get('/api/briefings', authenticate, async (req, res) => {
    try { const [rows] = await pool.query('SELECT * FROM briefing_items'); res.json(rows); } 
    catch(e) { res.status(500).json({error: e.message}) }
});

app.delete('/api/crud/:type/:id', authenticate, async (req, res) => {
    try {
        const { type, id } = req.params;
        const validTypes = ['users', 'companies', 'events'];
        if (validTypes.includes(type)) {
            await pool.query(`DELETE FROM ${type} WHERE id = ?`, [id]);
        }
        res.json({ success: true });
    } catch(e) { res.status(500).json({ error: e.message }); }
});

app.post('/api/crud/companies', authenticate, async (req, res) => {
    try {
        const { name, descriptor, employees } = req.body;
        const id = 'comp_' + Date.now();
        await pool.query('INSERT INTO companies (id, name, descriptor, employees) VALUES (?, ?, ?, ?)', [id, name, descriptor, employees || 0]);
        res.json({ success: true, id });
    } catch(e) { res.status(500).json({ success: false, error: e.message }) }
});

app.post('/api/crud/users', authenticate, async (req, res) => {
    try {
        const { name, email, company, title } = req.body;
        const id = 'user_' + Date.now();
        const username = email;
        const password = 'password123'; // Default password for admin-created users
        await pool.query('INSERT INTO users (id, username, password, name, email, company, title) VALUES (?, ?, ?, ?, ?, ?, ?)', 
            [id, username, password, name, email, company, title]);
        res.json({ success: true, id });
    } catch(e) { res.status(500).json({ success: false, error: e.message }) }
});

// FCM Push Notification Helper
const FCM_SERVER_KEY = process.env.FCM_SERVER_KEY || "";

function sendFcmPush(userId, title, body, data) {
    return new Promise(async (resolve) => {
        if (!FCM_SERVER_KEY) { resolve(); return; }
        try {
            const [rows] = await pool.query('SELECT fcm_token FROM users WHERE id = ? AND fcm_token IS NOT NULL', [userId]);
            if (rows.length === 0 || !rows[0].fcm_token) { resolve(); return; }
            const token = rows[0].fcm_token;
            const message = {
                to: token,
                notification: { title, body },
                data: data || {},
                priority: "high"
            };
            const postData = JSON.stringify(message);
            const options = {
                hostname: 'fcm.googleapis.com',
                path: '/fcm/send',
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': 'key=' + FCM_SERVER_KEY,
                    'Content-Length': Buffer.byteLength(postData)
                }
            };
            const req = https.request(options, (res) => { resolve(); });
            req.on('error', () => { resolve(); });
            req.write(postData);
            req.end();
        } catch(e) { resolve(); }
    });
}

// AI Briefing: Get user profile with role context
app.get('/api/user-profile/:userId', authenticate, async (req, res) => {
    try {
        const [rows] = await pool.query('SELECT id, name, title, company, email FROM users WHERE id = ?', [req.params.userId]);
        if (rows.length === 0) return res.status(404).json({ error: "User not found" });
        res.json(rows[0]);
    } catch(e) { res.status(500).json({error: e.message}) }
});

// AI Briefing: Get recent companies matching user's industry
app.get('/api/recent-companies', authenticate, async (req, res) => {
    try {
        const userId = req.query.userId;
        const [users] = await pool.query('SELECT company, title FROM users WHERE id = ?', [userId]);
        if (users.length === 0) return res.json([]);
        const userCompany = users[0].company || '';
        const userTitle = users[0].title || '';
        const keywords = [userCompany, userTitle].filter(Boolean).join(' ');

        let query = 'SELECT * FROM companies';
        let params = [];
        if (keywords) {
            const terms = keywords.split(' ').filter(t => t.length > 2);
            if (terms.length > 0) {
                const conditions = terms.map(() => 'name LIKE ? OR descriptor LIKE ?').join(' OR ');
                query += ' WHERE ' + conditions;
                params = terms.flatMap(t => [`%${t}%`, `%${t}%`]);
            }
        }
        query += ' ORDER BY id DESC LIMIT 5';
        const [rows] = await pool.query(query, params);
        res.json(rows);
    } catch(e) { res.status(500).json({error: e.message}) }
});

const PORT = process.env.PORT || 3000;
if (require.main === module) { app.listen(PORT, () => console.log(`Server running on ${PORT}`)); }
module.exports = app;

// AUTHENTICATION ENDPOINTS
app.post('/api/register', async (req, res) => {
    try {
        const { username, password, name, email } = req.body;
        const id = 'user_' + Date.now();
        await pool.query('INSERT INTO users (id, username, password, name, email) VALUES (?, ?, ?, ?, ?)', [id, username, password, name, email]);
        res.json({ success: true, userId: id });
    } catch(e) { 
        console.error(e);
        res.status(500).json({ success: false, message: e.message }); 
    }
});

app.post('/api/login', async (req, res) => {
    try {
        const { usernameOrEmail, password } = req.body;
        const [rows] = await pool.query('SELECT id, password FROM users WHERE username = ? OR email = ?', [usernameOrEmail, usernameOrEmail]);
        if (rows.length > 0 && rows[0].password === password) {
            res.json({ success: true, userId: rows[0].id });
        } else {
            res.status(401).json({ success: false, message: 'Invalid credentials' });
        }
    } catch(e) { 
        res.status(500).json({ success: false, message: e.message }); 
    }
});

app.post('/api/messages/read', authenticate, async (req, res) => {
    try {
        const { chatId, userId } = req.body;
        await pool.query('UPDATE messages SET is_read = TRUE WHERE chat_id = ? AND sender_id != ? AND is_read = FALSE', [chatId, userId]);
        res.json({ success: true });
    } catch(e) { res.status(500).json({ error: e.message }); }
});

app.get('/api/notifications/:userId', authenticate, async (req, res) => {
    try { const [rows] = await pool.query('SELECT * FROM notifications WHERE user_id = ? ORDER BY created_at DESC', [req.params.userId]); res.json(rows); } 
    catch(e) { res.status(500).json({error: e.message}) }
});
app.post('/api/notifications/:id/read', authenticate, async (req, res) => {
    try { await pool.query('UPDATE notifications SET is_read = TRUE WHERE id = ?', [req.params.id]); res.json({success: true}); } 
    catch(e) { res.status(500).json({error: e.message}) }
});

app.post('/api/users/fcm-token', authenticate, async (req, res) => {
    try {
        const { userId, token } = req.body;
        await pool.query('UPDATE users SET fcm_token = ? WHERE id = ?', [token, userId]);
        res.json({ success: true });
    } catch(e) { res.status(500).json({ error: e.message }); }
});

app.post('/api/users/location', authenticate, async (req, res) => {
    try {
        const { userId, lat, lng } = req.body;
        await pool.query('UPDATE users SET lat = ?, lng = ? WHERE id = ?', [lat, lng, userId]);
        res.json({ success: true });
    } catch(e) { res.status(500).json({ error: e.message }); }
});

app.post('/api/connections', authenticate, async (req, res) => {
    try {
        const { senderId, receiverId } = req.body;
        const id = 'conn_' + Date.now();
        await pool.query('INSERT INTO connections (id, sender_id, receiver_id) VALUES (?, ?, ?)', [id, senderId, receiverId]);
        
        // Fetch sender details to personalize notification
        const [users] = await pool.query('SELECT name FROM users WHERE id = ?', [senderId]);
        const senderName = users.length > 0 ? users[0].name : 'Someone';
        
        const notifId = 'notif_' + Date.now();
        await pool.query('INSERT INTO notifications (id, user_id, title, description, type, action_id) VALUES (?, ?, ?, ?, ?, ?)', 
            [notifId, receiverId, 'New Connection Request', `${senderName} wants to connect with you.`, 'CONNECTION_REQUEST', senderId]);
        
        sendFcmPush(receiverId, 'New Connection Request', `${senderName} wants to connect with you.`, { type: 'connection', action_id: senderId });
        
        res.json({ success: true });
    } catch(e) { res.status(500).json({ error: e.message }); }
});

app.post('/api/connections/reject', authenticate, async (req, res) => {
    try {
        const { senderId, receiverId } = req.body;
        await pool.query("UPDATE connections SET status = 'REJECTED' WHERE sender_id = ? AND receiver_id = ?", [senderId, receiverId]);
        res.json({ success: true });
    } catch(e) { res.status(500).json({ error: e.message }); }
});

app.post('/api/connections/accept', authenticate, async (req, res) => {
    try {
        const { senderId, receiverId } = req.body;
        // Update connection status
        await pool.query("UPDATE connections SET status = 'ACCEPTED' WHERE sender_id = ? AND receiver_id = ?", [senderId, receiverId]);
        
        // Create chat
        const chatId = 'chat_' + Date.now();
        await pool.query('INSERT INTO chats (id, user_id, contact_id, last_message, updated_at) VALUES (?, ?, ?, ?, ?)', 
            [chatId, receiverId, senderId, 'Connection accepted', new Date()]);
        
        const chatId2 = 'chat_' + (Date.now() + 1);
        await pool.query('INSERT INTO chats (id, user_id, contact_id, last_message, updated_at) VALUES (?, ?, ?, ?, ?)', 
            [chatId2, senderId, receiverId, 'Connection accepted', new Date()]);
            
        // Notify sender
        const notifId = 'notif_' + Date.now();
        await pool.query('INSERT INTO notifications (id, user_id, title, description, type, action_id) VALUES (?, ?, ?, ?, ?, ?)', 
            [notifId, senderId, 'Connection Accepted', 'Someone accepted your connection request.', 'NEW_MESSAGE', receiverId]);
        
        sendFcmPush(senderId, 'Connection Accepted', 'Your connection request was accepted!', { type: 'chat', chatId: chatId2, action_id: chatId2 });
            
        res.json({ success: true });
    } catch(e) { res.status(500).json({ error: e.message }); }
});
