// @ts-nocheck
const express = require('express');
const mysql = require('mysql2/promise');
const cors = require('cors');
const path = require('path');
const { RtcTokenBuilder, RtcRole } = require('agora-token');
const bcrypt = require('bcrypt');
const jwt = require('jsonwebtoken');
const rateLimit = require('express-rate-limit');
const multer = require('multer');
require('dotenv').config();

const app = express();
app.use(cors());
app.use(express.json({ limit: '10mb' }));
app.use(express.urlencoded({ limit: '10mb', extended: true }));
app.use(express.static(path.join(__dirname, 'public')));

const pool = mysql.createPool({ uri: process.env.DATABASE_URL || "mysql://user:pass@localhost:3306/db", waitForConnections: true, connectionLimit: 10 });

// Global Rate Limiter
const apiLimiter = rateLimit({
    windowMs: 15 * 60 * 1000, // 15 minutes
    max: 100, // limit each IP to 100 requests per windowMs
    message: { error: "Too many requests from this IP, please try again after 15 minutes" }
});
app.use('/api/', apiLimiter);

// JWT Middleware
const JWT_SECRET = process.env.JWT_SECRET || 'mitron_jwt_secret_production_2026_change_me';
const JWT_REFRESH_SECRET = process.env.JWT_REFRESH_SECRET || 'mitron_refresh_secret_production_2026_change_me';

const authenticateToken = (req, res, next) => {
    const authHeader = req.headers.authorization;
    if (!authHeader) return res.status(401).json({ error: "Unauthorized. Missing token." });
    const token = authHeader.split(' ')[1];
    jwt.verify(token, JWT_SECRET, (err, user) => {
        if (err) return res.status(403).json({ error: "Forbidden. Invalid or expired token." });
        req.user = user;
        next();
    });
};

app.get('/', (req, res) => { res.sendFile(path.join(__dirname, 'public', 'index.html')); });
app.get('/api/version', (req, res) => { res.json({ version: '2.0.0_jwt_auth' }); });

// ======= AUTHENTICATION ENDPOINTS =======

app.post('/api/register', async (req, res) => {
    try {
        const { username, password, name, email, phone } = req.body;
        const id = 'user_' + Date.now();
        const hashedPassword = await bcrypt.hash(password, 10);
        await pool.query('INSERT INTO users (id, username, password, name, email, phone) VALUES (?, ?, ?, ?, ?, ?)', [id, username, hashedPassword, name, email, phone]);
        
        const payload = { userId: id, username };
        const accessToken = jwt.sign(payload, JWT_SECRET, { expiresIn: '15m' });
        const refreshToken = jwt.sign(payload, JWT_REFRESH_SECRET, { expiresIn: '7d' });
        
        const userObj = { id, username, name, email, phone };
        res.json({ success: true, user: userObj, accessToken, refreshToken });
    } catch (e) {
        console.error(e);
        res.status(500).json({ success: false, message: e.message });
    }
});

app.post('/api/login', async (req, res) => {
    try {
        const { usernameOrEmail, password } = req.body;
        const [rows] = await pool.query('SELECT * FROM users WHERE username = ? OR email = ?', [usernameOrEmail, usernameOrEmail]);
        if (rows.length === 0) return res.status(401).json({ success: false, message: 'Invalid credentials' });
        
        const user = rows[0];
        const match = await bcrypt.compare(password, user.password);
        if (match) {
            const payload = { userId: user.id, username: user.username };
            const accessToken = jwt.sign(payload, JWT_SECRET, { expiresIn: '15m' });
            const refreshToken = jwt.sign(payload, JWT_REFRESH_SECRET, { expiresIn: '7d' });
            
            const userObj = { id: user.id, username: user.username, name: user.name, email: user.email };
            res.json({ success: true, user: userObj, accessToken, refreshToken });
        } else {
            res.status(401).json({ success: false, message: 'Invalid credentials' });
        }
    } catch (e) {
        res.status(500).json({ success: false, message: e.message });
    }
});

app.post('/api/refresh', (req, res) => {
    const { refreshToken } = req.body;
    if (!refreshToken) return res.status(401).json({ error: "Missing refresh token" });
    jwt.verify(refreshToken, JWT_REFRESH_SECRET, (err, user) => {
        if (err) return res.status(403).json({ error: "Invalid refresh token" });
        const accessToken = jwt.sign({ userId: user.userId, username: user.username }, JWT_SECRET, { expiresIn: '15m' });
        res.json({ accessToken });
    });
});

// ======= ANDROID PROTECTED ENDPOINTS =======

app.get('/api/users', authenticateToken, async (req, res) => {
    try {
        const { lat, lng } = req.query;
        let query = 'SELECT id, username, name, email, phone, company, title, avatar_url, score, days_since_contact FROM users';
        let params = [];
        if (lat && lng) {
            query = 'SELECT id, username, name, email, phone, company, title, avatar_url, score, days_since_contact, ( 6371 * acos( cos( radians(?) ) * cos( radians( lat ) ) * cos( radians( lng ) - radians(?) ) + sin( radians(?) ) * sin( radians( lat ) ) ) ) AS distance FROM users ORDER BY distance';
            params = [lat, lng, lat];
        }
        const [rows] = await pool.query(query, params);
        res.json(rows);
    } catch (e) { res.status(500).json({ error: e.message }) }
});

app.get('/api/companies', authenticateToken, async (req, res) => {
    try { const [rows] = await pool.query('SELECT * FROM companies'); res.json(rows); }
    catch (e) { res.status(500).json({ error: e.message }) }
});

app.get('/api/events', authenticateToken, async (req, res) => {
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

// File Upload Config
const storage = multer.diskStorage({
    destination: (req, file, cb) => cb(null, '/tmp/'),
    filename: (req, file, cb) => cb(null, Date.now() + path.extname(file.originalname))
});
const upload = multer({ storage, limits: { fileSize: 5 * 1024 * 1024 } }); // 5MB limit

app.post('/api/chat/upload', authenticateToken, upload.single('file'), (req, res) => {
    if (!req.file) return res.status(400).json({ success: false, error: 'No file uploaded' });
    // In a real app, upload to S3. Here we return a mock URL or the filename.
    const fileUrl = 'https://connect-mitron.vercel.app/uploads/' + req.file.filename;
    res.json({ success: true, fileUrl });
});

// User Global Stream
const userClients = new Map();
app.get('/api/users/:userId/stream', authenticateToken, (req, res) => {
    const userId = req.params.userId;
    res.setHeader('Content-Type', 'text/event-stream');
    res.setHeader('Cache-Control', 'no-cache');
    res.setHeader('Connection', 'keep-alive');
    res.flushHeaders();

    if (!userClients.has(userId)) userClients.set(userId, new Set());
    userClients.get(userId).add(res);

    const keepAlive = setInterval(() => {
        res.write(': keep-alive\n\n');
    }, 15000);

    req.on('close', () => {
        clearInterval(keepAlive);
        if (userClients.has(userId)) {
            userClients.get(userId).delete(res);
            if (userClients.get(userId).size === 0) userClients.delete(userId);
        }
    });
});

function sendToUser(userId, event, data) {
    if (userClients.has(userId)) {
        for (const res of userClients.get(userId)) {
            res.write(`event: ${event}\ndata: ${JSON.stringify(data)}\n\n`);
        }
    }
}

// SSE Chat Stream
const clients = new Map();
app.get('/api/chats/:chatId/stream', authenticateToken, (req, res) => {
    const chatId = req.params.chatId;
    res.setHeader('Content-Type', 'text/event-stream');
    res.setHeader('Cache-Control', 'no-cache');
    res.setHeader('Connection', 'keep-alive');
    res.flushHeaders();

    if (!clients.has(chatId)) clients.set(chatId, new Set());
    clients.get(chatId).add(res);

    const keepAlive = setInterval(() => {
        res.write(': keep-alive\n\n');
    }, 15000);

    req.on('close', () => {
        clearInterval(keepAlive);
        if (clients.has(chatId)) {
            clients.get(chatId).delete(res);
            if (clients.get(chatId).size === 0) clients.delete(chatId);
        }
    });
});

function broadcastToChat(chatId, event, data) {
    if (clients.has(chatId)) {
        for (const res of clients.get(chatId)) {
            res.write(`event: ${event}\ndata: ${JSON.stringify(data)}\n\n`);
        }
    }
}

// ======= CHAT ENDPOINTS =======
app.get('/api/chats/:userId', authenticateToken, async (req, res) => {
    try {
        const userId = req.params.userId;
        const [rows] = await pool.query(`SELECT c.*, u.id as actual_contact_id, u.name as contact_name, u.company as contact_company, u.score as contact_score FROM chats c JOIN users u ON (c.contact_id = u.id AND c.user_id = ?) OR (c.user_id = u.id AND c.contact_id = ?) WHERE c.user_id = ? OR c.contact_id = ?`, [userId, userId, userId, userId]);
        const mappedRows = await Promise.all(rows.map(async (r) => {
            const [unreadRows] = await pool.query('SELECT COUNT(*) as cnt FROM messages WHERE chat_id = ? AND sender_id != ? AND is_read = FALSE AND deleted_at IS NULL', [r.id, userId]);
            const unreadCount = unreadRows[0].cnt;
            return {
                id: r.id,
                contactId: r.actual_contact_id,
                initials: (r.contact_name || 'Unknown').substring(0, 2).toUpperCase(),
                name: r.contact_name || 'Unknown User',
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

app.get('/api/chats/:chatId/messages', authenticateToken, async (req, res) => {
    try {
        const userId = req.query.userId || req.user.userId;
        const [rows] = await pool.query(`SELECT * FROM messages WHERE chat_id = ? AND deleted_at IS NULL ORDER BY created_at ASC`, [req.params.chatId]);
        const mappedRows = rows.map(r => ({
            id: r.id,
            chat_id: r.chat_id,
            content: r.text,
            sender_id: r.sender_id,
            from_user: r.sender_id === userId,
            is_ai_labeled: false,
            is_read: r.is_read == 1 ? 1 : 0,
            created_at: r.created_at ? r.created_at.toISOString() : new Date().toISOString(),
            read_at: r.read_at ? r.read_at.toISOString() : null,
            reactions: r.reactions || null
        }));
        res.json(mappedRows);
    } catch (e) { res.status(500).json({ error: e.message }) }
});

app.post('/api/chats/:chatId/messages', authenticateToken, async (req, res) => {
    try {
        const { senderId, text } = req.body;
        const chatId = req.params.chatId;
        const id = 'msg_' + Date.now();
        const now = new Date();
        await pool.query('INSERT INTO messages (id, chat_id, sender_id, text, is_read, created_at) VALUES (?, ?, ?, ?, ?, ?)', [id, chatId, senderId, text, false, now]);
        await pool.query('UPDATE chats SET last_message = ?, updated_at = ? WHERE id = ?', [text, now, chatId]);

        const [chats] = await pool.query('SELECT contact_id, user_id FROM chats WHERE id = ?', [chatId]);
        const chat = chats[0];
        if (chat) {
            const receiverId = chat.user_id === senderId ? chat.contact_id : chat.user_id;
            const [users] = await pool.query('SELECT name FROM users WHERE id = ?', [senderId]);
            const senderName = users.length > 0 ? users[0].name : 'Someone';
            sendToUser(receiverId, 'notification', { title: `New Message from ${senderName}`, body: text.substring(0, 100), data: { type: 'chat', chatId: chatId, action_id: chatId } });
        }

        const msgData = { id, chat_id: chatId, content: text, sender_id: senderId, created_at: now.toISOString(), is_read: 0 };
        broadcastToChat(chatId, 'new_message', msgData);
        res.json({ success: true, ...msgData });
    } catch (e) { res.status(500).json({ error: e.message }); }
});

// Chat Enhancements
app.post('/api/chat/typing', authenticateToken, (req, res) => {
    const { chatId, isTyping } = req.body;
    const senderId = req.user.userId;
    if (clients.has(chatId)) {
        for (const clientRes of clients.get(chatId)) {
            // Broadcast to everyone else (Android app handles filtering on client side or we could filter here if we mapped res to userId)
            clientRes.write(`event: typing\ndata: ${JSON.stringify({ senderId, isTyping })}\n\n`);
        }
    }
    res.json({ success: true });
});

app.post('/api/messages/read', authenticateToken, async (req, res) => {
    try {
        const { messageId } = req.body;
        const now = new Date();
        await pool.query('UPDATE messages SET is_read = TRUE, read_at = ? WHERE id = ?', [now, messageId]);
        
        // Notify sender via SSE
        const [rows] = await pool.query('SELECT chat_id, sender_id FROM messages WHERE id = ?', [messageId]);
        if (rows.length > 0) {
            sendToUser(rows[0].sender_id, 'message_read', { messageId, read_at: now });
        }
        res.json({ success: true });
    } catch (e) { res.status(500).json({ error: e.message }); }
});

app.post('/api/messages/reaction', authenticateToken, async (req, res) => {
    try {
        const { messageId, reaction } = req.body;
        const userId = req.user.userId;
        const [rows] = await pool.query('SELECT reactions, chat_id FROM messages WHERE id = ?', [messageId]);
        if (rows.length === 0) return res.status(404).json({ error: "Message not found" });
        
        let reactions = rows[0].reactions || {};
        if (typeof reactions === 'string') reactions = JSON.parse(reactions);
        if (!reactions[reaction]) reactions[reaction] = [];
        if (!reactions[reaction].includes(userId)) reactions[reaction].push(userId);
        
        await pool.query('UPDATE messages SET reactions = ? WHERE id = ?', [JSON.stringify(reactions), messageId]);
        broadcastToChat(rows[0].chat_id, 'reaction_update', { messageId, reactions });
        res.json({ success: true, reactions });
    } catch (e) { res.status(500).json({ error: e.message }); }
});

app.put('/api/messages/:id', authenticateToken, async (req, res) => {
    try {
        const { content } = req.body;
        const messageId = req.params.id;
        const [rows] = await pool.query('SELECT sender_id, chat_id FROM messages WHERE id = ?', [messageId]);
        if (rows.length === 0) return res.status(404).json({ error: "Message not found" });
        if (rows[0].sender_id !== req.user.userId) return res.status(403).json({ error: "Unauthorized" });
        
        await pool.query('UPDATE messages SET text = ? WHERE id = ?', [content, messageId]);
        broadcastToChat(rows[0].chat_id, 'message_updated', { messageId, content });
        res.json({ success: true, content });
    } catch (e) { res.status(500).json({ error: e.message }); }
});

app.delete('/api/messages/:id', authenticateToken, async (req, res) => {
    try {
        const messageId = req.params.id;
        const [rows] = await pool.query('SELECT sender_id, chat_id FROM messages WHERE id = ?', [messageId]);
        if (rows.length === 0) return res.status(404).json({ error: "Message not found" });
        if (rows[0].sender_id !== req.user.userId) return res.status(403).json({ error: "Unauthorized" });
        
        const now = new Date();
        await pool.query('UPDATE messages SET deleted_at = ? WHERE id = ?', [now, messageId]);
        broadcastToChat(rows[0].chat_id, 'message_deleted', { messageId });
        res.json({ success: true });
    } catch (e) { res.status(500).json({ error: e.message }); }
});

// ======= CALL LOGGING & HISTORY =======
app.post('/api/calls/log', authenticateToken, async (req, res) => {
    try {
        const { receiver_id, call_type, status, duration_sec } = req.body;
        const initiator_id = req.user.userId;
        const id = 'call_' + Date.now();
        
        await pool.query(
            'INSERT INTO calls (id, initiator_id, receiver_id, call_type, status, duration_sec) VALUES (?, ?, ?, ?, ?, ?)',
            [id, initiator_id, receiver_id, call_type, status, parseInt(duration_sec) || 0]
        );
        
        if (status === 'missed') {
            const [users] = await pool.query('SELECT name FROM users WHERE id = ?', [initiator_id]);
            const senderName = users.length > 0 ? users[0].name : 'Someone';
            sendToUser(receiver_id, 'notification', { title: 'Missed Call', body: `You missed a ${call_type} call from ${senderName}` });
        }
        res.json({ success: true, id });
    } catch (e) { res.status(500).json({ error: e.message }); }
});

app.get('/api/calls/history', authenticateToken, async (req, res) => {
    try {
        const userId = req.user.userId;
        const [rows] = await pool.query(
            `SELECT c.*, 
            u1.name as initiator_name, u1.avatar_url as initiator_avatar,
            u2.name as receiver_name, u2.avatar_url as receiver_avatar
            FROM calls c
            LEFT JOIN users u1 ON c.initiator_id = u1.id
            LEFT JOIN users u2 ON c.receiver_id = u2.id
            WHERE c.initiator_id = ? OR c.receiver_id = ?
            ORDER BY c.start_time DESC`,
            [userId, userId]
        );
        res.json(rows);
    } catch (e) { res.status(500).json({ error: e.message }); }
});

// ======= SEARCH & SUGGESTIONS =======
app.get('/api/search', authenticateToken, async (req, res) => {
    try {
        const { q, type } = req.query;
        if (!q) return res.json([]);
        
        let results = [];
        if (!type || type === 'all' || type === 'users') {
            const [u] = await pool.query("SELECT id, name, company, location, 'user' as source_type FROM users WHERE MATCH(name, company) AGAINST(? IN BOOLEAN MODE)", [q + '*']);
            results.push(...u);
        }
        if (!type || type === 'all' || type === 'companies') {
            const [c] = await pool.query("SELECT id, name, industry, location, 'company' as source_type FROM companies WHERE MATCH(name, industry) AGAINST(? IN BOOLEAN MODE)", [q + '*']);
            results.push(...c);
        }
        if (!type || type === 'all' || type === 'events') {
            const [e] = await pool.query("SELECT id, title as name, location, 'event' as source_type FROM events WHERE MATCH(title, description, location) AGAINST(? IN BOOLEAN MODE)", [q + '*']);
            results.push(...e);
        }
        res.json(results);
    } catch (e) { res.status(500).json({ error: e.message }); }
});

app.get('/api/suggestions', authenticateToken, async (req, res) => {
    try {
        const userId = req.user.userId;
        const [userRows] = await pool.query('SELECT company FROM users WHERE id = ?', [userId]);
        if (userRows.length === 0) return res.json([]);
        const company = userRows[0].company;
        
        // Find users with same company but not already connected
        const [suggestions] = await pool.query(`
            SELECT id, name, company, avatar_url, 'Same Company' as reason, 85 as match_score 
            FROM users 
            WHERE company = ? AND id != ? AND id NOT IN (
                SELECT contact_id FROM chats WHERE user_id = ?
            ) LIMIT 10`, 
            [company, userId, userId]
        );
        res.json(suggestions);
    } catch (e) { res.status(500).json({ error: e.message }); }
});

// ======= AGORA ENDPOINTS =======
app.post('/api/calls/token', authenticateToken, (req, res) => {
    try {
        const { channelName, uid, role } = req.body;
        const appId = process.env.AGORA_APP_ID || 'd5d98935c72d46a68385409d52c31ad9';
        const appCertificate = process.env.AGORA_APP_CERTIFICATE || '1db0c2b1688e4547a95ca0232359bfa3';
        const expireTime = 3600; 
        const privilegeExpireTime = Math.floor(Date.now() / 1000) + expireTime;
        const tokenRole = role === 'publisher' ? RtcRole.PUBLISHER : RtcRole.SUBSCRIBER;
        const token = RtcTokenBuilder.buildTokenWithUid(appId, appCertificate, channelName, uid || 0, tokenRole, privilegeExpireTime);
        res.json({ success: true, token });
    } catch (e) { res.status(500).json({ success: false, error: e.message }); }
});

app.post('/api/calls/initiate', authenticateToken, async (req, res) => {
    try {
        const { callerId, receiverId, isVideo } = req.body;
        const channelName = `call_${callerId}_${receiverId}_${Date.now()}`;
        const appId = process.env.AGORA_APP_ID || 'd5d98935c72d46a68385409d52c31ad9';
        const appCertificate = process.env.AGORA_APP_CERTIFICATE || '1db0c2b1688e4547a95ca0232359bfa3';
        const privilegeExpireTime = Math.floor(Date.now() / 1000) + 3600;
        
        const callerToken = RtcTokenBuilder.buildTokenWithUid(appId, appCertificate, channelName, 0, RtcRole.PUBLISHER, privilegeExpireTime);
        const [users] = await pool.query('SELECT name, avatar_url FROM users WHERE id = ?', [callerId]);
        const callerName = users.length > 0 ? users[0].name : 'Someone';
        
        sendToUser(receiverId, 'incoming_call', { channelName, callerId, callerName, isVideo });
        res.json({ success: true, channelName, token: callerToken });
    } catch (e) { res.status(500).json({ success: false, error: e.message }); }
});

// Start Server
const PORT = process.env.PORT || 3000;
if (require.main === module) { app.listen(PORT, () => console.log(`Server running on ${PORT}`)); }
module.exports = app;
