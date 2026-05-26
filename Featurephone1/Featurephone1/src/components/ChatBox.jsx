import { useState } from "react";
import { sendMessage } from "./api";
import "./ChatBox.css";

export default function ChatBox({ phone }) {
  const [messages, setMessages] = useState([
    { from: "bot", text: "Welcome!\nType: Bank Transaction" }
  ]);
  const [input, setInput] = useState("");
  const [online, setOnline] = useState(false);

  const [successTokens, setSuccessTokens] = useState([]);
  const [failureTokens, setFailureTokens] = useState([]);

  const send = async () => {
    if (!input.trim()) return;

    setMessages((prev) => [...prev, { from: "user", text: input }]);

    try {
      const res = await sendMessage(phone, input);
      setMessages((prev) => [...prev, { from: "bot", text: res.reply }]);
    } catch {
      setMessages((prev) => [
        ...prev,
        { from: "bot", text: "❌ Server error. Try again." }
      ]);
    }

    setInput("");
  };

  // ================= ONLINE / OFFLINE SYNC =================
  const toggleSync = async () => {
    const newState = !online;
    setOnline(newState);

    if (newState) {
      try {
        // 1️⃣ Wallet sync
        await fetch("http://localhost:8080/user/sync", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ phoneNo: phone })
        });

        // 2️⃣ Fetch success tokens
        const successRes = await fetch(
          `http://localhost:8080/token/success?phoneNo=${phone}`
        );
        const successData = await successRes.json();
        setSuccessTokens(successData || []);

        // 3️⃣ Fetch failure tokens
        const failRes = await fetch(
          `http://localhost:8080/token/failure?phoneNo=${phone}`
        );
        const failData = await failRes.json();
        setFailureTokens(failData || []);

        setMessages((prev) => [
          ...prev,
          { from: "bot", text: "✔ Wallet synced successfully" }
        ]);
      } catch (err) {
        console.error(err);
        setMessages((prev) => [
          ...prev,
          { from: "bot", text: "❌ Sync failed" }
        ]);
        setOnline(false);
      }
    }
  };

  return (
    <div className="main-layout">
      {/* LEFT : FEATURE PHONE */}
      <div className="phone-container">
        <div className="phone-header"></div>

        <div className="phone-screen">
          {messages.map((m, i) => (
            <div key={i} className={m.from === "bot" ? "msg-bot" : "msg-user"}>
              {m.from === "bot" ? "▶ " : "◀ "}
              {m.text}
            </div>
          ))}
        </div>

        <div className="phone-input">
          <input
            placeholder="Type reply..."
            value={input}
            onChange={(e) => setInput(e.target.value)}
            onKeyDown={(e) => e.key === "Enter" && send()}
          />
          <button onClick={send}>OK</button>
        </div>
      </div>

      {/* RIGHT : STATUS + TOKENS */}
      <div className="right-panel">
        <button
          className={`status-btn ${online ? "online" : "offline"}`}
          onClick={toggleSync}
        >
          {online ? "ONLINE" : "OFFLINE"}
        </button>

        {/* SUCCESS TOKENS */}
        <div className="token-box success">
          <h3>✅ Success Tokens</h3>
          {successTokens.length === 0 ? (
            <p>No success tokens</p>
          ) : (
            successTokens.map((t, i) => (
              <div key={i} className="token-item">
                <div><b>Token:</b> {t.tokenId}</div>
                <div><b>Amount:</b> ₹{t.amount}</div>
                <div><b>To:</b> {t.receiverMobile}</div>
              </div>
            ))
          )}
        </div>

        {/* FAILURE TOKENS */}
        <div className="token-box failure">
          <h3>❌ Failure Tokens</h3>
          {failureTokens.length === 0 ? (
            <p>No failure tokens</p>
          ) : (
            failureTokens.map((t, i) => (
              <div key={i} className="token-item">
                <div><b>Token:</b> {t.tokenId}</div>
                <div><b>Amount:</b> ₹{t.amount}</div>
                <div><b>Reason:</b> {t.reason}</div>
              </div>
            ))
          )}
        </div>
      </div>
    </div>
  );
}