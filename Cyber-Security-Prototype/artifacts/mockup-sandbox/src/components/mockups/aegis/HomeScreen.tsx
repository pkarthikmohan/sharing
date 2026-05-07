import { useEffect, useMemo, useState } from "react";
import { AegisNavMenu } from "./shared/AegisNavMenu";

const bandColor: Record<string, string> = {
  CONFIRMED: "#C0392B",
  LIKELY:    "#E67E22",
  SUSPICIOUS:"#F1C40F",
  SAFE:      "#1E6B45",
};

type Threat = {
  id: number;
  type: string;
  sender: string;
  body?: string | null;
  score: number;
  band: keyof typeof bandColor;
  timestamp: string;
};

type Stats = {
  smsScanned: number;
  callsMonitored: number;
  threatsBlocked: number;
};

function formatAgo(ts: string): string {
  const t = new Date(ts).getTime();
  if (!Number.isFinite(t)) return "";
  const diff = Date.now() - t;
  if (diff < 60_000) return "just now";
  if (diff < 3_600_000) return `${Math.floor(diff / 60_000)} min ago`;
  if (diff < 86_400_000) return `${Math.floor(diff / 3_600_000)} hr ago`;
  return `${Math.floor(diff / 86_400_000)} days ago`;
}

function getApiBase(): string {
  const env = (import.meta as any).env as Record<string, unknown> | undefined;
  const configured = typeof env?.VITE_API_BASE_URL === "string" ? env.VITE_API_BASE_URL : "";
  return (configured || "http://localhost:5000/api").replace(/\/$/, "");
}

function ShieldIcon({ size = 64, pulse = false }: { size?: number; pulse?: boolean }) {
  return (
    <div className={pulse ? "animate-pulse" : ""} style={{ position: "relative", display: "inline-flex" }}>
      <svg width={size} height={size} viewBox="0 0 64 64" fill="none">
        <path d="M32 4L8 14v18c0 14 10 26 24 30 14-4 24-16 24-30V14L32 4z"
          fill="#1A3C6B" stroke="#4A90D9" strokeWidth="2" />
        {[[32,24],[22,32],[32,32],[42,32],[26,40],[38,40]].map(([cx,cy],i) => (
          <circle key={i} cx={cx} cy={cy} r="2.5" fill="#4A90D9" opacity="0.9" />
        ))}
        <line x1="32" y1="24" x2="22" y2="32" stroke="#4A90D9" strokeWidth="1" opacity="0.6" />
        <line x1="32" y1="24" x2="32" y2="32" stroke="#4A90D9" strokeWidth="1" opacity="0.6" />
        <line x1="32" y1="24" x2="42" y2="32" stroke="#4A90D9" strokeWidth="1" opacity="0.6" />
        <line x1="22" y1="32" x2="26" y2="40" stroke="#4A90D9" strokeWidth="1" opacity="0.6" />
        <line x1="32" y1="32" x2="26" y2="40" stroke="#4A90D9" strokeWidth="1" opacity="0.6" />
        <line x1="32" y1="32" x2="38" y2="40" stroke="#4A90D9" strokeWidth="1" opacity="0.6" />
        <line x1="42" y1="32" x2="38" y2="40" stroke="#4A90D9" strokeWidth="1" opacity="0.6" />
      </svg>
      {pulse && (
        <div style={{
          position: "absolute", inset: -8, borderRadius: "50%",
          border: "2px solid #4A90D9",
          animation: "ping 1.5s cubic-bezier(0,0,0.2,1) infinite",
          opacity: 0.4,
        }} />
      )}
    </div>
  );
}

export function HomeScreen() {
  const apiBase = useMemo(() => getApiBase(), []);
  const [stats, setStats] = useState<Stats | null>(null);
  const [threats, setThreats] = useState<Threat[] | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let cancelled = false;
    async function load() {
      try {
        const [statsRes, threatsRes] = await Promise.all([
          fetch(`${apiBase}/threats/stats`),
          fetch(`${apiBase}/threats/recent?limit=5`),
        ]);

        if (!statsRes.ok) throw new Error(`Stats request failed (${statsRes.status})`);
        if (!threatsRes.ok) throw new Error(`Threats request failed (${threatsRes.status})`);

        const statsJson = (await statsRes.json()) as Stats;
        const threatsJson = (await threatsRes.json()) as { threats: Threat[] };

        if (cancelled) return;
        setStats(statsJson);
        setThreats(threatsJson.threats);
      } catch (e) {
        if (cancelled) return;
        setError(e instanceof Error ? e.message : String(e));
        setStats({ smsScanned: 0, callsMonitored: 0, threatsBlocked: 0 });
        setThreats([]);
      }
    }
    void load();
    return () => {
      cancelled = true;
    };
  }, [apiBase]);

  return (
    <div style={{
      width: 390, minHeight: 844, background: "#0F1F3D",
      fontFamily: "'Inter', -apple-system, sans-serif",
      color: "#fff", display: "flex", flexDirection: "column",
      position: "relative", overflow: "hidden",
    }}>
      {/* Status bar */}
      <div style={{ padding: "12px 20px 0", display: "flex", justifyContent: "space-between", fontSize: 12, color: "#8BA3C7" }}>
        <span>9:41</span>
        <span>▶▶ 📶 🔋</span>
      </div>

      {/* Header */}
      <div style={{ padding: "12px 20px 12px", display: "flex", alignItems: "center", gap: 12, position: "relative" }}>
        <ShieldIcon size={40} pulse />
        <div style={{ flex: 1 }}>
          <div style={{ fontSize: 22, fontWeight: 700, letterSpacing: -0.5 }}>Aegis</div>
          <div style={{ fontSize: 11, color: "#4A90D9", letterSpacing: 1 }}>YOUR AI SHIELD AGAINST SCAMS</div>
        </div>
        <AegisNavMenu current="HomeScreen" />
      </div>

      {/* Protection status card */}
      <div style={{
        margin: "0 16px 16px", padding: "16px",
        background: "linear-gradient(135deg, #1A3C6B 0%, #0D2647 100%)",
        borderRadius: 16, border: "1px solid #2A5C9B",
        display: "flex", alignItems: "center", gap: 14,
      }}>
        <div style={{
          width: 44, height: 44, borderRadius: "50%",
          background: "#1E6B45", display: "flex", alignItems: "center", justifyContent: "center", fontSize: 20,
        }}>✓</div>
        <div>
          <div style={{ fontSize: 14, fontWeight: 600 }}>Aegis is active</div>
          <div style={{ fontSize: 12, color: "#8BA3C7", marginTop: 2 }}>Protecting your calls and SMS</div>
        </div>
        <div style={{ marginLeft: "auto", display: "flex", flexDirection: "column", alignItems: "center" }}>
          <div style={{ width: 10, height: 10, borderRadius: "50%", background: "#1E6B45", boxShadow: "0 0 8px #1E6B45" }} />
          <div style={{ fontSize: 10, color: "#1E6B45", marginTop: 4 }}>LIVE</div>
        </div>
      </div>

      {/* Quick stats */}
      <div style={{ display: "flex", gap: 10, margin: "0 16px 16px" }}>
        {[
          { label: "SMS Scanned",     value: stats?.smsScanned ?? 0, color: "#4A90D9" },
          { label: "Calls Monitored", value: stats?.callsMonitored ?? 0, color: "#9B59B6" },
          { label: "Threats Blocked", value: stats?.threatsBlocked ?? 0, color: "#C0392B" },
        ].map((stat) => (
          <div key={stat.label} style={{
            flex: 1, padding: "12px 8px",
            background: "#132040", borderRadius: 12,
            border: `1px solid ${stat.color}33`, textAlign: "center",
          }}>
            <div style={{ fontSize: 24, fontWeight: 700, color: stat.color }}>{stat.value}</div>
            <div style={{ fontSize: 10, color: "#8BA3C7", marginTop: 2, lineHeight: 1.3 }}>{stat.label}</div>
          </div>
        ))}
      </div>

      {/* Recent threats */}
      <div style={{ padding: "0 16px", flex: 1 }}>
        <div style={{ fontSize: 13, fontWeight: 600, color: "#8BA3C7", marginBottom: 10, letterSpacing: 0.5 }}>
          RECENT THREATS
        </div>
        {error && (
          <div style={{ marginBottom: 10, fontSize: 11, color: "#E67E22" }}>
            Unable to load live data: {error}
          </div>
        )}
        {threats === null ? (
          <div style={{ fontSize: 12, color: "#5A7BA6", padding: "12px 0" }}>Loading…</div>
        ) : threats.length === 0 ? (
          <div style={{ fontSize: 12, color: "#5A7BA6", padding: "12px 0" }}>No threats yet.</div>
        ) : (
          threats.map((t) => {
            const band = t.band in bandColor ? t.band : "SUSPICIOUS";
            const time = formatAgo(t.timestamp);
            return (
              <div key={t.id} style={{
                padding: "12px 14px", marginBottom: 8,
                background: "#132040", borderRadius: 12,
                borderLeft: `3px solid ${bandColor[band]}`,
                display: "flex", alignItems: "center", gap: 12,
              }}>
                <div style={{ flex: 1 }}>
                  <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
                    <span style={{
                      fontSize: 10, fontWeight: 700, color: bandColor[band],
                      background: `${bandColor[band]}22`, padding: "2px 7px", borderRadius: 20,
                    }}>{band}</span>
                    <span style={{ fontSize: 11, color: "#8BA3C7" }}>{t.type}</span>
                  </div>
                  <div style={{ fontSize: 13, color: "#fff", marginTop: 4 }}>{t.sender}</div>
                  <div style={{ fontSize: 11, color: "#5A7BA6", marginTop: 2 }}>{time}</div>
                </div>
                <div style={{
                  width: 40, height: 40, borderRadius: "50%",
                  background: `${bandColor[band]}22`, border: `2px solid ${bandColor[band]}`,
                  display: "flex", alignItems: "center", justifyContent: "center",
                  fontSize: 13, fontWeight: 700, color: bandColor[band],
                }}>{t.score}</div>
              </div>
            );
          })
        )}
      </div>

      <style>{`@keyframes ping { 75%, 100% { transform: scale(1.8); opacity: 0; } }`}</style>
    </div>
  );
}
