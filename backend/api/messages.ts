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

    const chatId = req.query.id; // from /api/chats/:id/messages
    if (!chatId) return res.status(400).json({ error: "Missing chatId" });

    if (req.method === 'GET') {
      const messages = await convex.query(api.chat.getMessagesForChat, { chatId: chatId as any });
      
      const mapped = messages.map((m: any) => ({
        id: m._id,
        chat_id: m.chat_id,
        content: m.text,
        sender_id: m.sender_id,
        from_user: false, // will be evaluated on client
        is_read: m.is_read ? 1 : 0,
        created_at: m.created_at,
      }));
      return res.json(mapped);
    } 
    
    if (req.method === 'POST') {
      const { senderId, text } = req.body;
      const msg = await convex.mutation(api.chat.sendMessage, { 
        chatId: chatId as any, 
        senderId, 
        text 
      });
      
      const mapped = {
        id: msg.id,
        chat_id: msg.chatId,
        content: msg.text,
        sender_id: msg.senderId,
        created_at: msg.created_at,
        is_read: 0
      };
      
      return res.json({ success: true, ...mapped });
    }

    res.status(405).json({ error: 'Method Not Allowed' });
  } catch (e: any) {
    res.status(500).json({ error: e.message });
  }
}
