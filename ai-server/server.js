const express = require('express');
const http = require('http');
const NodeCache = require('node-cache');
const crypto = require('crypto');

const app = express();
const PORT = process.env.PORT || 3001;
const OLLAMA_HOST = process.env.OLLAMA_HOST || 'http://localhost:11434';
const DEFAULT_MODEL = process.env.MODEL || 'llama3.2:1b';

const cache = new NodeCache({ stdTTL: 3600 });
app.use(express.json());

const BASE_SYSTEM_PROMPT = `You are Connect AI, a helpful networking assistant for a professional relationship management app called Connect.
Keep responses concise, professional, and actionable.`;

function buildPrompt(messages, contextData) {
  let systemPrompt = BASE_SYSTEM_PROMPT;
  if (contextData) systemPrompt += `\n\nCurrent Context:\n${contextData}`;
  const parts = [{ role: 'system', content: systemPrompt }];
  for (const msg of messages) {
    parts.push({ role: msg.fromUser ? 'user' : 'assistant', content: msg.text });
  }
  return parts;
}

function generateCacheKey(reqBody) {
    const hash = crypto.createHash('sha256');
    hash.update(JSON.stringify(reqBody));
    return hash.digest('hex');
}

async function callOllama(messages, model) {
    const controller = new AbortController();
    const timeout = setTimeout(() => controller.abort(), 30000); // 30s timeout
    try {
        const response = await fetch(`${OLLAMA_HOST}/api/chat`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            signal: controller.signal,
            body: JSON.stringify({
                model: model,
                messages: messages,
                stream: false,
                options: { temperature: 0.7, top_p: 0.9 },
            }),
        });
        clearTimeout(timeout);
        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(`Ollama error: ${response.status} ${errorText}`);
        }
        const data = await response.json();
        return data.message.content;
    } catch (e) {
        clearTimeout(timeout);
        if (e.name === 'AbortError') throw new Error("AI is thinking too long, try again");
        throw e;
    }
}

app.post('/api/ai/chat', async (req, res) => {
  const { messages, contextData, model } = req.body;
  if (!messages || !Array.isArray(messages) || messages.length === 0) {
    return res.status(400).json({ error: 'messages array is required' });
  }
  const cacheKey = generateCacheKey(req.body);
  const cachedResponse = cache.get(cacheKey);
  if (cachedResponse) return res.json({ text: cachedResponse, cached: true });

  try {
    const prompt = buildPrompt(messages, contextData);
    const result = await callOllama(prompt, model || DEFAULT_MODEL);
    cache.set(cacheKey, result);
    res.json({ text: result, cached: false });
  } catch (error) {
    res.status(500).json({ error: 'Internal server error', detail: error.message });
  }
});

app.post('/api/ai/icebreaker', async (req, res) => {
    const { contactName, companyName, model } = req.body;
    try {
        const result = await callOllama([{ role: 'user', content: `Suggest 3 ice-breakers for ${contactName} from ${companyName}.` }], model || DEFAULT_MODEL);
        res.json({ text: result });
    } catch (e) { res.status(500).json({ error: e.message }); }
});

app.post('/api/ai/followup', async (req, res) => {
    const { contactName, model } = req.body;
    try {
        const result = await callOllama([{ role: 'user', content: `Draft a professional follow-up email for ${contactName}.` }], model || DEFAULT_MODEL);
        res.json({ text: result });
    } catch (e) { res.status(500).json({ error: e.message }); }
});

app.post('/api/ai/summarize', async (req, res) => {
    const { meetingNotes, model } = req.body;
    try {
        const result = await callOllama([{ role: 'user', content: `Summarize key points from this meeting: ${meetingNotes}` }], model || DEFAULT_MODEL);
        res.json({ text: result });
    } catch (e) { res.status(500).json({ error: e.message }); }
});

app.get('/health', (_req, res) => res.json({ status: 'ok', model: DEFAULT_MODEL }));
app.listen(PORT, '0.0.0.0', () => console.log(`Connect AI server running on http://0.0.0.0:${PORT}`));
