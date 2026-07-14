import { ConvexHttpClient } from "convex/browser";
import { api } from "../convex/_generated/api";
import jwt from "jsonwebtoken";

const convex = new ConvexHttpClient(process.env.CONVEX_URL || "https://hearty-gopher-467.convex.cloud");
const JWT_SECRET = process.env.JWT_SECRET || 'mitron_jwt_secret_production_2026_change_me';

export default async function handler(req: any, res: any) {
  if (req.method !== 'GET') return res.status(405).json({ error: 'Method Not Allowed' });
  try {
    const authHeader = req.headers.authorization;
    if (!authHeader) return res.status(401).json({ error: "Unauthorized. Missing token." });
    
    // Quick JWT verify
    jwt.verify(authHeader.split(' ')[1], JWT_SECRET);
    
    // Fetch all users mapped to legacy format
    const users = await convex.query(api.auth.getAllUsers);
    
    const mappedUsers = users.map((u: any) => ({
        id: u._id,
        username: u.username,
        name: u.name,
        email: u.email,
        phone: u.phone,
        company: u.company,
        title: u.title,
        avatar_url: u.avatar_url,
        score: u.score || 100,
        days_since_contact: u.days_since_contact || 0
    }));
    
    res.json(mappedUsers);
  } catch (e: any) {
    res.status(500).json({ error: e.message });
  }
}
