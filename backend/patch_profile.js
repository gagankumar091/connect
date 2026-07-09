const fs = require('fs');
let code = fs.readFileSync('index.js', 'utf8');

const profileGetEndpoint = `
app.get('/api/user-profile/:id', authenticate, async (req, res) => {
    try {
        const [rows] = await pool.query('SELECT * FROM users WHERE id = ?', [req.params.id]);
        if (rows.length === 0) return res.status(404).json({ error: 'User not found' });
        res.json(rows[0]);
    } catch (e) {
        res.status(500).json({ error: e.message });
    }
});
`;

code = code.replace(/app\.put\('\/api\/users\/:id'/, profileGetEndpoint + "\napp.put('/api/users/:id'");

fs.writeFileSync('index.js', code);
console.log("Profile GET endpoint added to index.js");
