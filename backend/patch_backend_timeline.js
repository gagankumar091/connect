const fs = require('fs');

const indexJsPath = './index.js';
let content = fs.readFileSync(indexJsPath, 'utf8');

const postTimelineRoute = `
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
`;

if (!content.includes('/api/timeline\'') && !content.includes('/api/timeline\',')) {
    // wait it already has app.get('/api/timeline/:contactId'
}
if (!content.includes('app.post(\'/api/timeline\'')) {
    content = content.replace("app.get('/api/timeline/:contactId',", postTimelineRoute + "\napp.get('/api/timeline/:contactId',");
    fs.writeFileSync(indexJsPath, content);
    console.log("Added POST routes.");
} else {
    console.log("Routes already exist.");
}
