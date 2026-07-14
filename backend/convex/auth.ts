import { mutation, query } from "./_generated/server";
import { v } from "convex/values";

export const registerUser = mutation({
  args: {
    username: v.string(),
    password: v.string(),
    name: v.string(),
    email: v.string(),
    phone: v.optional(v.string()),
  },
  handler: async (ctx, args) => {
    const existing = await ctx.db.query("users").withIndex("by_username", q => q.eq("username", args.username)).first();
    if (existing) throw new Error("Username already taken");
    
    const userId = await ctx.db.insert("users", {
      username: args.username,
      password: args.password,
      name: args.name,
      email: args.email,
      phone: args.phone,
      score: 100,
      days_since_contact: 0,
    });
    return { id: userId };
  },
});

export const getUserByUsernameOrEmail = query({
  args: { usernameOrEmail: v.string() },
  handler: async (ctx, args) => {
    let user = await ctx.db.query("users").withIndex("by_username", q => q.eq("username", args.usernameOrEmail)).first();
    if (!user) {
      user = await ctx.db.query("users").withIndex("by_email", q => q.eq("email", args.usernameOrEmail)).first();
    }
    return user;
  }
});

export const getAllUsers = query({
  args: {},
  handler: async (ctx) => {
    return await ctx.db.query("users").collect();
  }
});
