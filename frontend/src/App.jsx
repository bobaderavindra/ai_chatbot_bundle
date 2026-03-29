import { startTransition, useEffect, useState } from "react";
import { askAi, generateItinerary, getBookingQuote, getDashboard, predictPrice, searchTravel } from "./api";
import { HotelSearchPage } from "./components/HotelSearchPage";

const initialQuery = {
  destination: "Bali",
  tripDays: 4,
  budgetLevel: "mid-range",
  travelerProfile: "family",
  requiredAmenities: ["free breakfast"],
  interests: ["culture", "family", "beach"]
};

function App() {
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
  const [error, setError] = useState("");
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    loadInitialData(initialQuery);
  }, []);

  async function loadInitialData(activeQuery) {
    setIsLoading(true);
    setError("");

    try {
      const searchResponse = await searchTravel(activeQuery);
      const firstHotel = searchResponse.hotels[0];

      const [dashboardResponse, itineraryResponse, priceResponse, aiResponse, bookingResponse] = await Promise.all([
        getDashboard("demo-user"),
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
        askAi({
          userId: "demo-user",
          message: "Can I cancel for free and stay under budget?",
          stage: "discovery",
          destination: activeQuery.destination,
          selectedHotel: firstHotel ? firstHotel.name : "",
          budgetLevel: activeQuery.budgetLevel,
          preferences: activeQuery.interests
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
        setChatResponse(aiResponse);
        setBookingQuote(bookingResponse);
      });
    } catch (loadError) {
      setError(loadError.message);
    } finally {
      setIsLoading(false);
    }
  }

  async function handleSearch(nextQuery) {
    setQuery(nextQuery);
    await loadInitialData(nextQuery);
  }

  async function handleHotelSelect(hotel) {
    setSelectedHotelId(hotel.id);
    try {
      const [bookingResponse, aiResponse, priceResponse] = await Promise.all([
        getBookingQuote({
          hotelId: hotel.id,
          roomType: "Family Deluxe Room",
          nights: query.tripDays,
          guests: 2,
          breakfastIncluded: true,
          refundable: true
        }),
        askAi({
          userId: "demo-user",
          message: `Should I book ${hotel.name}?`,
          stage: "checkout",
          destination: query.destination,
          selectedHotel: hotel.name,
          budgetLevel: query.budgetLevel,
          preferences: query.interests
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
        setChatResponse(aiResponse);
        setPricePrediction(priceResponse);
      });
    } catch (selectionError) {
      setError(selectionError.message);
    }
  }

  return (
    <HotelSearchPage
      bookingQuote={bookingQuote}
      chatResponse={chatResponse}
      dashboard={dashboard}
      error={error}
      hotels={hotels}
      itinerary={itinerary}
      isLoading={isLoading}
      nextQuestions={nextQuestions}
      onHotelSelect={handleHotelSelect}
      onSearch={handleSearch}
      pricePrediction={pricePrediction}
      query={query}
      searchSummary={searchSummary}
      selectedHotelId={selectedHotelId}
    />
  );
}

export default App;
