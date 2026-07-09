const fs = require('fs');
let code = fs.readFileSync('index.js', 'utf8');

const sseCode = `
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
            res.write(\`event: \${event}\\ndata: \${JSON.stringify(data)}\\n\\n\`);
        }
    }
}
`;

code = code.replace("app.get('/api/chats/:chatId/messages'", sseCode + "\napp.get('/api/chats/:chatId/messages'");

const broadcastCall = `        broadcastToChat(chatId, 'new_message', { id, chat_id: chatId, content: text, from_user: false, sender_id: senderId, created_at: now.toISOString(), is_read: 0 });\n`;

code = code.replace("res.json({ success: true, id, chat_id: chatId, content: text, from_user: true, created_at: now.toISOString(), is_read: 0 });", broadcastCall + "        res.json({ success: true, id, chat_id: chatId, content: text, from_user: true, created_at: now.toISOString(), is_read: 0 });");

fs.writeFileSync('index.js', code);
console.log("SSE added to index.js");
