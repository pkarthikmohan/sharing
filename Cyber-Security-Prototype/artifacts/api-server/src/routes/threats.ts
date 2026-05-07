import { Router, type IRouter } from "express";
import { desc, sql } from "drizzle-orm";
import { db, threatsTable, insertThreatSchema } from "@workspace/db";

const router: IRouter = Router();

router.get("/threats/recent", async (req, res) => {
  const rawLimit = typeof req.query.limit === "string" ? req.query.limit : undefined;
  const limit = rawLimit ? Number(rawLimit) : 5;
  const safeLimit = Number.isFinite(limit) ? Math.max(1, Math.min(50, limit)) : 5;

  const threats = await db
    .select()
    .from(threatsTable)
    .orderBy(desc(threatsTable.timestamp))
    .limit(safeLimit);

  res.json({ threats });
});

router.get("/threats/since", async (req, res) => {
  const rawSince = typeof req.query.since === "string" ? req.query.since : undefined;
  const sinceMs = rawSince ? Number(rawSince) : NaN;
  if (!Number.isFinite(sinceMs) || sinceMs <= 0) {
    res.status(400).json({ error: "Query param 'since' (ms since epoch) is required." });
    return;
  }

  const rawLimit = typeof req.query.limit === "string" ? req.query.limit : undefined;
  const limit = rawLimit ? Number(rawLimit) : 200;
  const safeLimit = Number.isFinite(limit) ? Math.max(1, Math.min(2000, limit)) : 200;

  const threats = await db
    .select()
    .from(threatsTable)
    .where(sql`${threatsTable.timestamp} >= ${new Date(sinceMs)}`)
    .orderBy(desc(threatsTable.timestamp))
    .limit(safeLimit);

  res.json({ threats });
});

router.get("/threats/stats", async (_req, res) => {
  const [row] = await db
    .select({
      smsScanned: sql<number>`sum(case when ${threatsTable.type} = 'SMISHING' then 1 else 0 end)`.mapWith(Number),
      callsMonitored: sql<number>`sum(case when ${threatsTable.type} = 'VISHING' then 1 else 0 end)`.mapWith(Number),
      threatsBlocked: sql<number>`sum(case when ${threatsTable.band} in ('CONFIRMED','LIKELY') then 1 else 0 end)`.mapWith(Number),
    })
    .from(threatsTable);

  res.json({
    smsScanned: row?.smsScanned ?? 0,
    callsMonitored: row?.callsMonitored ?? 0,
    threatsBlocked: row?.threatsBlocked ?? 0,
  });
});

router.post("/threats", async (req, res) => {
  const parsed = insertThreatSchema.safeParse(req.body);
  if (!parsed.success) {
    res.status(400).json({ error: "Invalid payload", issues: parsed.error.issues });
    return;
  }

  const [inserted] = await db.insert(threatsTable).values(parsed.data).returning({ id: threatsTable.id });
  res.status(201).json({ id: inserted?.id });
});

// Dev-only helper to get "real-looking" data quickly
router.post("/threats/seed", async (_req, res) => {
  if (process.env.NODE_ENV === "production") {
    res.status(404).end();
    return;
  }

  const now = new Date();
  const sample = [
    { type: "SMISHING", sender: "+91-98765-43210", body: "KYC expired. Click http://bit.ly/kyc-now", score: 94, band: "CONFIRMED", timestamp: new Date(now.getTime() - 2 * 60_000) },
    { type: "VISHING", sender: "+91-11234-56789", body: null, score: 82, band: "LIKELY", timestamp: new Date(now.getTime() - 18 * 60_000) },
    { type: "SMISHING", sender: "VM-TRAI", body: "Your SIM will be blocked. Pay ₹500 immediately.", score: 67, band: "SUSPICIOUS", timestamp: new Date(now.getTime() - 60 * 60_000) },
    { type: "URL_SCAM", sender: "AD-FAKEBANK", body: "Unusual login. Verify at https://fakebank.xyz", score: 97, band: "CONFIRMED", timestamp: new Date(now.getTime() - 3 * 60 * 60_000) },
  ];

  await db.insert(threatsTable).values(sample);
  res.json({ inserted: sample.length });
});

export default router;

