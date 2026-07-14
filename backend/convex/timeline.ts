import { mutation } from "./_generated/server";
import { v } from "convex/values";

export const syncTimeline = mutation({
  args: {
    contactId: v.id("contacts"),
    type: v.string(),
    content: v.string(),
  },
  handler: async (ctx, args) => {
    const timestamp = new Date().toISOString();
    
    // Insert new timeline event
    const eventId = await ctx.db.insert("timelineEvents", {
      contactId: args.contactId,
      type: args.type,
      content: args.content,
      timestamp,
    });
    
    // Trigger health recalculation (simple logic for now)
    const contact = await ctx.db.get(args.contactId);
    if (contact) {
      const newScore = Math.min(100, contact.healthScore + 5);
      await ctx.db.patch(args.contactId, {
        healthScore: newScore,
        lastContactedAt: timestamp,
      });
    }
    
    return { success: true, eventId };
  },
});
