import { ConvexHttpClient } from 'convex/browser';
import { api } from '../convex/_generated/api';

const convex = new ConvexHttpClient(process.env.CONVEX_URL || "https://hearty-gopher-467.convex.cloud");

export default async function handler(req: any, res: any) {
  if (req.method !== 'GET') {
    return res.status(405).end();
  }
  
  // The Android app probably passes userId in query for GET
  const userId = req.query.userId || req.body?.userId || "user_123";
  
  try {
    const result = await convex.query(api.briefing.getBriefing, { userId });
    res.status(200).json(result);
  } catch (error: any) {
    res.status(500).json({ error: error.message });
  }
}
