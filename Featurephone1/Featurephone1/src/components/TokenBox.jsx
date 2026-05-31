// components/TokenBox.jsx

import TokenCard from "./TokenCard";

const VARIANTS = {
  success: { headerBg: "#f0fdf4", accent: "#16a34a", badgeBg: "#16a34a", icon: "✓" },
  failure: { headerBg: "#fef2f2", accent: "#dc2626", badgeBg: "#dc2626", icon: "✕" },
  pending: { headerBg: "#fffbeb", accent: "#d97706", badgeBg: "#d97706", icon: "⏱" },
};

const CARD_HEIGHT = 62;   // each card px
const GAP         = 6;    // gap between cards
const PADDING     = 10;   // box padding top+bottom
const TWO_CARDS   = PADDING * 2 + CARD_HEIGHT * 2 + GAP; // = 156px

export default function TokenBox({ title, tokens, type, variant }) {
  const s = VARIANTS[variant] || VARIANTS.pending;
  const list    = Array.isArray(tokens) ? tokens : [];
  const isEmpty = list.length === 0;
  const hasMore = list.length > 2;

  return (
    <div style={{
      background: "#fff",
      border: "1px solid #e5e7eb",
      borderRadius: 10,
      overflow: "hidden",
      display: "flex",
      flexDirection: "column",
    }}>

      {/* Header */}
      <div style={{
        display: "flex", alignItems: "center", gap: 8,
        padding: "8px 12px",
        background: s.headerBg,
        borderBottom: "1px solid #e5e7eb",
        flexShrink: 0,
      }}>
        <span style={{ color: s.accent, fontSize: 13, fontWeight: 700 }}>{s.icon}</span>
        <span style={{ fontSize: 12, fontWeight: 600, color: s.accent, flex: 1 }}>{title}</span>
        {!isEmpty && (
          <span style={{
            background: s.badgeBg, color: "#fff",
            fontSize: 10, fontWeight: 700,
            padding: "2px 7px", borderRadius: 99,
          }}>{list.length}</span>
        )}
      </div>

      {/* Body — fixed height = exactly 2 cards */}
      <div style={{
        height: TWO_CARDS,
        overflowY: hasMore ? "auto" : "hidden",
        padding: PADDING,
        paddingBottom: hasMore ? 4 : PADDING,
        display: "flex",
        flexDirection: "column",
        gap: GAP,
        boxSizing: "border-box",
        alignItems: isEmpty ? "center" : "stretch",
        justifyContent: isEmpty ? "center" : "flex-start",
      }}>
        {isEmpty ? (
          <span style={{ fontSize: 12, color: "#9ca3af" }}>No tokens found</span>
        ) : (
          list.map((t, i) => <TokenCard key={i} token={t} type={type} />)
        )}
      </div>

      {/* Scroll hint */}
      {hasMore && (
        <div style={{
          textAlign: "center", fontSize: 10, color: "#9ca3af",
          padding: "3px 0 5px",
          borderTop: "1px solid #f3f4f6",
          background: "#fafafa",
          flexShrink: 0,
        }}>
          ↕ scroll · {list.length - 2} more
        </div>
      )}
    </div>
  );
}