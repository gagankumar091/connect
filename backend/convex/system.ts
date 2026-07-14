import { mutation, query } from "./_generated/server";
import { v } from "convex/values";

export const getEvents = query({
  args: {},
  handler: async (ctx) => {
    return await ctx.db.query("events").collect();
  }
});

export const getCompanies = query({
  args: {},
  handler: async (ctx) => {
    return await ctx.db.query("companies").collect();
  }
});

export const getReminders = query({
  args: { userId: v.string() },
  handler: async (ctx, args) => {
    return await ctx.db.query("reminders").withIndex("by_user", q => q.eq("user_id", args.userId)).collect();
  }
});
