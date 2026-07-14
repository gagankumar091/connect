import { query } from "./_generated/server";
import { v } from "convex/values";

export const healthScore = query({
  args: { contactId: v.id("contacts") },
  handler: async (ctx, args) => {
    const contact = await ctx.db.get(args.contactId);
    if (!contact) {
      throw new Error("Contact not found");
    }
    
    // In a real app we might recalculate it here, 
    // but the DB value was updated on syncTimeline.
    return {
      contactId: contact._id,
      healthScore: contact.healthScore,
      lastContactedAt: contact.lastContactedAt,
    };
  },
});
