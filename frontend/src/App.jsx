import { startTransition, useEffect, useMemo, useState } from "react";
import {
  askAi,
  createChatSession,
  generateItinerary,
  getBookingQuote,
  getChatSession,
  getChatSessions,
  getChatbots,
  getDashboard,
  predictPrice,
  searchTravel
} from "./api";
import { BotDashboardPage } from "./components/BotDashboardPage";
import { EducationPage } from "./components/EducationPage";
import { HotelSearchPage } from "./components/HotelSearchPage";

const DEMO_USER_ID = "demo-user";
const DEFAULT_CHATBOT_CODE = "travel_ai_guide";
const DASHBOARD_BY_CHATBOT = {
  travel_ai_guide: "travel",
  education_helpful: "education"
};

const initialQuery = {
  destination: "Bali",
  tripDays: 4,
  budgetLevel: "mid-range",
  travelerProfile: "family",
  requiredAmenities: ["free breakfast"],
  interests: ["culture", "family", "beach"]
};

function App() {
  const [activeView, setActiveView] = useState("home");
  const [query, setQuery] = useState(initialQuery);
  const [hotels, setHotels] = useState([]);
  const [searchSummary, setSearchSummary] = useState("");
  const [nextQuestions, setNextQuestions] = useState([]);
  const [pricePrediction, setPricePrediction] = useState(null);
  const [itinerary, setItinerary] = useState(null);
  const [dashboard, setDashboard] = useState(null);
  const [chatResponse, setChatResponse] = useState(null);
  const [bookingQuote, setBookingQuote] = useState(null);
  const [selectedHotelId, setSelectedHotelId] = useState(null);
  const [chatbots, setChatbots] = useState([]);
  const [chatSessions, setChatSessions] = useState([]);
  const [activeSession, setActiveSession] = useState(null);
  const [activeChatbotCode, setActiveChatbotCode] = useState(DEFAULT_CHATBOT_CODE);
  const [chatInput, setChatInput] = useState("");
  const [error, setError] = useState("");
  const [isLoading, setIsLoading] = useState(true);
  const [isChatLoading, setIsChatLoading] = useState(true);
  const [isSendingMessage, setIsSendingMessage] = useState(false);

  useEffect(() => {
    void bootstrap();
  }, []);

  const activeChatbot = useMemo(
    () => chatbots.find((chatbot) => chatbot.chatbotCode === activeChatbotCode) ?? null,
    [chatbots, activeChatbotCode]
  );

  const visibleChatSessions = useMemo(
    () => chatSessions.filter((session) => session.chatbotCode === activeChatbotCode),
    [chatSessions, activeChatbotCode]
  );

  async function bootstrap() {
    setIsLoading(true);
    setIsChatLoading(true);
    setError("");

    try {
      const availableChatbots = await getChatbots();
      setChatbots(availableChatbots);

      await Promise.all([
        loadInitialData(initialQuery),
        initializeChatWorkspace(DEFAULT_CHATBOT_CODE, availableChatbots)
      ]);
    } catch (bootstrapError) {
      setError(bootstrapError.message);
    } finally {
      setIsLoading(false);
      setIsChatLoading(false);
    }
  }

  async function loadInitialData(activeQuery) {
    const searchResponse = await searchTravel(activeQuery);
    const firstHotel = searchResponse.hotels[0];

    const [dashboardResponse, itineraryResponse, priceResponse, bookingResponse] = await Promise.all([
      getDashboard(DEMO_USER_ID),
      generateItinerary({
        destination: activeQuery.destination,
        days: activeQuery.tripDays,
        travelerProfile: activeQuery.travelerProfile,
        budgetLevel: activeQuery.budgetLevel,
        interests: activeQuery.interests
      }),
      predictPrice({
        destination: activeQuery.destination,
        hotelSegment: activeQuery.travelerProfile,
        currentPrice: firstHotel ? firstHotel.nightlyPrice : 180,
        daysUntilCheckIn: 2,
        weekendStay: true,
        peakSeason: true
      }),
      firstHotel
        ? getBookingQuote({
            hotelId: firstHotel.id,
            roomType: "Family Deluxe Room",
            nights: activeQuery.tripDays,
            guests: 2,
            breakfastIncluded: true,
            refundable: true
          })
        : Promise.resolve(null)
    ]);

    startTransition(() => {
      setHotels(searchResponse.hotels);
      setSearchSummary(searchResponse.summary);
      setNextQuestions(searchResponse.nextQuestions);
      setSelectedHotelId(firstHotel ? firstHotel.id : null);
      setDashboard(dashboardResponse);
      setItinerary(itineraryResponse);
      setPricePrediction(priceResponse);
      setBookingQuote(bookingResponse);
    });
  }

  async function initializeChatWorkspace(preferredChatbotCode, availableChatbots = chatbots) {
    const selectedChatbotCode = preferredChatbotCode ?? availableChatbots[0]?.chatbotCode ?? DEFAULT_CHATBOT_CODE;
    const sessions = await getChatSessions(DEMO_USER_ID);
    const matchingSession = sessions.find((session) => session.chatbotCode === selectedChatbotCode) ?? sessions[0];

    let sessionDetail = null;
    let nextSessions = sessions;

    if (matchingSession) {
      sessionDetail = await getChatSession(DEMO_USER_ID, matchingSession.sessionId);
    } else {
      sessionDetail = await createChatSession({
        userId: DEMO_USER_ID,
        chatbotCode: selectedChatbotCode,
        title: null
      });
      nextSessions = await getChatSessions(DEMO_USER_ID);
    }

    startTransition(() => {
      setChatSessions(nextSessions);
      setActiveChatbotCode(sessionDetail.chatbotCode);
      setActiveSession(sessionDetail);
      setChatResponse(null);
    });
  }

  async function refreshChatWorkspace(targetSessionId, preferredChatbotCode = activeChatbotCode) {
    const [sessions, sessionDetail] = await Promise.all([
      getChatSessions(DEMO_USER_ID),
      getChatSession(DEMO_USER_ID, targetSessionId)
    ]);

    startTransition(() => {
      setChatSessions(sessions);
      setActiveSession(sessionDetail);
      setActiveChatbotCode(sessionDetail.chatbotCode || preferredChatbotCode);
    });
  }

  async function handleSearch(nextQuery) {
    setQuery(nextQuery);
    setIsLoading(true);
    setError("");

    try {
      await loadInitialData(nextQuery);
    } catch (loadError) {
      setError(loadError.message);
    } finally {
      setIsLoading(false);
    }
  }

  async function handleHotelSelect(hotel) {
    setSelectedHotelId(hotel.id);
    setError("");

    try {
      const [bookingResponse, priceResponse] = await Promise.all([
        getBookingQuote({
          hotelId: hotel.id,
          roomType: "Family Deluxe Room",
          nights: query.tripDays,
          guests: 2,
          breakfastIncluded: true,
          refundable: true
        }),
        predictPrice({
          destination: query.destination,
          hotelSegment: query.travelerProfile,
          currentPrice: hotel.nightlyPrice,
          daysUntilCheckIn: 2,
          weekendStay: true,
          peakSeason: true
        })
      ]);

      startTransition(() => {
        setBookingQuote(bookingResponse);
        setPricePrediction(priceResponse);
      });

      if (activeChatbotCode === "travel_ai_guide") {
        await handleSendMessage(`Should I book ${hotel.name}?`, {
          selectedHotel: hotel.name,
          stage: "checkout"
        });
      }
    } catch (selectionError) {
      setError(selectionError.message);
    }
  }

  async function handleCreateSession(chatbotCode = activeChatbotCode) {
    setIsChatLoading(true);
    setError("");

    try {
      const createdSession = await createChatSession({
        userId: DEMO_USER_ID,
        chatbotCode,
        title: null
      });
      await refreshChatWorkspace(createdSession.sessionId, chatbotCode);
    } catch (chatError) {
      setError(chatError.message);
    } finally {
      setIsChatLoading(false);
    }
  }

  async function handleSelectSession(sessionId) {
    setIsChatLoading(true);
    setError("");

    try {
      await refreshChatWorkspace(sessionId);
    } catch (chatError) {
      setError(chatError.message);
    } finally {
      setIsChatLoading(false);
    }
  }

  async function handleSendMessage(messageOverride, options = {}) {
    const message = (messageOverride ?? chatInput).trim();
    if (!message) {
      return;
    }

    setIsSendingMessage(true);
    setError("");

    try {
      let sessionId = activeSession?.sessionId;
      if (!sessionId) {
        const createdSession = await createChatSession({
          userId: DEMO_USER_ID,
          chatbotCode: activeChatbotCode,
          title: null
        });
        sessionId = createdSession.sessionId;
      }

      const response = await askAi({
        userId: DEMO_USER_ID,
        sessionId,
        chatbotCode: activeChatbotCode,
        message,
        stage: options.stage ?? (activeChatbot?.domainName === "education" ? "learning" : "discovery"),
        destination: options.destination ?? query.destination,
        selectedHotel: options.selectedHotel ?? hotels.find((hotel) => hotel.id === selectedHotelId)?.name ?? "",
        budgetLevel: options.budgetLevel ?? query.budgetLevel,
        preferences: options.preferences ?? query.interests
      });

      setChatInput("");
      setChatResponse(response);
      await refreshChatWorkspace(response.sessionId, response.chatbotCode);
    } catch (chatError) {
      setError(chatError.message);
    } finally {
      setIsSendingMessage(false);
    }
  }

  async function handleOpenBot(chatbotCode) {
    const nextView = DASHBOARD_BY_CHATBOT[chatbotCode] ?? "home";
    setActiveView(nextView);
    setActiveChatbotCode(chatbotCode);
    setChatInput("");
    await initializeChatWorkspace(chatbotCode);
  }

  if (activeView === "home") {
    return (
      <BotDashboardPage
        chatbots={chatbots}
        onSelectBot={handleOpenBot}
      />
    );
  }

  if (activeView === "education") {
    return (
      <EducationPage
        activeChatbot={activeChatbot}
        activeSession={activeSession}
        chatInput={chatInput}
        chatResponse={chatResponse}
        chatSessions={visibleChatSessions}
        error={error}
        isChatLoading={isChatLoading}
        isSendingMessage={isSendingMessage}
        onBack={() => setActiveView("home")}
        onChatInputChange={setChatInput}
        onNewSession={() => handleCreateSession("education_helpful")}
        onSelectSession={handleSelectSession}
        onSendMessage={handleSendMessage}
      />
    );
  }

  return (
    <HotelSearchPage
      activeChatbot={activeChatbot}
      activeSession={activeSession}
      bookingQuote={bookingQuote}
      chatInput={chatInput}
      chatResponse={chatResponse}
      chatSessions={visibleChatSessions}
      dashboard={dashboard}
      error={error}
      hotels={hotels}
      isChatLoading={isChatLoading}
      isLoading={isLoading}
      isSendingMessage={isSendingMessage}
      itinerary={itinerary}
      nextQuestions={nextQuestions}
      onBack={() => setActiveView("home")}
      onChatInputChange={setChatInput}
      onChatSessionCreate={() => handleCreateSession("travel_ai_guide")}
      onChatSessionSelect={handleSelectSession}
      onHotelSelect={handleHotelSelect}
      onSearch={handleSearch}
      onSendMessage={handleSendMessage}
      pricePrediction={pricePrediction}
      query={query}
      searchSummary={searchSummary}
      selectedHotelId={selectedHotelId}
    />
  );
}

export default App;
