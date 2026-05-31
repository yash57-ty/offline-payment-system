// components/TokensPage.jsx

import { useEffect, useState } from "react";
import TokenBox from "./TokenBox";
import {
  fetchSenderSuccessTokens,
  fetchSenderFailureTokens,
  fetchPendingSentTokens,
  fetchPendingReceivedTokens,
  fetchReceiverSuccessTokens,
  fetchReceiverFailureTokens,
} from "../api";

const SectionLabel = ({ text }) => (
  <p style={{
    margin: "0 0 6px",
    fontSize: 10,
    fontWeight: 700,
    color: "#6b7280",
    textTransform: "uppercase",
    letterSpacing: "0.08em",
  }}>{text}</p>
);

const Row = ({ children }) => (
  <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 10, marginBottom: 12 }}>
    {children}
  </div>
);

export default function TokensPage({ phone, onBack }) {
  const [loading, setLoading]             = useState(true);
  const [senderSuccess,   setSS]          = useState([]);
  const [senderFailure,   setSF]          = useState([]);
  const [pendingSent,     setPS]          = useState([]);
  const [pendingReceived, setPR]          = useState([]);
  const [receiverSuccess, setRS]          = useState([]);
  const [receiverFailure, setRF]          = useState([]);

  useEffect(() => {
    (async () => {
      setLoading(true);
      try {
        const [ss, sf, ps, pr, rs, rf] = await Promise.all([
          fetchSenderSuccessTokens(phone),
          fetchSenderFailureTokens(phone),
          fetchPendingSentTokens(phone),
          fetchPendingReceivedTokens(phone),
          fetchReceiverSuccessTokens(phone),
          fetchReceiverFailureTokens(phone),
        ]);
        setSS(Array.isArray(ss) ? ss : []);
        setSF(Array.isArray(sf) ? sf : []);
        setPS(Array.isArray(ps) ? ps : []);
        setPR(Array.isArray(pr) ? pr : []);
        setRS(Array.isArray(rs) ? rs : []);
        setRF(Array.isArray(rf) ? rf : []);
      } catch (e) { console.error(e); }
      finally { setLoading(false); }
    })();
  }, [phone]);

  const total   = senderSuccess.length + senderFailure.length + pendingSent.length
                + pendingReceived.length + receiverSuccess.length + receiverFailure.length;
  const success = senderSuccess.length + receiverSuccess.length;
  const failed  = senderFailure.length + receiverFailure.length;

  if (loading) return (
    <div style={{ height: "100vh", display: "flex", alignItems: "center", justifyContent: "center" }}>
      <p style={{ color: "#6b7280", fontSize: 13 }}>Loading…</p>
    </div>
  );

  return (
    <div style={{
      minHeight: "100vh",
      background: "#f3f4f6",
      padding: 16,
      boxSizing: "border-box",
    }}>

      {/* ── Header ── */}
      <div style={{
        display: "flex",
        alignItems: "center",
        justifyContent: "space-between",
        background: "#fff",
        border: "1px solid #e5e7eb",
        borderRadius: 10,
        padding: "10px 14px",
        marginBottom: 14,
        flexWrap: "wrap",
        gap: 8,
      }}>
        <div style={{ display: "flex", alignItems: "center", gap: 12 }}>
          <button onClick={onBack} style={{
            padding: "5px 12px", fontSize: 12,
            border: "1px solid #d1d5db", borderRadius: 7,
            background: "#fff", cursor: "pointer", fontWeight: 600,
          }}>← Back</button>
          <div>
            <p style={{ margin: 0, fontSize: 10, color: "#9ca3af", textTransform: "uppercase", letterSpacing: "0.07em" }}>
              Token Dashboard
            </p>
            <p style={{ margin: 0, fontSize: 14, fontWeight: 700, color: "#111", fontFamily: "monospace" }}>
              {phone}
            </p>
          </div>
        </div>
        <div style={{ display: "flex", gap: 6 }}>
          {[
            { label: "Total",   value: total,   bg: "#f3f4f6", color: "#374151" },
            { label: "Success", value: success, bg: "#f0fdf4", color: "#16a34a" },
            { label: "Failed",  value: failed,  bg: "#fef2f2", color: "#dc2626" },
          ].map(({ label, value, bg, color }) => (
            <div key={label} style={{ background: bg, borderRadius: 7, padding: "5px 12px", textAlign: "center" }}>
              <p style={{ margin: 0, fontSize: 10, color }}>{label}</p>
              <p style={{ margin: 0, fontSize: 17, fontWeight: 700, color }}>{value}</p>
            </div>
          ))}
        </div>
      </div>

      {/* ── Sender ── */}
      <SectionLabel text="Sender" />
      <Row>
        <TokenBox title="Success" tokens={senderSuccess} type="sent"     variant="success" />
        <TokenBox title="Failed"  tokens={senderFailure} type="sent"     variant="failure" />
      </Row>

      {/* ── Pending ── */}
      <SectionLabel text="Pending" />
      <Row>
        <TokenBox title="Pending sent"     tokens={pendingSent}     type="sent"     variant="pending" />
        <TokenBox title="Pending received" tokens={pendingReceived} type="received" variant="pending" />
      </Row>

      {/* ── Receiver ── */}
      <SectionLabel text="Receiver" />
      <Row>
        <TokenBox title="Success" tokens={receiverSuccess} type="received" variant="success" />
        <TokenBox title="Failed"  tokens={receiverFailure} type="received" variant="failure" />
      </Row>

    </div>
  );
}