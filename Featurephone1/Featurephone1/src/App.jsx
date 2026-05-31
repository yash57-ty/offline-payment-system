import { useState } from "react";
import Login from "./components/login";
import ChatBox from "./components/ChatBox";
import "./App.css";

export default function App() {
  const savedPhone = localStorage.getItem("phone");
  const [phone, setPhone] = useState(
    savedPhone && savedPhone !== "undefined"
      ? savedPhone
      : null
  )
  function handelLogin(phone){
    setPhone(phone)
    localStorage.setItem("phone",phone)
  }
  return (
    <div className="app-layout">
      {!phone ? (
        <Login onLogin={handelLogin} />
      ) : (
        <ChatBox phone={phone} setphone={setPhone}/>
      )}
    </div>
  );
}
