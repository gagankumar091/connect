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
    const { username, password, name, email, phone } = req.body;
    const hashedPassword = await bcrypt.hash(password, 10);
    
    const result = await convex.mutation(api.auth.registerUser, {
      username, password: hashedPassword, name, email, phone
    });
    
    const payload = { userId: result.id, username };
    const accessToken = jwt.sign(payload, JWT_SECRET, { expiresIn: '15m' });
    const refreshToken = jwt.sign(payload, JWT_REFRESH_SECRET, { expiresIn: '7d' });
    
    const userObj = { id: result.id, username, name, email, phone };
    res.json({ success: true, user: userObj, accessToken, refreshToken });
  } catch (e: any) {
    res.status(500).json({ success: false, message: e.message });
  }
}
