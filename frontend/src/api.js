const API_BASE_URL = "http://localhost:7000";

async function postJson(path, payload) {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify(payload)
  });

  if (!response.ok) {
    throw new Error(`Request failed for ${path} with status ${response.status}`);
  }

  return response.json();
}

async function getJson(path) {
  const response = await fetch(`${API_BASE_URL}${path}`);
  if (!response.ok) {
    throw new Error(`Request failed for ${path} with status ${response.status}`);
  }
  return response.json();
}

export function searchTravel(payload) {
  return postJson("/api/travel/search", payload);
}

export function generateItinerary(payload) {
  return postJson("/api/travel/itinerary", payload);
}

export function askAi(payload) {
  return postJson("/api/ai/chat", payload);
}

export function getChatbots() {
  return getJson("/api/ai/chatbots");
}

export function getChatSessions(userId) {
  return getJson(`/api/ai/sessions?userId=${encodeURIComponent(userId)}`);
}

export function getChatSession(userId, sessionId) {
  return getJson(`/api/ai/sessions/${encodeURIComponent(sessionId)}?userId=${encodeURIComponent(userId)}`);
}

export function createChatSession(payload) {
  return postJson("/api/ai/sessions", payload);
}

export function predictPrice(payload) {
  return postJson("/api/pricing/predict", payload);
}

export function getDashboard(userId) {
  return getJson(`/api/travel/dashboard/${userId}`);
}

export function getBookingQuote(payload) {
  return postJson("/api/booking/quote", payload);
}
