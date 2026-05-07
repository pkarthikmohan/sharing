import { useState, useEffect } from "react";
import { AegisNavMenu } from "./shared/AegisNavMenu";

const CONTACTS = [
  { name: "Priya Sharma", relation: "Wife",   number: "+91 98765 00001", registered: true },
  { name: "Ravi Kumar",   relation: "Father", number: "+91 98765 00002", registered: true },
  { name: "Neha Singh",   relation: "Sister", number: "+91 98765 00003", registered: true },
];

type ChallengeState = "idle" | "sending" | "waiting" | "verified" | "failed";

export function SafeWordScreen() {
  const [selected,   setSelected]   = useState<number | null>(null);
  const [state,      setState]      = useState<ChallengeState>("idle");
  const [countdown,  setCountdown]  = useState(30);
  const [pin,        setPin]        = useState("");
  const [showPin,    setShowPin]    = useState(false);
  const familyPin = "482917";

  useEffect(() => {
    if (state !== "waiting") return;
    setCountdown(30);
    const iv = setInterval(() => {
      setCountdown(p => {
        if (p <= 1) { clearInterval(iv); setState("idle"); return 0; }
        return p - 1;
      });
    }, 1000);
    return () => clearInterval(iv);
  }, [state]);

  const sendChallenge = () => {
    if (selected === null) return;
    setState("sending");
    setTimeout(() => setState("waiting"), 1500);
  };

  const bgForState: Record<ChallengeState, string> = {
    idle: "#132040", sending: "#1A3C6B", waiting: "#1A3060",
    verified: "#0D2E1A", failed: "#2E0D0D",
  };

  return (
    <div style={{
      width: 390, minHeight: 844, background: "#0F1F3D",
      fontFamily: "'Inter', -apple-system, sans-serif",
      color: "#fff", display: "flex", flexDirection: "column", overflow: "hidden",
    }}>
      {/* Header */}
      <div style={{ padding: "46px 20px 20px", display: "flex", alignItems: "center", gap: 14, position: "relative" }}>
        <div style={{ fontSize: 28 }}>🔐</div>
        <div style={{ flex: 1 }}>
          <div style={{ fontSize: 20, fontWeight: 700 }}>Safe-Word</div>
          <div style={{ fontSize: 12, color: "#8BA3C7" }}>Zero-knowledge identity challenge</div>
        </div>
        <AegisNavMenu current="SafeWordScreen" />
      </div>

      {/* Info */}
      <div style={{ margin: "0 16px 20px", padding: "12px 14px", background: "#1A3C6B22", border: "1px solid #1A3C6B", borderRadius: 12 }}>
        <div style={{ fontSize: 12, color: "#8BA3C7", lineHeight: 1.6 }}>
          Select a trusted contact to send a silent TOTP challenge.{" "}
          <span style={{ color: "#4A90D9" }}>Your secret never leaves this device.</span>
        </div>
      </div>

      {/* Contacts */}
      <div style={{ padding: "0 16px", marginBottom: 20 }}>
        <div style={{ fontSize: 12, fontWeight: 600, color: "#8BA3C7", marginBottom: 10, letterSpacing: 0.5 }}>TRUSTED CONTACTS</div>
        {CONTACTS.map((c, i) => (
          <div key={i} onClick={() => { setSelected(i); setState("idle"); setPin(""); }} style={{
            padding: "14px", marginBottom: 8,
            background: selected === i ? "#1A3C6B" : "#132040",
            border: `1px solid ${selected === i ? "#4A90D9" : "#1A3C6B55"}`,
            borderRadius: 14, cursor: "pointer", display: "flex", alignItems: "center", gap: 14, transition: "all 0.2s",
          }}>
            <div style={{
              width: 44, height: 44, borderRadius: "50%",
              background: `hsl(${i * 80 + 200}, 60%, 35%)`,
              display: "flex", alignItems: "center", justifyContent: "center", fontSize: 18, fontWeight: 700,
            }}>{c.name[0]}</div>
            <div style={{ flex: 1 }}>
              <div style={{ fontSize: 14, fontWeight: 600 }}>{c.name}</div>
              <div style={{ fontSize: 12, color: "#8BA3C7" }}>{c.relation} · {c.number}</div>
            </div>
            {selected === i && <div style={{ color: "#4A90D9", fontSize: 18 }}>✓</div>}
          </div>
        ))}
      </div>

      {/* Challenge card */}
      <div style={{
        margin: "0 16px 20px", padding: "20px",
        background: bgForState[state], borderRadius: 16,
        border: `1px solid ${state === "verified" ? "#1E6B45" : state === "failed" ? "#C0392B" : "#1A3C6B"}`,
        transition: "all 0.4s",
      }}>
        {state === "idle" && (
          <button onClick={sendChallenge} disabled={selected === null} style={{
            width: "100%", padding: "16px",
            background: selected !== null ? "#1A3C6B" : "#0A1020",
            color: selected !== null ? "#4A90D9" : "#333",
            border: `1px solid ${selected !== null ? "#4A90D9" : "#222"}`,
            borderRadius: 14, fontSize: 15, fontWeight: 700,
            cursor: selected !== null ? "pointer" : "not-allowed",
          }}>Send Silent Challenge</button>
        )}

        {state === "sending" && (
          <div style={{ textAlign: "center" }}>
            <div style={{ fontSize: 24, marginBottom: 10 }}>📡</div>
            <div style={{ fontSize: 14, color: "#4A90D9" }}>Sending secure challenge to {CONTACTS[selected!].name}…</div>
            <div style={{ marginTop: 12, height: 4, background: "#1A3C6B", borderRadius: 2 }}>
              <div style={{ height: "100%", background: "#4A90D9", borderRadius: 2, width: "60%", animation: "progress 1.5s ease infinite" }} />
            </div>
          </div>
        )}

        {state === "waiting" && (
          <div>
            <div style={{ textAlign: "center", marginBottom: 16 }}>
              <div style={{ fontSize: 12, color: "#4A90D9", marginBottom: 8 }}>CHALLENGE SENT · WAITING FOR CALLER</div>
              <div style={{ position: "relative", display: "inline-flex", alignItems: "center", justifyContent: "center" }}>
                <svg width="80" height="80">
                  <circle cx="40" cy="40" r="34" fill="none" stroke="#1A3C6B" strokeWidth="6" />
                  <circle cx="40" cy="40" r="34" fill="none" stroke="#4A90D9" strokeWidth="6"
                    strokeDasharray={`${2 * Math.PI * 34}`}
                    strokeDashoffset={2 * Math.PI * 34 * (1 - countdown / 30)}
                    strokeLinecap="round" transform="rotate(-90 40 40)"
                    style={{ transition: "stroke-dashoffset 1s linear" }} />
                </svg>
                <div style={{ position: "absolute", fontSize: 22, fontWeight: 700, color: "#4A90D9" }}>{countdown}</div>
              </div>
              <div style={{ fontSize: 12, color: "#8BA3C7", marginTop: 8 }}>seconds remaining</div>
            </div>
            <div style={{ marginBottom: 14 }}>
              <div style={{ fontSize: 11, color: "#8BA3C7", marginBottom: 6 }}>Simulating family device — they see:</div>
              <div style={{
                padding: "10px", background: "#0A1628", borderRadius: 10,
                textAlign: "center", fontSize: 24, fontWeight: 800, letterSpacing: 8,
                color: "#4A90D9", border: "1px dashed #2A5C9B",
              }}>{showPin ? familyPin : "••••••"}</div>
              <button onClick={() => setShowPin(p => !p)} style={{
                width: "100%", marginTop: 6, padding: "6px", background: "none", border: "none",
                color: "#5A7BA6", fontSize: 11, cursor: "pointer",
              }}>{showPin ? "Hide PIN (demo)" : "Show PIN (demo)"}</button>
            </div>
            <div style={{ fontSize: 12, color: "#8BA3C7", marginBottom: 8 }}>Ask caller to state their PIN:</div>
            <input type="text" maxLength={6} value={pin}
              onChange={(e) => setPin(e.target.value.replace(/\D/g, "").slice(0, 6))}
              placeholder="Enter 6-digit PIN"
              style={{
                width: "100%", padding: "14px", textAlign: "center",
                fontSize: 24, fontWeight: 700, letterSpacing: 8,
                background: "#0A1628", border: "1px solid #2A5C9B",
                borderRadius: 12, color: "#fff", boxSizing: "border-box",
              }} />
            <button onClick={() => setState(pin === familyPin ? "verified" : "failed")} disabled={pin.length !== 6} style={{
              width: "100%", marginTop: 12, padding: "14px",
              background: pin.length === 6 ? "#1A3C6B" : "#0A1020",
              color: pin.length === 6 ? "#fff" : "#333",
              border: "none", borderRadius: 12, fontSize: 14, fontWeight: 700,
              cursor: pin.length === 6 ? "pointer" : "not-allowed",
            }}>Verify Identity</button>
          </div>
        )}

        {state === "verified" && (
          <div style={{ textAlign: "center" }}>
            <div style={{ fontSize: 48, marginBottom: 12 }}>✅</div>
            <div style={{ fontSize: 18, fontWeight: 800, color: "#1E6B45", marginBottom: 8 }}>Identity Verified</div>
            <div style={{ fontSize: 13, color: "#88CCAA", lineHeight: 1.6 }}>
              This is a real call from <strong>{CONTACTS[selected!].name}</strong>. TOTP matched.
            </div>
            <button onClick={() => { setState("idle"); setPin(""); setSelected(null); setShowPin(false); }} style={{
              marginTop: 16, padding: "10px 24px", background: "#1E6B45", color: "#fff",
              border: "none", borderRadius: 10, cursor: "pointer", fontSize: 13,
            }}>Close</button>
          </div>
        )}

        {state === "failed" && (
          <div style={{ textAlign: "center" }}>
            <div style={{ fontSize: 48, marginBottom: 12 }}>🚫</div>
            <div style={{ fontSize: 18, fontWeight: 800, color: "#C0392B", marginBottom: 8 }}>Verification Failed</div>
            <div style={{ fontSize: 13, color: "#FFAAAA", lineHeight: 1.6 }}>
              This is NOT {CONTACTS[selected!].name}. End this call immediately.
            </div>
            <button onClick={() => { setState("idle"); setPin(""); setShowPin(false); }} style={{
              marginTop: 16, padding: "10px 24px", background: "#C0392B", color: "#fff",
              border: "none", borderRadius: 10, cursor: "pointer", fontSize: 13,
            }}>Try Again</button>
          </div>
        )}
      </div>
      <style>{`@keyframes progress { 0% { width: 20% } 100% { width: 100% } }`}</style>
    </div>
  );
}
