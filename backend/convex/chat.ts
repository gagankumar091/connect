import { mutation, query } from "./_generated/server";
import { v } from "convex/values";

export const getChatsForUser = query({
  args: { userId: v.string() },
  handler: async (ctx, args) => {
    // Find chats where user is user_id
    const chats1 = await ctx.db.query("chats").withIndex("by_user", q => q.eq("user_id", args.userId)).collect();
    // Find chats where user is contact_id
    const chats2 = await ctx.db.query("chats").withIndex("by_contact", q => q.eq("contact_id", args.userId)).collect();
    
    return [...chats1, ...chats2];
  }
});

export const getMessagesForChat = query({
  args: { chatId: v.id("chats") },
  handler: async (ctx, args) => {
    return await ctx.db.query("messages").withIndex("by_chat", q => q.eq("chat_id", args.chatId)).collect();
  }
});

export const sendMessage = mutation({
  args: {
    chatId: v.id("chats"),
    senderId: v.string(),
    text: v.string()
  },
  handler: async (ctx, args) => {
    const now = new Date().toISOString();
    const msgId = await ctx.db.insert("messages", {
      chat_id: args.chatId,
      sender_id: args.senderId,
      text: args.text,
      is_read: false,
      created_at: now
    });
    
    await ctx.db.patch(args.chatId, {
      last_message: args.text,
      updated_at: now
    });
    
    return { id: msgId, ...args, created_at: now, is_read: false };
  }
});
