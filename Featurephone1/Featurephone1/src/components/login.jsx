import { useState } from "react";
import "./Login.css";

export default function Login({ onLogin }) {
  const [phone, setPhone] = useState("");

  return (
    <div className="login-wrapper">
      <div className="login-container">
        <div className="login-top">FEATURE PHONE</div>

        <div className="login-screen">
          <div className="login-text">Enter Mobile Number</div>
          <input
            placeholder="XXXXXXXXXX"
            value={phone}
            onChange={(e) => setPhone(e.target.value)}
            maxLength={10}
          />
        </div>

        <button className="login-btn" onClick={() => onLogin(phone)}>
          START
        </button>
      </div>
    </div>
  );
}