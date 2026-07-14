import { ConvexHttpClient } from "convex/browser";
import { api } from "../convex/_generated/api";
import bcrypt from "bcrypt";
import jwt from "jsonwebtoken";

const convex = new ConvexHttpClient(process.env.CONVEX_URL || "https://hearty-gopher-467.convex.cloud");
const JWT_SECRET = process.env.JWT_SECRET || 'mitron_jwt_secret_production_2026_change_me';
const JWT_REFRESH_SECRET = process.env.JWT_REFRESH_SECRET || 'mitron_refresh_secret_production_2026_change_me';

export default async function handler(req: any, res: any) {
  if (req.method !== 'POST') return res.status(405).json({ error: 'Method Not Allowed' });
  try {
    const { usernameOrEmail, password } = req.body;
    
    const user = await convex.query(api.auth.getUserByUsernameOrEmail, { usernameOrEmail });
    if (!user) return res.status(401).json({ success: false, message: 'Invalid credentials' });
    
    const match = await bcrypt.compare(password, user.password);
    if (match) {
        const payload = { userId: user._id, username: user.username };
        const accessToken = jwt.sign(payload, JWT_SECRET, { expiresIn: '15m' });
        const refreshToken = jwt.sign(payload, JWT_REFRESH_SECRET, { expiresIn: '7d' });
        
        const userObj = { id: user._id, username: user.username, name: user.name, email: user.email };
        res.json({ success: true, user: userObj, accessToken, refreshToken });
    } else {
        res.status(401).json({ success: false, message: 'Invalid credentials' });
    }
  } catch (e: any) {
    res.status(500).json({ success: false, message: e.message });
  }
}
