import { useState, useEffect } from "react";
import { AegisNavMenu } from "./shared/AegisNavMenu";

function FakeScamPage() {
  return (
    <div style={{
      width: "100%", background: "#fff", borderRadius: 8,
      fontFamily: "Arial, sans-serif", fontSize: 12, color: "#333",
      position: "relative", overflow: "hidden", padding: "12px",
    }}>
      <div style={{ background: "#003366", color: "#fff", padding: "8px 12px", borderRadius: "4px 4px 0 0", margin: "-12px -12px 10px" }}>
        <div style={{ fontSize: 10, fontWeight: 700 }}>echallan-paytm.xyz</div>
        <div style={{ fontSize: 9, color: "#AACCFF" }}>🔒 NOT VERIFIED</div>
      </div>
      <div style={{ textAlign: "center", marginBottom: 10 }}>
        <div style={{ fontSize: 14, fontWeight: 700, color: "#003366" }}>Ministry of Road Transport</div>
        <div style={{ fontSize: 10, color: "#666" }}>Government of India (FAKE)</div>
        <div style={{ fontSize: 20 }}>🏛️</div>
      </div>
      <div style={{ background: "#FFF3CD", border: "1px solid #FFC107", padding: "8px", borderRadius: 4, marginBottom: 8 }}>
        <div style={{ fontSize: 11, fontWeight: 700, color: "#856404" }}>⚠️ OUTSTANDING CHALLAN NOTICE</div>
        <div style={{ fontSize: 10, color: "#333", marginTop: 4 }}>Vehicle: MH02XX1234 | Fine: ₹1,500</div>
      </div>
      <div style={{ fontSize: 10, marginBottom: 8, lineHeight: 1.6 }}>Pay immediately to avoid legal action and suspension of driving licence.</div>
      <div style={{ textAlign: "center", marginBottom: 8 }}>
        <div style={{ fontSize: 9, color: "#666", marginBottom: 4 }}>Scan QR to Pay</div>
        <div style={{ width: 60, height: 60, margin: "0 auto", background: "repeating-conic-gradient(#000 0% 25%, #fff 0% 50%) 0 0 / 6px 6px", borderRadius: 4 }} />
      </div>
      <div style={{ background: "#F8F9FA", border: "1px solid #DEE2E6", padding: "6px 8px", borderRadius: 4, fontSize: 9 }}>
        <div>UPI ID: fakegov@paytm</div>
        <div>Account: 9876543210 | IFSC: FAKE001</div>
      </div>
      {/* Watermark */}
      <div style={{ position: "absolute", inset: 0, display: "flex", alignItems: "center", justifyContent: "center", pointerEvents: "none" }}>
        <div style={{
          transform: "rotate(-30deg)", color: "#C0392B", fontSize: 16, fontWeight: 900,
          opacity: 0.65, textAlign: "center", border: "3px solid #C0392B",
          padding: "8px 16px", whiteSpace: "nowrap", letterSpacing: 2,
        }}>KNOWN SCAM<br />DO NOT PAY<br />— Aegis —</div>
      </div>
    </div>
  );
}

export function SandboxScreen() {
  const [loading,  setLoading]  = useState(true);
  const [progress, setProgress] = useState(0);
  useEffect(() => {
    const iv = setInterval(() => {
      setProgress(p => { if (p >= 100) { clearInterval(iv); setLoading(false); return 100; } return p + 8; });
    }, 250);
    return () => clearInterval(iv);
  }, []);

  return (
    <div style={{
      width: 390, minHeight: 844, background: "#0F1F3D",
      fontFamily: "'Inter', -apple-system, sans-serif",
      color: "#fff", display: "flex", flexDirection: "column", overflow: "hidden",
    }}>
      {/* Header */}
      <div style={{ background: "#0A1628", padding: "46px 20px 16px", borderBottom: "1px solid #1A3C6B", position: "relative" }}>
        <div style={{ display: "flex", alignItems: "center", gap: 12 }}>
          <div style={{ fontSize: 22 }}>🔍</div>
          <div style={{ flex: 1 }}>
            <div style={{ fontSize: 18, fontWeight: 700 }}>URL Sandbox</div>
            <div style={{ fontSize: 11, color: "#8BA3C7" }}>Detonating in isolated container</div>
          </div>
          <AegisNavMenu current="SandboxScreen" />
        </div>
      </div>

      {/* URL */}
      <div style={{ margin: "16px 16px 0", padding: "10px 14px", background: "#132040", borderRadius: 12 }}>
        <div style={{ fontSize: 10, color: "#8BA3C7", marginBottom: 4 }}>SUSPICIOUS URL</div>
        <div style={{ fontSize: 12, color: "#FF6B6B", fontFamily: "monospace", wordBreak: "break-all" }}>
          http://echallan-paytm.xyz/pay?ref=MH02XX1234
        </div>
      </div>

      {loading ? (
        <div style={{ flex: 1, display: "flex", flexDirection: "column", alignItems: "center", justifyContent: "center", padding: "40px 20px" }}>
          <div style={{ fontSize: 32, marginBottom: 20 }}>🕵️</div>
          <div style={{ fontSize: 14, fontWeight: 600, marginBottom: 8 }}>Analyzing scam page…</div>
          <div style={{ fontSize: 12, color: "#8BA3C7", marginBottom: 20, textAlign: "center" }}>Running in isolated container. No data is submitted.</div>
          <div style={{ width: 200, height: 6, background: "#132040", borderRadius: 3 }}>
            <div style={{ height: "100%", borderRadius: 3, background: "linear-gradient(90deg, #1A3C6B, #4A90D9)", width: `${progress}%`, transition: "width 0.25s" }} />
          </div>
          <div style={{ fontSize: 11, color: "#8BA3C7", marginTop: 8 }}>{progress}% · Playwright scanning…</div>
          <div style={{ marginTop: 24, fontSize: 12, color: "#5A7BA6" }}>
            {progress < 30 && "Navigating to URL…"}
            {progress >= 30 && progress < 60 && "Capturing full-page screenshot…"}
            {progress >= 60 && progress < 85 && "Detecting payment elements…"}
            {progress >= 85 && "Cross-referencing PhishTank + VirusTotal…"}
          </div>
        </div>
      ) : (
        <div style={{ flex: 1, overflowY: "auto" }}>
          <div style={{ margin: "16px 16px 12px", display: "flex", alignItems: "center", gap: 10 }}>
            <div style={{ background: "#C0392B", color: "#fff", padding: "8px 18px", borderRadius: 30, fontSize: 13, fontWeight: 800, letterSpacing: 1 }}>CONFIRMED SCAM</div>
            <div style={{ fontSize: 12, color: "#8BA3C7" }}>e-challan phishing</div>
          </div>
          <div style={{ margin: "0 16px 16px", borderRadius: 12, overflow: "hidden", border: "4px solid #C0392B" }}>
            <FakeScamPage />
          </div>
          <div style={{ margin: "0 16px 16px", padding: "14px", background: "#132040", borderRadius: 14 }}>
            <div style={{ fontSize: 11, color: "#8BA3C7", marginBottom: 10, letterSpacing: 0.5 }}>THREAT INTELLIGENCE</div>
            <div style={{ display: "flex", gap: 8, marginBottom: 8 }}>
              {[{ label: "PhishTank", hit: true }, { label: "VirusTotal", hit: true }].map((src) => (
                <div key={src.label} style={{
                  flex: 1, padding: "8px", background: "#2E0D0D",
                  border: "1px solid #C0392B44", borderRadius: 10, textAlign: "center",
                }}>
                  <div style={{ fontSize: 12, color: "#C0392B", fontWeight: 700 }}>⚠️ FLAGGED</div>
                  <div style={{ fontSize: 10, color: "#8BA3C7", marginTop: 2 }}>{src.label}</div>
                </div>
              ))}
            </div>
          </div>
          <div style={{ margin: "0 16px 16px" }}>
            <div style={{ fontSize: 11, color: "#8BA3C7", marginBottom: 10, letterSpacing: 0.5 }}>DETECTED ELEMENTS</div>
            {[
              { icon: "📱", label: "UPI QR Code" },
              { icon: "🏦", label: "Fake Bank Form" },
              { icon: "🏛️", label: "Govt Logo Misuse" },
              { icon: "💳", label: "Payment Gateway" },
            ].map((el) => (
              <div key={el.label} style={{
                display: "flex", alignItems: "center", gap: 10,
                padding: "10px 14px", marginBottom: 6,
                background: "#2E0D0D", borderRadius: 10, border: "1px solid #C0392B44",
              }}>
                <span style={{ fontSize: 18 }}>{el.icon}</span>
                <span style={{ fontSize: 13, flex: 1 }}>{el.label}</span>
                <span style={{ color: "#C0392B", fontSize: 13 }}>Detected</span>
              </div>
            ))}
          </div>
          <div style={{ margin: "0 16px 16px", padding: "14px", background: "#132040", borderRadius: 14 }}>
            <div style={{ fontSize: 11, color: "#8BA3C7", marginBottom: 6 }}>ANALYSIS</div>
            <div style={{ fontSize: 12, color: "#CCC", lineHeight: 1.6 }}>
              This page impersonates the Ministry of Road Transport using stolen government logos.
              It contains a fake UPI QR code and bank fields to steal money. The .xyz domain has no
              affiliation with any government agency.
            </div>
          </div>
          <div style={{ padding: "0 16px 32px", display: "flex", flexDirection: "column", gap: 10 }}>
            <button style={{ padding: "14px", background: "#C0392B", color: "#fff", border: "none", borderRadius: 14, fontSize: 14, fontWeight: 700, cursor: "pointer" }}>📨 Report to CERT-In</button>
            <button style={{ padding: "14px", background: "#1A3C6B", color: "#4A90D9", border: "1px solid #2A5C9B", borderRadius: 14, fontSize: 14, fontWeight: 600, cursor: "pointer" }}>⚠️ Share Warning with Contacts</button>
          </div>
        </div>
      )}
    </div>
  );
}
