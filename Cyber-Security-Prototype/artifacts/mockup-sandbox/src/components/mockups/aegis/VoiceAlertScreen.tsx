import { useState, useEffect } from "react";
import { AegisNavMenu } from "./shared/AegisNavMenu";

function WaveformBars() {
  const heights = [12, 20, 32, 24, 40, 28, 36, 18, 44, 30, 38, 22, 34, 16, 26];
  return (
    <div style={{ display: "flex", alignItems: "center", gap: 3, height: 50 }}>
      {heights.map((h, i) => (
        <div key={i} style={{
          width: 4, borderRadius: 2, background: "#C0392B", height: h,
          animation: `wave ${0.5 + i * 0.07}s ease-in-out infinite alternate`, opacity: 0.8,
        }} />
      ))}
      <style>{`@keyframes wave { from { transform: scaleY(0.4); } to { transform: scaleY(1.2); } }`}</style>
    </div>
  );
}

function PulsingAlert() {
  return (
    <div style={{ position: "relative", display: "flex", alignItems: "center", justifyContent: "center", width: 120, height: 120 }}>
      {[1, 2, 3].map((i) => (
        <div key={i} style={{
          position: "absolute", width: 40 + i * 24, height: 40 + i * 24,
          borderRadius: "50%", border: "2px solid #C0392B",
          opacity: 0, animation: `ripple 2s ${i * 0.6}s ease-out infinite`,
        }} />
      ))}
      <div style={{
        width: 64, height: 64, borderRadius: "50%", background: "#C0392B",
        display: "flex", alignItems: "center", justifyContent: "center",
        fontSize: 30, zIndex: 1, boxShadow: "0 0 24px #C0392B88",
      }}>🛡️</div>
      <style>{`@keyframes ripple { 0% { transform:scale(0.8); opacity:0.8; } 100% { transform:scale(2); opacity:0; } }`}</style>
    </div>
  );
}

export function VoiceAlertScreen() {
  const [challengeSent, setChallengeSent] = useState(false);
  const [elapsed, setElapsed] = useState(0);
  useEffect(() => { const iv = setInterval(() => setElapsed(p => p + 1), 1000); return () => clearInterval(iv); }, []);
  const mins = String(Math.floor(elapsed / 60)).padStart(2, "0");
  const secs = String(elapsed % 60).padStart(2, "0");

  return (
    <div style={{
      width: 390, minHeight: 844, background: "rgba(10,10,10,0.97)",
      fontFamily: "'Inter', -apple-system, sans-serif",
      color: "#fff", display: "flex", flexDirection: "column", overflow: "hidden", position: "relative",
    }}>
      <div style={{ position: "absolute", inset: 0, background: "linear-gradient(180deg, #1a0000 0%, #0A0A0A 60%)", zIndex: 0 }} />

      <div style={{ position: "relative", zIndex: 1, display: "flex", flexDirection: "column", flex: 1 }}>
        {/* Status bar */}
        <div style={{ padding: "12px 20px 0", display: "flex", justifyContent: "space-between", fontSize: 12, color: "#8BA3C7", position: "relative" }}>
          <span>9:41</span>
          <span style={{ color: "#C0392B", fontWeight: 600 }}>⚠ CALL ANALYSIS ACTIVE</span>
          <AegisNavMenu current="VoiceAlertScreen" />
        </div>

        {/* Call info */}
        <div style={{ textAlign: "center", padding: "20px 20px 0" }}>
          <div style={{ fontSize: 14, color: "#8BA3C7", marginBottom: 6 }}>Incoming call</div>
          <div style={{ fontSize: 24, fontWeight: 700 }}>+91 98765 43210</div>
          <div style={{ fontSize: 13, color: "#8BA3C7", marginTop: 4 }}>Unknown · {mins}:{secs}</div>
        </div>

        <div style={{ display: "flex", justifyContent: "center", padding: "16px 0" }}>
          <WaveformBars />
        </div>

        {/* Alert card */}
        <div style={{
          margin: "8px 16px",
          background: "linear-gradient(135deg, #3D0000 0%, #1A0000 100%)",
          border: "2px solid #C0392B", borderRadius: 20, padding: "20px",
          boxShadow: "0 0 40px #C0392B44",
        }}>
          <div style={{ display: "flex", justifyContent: "center", marginBottom: 16 }}>
            <PulsingAlert />
          </div>

          <div style={{ textAlign: "center", marginBottom: 16 }}>
            <div style={{ fontSize: 13, color: "#FFAAAA", fontWeight: 600, letterSpacing: 2, marginBottom: 8 }}>AI VOICE DETECTED</div>
            <div style={{ fontSize: 42, fontWeight: 800, color: "#C0392B", lineHeight: 1 }}>82%</div>
            <div style={{ fontSize: 13, color: "#FFAAAA", marginTop: 4 }}>probability this voice is AI-generated</div>
          </div>

          <div style={{ background: "#0A0000", borderRadius: 12, padding: "12px 14px", marginBottom: 16 }}>
            <div style={{ fontSize: 11, color: "#8BA3C7", marginBottom: 8 }}>DETECTED ARTIFACTS</div>
            {[
              { label: "Spectral Flatness", value: 88 },
              { label: "MFCC Anomaly",      value: 76 },
              { label: "Pitch Regularity",  value: 84 },
            ].map((a) => (
              <div key={a.label} style={{ marginBottom: 8 }}>
                <div style={{ display: "flex", justifyContent: "space-between", marginBottom: 3 }}>
                  <span style={{ fontSize: 11, color: "#CCC" }}>{a.label}</span>
                  <span style={{ fontSize: 11, color: "#C0392B" }}>{a.value}%</span>
                </div>
                <div style={{ height: 4, background: "#222", borderRadius: 2 }}>
                  <div style={{ height: "100%", borderRadius: 2, background: "#C0392B", width: `${a.value}%` }} />
                </div>
              </div>
            ))}
          </div>

          <div style={{ fontSize: 12, color: "#FFAAAA", textAlign: "center", lineHeight: 1.5, marginBottom: 16 }}>
            Voice analysis indicates this may be an AI-cloned voice. The caller may not be who they claim.
          </div>

          <div style={{ display: "flex", flexDirection: "column", gap: 10 }}>
            <button onClick={() => setChallengeSent(true)} style={{
              padding: "14px",
              background: challengeSent ? "#1E6B45" : "#1A3C6B",
              color: "#fff", border: `1px solid ${challengeSent ? "#1E6B45" : "#2A5C9B"}`,
              borderRadius: 14, fontSize: 14, fontWeight: 700, cursor: "pointer", transition: "all 0.3s",
            }}>{challengeSent ? "✓ CHALLENGE SENT" : "🔐 SEND SAFE-WORD CHALLENGE"}</button>
            <div style={{ display: "flex", gap: 10 }}>
              <button style={{ flex: 1, padding: "12px", background: "#C0392B", color: "#fff", border: "none", borderRadius: 14, fontSize: 13, fontWeight: 700, cursor: "pointer" }}>📵 END CALL</button>
              <button style={{ flex: 1, padding: "12px", background: "#1A1A1A", color: "#888", border: "1px solid #333", borderRadius: 14, fontSize: 13, cursor: "pointer" }}>DISMISS</button>
            </div>
          </div>
        </div>

        <div style={{ textAlign: "center", padding: "12px 0", fontSize: 11, color: "#5A7BA6" }}>
          Aegis is analyzing audio in real-time...
        </div>
      </div>
    </div>
  );
}
