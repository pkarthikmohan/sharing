import { useState } from "react";

const SCREENS = [
  { id: "HomeScreen",       label: "Home",          icon: "🏠", color: "#4A90D9",  desc: "Protection status & feed" },
  { id: "AlertScreen",      label: "SMS Alert",     icon: "⚠️", color: "#C0392B",  desc: "Smishing detection demo" },
  { id: "VoiceAlertScreen", label: "Voice Alert",   icon: "🎙️", color: "#E67E22",  desc: "Deepfake voice detection" },
  { id: "SafeWordScreen",   label: "Safe-Word",     icon: "🔐", color: "#1E6B45",  desc: "TOTP identity challenge" },
  { id: "SandboxScreen",    label: "URL Sandbox",   icon: "🔍", color: "#9B59B6",  desc: "Scam page detonator" },
  { id: "DashboardScreen",  label: "Dashboard",     icon: "📊", color: "#3498DB",  desc: "Analytics & history" },
  { id: "SettingsScreen",   label: "Settings",      icon: "⚙️", color: "#95A5A6",  desc: "Configure protection" },
];

export function AegisNavMenu({ current }: { current: string }) {
  const [open, setOpen] = useState(false);

  const navigate = (screenId: string) => {
    if (screenId === current) { setOpen(false); return; }
    window.location.href = `/preview/aegis/${screenId}`;
  };

  return (
    <>
      {/* Trigger button — top-right corner */}
      <button
        onClick={() => setOpen(true)}
        style={{
          position: "absolute", top: 10, right: 16, zIndex: 50,
          width: 36, height: 36, borderRadius: 10,
          background: "#1A3C6B", border: "1px solid #2A5C9B",
          display: "flex", alignItems: "center", justifyContent: "center",
          cursor: "pointer", flexShrink: 0,
          boxShadow: "0 2px 12px #00000044"
        }}
        title="All screens"
      >
        <svg width="18" height="14" viewBox="0 0 18 14" fill="none">
          <rect y="0"  width="18" height="2.5" rx="1.25" fill="#8BBDEE" />
          <rect y="5.75" width="13" height="2.5" rx="1.25" fill="#8BBDEE" />
          <rect y="11.5" width="18" height="2.5" rx="1.25" fill="#8BBDEE" />
        </svg>
      </button>

      {/* Full-screen overlay */}
      {open && (
        <div
          onClick={() => setOpen(false)}
          style={{
            position: "fixed", inset: 0, zIndex: 100,
            background: "rgba(5, 12, 28, 0.92)",
            backdropFilter: "blur(8px)",
            display: "flex", flexDirection: "column",
            padding: "0 0 24px",
            overflowY: "auto",
          }}
        >
          {/* Header */}
          <div
            onClick={(e) => e.stopPropagation()}
            style={{
              padding: "20px 20px 16px",
              borderBottom: "1px solid #1A3C6B",
              display: "flex", alignItems: "center", justifyContent: "space-between",
            }}
          >
            <div style={{ display: "flex", alignItems: "center", gap: 10 }}>
              <svg width="26" height="30" viewBox="0 0 64 64" fill="none">
                <path d="M32 4L8 14v18c0 14 10 26 24 30 14-4 24-16 24-30V14L32 4z"
                  fill="#1A3C6B" stroke="#4A90D9" strokeWidth="2" />
                {[[32,24],[22,32],[32,32],[42,32],[26,40],[38,40]].map(([cx,cy],i) => (
                  <circle key={i} cx={cx} cy={cy} r="2.5" fill="#4A90D9" />
                ))}
                <line x1="32" y1="24" x2="22" y2="32" stroke="#4A90D9" strokeWidth="1.2" opacity="0.7" />
                <line x1="32" y1="24" x2="32" y2="32" stroke="#4A90D9" strokeWidth="1.2" opacity="0.7" />
                <line x1="32" y1="24" x2="42" y2="32" stroke="#4A90D9" strokeWidth="1.2" opacity="0.7" />
                <line x1="22" y1="32" x2="26" y2="40" stroke="#4A90D9" strokeWidth="1.2" opacity="0.7" />
                <line x1="32" y1="32" x2="26" y2="40" stroke="#4A90D9" strokeWidth="1.2" opacity="0.7" />
                <line x1="32" y1="32" x2="38" y2="40" stroke="#4A90D9" strokeWidth="1.2" opacity="0.7" />
                <line x1="42" y1="32" x2="38" y2="40" stroke="#4A90D9" strokeWidth="1.2" opacity="0.7" />
              </svg>
              <div>
                <div style={{ fontSize: 16, fontWeight: 700, color: "#fff" }}>Aegis</div>
                <div style={{ fontSize: 10, color: "#4A90D9", letterSpacing: 1 }}>NAVIGATE SCREENS</div>
              </div>
            </div>
            <button
              onClick={() => setOpen(false)}
              style={{
                width: 34, height: 34, borderRadius: 8,
                background: "#132040", border: "1px solid #1A3C6B",
                color: "#8BA3C7", fontSize: 18, cursor: "pointer",
                display: "flex", alignItems: "center", justifyContent: "center",
              }}
            >×</button>
          </div>

          {/* Screen grid */}
          <div
            onClick={(e) => e.stopPropagation()}
            style={{ padding: "20px 16px", display: "grid", gridTemplateColumns: "1fr 1fr", gap: 12 }}
          >
            {SCREENS.map((s) => {
              const isCurrent = s.id === current;
              return (
                <button
                  key={s.id}
                  onClick={() => navigate(s.id)}
                  style={{
                    padding: "16px 12px",
                    background: isCurrent ? `${s.color}22` : "#132040",
                    border: `1px solid ${isCurrent ? s.color : "#1A3C6B"}`,
                    borderRadius: 14,
                    cursor: isCurrent ? "default" : "pointer",
                    textAlign: "left",
                    transition: "all 0.15s",
                    position: "relative",
                    overflow: "hidden",
                  }}
                >
                  {isCurrent && (
                    <div style={{
                      position: "absolute", top: 8, right: 8,
                      width: 7, height: 7, borderRadius: "50%",
                      background: s.color, boxShadow: `0 0 6px ${s.color}`,
                    }} />
                  )}
                  <div style={{ fontSize: 26, marginBottom: 8 }}>{s.icon}</div>
                  <div style={{ fontSize: 13, fontWeight: 700, color: isCurrent ? s.color : "#fff", marginBottom: 3 }}>
                    {s.label}
                  </div>
                  <div style={{ fontSize: 10, color: "#5A7BA6", lineHeight: 1.4 }}>{s.desc}</div>
                </button>
              );
            })}
          </div>

          {/* Footer */}
          <div
            onClick={(e) => e.stopPropagation()}
            style={{ padding: "0 16px", textAlign: "center" }}
          >
            <div style={{ fontSize: 10, color: "#2A4C6B" }}>Tap outside to dismiss</div>
          </div>
        </div>
      )}
    </>
  );
}
