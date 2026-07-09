const fs = require('fs');
let code = fs.readFileSync('index.js', 'utf8');

const userStreamCode = `
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
            res.write(\`event: \${event}\\ndata: \${JSON.stringify(data)}\\n\\n\`);
        }
    }
}
`;

// Insert the user stream code right before the FCM_SERVER_KEY definition
code = code.replace("const FCM_SERVER_KEY = process.env.FCM_SERVER_KEY || \"\";", userStreamCode + "\nconst FCM_SERVER_KEY = process.env.FCM_SERVER_KEY || \"\";");

// Replace sendFcmPush body with SSE logic
const newSendFcmPush = `function sendFcmPush(userId, title, body, data) {
    return new Promise((resolve) => {
        sendToUser(userId, 'notification', { title, body, data: data || {} });
        resolve();
    });
}`;

code = code.replace(/function sendFcmPush\([\s\S]*?\}\n\}/, newSendFcmPush);

fs.writeFileSync('index.js', code);
console.log("Patched FCM to use custom SSE in index.js");
