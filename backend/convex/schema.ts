import { defineSchema, defineTable } from "convex/server";
import { v } from "convex/values";

export default defineSchema({
  users: defineTable({
    username: v.string(),
    password: v.string(),
    name: v.string(),
    email: v.string(),
    phone: v.optional(v.string()),
    company: v.optional(v.string()),
    title: v.optional(v.string()),
    avatar_url: v.optional(v.string()),
    score: v.optional(v.number()),
    days_since_contact: v.optional(v.number()),
  }).index("by_email", ["email"]).index("by_username", ["username"]),

  companies: defineTable({
    name: v.string(),
    industry: v.optional(v.string()),
    location: v.optional(v.string()),
  }),

  chats: defineTable({
    user_id: v.string(), // userId of one participant
    contact_id: v.string(), // userId of the other participant
    last_message: v.optional(v.string()),
    updated_at: v.optional(v.string()),
  }).index("by_user", ["user_id"]).index("by_contact", ["contact_id"]),

  messages: defineTable({
    chat_id: v.id("chats"),
    sender_id: v.string(),
    text: v.string(),
    is_read: v.boolean(),
    created_at: v.string(),
    read_at: v.optional(v.string()),
  }).index("by_chat", ["chat_id"]),

  contacts: defineTable({
    name: v.string(),
    company: v.string(),
    relationshipType: v.string(),
    healthScore: v.number(),
    lastContactedAt: v.string(),
  }),

  timelineEvents: defineTable({
    contactId: v.id("contacts"),
    type: v.string(),
    content: v.string(),
    timestamp: v.string(),
  }).index("by_contact", ["contactId"]),

  meetingSummaries: defineTable({
    contactId: v.id("contacts"),
    date: v.string(),
    summary: v.string(),
    actionItems: v.array(v.string()),
  }).index("by_contact", ["contactId"]),

  briefings: defineTable({
    date: v.string(),
    summary: v.string(),
    userId: v.string(),
  }).index("by_user_date", ["userId", "date"]),
});
