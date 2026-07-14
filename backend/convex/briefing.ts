import { query } from "./_generated/server";
import { v } from "convex/values";

export const getBriefing = query({
  args: { userId: v.string() },
  handler: async (ctx, args) => {
    const today = new Date().toISOString().split("T")[0];
    
    // In a real app we would join tables, here we just return matching briefings
    // Or we dynamically generate it from meetings. Let's find meetings for today.
    
    const briefings = await ctx.db
      .query("briefings")
      .withIndex("by_user_date", (q) => 
        q.eq("userId", args.userId).eq("date", today)
      )
      .collect();
      
    if (briefings.length > 0) {
      return briefings.map(b => ({
        id: b._id,
        text: b.summary,
        color: "NEUTRAL",
        contactName: null,
        meetingTime: null,
        lastContextSummary: null
      }));
    }
    
    // Fallback: return an empty briefing format if none exists
    return [{
      id: "empty-briefing",
      text: "No briefing generated for today.",
      color: "NEUTRAL",
      contactName: null,
      meetingTime: null,
      lastContextSummary: null
    }];
  },
});
