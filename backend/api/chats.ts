import { ConvexHttpClient } from "convex/browser";
import { api } from "../convex/_generated/api";
import jwt from "jsonwebtoken";

const convex = new ConvexHttpClient(process.env.CONVEX_URL || "https://hearty-gopher-467.convex.cloud");
const JWT_SECRET = process.env.JWT_SECRET || 'mitron_jwt_secret_production_2026_change_me';

export default async function handler(req: any, res: any) {
  try {
    const authHeader = req.headers.authorization;
    if (!authHeader) return res.status(401).json({ error: "Unauthorized. Missing token." });
    jwt.verify(authHeader.split(' ')[1], JWT_SECRET);

    // Extract userId from query (vercel rewrites /api/chats/:id to ?id=...)
    const userId = req.query.id;
    if (!userId) return res.status(400).json({ error: "Missing userId" });

    const chats = await convex.query(api.chat.getChatsForUser, { userId });
    
    // Legacy mapping requires resolving contacts, but for now return mocked chat format
    const mappedChats = chats.map((c: any) => ({
      id: c._id,
      contactId: c.user_id === userId ? c.contact_id : c.user_id,
      initials: 'XX',
      name: 'Unknown User',
      company: 'No Company',
      lastMessage: c.last_message || '',
      lastMessageTime: c.updated_at || new Date().toISOString(),
      unreadCount: 0,
      scoreLabel: '100',
      color: 'PRO',
      overdue: false
    }));

    res.json(mappedChats);
  } catch (e: any) {
    res.status(500).json({ error: e.message });
  }
}
