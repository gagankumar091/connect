import { ConvexHttpClient } from 'convex/browser';
import { api } from '../convex/_generated/api';
import { Id } from '../convex/_generated/dataModel';

const convex = new ConvexHttpClient(process.env.CONVEX_URL || "https://dependable-mammoth-695.convex.cloud");

export default async function handler(req: any, res: any) {
  if (req.method !== 'GET') {
    return res.status(405).end();
  }
  
  // Usually this would be /health-score/[contactId].ts in next.js or /api/health-score?contactId=...
  const contactId = req.query.contactId || "default";
  
  try {
    const result = await convex.query(api.health.healthScore, { 
      contactId: contactId as Id<"contacts">
    });
    res.status(200).json(result);
  } catch (error: any) {
    res.status(500).json({ error: error.message });
  }
}
