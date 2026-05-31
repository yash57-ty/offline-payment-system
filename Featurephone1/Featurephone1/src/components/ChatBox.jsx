import { useEffect, useState ,useRef} from "react";
import { sendMessage, syncUser } from "../api";
import TokensPage from "./TokensPage";
import "./ChatBox.css";

export default function ChatBox({ phone,setphone }) {
  let intialized=useRef(false);
  const [messages, setMessages] = useState([]);
  const [input, setInput] = useState("");
  const [online, setOnline] = useState(false);
  const [showTokens, setShowTokens] = useState(false);
  const [pendingSentCount, setPendingSentCount] = useState(0);
  const [pendingReceivedCount, setPendingReceivedCount] = useState(0);
  const [btn,setbtn]=useState(true)
  
  useEffect(() => {
    const interval = setInterval(async () => {
      try {
        const [sentRes, recRes] = await Promise.all([
          fetch(`http://localhost:8080/token/pending/sent?phoneNo=${phone}`),
          fetch(`http://localhost:8080/token/pending/received?phoneNo=${phone}`),
        ]);
        const sentData = await sentRes.json();
        const recData = await recRes.json();
        setPendingSentCount((sentData || []).length);
        setPendingReceivedCount((recData || []).length);
      } catch { 

      }
    }, 4000);

    return () => clearInterval(interval);
  }, [phone]);

  const addMessage = (from,type,text) => setMessages(prev => [
    ...prev,
    { from, type, text }
  ]);
  
  useEffect(()=>{
    if(intialized.current) return
    intialized.current=true
    addMessage("bot", "button","start" )
  },[])

  const send = async () => {
    if (!input.trim()) return;
    addMessage("user","msg" ,input);
    try {
      const res = await sendMessage(phone, input);
      if(res.reply.startsWith("verdict")){
        addMessage("user","button", res.reply.substring(7));
      }else{
        addMessage("bot", "msg",res.reply);
      }
    } catch {
      addMessage("bot", "msg","❌ Server error");
    }
    setInput("");
  };
  const toggleSync = async () => {
    const newState = !online;
    setOnline(newState);
    if (newState) {
      try {
        await syncUser(phone);
        addMessage("bot", "msg","✔ Wallet synced successfully");
      } catch {
        addMessage("bot","msg" ,"❌ Sync failed");
        setOnline(false);
      }
    }
  };
  if (showTokens) {
    return <TokensPage phone={phone} onBack={() => setShowTokens(false)} />;
  }
  const totalPending = pendingSentCount + pendingReceivedCount;
  return (
    
    <div className="main-layout">
      <div className="phone-container">
        <div className="phone-header"></div>
        <div className="phone-screen">
          {messages.map((m, i) => (
            <div key={i}
              className={m.from === "bot"  ? "msg-bot" : "msg-user"}>
               {m.type === "button" ? (
                <>
                  <button onClick={async ()=>{
                    setbtn(false)
                     addMessage("user","msg" ,"bank");
                    try {
                      const res = await sendMessage(phone, "bank");
                      addMessage("bot", "msg", res.reply);
                    } catch {
                      addMessage("bot", "msg", "❌ Server error");
                    }
                  }}>To start chat press here</button>
                  <p>{m.text}</p>
                  </>
                ) : (
                  <p>{m.from === "bot" ? "▶ " : "◀ "}{m.text}</p>
                )}
            </div>
          ))}
        </div>
        <div className="phone-input">
          <input
            placeholder="Type reply..."
            value={input}
            onChange={(e) => setInput(e.target.value)}
            disabled={btn}
            onKeyDown={(e) => e.key === "Enter" && send()}
          />
          <button onClick={send}>OK</button>
        </div>
      </div>
      <div className="right-panel">
        <button
          className={`status-btn ${online ? "online" : "offline"}`}
          onClick={toggleSync}
        >
          {online ? "🟢 ONLINE" : "🔴 OFFLINE"}
        </button>
        <button
          className="logout-btn"
          onClick={()=>{
            localStorage.removeItem("phone")
            setphone(null);
          }}
        >
          🚪 Logout
        </button>
        <button
          className="view-tokens-btn"
          onClick={() => setShowTokens(true)}
        >
          📋 View All Tokens
          {totalPending > 0 && (
            <span className="badge">{totalPending}</span>
          )}
        </button>

        <div className="pending-summary">
          <div className="summary-row">
            <span>🟡 Pending Sent</span>
            <span className="count">{pendingSentCount}</span>
          </div>
          <div className="summary-row">
            <span>🟡 Pending Received</span>
            <span className="count">{pendingReceivedCount}</span>
          </div>
        </div>
      </div>
    </div>
  );
}