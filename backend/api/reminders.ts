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

    const userId = req.query.id;
    if (!userId) return res.status(400).json({ error: "Missing userId" });

    if (req.method === 'GET') {
      const reminders = await convex.query(api.system.getReminders, { userId });
      
      const mapped = reminders.map((r: any) => ({
        id: r._id,
        userId: r.user_id,
        title: r.title,
        dueDate: r.due_date,
        isCompleted: r.is_completed
      }));
      return res.json(mapped);
    }
    
    res.status(405).json({ error: 'Method Not Allowed' });
  } catch (e: any) {
    res.status(500).json({ error: e.message });
  }
}
