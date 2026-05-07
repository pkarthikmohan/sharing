import { useState, useEffect } from "react";
import { AegisNavMenu } from "./shared/AegisNavMenu";

const SCORE = 94;
const THREAT_VECTORS = [
  { name: "Urgency",           score: 98, icon: "⚡" },
  { name: "Financial Demand",  score: 92, icon: "💰" },
  { name: "Authority Spoof",   score: 95, icon: "🏛️" },
  { name: "Link Lure",         score: 88, icon: "🔗" },
  { name: "Impersonation",     score: 91, icon: "👤" },
];

function RadialGauge({ score, size = 200 }: { score: number; size?: number }) {
  const [animated, setAnimated] = useState(0);
  useEffect(() => { const t = setTimeout(() => setAnimated(score), 300); return () => clearTimeout(t); }, [score]);
  const r = 80, circumference = 2 * Math.PI * r, arc = (3 / 4) * circumference;
  const offset = arc - (animated / 100) * arc;
  const angle  = -135 + (animated / 100) * 270;
  const color  = animated >= 90 ? "#C0392B" : animated >= 75 ? "#E67E22" : animated >= 40 ? "#F1C40F" : "#1E6B45";
  return (
    <div style={{ position: "relative", width: size, height: size }}>
      <svg width={size} height={size} viewBox="0 0 200 200">
        <circle cx="100" cy="100" r={r} fill="none" stroke="#1A3C6B" strokeWidth="12"
          strokeDasharray={`${arc} ${circumference - arc}`}
          strokeDashoffset={-circumference * (1 / 8)}
          strokeLinecap="round" transform="rotate(135 100 100)" />
        <circle cx="100" cy="100" r={r} fill="none" stroke={color} strokeWidth="12"
          strokeDasharray={`${arc} ${circumference - arc}`}
          strokeDashoffset={offset - circumference * (1 / 8)}
          strokeLinecap="round" transform="rotate(135 100 100)"
          style={{ transition: "stroke-dashoffset 1.2s cubic-bezier(0.4,0,0.2,1), stroke 0.5s" }} />
        <g transform={`rotate(${angle} 100 100)`}>
          <line x1="100" y1="100" x2="100" y2="32" stroke={color} strokeWidth="3" strokeLinecap="round" />
          <circle cx="100" cy="100" r="6" fill={color} />
        </g>
        <text x="100" y="108" textAnchor="middle" fontSize="32" fontWeight="800" fill={color}>{animated}</text>
        <text x="100" y="128" textAnchor="middle" fontSize="12" fill="#8BA3C7">RISK SCORE</text>
      </svg>
    </div>
  );
}

export function AlertScreen() {
  const [blocked,  setBlocked]  = useState(false);
  const [reported, setReported] = useState(false);

  return (
    <div style={{
      width: 390, minHeight: 844, background: "#0A0A0A",
      fontFamily: "'Inter', -apple-system, sans-serif",
      color: "#fff", display: "flex", flexDirection: "column", overflow: "hidden",
    }}>
      {/* Red alert header */}
      <div style={{
        background: "linear-gradient(180deg, #C0392B 0%, #8B0000 100%)",
        padding: "14px 20px 12px",
        display: "flex", alignItems: "center", gap: 12, position: "relative",
      }}>
        <div style={{ fontSize: 24, animation: "pulse 1s infinite" }}>⚠️</div>
        <div style={{ flex: 1 }}>
          <div style={{ fontSize: 16, fontWeight: 800, letterSpacing: 1 }}>SCAM DETECTED</div>
          <div style={{ fontSize: 11, color: "#FFAAAA" }}>Intercepted before delivery</div>
        </div>
        <AegisNavMenu current="AlertScreen" />
      </div>

      {/* Gauge */}
      <div style={{ display: "flex", justifyContent: "center", padding: "20px 0 8px" }}>
        <RadialGauge score={SCORE} size={200} />
      </div>

      {/* Band */}
      <div style={{ textAlign: "center", marginBottom: 16 }}>
        <div style={{
          display: "inline-block", background: "#C0392B", color: "#fff",
          fontSize: 15, fontWeight: 800, letterSpacing: 3, padding: "6px 24px", borderRadius: 30,
        }}>CONFIRMED SCAM</div>
      </div>

      {/* SMS preview */}
      <div style={{ margin: "0 16px 16px", padding: "12px 14px", background: "#1A1A1A", borderRadius: 12, border: "1px solid #333" }}>
        <div style={{ fontSize: 11, color: "#C0392B", fontWeight: 600, marginBottom: 6 }}>INTERCEPTED MESSAGE · VM-TRAI</div>
        <div style={{ fontSize: 12, color: "#CCC", lineHeight: 1.5 }}>
          "Dear customer, your vehicle MH02XX1234 has an unpaid e-challan of Rs.1,500. Pay immediately at{" "}
          <span style={{ color: "#FF6B6B", textDecoration: "line-through" }}>http://echallan-paytm.xyz/pay</span>
          {" "}to avoid legal action. - Govt of India"
        </div>
      </div>

      {/* Threat vectors */}
      <div style={{ padding: "0 16px", marginBottom: 16 }}>
        <div style={{ fontSize: 12, fontWeight: 600, color: "#8BA3C7", marginBottom: 10, letterSpacing: 0.5 }}>THREAT VECTORS</div>
        {THREAT_VECTORS.map((tv) => (
          <div key={tv.name} style={{ marginBottom: 8 }}>
            <div style={{ display: "flex", justifyContent: "space-between", marginBottom: 4 }}>
              <span style={{ fontSize: 12, color: "#CCC" }}>{tv.icon} {tv.name}</span>
              <span style={{ fontSize: 12, fontWeight: 600, color: "#C0392B" }}>{tv.score}%</span>
            </div>
            <div style={{ height: 6, background: "#222", borderRadius: 3 }}>
              <div style={{ height: "100%", borderRadius: 3, background: "linear-gradient(90deg, #C0392B, #E74C3C)", width: `${tv.score}%`, transition: "width 1s ease" }} />
            </div>
          </div>
        ))}
      </div>

      {/* Explanation */}
      <div style={{ margin: "0 16px 16px", padding: "12px 14px", background: "#1A1A1A", borderRadius: 12 }}>
        <div style={{ fontSize: 11, color: "#8BA3C7", marginBottom: 6 }}>WHY THIS IS SUSPICIOUS</div>
        <div style={{ fontSize: 12, color: "#CCC", lineHeight: 1.6 }}>
          This message impersonates a government authority, creates false urgency around a fake fine,
          and links to a suspicious .xyz domain unrelated to any official system. All 5 threat vectors triggered.
        </div>
      </div>

      {/* Actions */}
      <div style={{ padding: "0 16px 8px", display: "flex", flexDirection: "column", gap: 10 }}>
        <div style={{ display: "flex", gap: 10 }}>
          <button onClick={() => setBlocked(true)} style={{
            flex: 1, padding: "14px",
            background: blocked ? "#1E6B45" : "#C0392B", color: "#fff",
            border: "none", borderRadius: 14, fontSize: 14, fontWeight: 700, cursor: "pointer", transition: "background 0.3s",
          }}>{blocked ? "✓ BLOCKED" : "🚫 BLOCK"}</button>
          <button onClick={() => setReported(true)} style={{
            flex: 1, padding: "14px",
            background: reported ? "#1A3C6B" : "#E67E22", color: "#fff",
            border: "none", borderRadius: 14, fontSize: 14, fontWeight: 700, cursor: "pointer", transition: "background 0.3s",
          }}>{reported ? "✓ REPORTED" : "📢 REPORT"}</button>
        </div>
        <button style={{
          width: "100%", padding: "12px",
          background: "#1A3C6B", color: "#4A90D9", border: "1px solid #2A5C9B", borderRadius: 14,
          fontSize: 13, fontWeight: 600, cursor: "pointer",
        }}>🔍 Investigate Scam URL</button>
        <button style={{ width: "100%", padding: "10px", background: "transparent", color: "#666", border: "none", borderRadius: 14, fontSize: 13, cursor: "pointer" }}>DISMISS</button>
      </div>
      <style>{`@keyframes pulse { 0%,100% { opacity:1 } 50% { opacity:0.5 } }`}</style>
    </div>
  );
}
