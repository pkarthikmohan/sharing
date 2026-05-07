import { pgTable, serial, text, integer, timestamp, boolean } from "drizzle-orm/pg-core";
import { createInsertSchema } from "drizzle-zod";

export const threatsTable = pgTable("threats", {
  id: serial("id").primaryKey(),
  type: text("type").notNull(), // SMISHING | VISHING | URL_SCAM | ...
  sender: text("sender").notNull(),
  body: text("body"),
  score: integer("score").notNull(), // 0-100
  band: text("band").notNull(), // CONFIRMED | LIKELY | SUSPICIOUS | SAFE
  timestamp: timestamp("timestamp", { withTimezone: true }).notNull().defaultNow(),
  blocked: boolean("blocked").notNull().default(false),
  reported: boolean("reported").notNull().default(false),
});

export const insertThreatSchema = createInsertSchema(threatsTable).omit({
  id: true,
  timestamp: true,
});

export type Threat = typeof threatsTable.$inferSelect;
export type InsertThreat = typeof threatsTable.$inferInsert;

