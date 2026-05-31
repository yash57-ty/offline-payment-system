const BASE = "http://localhost:8080";

export async function sendMessage(phone, message) {
  const res = await fetch(`${BASE}/api/chat`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ phone, message }),
  });
  return res.json();
}

export async function syncUser(phoneNo) {
  return fetch(`${BASE}/user/sync`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ phoneNo }),
  });
}

export async function fetchSenderSuccessTokens(phoneNo) {
  const res = await fetch(`${BASE}/token/success?phoneNo=${phoneNo}`);
  return res.json();
}

export async function fetchSenderFailureTokens(phoneNo) {
  const res = await fetch(`${BASE}/token/failure?phoneNo=${phoneNo}`);
  return res.json();
}

export async function fetchPendingSentTokens(phoneNo) {
  const res = await fetch(`${BASE}/token/pending/sent?phoneNo=${phoneNo}`);
  return res.json();
}

export async function fetchPendingReceivedTokens(phoneNo) {
  const res = await fetch(`${BASE}/token/pending/received?phoneNo=${phoneNo}`);
  return res.json();
}

export async function fetchReceiverSuccessTokens(phoneNo) {
  const res = await fetch(`${BASE}/token/receiver/success?phoneNo=${phoneNo}`);
  return res.json();
}

export async function fetchReceiverFailureTokens(phoneNo) {
  const res = await fetch(`${BASE}/token/receiver/failure?phoneNo=${phoneNo}`);
  return res.json();
}