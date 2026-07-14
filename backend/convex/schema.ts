import { defineSchema, defineTable } from "convex/server";
import { v } from "convex/values";

export default defineSchema({
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
