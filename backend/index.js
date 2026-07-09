const express = require('express');
const mysql = require('mysql2/promise');
const cors = require('cors');
const path = require('path');
const https = require('https');
const { RtcTokenBuilder, RtcRole } = require('agora-token');
require('dotenv').config();

const app = express();
app.use(cors());
app.use(express.json({ limit: '10mb' }));
app.use(express.urlencoded({ limit: '10mb', extended: true }));
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
app.get('/api/version', (req, res) => { res.json({ version: '1.0.1_avatar' }); });

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
    } catch (e) { res.status(500).json({ error: e.message }) }
});

app.get('/api/companies', authenticate, async (req, res) => {
    try { const [rows] = await pool.query('SELECT * FROM companies'); res.json(rows); }
    catch (e) { res.status(500).json({ error: e.message }) }
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
    } catch (e) { res.status(500).json({ error: e.message }) }
});

app.post('/api/events', authenticate, async (req, res) => {
    try {
        const { title, location, date, description, lat, lng, attendees } = req.body;
        const id = 'evt_' + Date.now();
        const attendeeStr = (attendees && Array.isArray(attendees)) ? attendees.join(',') : null;
        await pool.query('INSERT INTO events (id, title, location, description, date, lat, lng, attendee_ids) VALUES (?, ?, ?, ?, ?, ?, ?, ?)',
            [id, title, location, description, date, lat, lng, attendeeStr]);
        res.json({ success: true, id });
    } catch (e) { res.status(500).json({ success: false, error: e.message }) }
});

app.get('/api/chats/:userId', authenticate, async (req, res) => {
    try {
        const userId = req.params.userId;
        const [rows] = await pool.query(`SELECT c.*, u.id as actual_contact_id, u.name as contact_name, u.company as contact_company, u.score as contact_score FROM chats c JOIN users u ON (c.contact_id = u.id AND c.user_id = ?) OR (c.user_id = u.id AND c.contact_id = ?) WHERE c.user_id = ? OR c.contact_id = ?`, [userId, userId, userId, userId]);
        const mappedRows = await Promise.all(rows.map(async (r) => {
            const [unreadRows] = await pool.query('SELECT COUNT(*) as cnt FROM messages WHERE chat_id = ? AND sender_id != ? AND is_read = FALSE', [r.id, userId]);
            const unreadCount = unreadRows[0].cnt;
            return {
                id: r.id,
                contactId: r.actual_contact_id,
                initials: (r.contact_name || '').substring(0, 2).toUpperCase(),
                name: r.contact_name,
                company: r.contact_company || 'No Company',
                lastMessage: r.last_message || '',
                lastMessageTime: r.updated_at ? r.updated_at.toISOString() : r.timestamp ? r.timestamp.toISOString() : new Date().toISOString(),
                unreadCount: unreadCount,
                scoreLabel: (r.contact_score || '0').toString(),
                color: 'PRO',
                overdue: false
            };
        }));
        res.json(mappedRows);
    } catch (e) { res.status(500).json({ error: e.message }) }
});


// SSE Chat Stream
const clients = new Map();
app.get('/api/chats/:chatId/stream', authenticate, (req, res) => {
    const chatId = req.params.chatId;
    res.setHeader('Content-Type', 'text/event-stream');
    res.setHeader('Cache-Control', 'no-cache');
    res.setHeader('Connection', 'keep-alive');
    res.flushHeaders();

    if (!clients.has(chatId)) clients.set(chatId, new Set());
    clients.get(chatId).add(res);

    req.on('close', () => {
        clients.get(chatId).delete(res);
        if (clients.get(chatId).size === 0) clients.delete(chatId);
    });
});

function broadcastToChat(chatId, event, data) {
    if (clients.has(chatId)) {
        for (const res of clients.get(chatId)) {
            res.write(`event: ${event}\ndata: ${JSON.stringify(data)}\n\n`);
        }
    }
}

app.get('/api/chats/:chatId/messages', authenticate, async (req, res) => {
    try {
        const userId = req.query.userId;
        const [rows] = await pool.query(`SELECT * FROM messages WHERE chat_id = ? ORDER BY created_at ASC`, [req.params.chatId]);
        const mappedRows = rows.map(r => ({
            id: r.id,
            chat_id: r.chat_id,
            content: r.text,
            sender_id: r.sender_id,
            from_user: r.sender_id === userId,
            is_ai_labeled: false,
            is_read: r.is_read == 1 ? 1 : 0,
            created_at: r.created_at ? r.created_at.toISOString() : new Date().toISOString()
        }));
        res.json(mappedRows);
    } catch (e) { res.status(500).json({ error: e.message }) }
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

        // Broadcast includes sender_id so each client can compute from_user themselves
        broadcastToChat(chatId, 'new_message', { id, chat_id: chatId, content: text, sender_id: senderId, created_at: now.toISOString(), is_read: 0 });
        // REST response also includes sender_id — Android app will compute from_user based on currentUserId
        res.json({ success: true, id, chat_id: chatId, content: text, sender_id: senderId, created_at: now.toISOString(), is_read: 0 });
    } catch (e) { res.status(500).json({ error: e.message }); }
});


app.get('/api/user-profile/:id', authenticate, async (req, res) => {
    try {
        const [rows] = await pool.query('SELECT * FROM users WHERE id = ?', [req.params.id]);
        if (rows.length === 0) return res.status(404).json({ error: 'User not found' });
        res.json(rows[0]);
    } catch (e) {
        res.status(500).json({ error: e.message });
    }
});

app.put('/api/users/:id', authenticate, async (req, res) => {
    try {
        const { username, email, name, title, company, linkedin, website, avatar_url } = req.body;
        await pool.query(
            'UPDATE users SET username = ?, email = ?, name = ?, title = ?, company = ?, linkedin = ?, website = ?, avatar_url = ? WHERE id = ?',
            [username, email, name, title, company, linkedin, website, avatar_url, req.params.id]
        );
        res.json({ success: true });
    } catch (e) { res.status(500).json({ error: e.message }); }
});

// MISSING DATA HOLES PATCHED:

app.post('/api/timeline', authenticate, async (req, res) => {
    try {
        const { id, contact_id, content, subtitle, icon, color, is_meeting } = req.body;
        await pool.query(
            'INSERT INTO timeline_events (id, contact_id, content, subtitle, icon, color, is_meeting) VALUES (?, ?, ?, ?, ?, ?, ?)',
            [id, contact_id, content, subtitle, icon, color, is_meeting]
        );
        res.json({ success: true, id });
    } catch (e) { res.status(500).json({ error: e.message }); }
});

app.post('/api/meeting-summaries', authenticate, async (req, res) => {
    try {
        const { id, contact_id, summary, pain_point, budget, action_items } = req.body;
        await pool.query(
            'INSERT INTO meeting_summaries (id, contact_id, summary, pain_point, budget, action_items) VALUES (?, ?, ?, ?, ?, ?)',
            [id, contact_id, summary, pain_point, budget, action_items]
        );
        res.json({ success: true, id });
    } catch (e) { res.status(500).json({ error: e.message }); }
});

app.get('/api/timeline/:contactId', authenticate, async (req, res) => {
    try { const [rows] = await pool.query('SELECT * FROM timeline_events WHERE contact_id = ?', [req.params.contactId]); res.json(rows); }
    catch (e) { res.status(500).json({ error: e.message }) }
});

app.get('/api/briefings', authenticate, async (req, res) => {
    try { const [rows] = await pool.query('SELECT * FROM briefing_items'); res.json(rows); }
    catch (e) { res.status(500).json({ error: e.message }) }
});

app.delete('/api/crud/:type/:id', authenticate, async (req, res) => {
    try {
        const { type, id } = req.params;
        const validTypes = ['users', 'companies', 'events'];
        if (validTypes.includes(type)) {
            await pool.query(`DELETE FROM ${type} WHERE id = ?`, [id]);
        }
        res.json({ success: true });
    } catch (e) { res.status(500).json({ error: e.message }); }
});

app.post('/api/crud/companies', authenticate, async (req, res) => {
    try {
        const { name, descriptor, employees } = req.body;
        const id = 'comp_' + Date.now();
        await pool.query('INSERT INTO companies (id, name, descriptor, employees) VALUES (?, ?, ?, ?)', [id, name, descriptor, employees || 0]);
        res.json({ success: true, id });
    } catch (e) { res.status(500).json({ success: false, error: e.message }) }
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
    } catch (e) { res.status(500).json({ success: false, error: e.message }) }
});

// FCM Push Notification Helper

// User Global Stream (Replaces FCM)
const userClients = new Map();
app.get('/api/users/:userId/stream', authenticate, (req, res) => {
    const userId = req.params.userId;
    res.setHeader('Content-Type', 'text/event-stream');
    res.setHeader('Cache-Control', 'no-cache');
    res.setHeader('Connection', 'keep-alive');
    res.flushHeaders();

    if (!userClients.has(userId)) userClients.set(userId, new Set());
    userClients.get(userId).add(res);

    req.on('close', () => {
        userClients.get(userId).delete(res);
        if (userClients.get(userId).size === 0) userClients.delete(userId);
    });
});

function sendToUser(userId, event, data) {
    if (userClients.has(userId)) {
        for (const res of userClients.get(userId)) {
            res.write(`event: ${event}\ndata: ${JSON.stringify(data)}\n\n`);
        }
    }
}

const FCM_SERVER_KEY = process.env.FCM_SERVER_KEY || "";

function sendFcmPush(userId, title, body, data) {
    return new Promise((resolve) => {
        sendToUser(userId, 'notification', { title, body, data: data || {} });
        resolve();
    });
}

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
    } catch (e) { res.status(500).json({ error: e.message }) }
});

const PORT = process.env.PORT || 3000;
if (require.main === module) { app.listen(PORT, () => console.log(`Server running on ${PORT}`)); }
module.exports = app;

// ======= ADDITIONAL ENDPOINTS =======

// Get single event by ID
app.get('/api/events/:id', authenticate, async (req, res) => {
    try {
        const [rows] = await pool.query('SELECT * FROM events WHERE id = ?', [req.params.id]);
        if (rows.length === 0) return res.status(404).json({ error: 'Event not found' });
        const r = rows[0];
        res.json({ ...r, attendee_ids: r.attendee_ids ? r.attendee_ids.split(',') : [] });
    } catch (e) { res.status(500).json({ error: e.message }); }
});

// Join / Attend an event
app.post('/api/events/:id/attend', authenticate, async (req, res) => {
    try {
        const { userId } = req.body;
        const [rows] = await pool.query('SELECT attendee_ids FROM events WHERE id = ?', [req.params.id]);
        if (rows.length === 0) return res.status(404).json({ error: 'Event not found' });
        const existing = rows[0].attendee_ids ? rows[0].attendee_ids.split(',') : [];
        if (!existing.includes(userId)) {
            existing.push(userId);
            await pool.query('UPDATE events SET attendee_ids = ? WHERE id = ?', [existing.join(','), req.params.id]);
        }
        res.json({ success: true });
    } catch (e) { res.status(500).json({ error: e.message }); }
});

// Get single company by ID
app.get('/api/companies/:id', authenticate, async (req, res) => {
    try {
        const [rows] = await pool.query('SELECT * FROM companies WHERE id = ?', [req.params.id]);
        if (rows.length === 0) return res.status(404).json({ error: 'Company not found' });
        res.json(rows[0]);
    } catch (e) { res.status(500).json({ error: e.message }); }
});

// Create company with full profile fields
app.post('/api/companies/create', authenticate, async (req, res) => {
    try {
        const { name, descriptor, employees, avatar_url, website, description, industry, founded, headquarters, employee_range, funding } = req.body;
        const id = 'comp_' + Date.now();
        await pool.query(
            'INSERT INTO companies (id, name, descriptor, employees, avatar_url, website, description, industry, founded, headquarters, employee_range, funding) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)',
            [id, name, descriptor, employees || 0, avatar_url, website, description, industry, founded, headquarters, employee_range, funding]
        );
        res.json({ success: true, id });
    } catch (e) { res.status(500).json({ success: false, error: e.message }); }
});

// Get users by company name
app.get('/api/users/by-company', authenticate, async (req, res) => {
    try {
        const { company } = req.query;
        const [rows] = await pool.query('SELECT * FROM users WHERE company = ?', [company]);
        res.json(rows);
    } catch (e) { res.status(500).json({ error: e.message }); }
});

// Get reminders for user
app.get('/api/reminders/:userId', authenticate, async (req, res) => {
    try {
        // Pull REMINDER type notifications as reminders
        const [rows] = await pool.query(
            "SELECT n.*, u.avatar_url FROM notifications n LEFT JOIN users u ON n.action_id = u.id WHERE n.user_id = ? AND n.type = 'REMINDER' ORDER BY n.created_at DESC",
            [req.params.userId]
        );
        const now = new Date();
        const mapped = rows.map(r => ({
            ...r,
            is_past_due: r.due_date ? new Date(r.due_date) < now : false
        }));
        res.json(mapped);
    } catch (e) { res.status(500).json({ error: e.message }); }
});

// Create a reminder
app.post('/api/reminders', authenticate, async (req, res) => {
    try {
        const { userId, title, description, actionId, dueDate } = req.body;
        const id = 'notif_remind_' + Date.now();
        await pool.query(
            'INSERT INTO notifications (id, user_id, title, description, type, action_id, due_date, is_read) VALUES (?, ?, ?, ?, ?, ?, ?, ?)',
            [id, userId, title, description, 'REMINDER', actionId, dueDate || null, false]
        );
        res.json({ success: true, id });
    } catch (e) { res.status(500).json({ success: false, error: e.message }); }
});

// Compute relationship score dynamically
app.get('/api/score/:contactId', authenticate, async (req, res) => {
    try {
        const userId = req.headers['x-user-id'] || req.query.userId;
        const contactId = req.params.contactId;
        
        // Base score from users table
        const [userRows] = await pool.query('SELECT score, days_since_contact FROM users WHERE id = ?', [contactId]);
        if (userRows.length === 0) return res.status(404).json({ error: 'User not found' });
        
        const baseScore = userRows[0].score || 50;
        const daysSince = userRows[0].days_since_contact || 30;
        
        // Compute deduction: -1 per day stale, max -40
        const stalePenalty = Math.min(40, Math.floor(daysSince * 0.5));
        
        // Count messages between users
        const [msgRows] = await pool.query(
            'SELECT COUNT(*) as cnt FROM messages m JOIN chats c ON m.chat_id = c.id WHERE (c.user_id = ? AND c.contact_id = ?) OR (c.user_id = ? AND c.contact_id = ?)',
            [userId, contactId, contactId, userId]
        );
        const msgBonus = Math.min(20, msgRows[0].cnt * 2);
        
        const finalScore = Math.max(0, Math.min(100, baseScore - stalePenalty + msgBonus));
        const strength = finalScore >= 80 ? 'Strong' : finalScore >= 50 ? 'Good' : 'Weak';
        
        res.json({
            score: finalScore,
            daysSinceContact: daysSince,
            strength,
            lastSpokeLabel: daysSince === 0 ? 'Today' : `${daysSince} days ago`
        });
    } catch (e) { res.status(500).json({ error: e.message }); }
});

// ======= AGORA CALLING ENDPOINTS =======

// Generate a token for a user to join a channel
app.post('/api/calls/token', authenticate, (req, res) => {
    try {
        const { channelName, uid, role } = req.body;
        const appId = process.env.AGORA_APP_ID;
        const appCertificate = process.env.AGORA_APP_CERTIFICATE;
        
        if (!appId || !appCertificate) {
            return res.status(500).json({ error: 'Agora credentials not configured' });
        }

        const expireTime = 3600; // 1 hour
        const currentTime = Math.floor(Date.now() / 1000);
        const privilegeExpireTime = currentTime + expireTime;

        const tokenRole = role === 'publisher' ? RtcRole.PUBLISHER : RtcRole.SUBSCRIBER;
        const token = RtcTokenBuilder.buildTokenWithUid(appId, appCertificate, channelName, uid || 0, tokenRole, expireTime, privilegeExpireTime);
        
        res.json({ success: true, token });
    } catch (e) { res.status(500).json({ success: false, error: e.message }); }
});

// Initiate a call and send SSE notification to receiver
app.post('/api/calls/initiate', authenticate, async (req, res) => {
    try {
        const { callerId, receiverId, isVideo } = req.body;
        const channelName = `call_${callerId}_${receiverId}_${Date.now()}`;
        
        const appId = process.env.AGORA_APP_ID;
        const appCertificate = process.env.AGORA_APP_CERTIFICATE;
        
        if (!appId || !appCertificate) {
            return res.status(500).json({ error: 'Agora credentials not configured' });
        }

        const expireTime = 3600;
        const privilegeExpireTime = Math.floor(Date.now() / 1000) + expireTime;
        
        // Token for caller
        const callerToken = RtcTokenBuilder.buildTokenWithUid(appId, appCertificate, channelName, 0, RtcRole.PUBLISHER, expireTime, privilegeExpireTime);

        // Fetch caller details for the incoming call screen
        const [users] = await pool.query('SELECT name, avatar_url FROM users WHERE id = ?', [callerId]);
        const callerName = users.length > 0 ? users[0].name : 'Someone';
        const callerAvatar = users.length > 0 ? users[0].avatar_url : null;
        
        // Send SSE event to receiver
        sendToUser(receiverId, 'incoming_call', {
            channelName,
            callerId,
            callerName,
            callerAvatar,
            isVideo
        });
        
        res.json({ success: true, channelName, token: callerToken });
    } catch (e) { res.status(500).json({ success: false, error: e.message }); }
});

// AUTHENTICATION ENDPOINTS
app.post('/api/register', async (req, res) => {
    try {
        const { username, password, name, email } = req.body;
        const id = 'user_' + Date.now();
        await pool.query('INSERT INTO users (id, username, password, name, email) VALUES (?, ?, ?, ?, ?)', [id, username, password, name, email]);
        res.json({ success: true, userId: id });
    } catch (e) {
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
    } catch (e) {
        res.status(500).json({ success: false, message: e.message });
    }
});

app.post('/api/messages/read', authenticate, async (req, res) => {
    try {
        const { chatId, userId } = req.body;
        await pool.query('UPDATE messages SET is_read = TRUE WHERE chat_id = ? AND sender_id != ? AND is_read = FALSE', [chatId, userId]);
        res.json({ success: true });
    } catch (e) { res.status(500).json({ error: e.message }); }
});

app.get('/api/notifications/:userId', authenticate, async (req, res) => {
    try { const [rows] = await pool.query('SELECT * FROM notifications WHERE user_id = ? ORDER BY created_at DESC', [req.params.userId]); res.json(rows); }
    catch (e) { res.status(500).json({ error: e.message }) }
});
app.post('/api/notifications/:id/read', authenticate, async (req, res) => {
    try { await pool.query('UPDATE notifications SET is_read = TRUE WHERE id = ?', [req.params.id]); res.json({ success: true }); }
    catch (e) { res.status(500).json({ error: e.message }) }
});

app.post('/api/users/fcm-token', authenticate, async (req, res) => {
    try {
        const { userId, token } = req.body;
        await pool.query('UPDATE users SET fcm_token = ? WHERE id = ?', [token, userId]);
        res.json({ success: true });
    } catch (e) { res.status(500).json({ error: e.message }); }
});

app.post('/api/users/location', authenticate, async (req, res) => {
    try {
        const { userId, lat, lng } = req.body;
        await pool.query('UPDATE users SET lat = ?, lng = ? WHERE id = ?', [lat, lng, userId]);
        res.json({ success: true });
    } catch (e) { res.status(500).json({ error: e.message }); }
});

app.post('/api/connections', authenticate, async (req, res) => {
    try {
        const { senderId, receiverId } = req.body;

        // --- DEDUPLICATION: Check if a connection already exists in either direction ---
        const [existing] = await pool.query(
            `SELECT id, status FROM connections
             WHERE (sender_id = ? AND receiver_id = ?)
                OR (sender_id = ? AND receiver_id = ?)`,
            [senderId, receiverId, receiverId, senderId]
        );

        if (existing.length > 0) {
            // Already sent or already accepted — do not create duplicate
            return res.json({ success: true, alreadyExists: true, status: existing[0].status });
        }

        const id = 'conn_' + Date.now();
        await pool.query('INSERT INTO connections (id, sender_id, receiver_id) VALUES (?, ?, ?)', [id, senderId, receiverId]);

        // Fetch sender details
        const [users] = await pool.query('SELECT name FROM users WHERE id = ?', [senderId]);
        const senderName = users.length > 0 ? users[0].name : 'Someone';

        // --- DEDUPLICATION: Only insert notification if one doesn't already exist for this pair ---
        const [existingNotif] = await pool.query(
            `SELECT id FROM notifications WHERE user_id = ? AND action_id = ? AND type = 'CONNECTION_REQUEST'`,
            [receiverId, senderId]
        );
        if (existingNotif.length === 0) {
            const notifId = 'notif_' + Date.now();
            await pool.query('INSERT INTO notifications (id, user_id, title, description, type, action_id) VALUES (?, ?, ?, ?, ?, ?)',
                [notifId, receiverId, 'New Connection Request', `${senderName} wants to connect with you.`, 'CONNECTION_REQUEST', senderId]);
            sendFcmPush(receiverId, 'New Connection Request', `${senderName} wants to connect with you.`, { type: 'connection', action_id: senderId });
        }

        res.json({ success: true });
    } catch (e) { res.status(500).json({ error: e.message }); }
});

app.post('/api/connections/reject', authenticate, async (req, res) => {
    try {
        const { senderId, receiverId } = req.body;
        await pool.query("UPDATE connections SET status = 'REJECTED' WHERE sender_id = ? AND receiver_id = ?", [senderId, receiverId]);
        res.json({ success: true });
    } catch (e) { res.status(500).json({ error: e.message }); }
});

app.post('/api/connections/accept', authenticate, async (req, res) => {
    try {
        const { senderId, receiverId } = req.body;

        // Update connection status (idempotent)
        await pool.query("UPDATE connections SET status = 'ACCEPTED' WHERE sender_id = ? AND receiver_id = ?", [senderId, receiverId]);

        // --- DEDUPLICATION: Check if a chat already exists between these two users ---
        const [existingChat] = await pool.query(
            `SELECT id FROM chats
             WHERE (user_id = ? AND contact_id = ?)
                OR (user_id = ? AND contact_id = ?)`,
            [receiverId, senderId, senderId, receiverId]
        );

        let chatId;
        if (existingChat.length > 0) {
            // Chat already exists — reuse it, don't create a duplicate
            chatId = existingChat[0].id;
        } else {
            chatId = 'chat_' + Date.now();
            await pool.query('INSERT INTO chats (id, user_id, contact_id, last_message, updated_at) VALUES (?, ?, ?, ?, ?)',
                [chatId, receiverId, senderId, 'Connection accepted', new Date()]);
        }

        // --- DEDUPLICATION: Only notify sender once ---
        const [existingNotif] = await pool.query(
            `SELECT id FROM notifications WHERE user_id = ? AND action_id = ? AND type = 'NEW_MESSAGE'`,
            [senderId, receiverId]
        );
        if (existingNotif.length === 0) {
            const notifId = 'notif_' + Date.now();
            await pool.query('INSERT INTO notifications (id, user_id, title, description, type, action_id) VALUES (?, ?, ?, ?, ?, ?)',
                [notifId, senderId, 'Connection Accepted', 'Your connection request was accepted.', 'NEW_MESSAGE', chatId]);
            sendFcmPush(senderId, 'Connection Accepted', 'Your connection request was accepted!', { type: 'chat', chatId, action_id: chatId });
        }

        // Mark the original connection-request notification as read so it disappears from the list
        await pool.query(
            `UPDATE notifications SET is_read = TRUE WHERE user_id = ? AND action_id = ? AND type = 'CONNECTION_REQUEST'`,
            [receiverId, senderId]
        );

        res.json({ success: true, chatId });
    } catch (e) { res.status(500).json({ error: e.message }); }
});
