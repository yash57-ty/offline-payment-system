import { useEffect, useState } from "react";
import { sendMessage } from "./api";
import "./ChatBox.css";

export default function ChatBox({ phone }) {

  const [messages, setMessages] = useState([
    {
      from: "bot",
      text: "Welcome!\nType: Bank Transaction"
    }
  ]);
  
  const [input, setInput] = useState("");

  const [online, setOnline] = useState(false);

  const [successTokens, setSuccessTokens] = useState([]);

  const [failureTokens, setFailureTokens] = useState([]);

  // SENDER PENDING
  const [pendingSentTokens, setPendingSentTokens] = useState([]);

  // RECEIVER PENDING
  const [pendingReceivedTokens, setPendingReceivedTokens] = useState([]);

  // =========================================
  // SEND MESSAGE
  // =========================================
  useEffect(() => {
  const interval = setInterval( async () => {
    const pendingSentRes = await fetch(
          `http://localhost:8080/token/pending/sent?phoneNo=${phone}`
        );
        const pendingSentData =
          await pendingSentRes.json();
        setPendingSentTokens(
          pendingSentData || []
        );
        const pendingReceivedRes = await fetch(
          `http://localhost:8080/token/pending/received?phoneNo=${phone}`
        );
        const pendingReceivedData =
          await pendingReceivedRes.json();
        setPendingReceivedTokens(
          pendingReceivedData || []
        );
  }, 4000);

  return () => clearInterval(interval); // cleanup on unmount
}, []);


  const send = async () => {

    if (!input.trim()) return;

    setMessages((prev) => [
      ...prev,
      {
        from: "user",
        text: input
      }
    ]);

    try {

      const res = await sendMessage(
        phone,
        input
      );

      setMessages((prev) => [
        ...prev,
        {
          from: "bot",
          text: res.reply
        }
      ]);

    } catch {

      setMessages((prev) => [
        ...prev,
        {
          from: "bot",
          text: "❌ Server error"
        }
      ]);
    }

    setInput("");
  };

  // =========================================
  // SYNC
  // =========================================

  const toggleSync = async () => {

    const newState = !online;

    setOnline(newState);

    if (newState) {

      try {

        // =====================================
        // SYNC API
        // =====================================

        await fetch(
          "http://localhost:8080/user/sync",
          {
            method: "POST",

            headers: {
              "Content-Type": "application/json"
            },

            body: JSON.stringify({
              phoneNo: phone
            })
          }
        );

        // =====================================
        // SUCCESS TOKENS
        // =====================================

        const successRes = await fetch(
          `http://localhost:8080/token/success?phoneNo=${phone}`
        );

        const successData =
          await successRes.json();

        setSuccessTokens(successData || []);

        // =====================================
        // FAILURE TOKENS
        // =====================================

        const failRes = await fetch(
          `http://localhost:8080/token/failure?phoneNo=${phone}`
        );

        const failData =
          await failRes.json();

        setFailureTokens(failData || []);

        setMessages((prev) => [
          ...prev,
          {
            from: "bot",
            text: "✔ Wallet synced successfully"
          }
        ]);

      } catch (err) {

        console.error(err);

        setMessages((prev) => [
          ...prev,
          {
            from: "bot",
            text: "❌ Sync failed"
          }
        ]);

        setOnline(false);
      }
    }
  };

  return (

    <div className="main-layout">

      {/* ===================================== */}
      {/* PHONE */}
      {/* ===================================== */}

      <div className="phone-container">

        <div className="phone-header"></div>

        <div className="phone-screen">

          {
            messages.map((m, i) => (

              <div
                key={i}
                className={
                  m.from === "bot"
                    ? "msg-bot"
                    : "msg-user"
                }
              >

                {
                  m.from === "bot"
                    ? "▶ "
                    : "◀ "
                }

                {m.text}

              </div>
            ))
          }

        </div>

        <div className="phone-input">

          <input
            placeholder="Type reply..."
            value={input}
            onChange={(e) =>
              setInput(e.target.value)
            }
            onKeyDown={(e) =>
              e.key === "Enter" && send()
            }
          />

          <button onClick={send}>
            OK
          </button>

        </div>

      </div>

      {/* ===================================== */}
      {/* RIGHT PANEL */}
      {/* ===================================== */}

      <div className="right-panel">

        {/* ================================= */}
        {/* NETWORK STATUS */}
        {/* ================================= */}

        <button
          className={`status-btn ${
            online
              ? "online"
              : "offline"
          }`}
          onClick={toggleSync}
        >

          {
            online
              ? "🟢 ONLINE"
              : "🔴 OFFLINE"
          }

        </button>

        {/* ================================= */}
        {/* SUCCESS TOKENS */}
        {/* ================================= */}

        <div className="token-box success">

          <h3>✅ Success Tokens</h3>

          {
            successTokens.length === 0 ? (

              <p>No success tokens</p>

            ) : (

              successTokens.map((t, i) => (

                <div
                  key={i}
                  className="token-item"
                >

                  <div>
                    <b>Token:</b> {t.tokenId}
                  </div>

                  <div>
                    <b>Amount:</b> ₹{t.amount}
                  </div>

                  <div>
                    <b>To:</b> {t.receiverMobile}
                  </div>

                </div>
              ))
            )
          }

        </div>

        {/* ================================= */}
        {/* FAILED TOKENS */}
        {/* ================================= */}

        <div className="token-box failure">

          <h3>❌ Failed Tokens</h3>

          {
            failureTokens.length === 0 ? (

              <p>No failed tokens</p>

            ) : (

              failureTokens.map((t, i) => (

                <div
                  key={i}
                  className="token-item"
                >

                  <div>
                    <b>Token:</b> {t.tokenId}
                  </div>

                  <div>
                    <b>Amount:</b> ₹{t.amount}
                  </div>

                  <div>
                    <b>Receiver:</b> {t.receiverMobile}
                  </div>

                </div>
              ))
            )
          }

        </div>

        {/* ================================= */}
        {/* PENDING SENT */}
        {/* ================================= */}

        <div className="token-box pending">

          <h3>🟡 Pending Sent Tokens</h3>

          {
            pendingSentTokens.length === 0 ? (

              <p>No pending sent tokens</p>

            ) : (

              pendingSentTokens.map((t, i) => (

                <div
                  key={i}
                  className="token-item"
                >

                  <div>
                    <b>Token:</b> {t.tokenId}
                  </div>

                  <div>
                    <b>Amount:</b> ₹{t.amount}
                  </div>

                  <div>
                    <b>To:</b> {t.receiverMobile}
                  </div>

                  <div>
                    <b>Status:</b> PENDING
                  </div>

                </div>
              ))
            )
          }

        </div>

        {/* ================================= */}
        {/* PENDING RECEIVED */}
        {/* ================================= */}

        <div className="token-box pending">

          <h3>🟡 Pending Received Tokens</h3>

          {
            pendingReceivedTokens.length === 0 ? (

              <p>No pending received tokens</p>

            ) : (

              pendingReceivedTokens.map((t, i) => (

                <div
                  key={i}
                  className="token-item"
                >

                  <div>
                    <b>Token:</b> {t.tokenId}
                  </div>

                  <div>
                    <b>Amount:</b> ₹{t.amount}
                  </div>

                  <div>
                    <b>From:</b> {t.senderMobile}
                  </div>

                  <div>
                    <b>Status:</b> PENDING
                  </div>

                </div>
              ))
            )
          }

        </div>

      </div>

    </div>
  );
}