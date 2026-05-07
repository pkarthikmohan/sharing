import { useEffect, useMemo, useState } from "react";
import { AegisNavMenu } from "./shared/AegisNavMenu";

type Threat = {
  id: number;
  type: string;
  sender: string;
  body?: string | null;
  score: number;
  band: string;
  timestamp: string;
  blocked?: boolean;
  reported?: boolean;
};

type BreakdownRow = { label: string; count: number; color: string };
type TopBlockedRow = { sender: string; count: number; type: string };

function getApiBase(): string {
  const env = (import.meta as any).env as Record<string, unknown> | undefined;
  const configured = typeof env?.VITE_API_BASE_URL === "string" ? env.VITE_API_BASE_URL : "";
  return (configured || "http://localhost:5000/api").replace(/\/$/, "");
}

function sinceMsForPeriod(period: string): number {
  const now = Date.now();
  if (period === "24h") return now - 24 * 60 * 60_000;
  if (period === "30d") return now - 30 * 24 * 60 * 60_000;
  return now - 7 * 24 * 60 * 60_000;
}

function dayLabel(d: Date): string {
  return d.toLocaleDateString(undefined, { weekday: "short" });
}

function MiniLineChart({ points }: { points: { day: string; score: number }[] }) {
  const max = Math.max(1, ...points.map((d) => d.score));
  const w = 320, h = 100;
  const pts = points.map((d, i) => {
    const x = 20 + (i / (points.length - 1 || 1)) * (w - 40);
    const y = h - 20 - ((d.score / max) * (h - 40));
    return `${x},${y}`;
  });
  const pathD  = `M ${pts.join(" L ")}`;
  const fillD  = `M ${pts[0]} L ${pts.join(" L ")} L ${20 + (w - 40)},${h - 20} L 20,${h - 20} Z`;
  return (
    <svg width={w} height={h} viewBox={`0 0 ${w} ${h}`}>
      <defs>
        <linearGradient id="lineGrad" x1="0" y1="0" x2="0" y2="1">
          <stop offset="0%"   stopColor="#4A90D9" stopOpacity="0.4" />
          <stop offset="100%" stopColor="#4A90D9" stopOpacity="0"   />
        </linearGradient>
      </defs>
      <path d={fillD} fill="url(#lineGrad)" />
      <path d={pathD} fill="none" stroke="#4A90D9" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round" />
      {points.map((d, i) => {
        const x = 20 + (i / (points.length - 1 || 1)) * (w - 40);
        const y = h - 20 - ((d.score / max) * (h - 40));
        return (
          <g key={i}>
            <circle cx={x} cy={y} r="5" fill="#0F1F3D" stroke="#4A90D9" strokeWidth="2" />
            <text x={x} y={h - 4} textAnchor="middle" fontSize="10" fill="#5A7BA6">{d.day}</text>
          </g>
        );
      })}
    </svg>
  );
}

function DonutChart({ rows }: { rows: BreakdownRow[] }) {
  const r = 50, cx = 70, cy = 70;
  const total = rows.reduce((s, d) => s + d.count, 0);
  let cumulative = 0;
  const arcs = rows.map((d) => {
    const start = (cumulative / total) * 360;
    cumulative += d.count;
    const end   = (cumulative / total) * 360;
    const s2r   = (deg: number) => (deg - 90) * (Math.PI / 180);
    const x1 = cx + r * Math.cos(s2r(start)), y1 = cy + r * Math.sin(s2r(start));
    const x2 = cx + r * Math.cos(s2r(end)),   y2 = cy + r * Math.sin(s2r(end));
    return { ...d, path: `M ${cx} ${cy} L ${x1} ${y1} A ${r} ${r} 0 ${end - start > 180 ? 1 : 0} 1 ${x2} ${y2} Z` };
  });
  return (
    <div style={{ display: "flex", alignItems: "center", gap: 16 }}>
      <svg width="140" height="140" viewBox="0 0 140 140">
        {total === 0 ? (
          <circle cx={cx} cy={cy} r={r} fill="none" stroke="#1A3C6B" strokeWidth="18" opacity="0.6" />
        ) : (
          arcs.map((a, i) => <path key={i} d={a.path} fill={a.color} opacity="0.9" />)
        )}
        <circle cx={cx} cy={cy} r="28" fill="#0F1F3D" />
        <text x={cx} y={cx - 4}  textAnchor="middle" fontSize="16" fontWeight="800" fill="#fff">{total}</text>
        <text x={cx} y={cx + 10} textAnchor="middle" fontSize="9"  fill="#8BA3C7">THREATS</text>
      </svg>
      <div style={{ flex: 1 }}>
        {rows.map((d) => (
          <div key={d.label} style={{ display: "flex", alignItems: "center", gap: 8, marginBottom: 8 }}>
            <div style={{ width: 8, height: 8, borderRadius: "50%", background: d.color, flexShrink: 0 }} />
            <div style={{ flex: 1, fontSize: 12 }}>{d.label}</div>
            <div style={{ fontSize: 12, color: d.color, fontWeight: 600 }}>{d.count}</div>
          </div>
        ))}
      </div>
    </div>
  );
}

export function DashboardScreen() {
  const [period, setPeriod] = useState("7d");
  const apiBase = useMemo(() => getApiBase(), []);
  const [threats, setThreats] = useState<Threat[] | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let cancelled = false;
    async function load() {
      setThreats(null);
      setError(null);
      try {
        const since = sinceMsForPeriod(period);
        const res = await fetch(`${apiBase}/threats/since?since=${since}&limit=2000`);
        if (!res.ok) throw new Error(`Threats request failed (${res.status})`);
        const json = (await res.json()) as { threats: Threat[] };
        if (cancelled) return;
        setThreats(json.threats);
      } catch (e) {
        if (cancelled) return;
        setError(e instanceof Error ? e.message : String(e));
        setThreats([]);
      }
    }
    void load();
    return () => {
      cancelled = true;
    };
  }, [apiBase, period]);

  const computed = useMemo(() => {
    const list = threats ?? [];

    // Breakdown by type
    const typeMap: Record<string, BreakdownRow> = {
      SMISHING: { label: "Smishing", count: 0, color: "#C0392B" },
      VISHING: { label: "Vishing", count: 0, color: "#E67E22" },
      URL_SCAM: { label: "URL Scam", count: 0, color: "#9B59B6" },
    };
    for (const t of list) {
      if (!typeMap[t.type]) typeMap[t.type] = { label: t.type, count: 0, color: "#3498DB" };
      typeMap[t.type]!.count += 1;
    }
    const breakdownRows = Object.values(typeMap).filter((r) => r.count > 0);

    // Top blocked senders = confirmed/likely, grouped by sender
    const isBlockedBand = (b: string) => b === "CONFIRMED" || b === "LIKELY";
    const bySender = new Map<string, { count: number; type: string }>();
    for (const t of list) {
      if (!isBlockedBand(t.band)) continue;
      const cur = bySender.get(t.sender);
      if (!cur) bySender.set(t.sender, { count: 1, type: t.type });
      else cur.count += 1;
    }
    const topBlocked: TopBlockedRow[] = Array.from(bySender.entries())
      .sort((a, b) => b[1].count - a[1].count)
      .slice(0, 4)
      .map(([sender, v]) => ({ sender, count: v.count, type: v.type }));

    // Risk score history: average score per day label (last 7 points)
    const scoreByDay = new Map<string, { sum: number; n: number }>();
    for (const t of list) {
      const d = new Date(t.timestamp);
      if (Number.isNaN(d.getTime())) continue;
      const label = dayLabel(d);
      const cur = scoreByDay.get(label) ?? { sum: 0, n: 0 };
      cur.sum += t.score;
      cur.n += 1;
      scoreByDay.set(label, cur);
    }

    const points = Array.from(scoreByDay.entries()).map(([day, v]) => ({
      day,
      score: Math.round(v.sum / Math.max(1, v.n)),
    }));

    const avgScore = list.length ? Math.round(list.reduce((s, t) => s + t.score, 0) / list.length) : 0;
    const confirmedLikely = list.filter((t) => isBlockedBand(t.band)).length;
    const reported = list.filter((t) => Boolean(t.reported)).length;

    return {
      breakdownRows,
      topBlocked,
      points: points.length ? points.slice(-7) : [{ day: "—", score: 0 }],
      avgScore,
      confirmedLikely,
      reported,
      total: list.length,
    };
  }, [threats]);

  return (
    <div style={{
      width: 390, minHeight: 844, background: "#0F1F3D",
      fontFamily: "'Inter', -apple-system, sans-serif",
      color: "#fff", display: "flex", flexDirection: "column", overflow: "hidden",
    }}>
      {/* Header */}
      <div style={{ padding: "46px 20px 16px", background: "#0A1628", borderBottom: "1px solid #1A3C6B", position: "relative" }}>
        <div style={{ display: "flex", alignItems: "center" }}>
          <div style={{ flex: 1 }}>
            <div style={{ fontSize: 20, fontWeight: 700 }}>Dashboard</div>
            <div style={{ fontSize: 12, color: "#8BA3C7" }}>Threat analytics & history</div>
          </div>
          <AegisNavMenu current="DashboardScreen" />
        </div>
      </div>

      <div style={{ flex: 1, overflowY: "auto" }}>
        {/* Period selector */}
        <div style={{ display: "flex", gap: 8, padding: "16px 16px 0" }}>
          {["24h", "7d", "30d"].map((p) => (
            <button key={p} onClick={() => setPeriod(p)} style={{
              padding: "6px 16px",
              background: period === p ? "#1A3C6B" : "#132040",
              color: period === p ? "#4A90D9" : "#5A7BA6",
              border: `1px solid ${period === p ? "#4A90D9" : "#1A3C6B"}`,
              borderRadius: 20, fontSize: 12, cursor: "pointer",
            }}>{p}</button>
          ))}
        </div>

        {error && (
          <div style={{ padding: "10px 16px 0", fontSize: 11, color: "#E67E22" }}>
            Unable to load live data: {error}
          </div>
        )}

        <div style={{ margin: "16px 16px", padding: "16px", background: "#132040", borderRadius: 16 }}>
          <div style={{ fontSize: 13, fontWeight: 600, marginBottom: 14 }}>Risk Score History</div>
          {threats === null ? (
            <div style={{ fontSize: 12, color: "#5A7BA6" }}>Loading…</div>
          ) : (
            <MiniLineChart points={computed.points} />
          )}
        </div>

        <div style={{ margin: "0 16px 16px", padding: "16px", background: "#132040", borderRadius: 16 }}>
          <div style={{ fontSize: 13, fontWeight: 600, marginBottom: 14 }}>Threat Breakdown</div>
          {threats === null ? (
            <div style={{ fontSize: 12, color: "#5A7BA6" }}>Loading…</div>
          ) : (
            <DonutChart rows={computed.breakdownRows.length ? computed.breakdownRows : [
              { label: "No data", count: 0, color: "#3498DB" },
            ]} />
          )}
        </div>

        <div style={{ margin: "0 16px 16px" }}>
          <div style={{ fontSize: 13, fontWeight: 600, marginBottom: 10 }}>Top Blocked Senders</div>
          {(computed.topBlocked.length ? computed.topBlocked : [{ sender: "—", count: 0, type: "" }]).map((b, i) => (
            <div key={i} style={{
              display: "flex", alignItems: "center", gap: 12,
              padding: "12px 14px", marginBottom: 8, background: "#132040", borderRadius: 12,
            }}>
              <div style={{ width: 36, height: 36, borderRadius: "50%", background: "#1A3C6B", display: "flex", alignItems: "center", justifyContent: "center", fontSize: 14, fontWeight: 700, color: "#4A90D9" }}>#{i + 1}</div>
              <div style={{ flex: 1 }}>
                <div style={{ fontSize: 13, fontWeight: 600 }}>{b.sender}</div>
                <div style={{ fontSize: 11, color: "#8BA3C7" }}>{b.type}</div>
              </div>
              <div style={{ padding: "4px 10px", background: "#C0392B22", border: "1px solid #C0392B44", borderRadius: 20, fontSize: 12, color: "#C0392B" }}>{b.count}x</div>
            </div>
          ))}
        </div>

        <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 10, margin: "0 16px 16px" }}>
          {[
            { label: "Avg Risk Score",  value: String(computed.avgScore), color: "#4A90D9" },
            { label: "Blocked (C/L)",   value: String(computed.confirmedLikely), color: "#C0392B" },
            { label: "Total Threats",   value: String(computed.total), color: "#E67E22" },
            { label: "Crowd Reports",   value: String(computed.reported), color: "#9B59B6" },
          ].map((s) => (
            <div key={s.label} style={{ padding: "14px", background: "#132040", borderRadius: 14, textAlign: "center" }}>
              <div style={{ fontSize: 22, fontWeight: 800, color: s.color }}>{s.value}</div>
              <div style={{ fontSize: 11, color: "#8BA3C7", marginTop: 4 }}>{s.label}</div>
            </div>
          ))}
        </div>

        <div style={{ padding: "0 16px 32px" }}>
          <button style={{ width: "100%", padding: "14px", background: "#132040", color: "#4A90D9", border: "1px solid #2A5C9B", borderRadius: 14, fontSize: 14, fontWeight: 600, cursor: "pointer" }}>
            📄 Export Threat Report (PDF)
          </button>
        </div>
      </div>
    </div>
  );
}
