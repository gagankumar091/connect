import { ConvexHttpClient } from 'convex/browser';
import { api } from '../../convex/_generated/api';
import { Id } from '../../convex/_generated/dataModel';

const convex = new ConvexHttpClient(process.env.CONVEX_URL || "https://dependable-mammoth-695.convex.cloud");

export default async function handler(req: any, res: any) {
  if (req.method !== 'POST') {
    return res.status(405).end();
  }
  
  const { contactId } = req.body;
  
  try {
    const result = await convex.action(api.ai.draftFollowup, { 
      contactId: contactId as Id<"contacts">
    });
    res.status(200).json(result);
  } catch (error: any) {
    res.status(500).json({ error: error.message });
  }
}
