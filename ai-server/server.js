const express = require('express');
const http = require('http');

const app = express();
const PORT = process.env.PORT || 3001;
const OLLAMA_HOST = process.env.OLLAMA_HOST || 'http://localhost:11434';
const MODEL = process.env.MODEL || 'llama3.2:1b';

app.use(express.json());

const SYSTEM_PROMPT = `You are Connect AI, a helpful networking assistant for a professional relationship management app called Connect.

Your role is to help users with:
- Writing ice-breakers and follow-up messages to contacts
- Summarizing contact information and conversation history
- Preparing for meetings with contacts
- Suggesting networking strategies
- Drafting professional emails and messages
- Analyzing relationship health and suggesting improvements

Keep responses concise, professional, and actionable. When suggesting messages, provide 2-3 options. When summarizing, keep it brief but insightful.

The user's app has features like: contact profiles, company profiles, chat, meeting summaries, timeline of interactions, daily briefing, smart search, QR-based connections, and relationship health dashboards.`;

function buildPrompt(messages) {
  const parts = [{ role: 'system', content: SYSTEM_PROMPT }];
  for (const msg of messages) {
    parts.push({ role: msg.fromUser ? 'user' : 'assistant', content: msg.text });
  }
  return parts;
}

app.post('/chat', async (req, res) => {
  const { messages } = req.body;

  if (!messages || !Array.isArray(messages) || messages.length === 0) {
    return res.status(400).json({ error: 'messages array is required' });
  }

  try {
    const prompt = buildPrompt(messages);
    const response = await fetch(`${OLLAMA_HOST}/api/chat`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        model: MODEL,
        messages: prompt,
        stream: false,
        options: {
          temperature: 0.7,
          top_p: 0.9,
        },
      }),
    });

    if (!response.ok) {
      const errorText = await response.text();
      console.error('Ollama error:', response.status, errorText);
      return res.status(502).json({ error: 'AI model error', detail: errorText });
    }

    const data = await response.json();
    res.json({ text: data.message.content });
  } catch (error) {
    console.error('Server error:', error.message);
    res.status(500).json({ error: 'Internal server error', detail: error.message });
  }
});

app.get('/health', (_req, res) => {
  res.json({ status: 'ok', model: MODEL });
});

app.listen(PORT, '0.0.0.0', () => {
  console.log(`Connect AI server running on http://0.0.0.0:${PORT}`);
  console.log(`Model: ${MODEL}`);
  console.log(`Ollama host: ${OLLAMA_HOST}`);
});
