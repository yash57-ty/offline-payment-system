// components/TokenCard.jsx

export default function TokenCard({ token, type }) {
  const isSender = type === "sent";

  return (
    <div style={{
      border: "1px solid #e5e7eb",
      borderRadius: 7,
      padding: "8px 10px",
      background: "#fff",
      flexShrink: 0,
    }}>
      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 3 }}>
        <code style={{ fontSize: 11, color: "#6b7280", fontFamily: "monospace" }}>{token.tokenId}</code>
        <span style={{ fontSize: 13, fontWeight: 700, color: "#111" }}>₹{token.amount}</span>
      </div>
      <div style={{ fontSize: 11, color: "#9ca3af" }}>
        {isSender ? `→ ${token.receiverMobile}` : `← ${token.senderMobile}`}
      </div>
    </div>
  );
}