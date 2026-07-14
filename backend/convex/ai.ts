import { action } from "./_generated/server";
import { v } from "convex/values";
import { api } from "./_generated/api";

export const draftFollowup = action({
  args: { contactId: v.id("contacts") },
  handler: async (ctx, args) => {
    // We can call a query to get contact info
    const contact = await ctx.runQuery(api.health.healthScore, { contactId: args.contactId });
    
    // Mocking an AI call (Ollama/LLM)
    const draftText = `Hi there,\n\nFollowing up on our last interaction. Let me know if you have any questions!\n\nBest,\nYour Connect App`;
    
    return {
      draft: draftText,
    };
  },
});
