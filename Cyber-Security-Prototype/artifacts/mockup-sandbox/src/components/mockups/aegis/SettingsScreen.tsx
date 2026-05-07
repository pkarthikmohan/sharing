import { useState } from "react";
import { AegisNavMenu } from "./shared/AegisNavMenu";

const CONTACTS_INIT = [
  { name: "Priya Sharma",  relation: "Wife",   number: "+91 98765 00001", registered: true },
  { name: "Ravi Kumar",    relation: "Father", number: "+91 98765 00002", registered: true },
  { name: "Neha Singh",    relation: "Sister", number: "+91 98765 00003", registered: true },
];

function Toggle({ on, onChange, color = "#4A90D9" }: { on: boolean; onChange: () => void; color?: string }) {
  return (
    <button
      onClick={onChange}
      style={{
        width: 48, height: 26, borderRadius: 13,
        background: on ? color : "#1A2D4A",
        border: `1px solid ${on ? color : "#2A4C6B"}`,
        cursor: "pointer", position: "relative",
        transition: "all 0.2s", flexShrink: 0,
        padding: 0,
      }}
    >
      <div style={{
        width: 20, height: 20, borderRadius: "50%",
        background: on ? "#fff" : "#4A6A8A",
        position: "absolute", top: 2,
        left: on ? 24 : 3,
        transition: "left 0.2s",
        boxShadow: "0 1px 4px #00000055",
      }} />
    </button>
  );
}

function SettingRow({
  icon, label, sub, on, onChange, color,
}: { icon: string; label: string; sub: string; on: boolean; onChange: () => void; color?: string }) {
  return (
    <div style={{
      display: "flex", alignItems: "center", gap: 14,
      padding: "14px 16px",
      borderBottom: "1px solid #0F1F3D",
    }}>
      <div style={{
        width: 38, height: 38, borderRadius: 10,
        background: "#0F1F3D",
        display: "flex", alignItems: "center", justifyContent: "center",
        fontSize: 18, flexShrink: 0,
      }}>{icon}</div>
      <div style={{ flex: 1, minWidth: 0 }}>
        <div style={{ fontSize: 14, fontWeight: 600, color: "#fff" }}>{label}</div>
        <div style={{ fontSize: 11, color: "#5A7BA6", marginTop: 2 }}>{sub}</div>
      </div>
      <Toggle on={on} onChange={onChange} color={color} />
    </div>
  );
}

function SectionHeader({ title }: { title: string }) {
  return (
    <div style={{ padding: "18px 16px 8px" }}>
      <span style={{ fontSize: 11, fontWeight: 700, color: "#4A90D9", letterSpacing: 1.2 }}>
        {title}
      </span>
    </div>
  );
}

export function SettingsScreen() {
  const [smsEnabled,   setSms]   = useState(true);
  const [callEnabled,  setCall]  = useState(true);
  const [autoReport,   setAuto]  = useState(false);
  const [overlay,      setOverlay] = useState(true);
  const [darkMode,     setDark]  = useState(true);
  const [sensitivity,  setSens]  = useState(1); // 0=Conservative 1=Balanced 2=Aggressive
  const [contacts, setContacts]  = useState(CONTACTS_INIT);
  const [addingContact, setAdding] = useState(false);
  const [newName, setNewName]    = useState("");
  const [newNum,  setNewNum]     = useState("");
  const [cleared, setCleared]    = useState(false);

  const sensLabels = ["Conservative", "Balanced", "Aggressive"];
  const sensColors = ["#1E6B45", "#4A90D9", "#C0392B"];

  const removeContact = (i: number) => setContacts(c => c.filter((_, j) => j !== i));

  const addContact = () => {
    if (!newName.trim() || !newNum.trim()) return;
    setContacts(c => [...c, { name: newName.trim(), relation: "Contact", number: newNum.trim(), registered: false }]);
    setNewName(""); setNewNum(""); setAdding(false);
  };

  return (
    <div style={{
      width: 390, minHeight: 844,
      background: "#0F1F3D",
      fontFamily: "'Inter', -apple-system, sans-serif",
      color: "#fff", display: "flex", flexDirection: "column",
      overflowX: "hidden",
    }}>
      {/* Status bar */}
      <div style={{ padding: "12px 20px 0", display: "flex", justifyContent: "space-between", fontSize: 12, color: "#8BA3C7" }}>
        <span>9:41</span>
        <span>▶▶ 📶 🔋</span>
      </div>

      {/* Header */}
      <div style={{
        padding: "12px 20px 16px",
        display: "flex", alignItems: "center", gap: 12,
        borderBottom: "1px solid #1A3C6B",
        position: "relative",
      }}>
        <div style={{ fontSize: 22 }}>⚙️</div>
        <div style={{ flex: 1 }}>
          <div style={{ fontSize: 20, fontWeight: 700 }}>Settings</div>
          <div style={{ fontSize: 11, color: "#8BA3C7" }}>Configure Aegis protection</div>
        </div>
        <AegisNavMenu current="SettingsScreen" />
      </div>

      {/* Scrollable content */}
      <div style={{ flex: 1, overflowY: "auto" }}>

        {/* ── Protection ── */}
        <SectionHeader title="PROTECTION" />
        <div style={{ background: "#132040", borderRadius: 16, margin: "0 12px", overflow: "hidden" }}>
          <SettingRow icon="💬" label="SMS Monitoring"
            sub="Intercept and analyse incoming messages"
            on={smsEnabled} onChange={() => setSms(v => !v)} color="#4A90D9" />
          <SettingRow icon="📞" label="Call Audio Analysis"
            sub="Real-time deepfake voice detection"
            on={callEnabled} onChange={() => setCall(v => !v)} color="#9B59B6" />
          <SettingRow icon="📡" label="Auto-Report Scams"
            sub="Anonymously share confirmed threats"
            on={autoReport} onChange={() => setAuto(v => !v)} color="#1E6B45" />
          <SettingRow icon="🪟" label="Overlay Alerts"
            sub="Show full-screen alerts for CONFIRMED threats"
            on={overlay} onChange={() => setOverlay(v => !v)} color="#E67E22" />
        </div>

        {/* ── Sensitivity ── */}
        <SectionHeader title="THREAT SENSITIVITY" />
        <div style={{ background: "#132040", borderRadius: 16, margin: "0 12px", overflow: "hidden", padding: "16px" }}>
          <div style={{ display: "flex", gap: 8, marginBottom: 14 }}>
            {sensLabels.map((l, i) => (
              <button key={l} onClick={() => setSens(i)} style={{
                flex: 1, padding: "10px 4px",
                background: sensitivity === i ? `${sensColors[i]}22` : "#0F1F3D",
                border: `1px solid ${sensitivity === i ? sensColors[i] : "#1A3C6B"}`,
                borderRadius: 10, cursor: "pointer",
                color: sensitivity === i ? sensColors[i] : "#5A7BA6",
                fontSize: 11, fontWeight: sensitivity === i ? 700 : 400,
                transition: "all 0.2s",
              }}>{l}</button>
            ))}
          </div>
          <div style={{ fontSize: 12, color: "#5A7BA6", lineHeight: 1.6, padding: "0 2px" }}>
            {sensitivity === 0 && "Only flags high-confidence threats (score ≥ 80). Minimises false positives. Best for experienced users."}
            {sensitivity === 1 && "Balanced detection threshold (score ≥ 60). Recommended for most users. Catches the majority of scams."}
            {sensitivity === 2 && "Flags all suspicious activity (score ≥ 40). May generate more false positives. Best for high-risk environments."}
          </div>
          <div style={{ marginTop: 12, display: "flex", alignItems: "center", gap: 6 }}>
            <div style={{ flex: 1, height: 6, background: "#0F1F3D", borderRadius: 3, overflow: "hidden" }}>
              <div style={{
                height: "100%", borderRadius: 3,
                background: sensColors[sensitivity],
                width: `${(sensitivity / 2) * 100}%`,
                transition: "width 0.3s, background 0.3s",
              }} />
            </div>
            <span style={{ fontSize: 11, color: sensColors[sensitivity], fontWeight: 600, width: 80, textAlign: "right" }}>
              {sensLabels[sensitivity]}
            </span>
          </div>
        </div>

        {/* ── Family Safe-Word ── */}
        <SectionHeader title="FAMILY SAFE-WORD CONTACTS" />
        <div style={{ background: "#132040", borderRadius: 16, margin: "0 12px", overflow: "hidden" }}>
          {contacts.map((c, i) => (
            <div key={i} style={{
              display: "flex", alignItems: "center", gap: 12,
              padding: "12px 16px",
              borderBottom: "1px solid #0F1F3D",
            }}>
              <div style={{
                width: 38, height: 38, borderRadius: "50%",
                background: `hsl(${i * 80 + 200}, 55%, 32%)`,
                display: "flex", alignItems: "center", justifyContent: "center",
                fontSize: 15, fontWeight: 700, flexShrink: 0,
              }}>{c.name[0]}</div>
              <div style={{ flex: 1, minWidth: 0 }}>
                <div style={{ fontSize: 13, fontWeight: 600 }}>{c.name}</div>
                <div style={{ display: "flex", alignItems: "center", gap: 6, marginTop: 2 }}>
                  <span style={{ fontSize: 10, color: "#5A7BA6" }}>{c.relation} · {c.number}</span>
                  {c.registered && (
                    <span style={{
                      fontSize: 9, padding: "1px 6px", borderRadius: 8,
                      background: "#1E6B4522", color: "#1E6B45", border: "1px solid #1E6B4544",
                    }}>REGISTERED</span>
                  )}
                </div>
              </div>
              <button onClick={() => removeContact(i)} style={{
                width: 28, height: 28, borderRadius: 7,
                background: "#C0392B22", border: "1px solid #C0392B44",
                color: "#C0392B", cursor: "pointer", fontSize: 14,
                display: "flex", alignItems: "center", justifyContent: "center",
              }}>×</button>
            </div>
          ))}

          {/* Add contact */}
          {addingContact ? (
            <div style={{ padding: "14px 16px" }}>
              <input value={newName} onChange={e => setNewName(e.target.value)}
                placeholder="Full name"
                style={{
                  width: "100%", padding: "10px 12px", marginBottom: 8,
                  background: "#0F1F3D", border: "1px solid #2A4C6B",
                  borderRadius: 10, color: "#fff", fontSize: 13,
                  boxSizing: "border-box",
                }} />
              <input value={newNum} onChange={e => setNewNum(e.target.value)}
                placeholder="+91 XXXXX XXXXX"
                style={{
                  width: "100%", padding: "10px 12px", marginBottom: 12,
                  background: "#0F1F3D", border: "1px solid #2A4C6B",
                  borderRadius: 10, color: "#fff", fontSize: 13,
                  boxSizing: "border-box",
                }} />
              <div style={{ display: "flex", gap: 8 }}>
                <button onClick={addContact} style={{
                  flex: 1, padding: "10px",
                  background: "#1A3C6B", color: "#4A90D9",
                  border: "1px solid #2A5C9B", borderRadius: 10,
                  fontSize: 13, fontWeight: 600, cursor: "pointer",
                }}>Add Contact</button>
                <button onClick={() => { setAdding(false); setNewName(""); setNewNum(""); }} style={{
                  flex: 1, padding: "10px",
                  background: "#0F1F3D", color: "#5A7BA6",
                  border: "1px solid #1A3C6B", borderRadius: 10,
                  fontSize: 13, cursor: "pointer",
                }}>Cancel</button>
              </div>
            </div>
          ) : (
            <button onClick={() => setAdding(true)} style={{
              width: "100%", padding: "14px 16px",
              background: "none", border: "none", cursor: "pointer",
              color: "#4A90D9", fontSize: 13, fontWeight: 600,
              display: "flex", alignItems: "center", gap: 8,
              textAlign: "left",
            }}>
              <span style={{
                width: 26, height: 26, borderRadius: 7,
                background: "#1A3C6B", display: "flex",
                alignItems: "center", justifyContent: "center",
                fontSize: 16, flexShrink: 0,
              }}>+</span>
              Add Family Member
            </button>
          )}
        </div>

        {/* ── Appearance ── */}
        <SectionHeader title="APPEARANCE" />
        <div style={{ background: "#132040", borderRadius: 16, margin: "0 12px", overflow: "hidden" }}>
          <SettingRow icon="🌙" label="Dark Mode"
            sub="Always-on deep navy theme"
            on={darkMode} onChange={() => setDark(v => !v)} color="#9B59B6" />
        </div>

        {/* ── Model info ── */}
        <SectionHeader title="MODEL" />
        <div style={{ background: "#132040", borderRadius: 16, margin: "0 12px", overflow: "hidden" }}>
          {[
            { icon: "🤖", label: "Smishing Model", value: "v2.4.1" },
            { icon: "🎙️", label: "Deepfake Model",  value: "v1.8.0" },
            { icon: "📅", label: "Last Updated",    value: "07 May 2026" },
          ].map((row) => (
            <div key={row.label} style={{
              display: "flex", alignItems: "center", padding: "13px 16px",
              borderBottom: "1px solid #0F1F3D",
            }}>
              <span style={{ fontSize: 18, marginRight: 12 }}>{row.icon}</span>
              <span style={{ flex: 1, fontSize: 13, color: "#CCC" }}>{row.label}</span>
              <span style={{ fontSize: 12, color: "#4A90D9", fontFamily: "monospace" }}>{row.value}</span>
            </div>
          ))}
          <button style={{
            width: "100%", padding: "14px 16px",
            background: "none", border: "none",
            color: "#4A90D9", fontSize: 13, fontWeight: 600,
            cursor: "pointer", textAlign: "left",
          }}>
            🔄 Check for Model Updates
          </button>
        </div>

        {/* ── Danger zone ── */}
        <SectionHeader title="DATA" />
        <div style={{ background: "#132040", borderRadius: 16, margin: "0 12px", overflow: "hidden" }}>
          <button
            onClick={() => setCleared(true)}
            style={{
              width: "100%", padding: "14px 16px",
              background: "none", border: "none", cursor: "pointer",
              color: cleared ? "#1E6B45" : "#C0392B",
              fontSize: 13, fontWeight: 600, textAlign: "left",
              transition: "color 0.3s",
            }}
          >
            {cleared ? "✓ Threat history cleared" : "🗑️  Clear Local Threat History"}
          </button>
        </div>

        {/* App version */}
        <div style={{ padding: "24px 16px 40px", textAlign: "center" }}>
          <div style={{ fontSize: 11, color: "#2A4C6B" }}>Aegis v1.0.0 · com.aegis.shield</div>
          <div style={{ fontSize: 10, color: "#1A3C6B", marginTop: 4 }}>Privacy-first · On-device ML · Zero raw data transmitted</div>
        </div>
      </div>
    </div>
  );
}
