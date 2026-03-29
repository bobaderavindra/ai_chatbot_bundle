import { useEffect, useMemo, useState } from "react";

function formatShortDate(value) {
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return "Add dates";
  }
  return new Intl.DateTimeFormat("en-SG", { month: "short", day: "numeric" }).format(date);
}

function addDays(dateText, days) {
  const date = new Date(dateText);
  if (Number.isNaN(date.getTime())) {
    return dateText;
  }
  date.setDate(date.getDate() + days);
  return date.toISOString().slice(0, 10);
}

function getMapPosition(index) {
  const positions = [
    { top: "18%", left: "16%" },
    { top: "26%", left: "62%" },
    { top: "46%", left: "34%" },
    { top: "61%", left: "70%" },
    { top: "74%", left: "20%" },
    { top: "38%", left: "76%" }
  ];
  return positions[index % positions.length];
}

function getGalleryTone(index, slideIndex) {
  const tones = [
    ["gallery-sky", "gallery-sunset", "gallery-sea"],
    ["gallery-olive", "gallery-sand", "gallery-coral"],
    ["gallery-lagoon", "gallery-dusk", "gallery-gold"]
  ];
  return tones[index % tones.length][slideIndex % 3];
}

export function HotelSearchPage({
  bookingQuote,
  chatResponse,
  dashboard,
  error,
  hotels = [],
  itinerary,
  isLoading,
  nextQuestions = [],
  onHotelSelect,
  onSearch,
  pricePrediction,
  query,
  searchSummary,
  selectedHotelId
}) {
  const [destination, setDestination] = useState(query.destination);
  const [maxBudget, setMaxBudget] = useState(304);
  const [selectedTag, setSelectedTag] = useState("All");
  const [guests, setGuests] = useState(2);
  const [checkIn, setCheckIn] = useState("2026-04-21");
  const [activePanel, setActivePanel] = useState(null);
  const [savedHotels, setSavedHotels] = useState(() => new Set());
  const [hoveredHotelId, setHoveredHotelId] = useState(null);
  const [galleryIndexByHotel, setGalleryIndexByHotel] = useState({});

  useEffect(() => {
    setDestination(query.destination);
  }, [query.destination]);

  const checkOut = useMemo(() => addDays(checkIn, query.tripDays), [checkIn, query.tripDays]);

  const visibleHotels = useMemo(() => hotels.filter((hotel) => {
    const tags = hotel.tags ?? [];
    const withinBudget = hotel.nightlyPrice <= maxBudget;
    const matchesTag = selectedTag === "All" || tags.includes(selectedTag);
    return withinBudget && matchesTag;
  }), [hotels, maxBudget, selectedTag]);

  const tags = ["All", "Beach access", "Breakfast included", "Flexible stay", "Budget", "Kids club"];
  const totalNights = query.tripDays;

  function submitSearch(event) {
    event.preventDefault();
    setActivePanel(null);
    onSearch({
      destination,
      tripDays: query.tripDays,
      budgetLevel: query.budgetLevel,
      travelerProfile: query.travelerProfile,
      requiredAmenities: ["free breakfast"],
      interests: ["culture", "family", "beach"]
    });
  }

  function toggleSaved(hotelId) {
    setSavedHotels((current) => {
      const next = new Set(current);
      if (next.has(hotelId)) {
        next.delete(hotelId);
      } else {
        next.add(hotelId);
      }
      return next;
    });
  }

  function changeGallery(hotelId, direction) {
    setGalleryIndexByHotel((current) => {
      const currentIndex = current[hotelId] ?? 0;
      const nextIndex = (currentIndex + direction + 3) % 3;
      return { ...current, [hotelId]: nextIndex };
    });
  }

  return (
    <div className="search-page">
      <header className="top-shell">
        <div className="brand-rail">
          <div className="brand-mark">T</div>
          <div>
            <strong>TravelWithAI</strong>
            <p>AI stays, pricing and itinerary</p>
          </div>
        </div>

        <form className="search-bar search-pill-bar" onSubmit={submitSearch}>
          <div className={activePanel === "where" ? "search-pill active" : "search-pill"}>
            <button type="button" className="pill-trigger" onClick={() => setActivePanel(activePanel === "where" ? null : "where")}>
              <span>Where</span>
              <strong>{destination}</strong>
            </button>
            {activePanel === "where" ? (
              <div className="pill-panel where-panel">
                <input autoFocus value={destination} onChange={(event) => setDestination(event.target.value)} />
              </div>
            ) : null}
          </div>

          <div className={activePanel === "dates" ? "search-pill active" : "search-pill"}>
            <button type="button" className="pill-trigger" onClick={() => setActivePanel(activePanel === "dates" ? null : "dates")}>
              <span>When</span>
              <strong>{formatShortDate(checkIn)} - {formatShortDate(checkOut)}</strong>
            </button>
            {activePanel === "dates" ? (
              <div className="pill-panel dates-panel">
                <label>
                  Check in
                  <input type="date" value={checkIn} onChange={(event) => setCheckIn(event.target.value)} />
                </label>
                <label>
                  Check out
                  <input type="date" value={checkOut} readOnly />
                </label>
              </div>
            ) : null}
          </div>

          <div className={activePanel === "guests" ? "search-pill active" : "search-pill"}>
            <button type="button" className="pill-trigger" onClick={() => setActivePanel(activePanel === "guests" ? null : "guests")}>
              <span>Who</span>
              <strong>{guests} guest{guests > 1 ? "s" : ""}</strong>
            </button>
            {activePanel === "guests" ? (
              <div className="pill-panel guests-panel">
                {[1, 2, 4, 6].map((count) => (
                  <button key={count} type="button" className={guests === count ? "guest-option active" : "guest-option"} onClick={() => setGuests(count)}>
                    {count} guest{count > 1 ? "s" : ""}
                  </button>
                ))}
              </div>
            ) : null}
          </div>

          <button type="submit" className="search-submit">Search</button>
        </form>

        <div className="profile-pill">Demo traveler</div>
      </header>

      <section className="filter-bar">
        <div className="chip-scroll">
          {tags.map((tag) => (
            <button
              type="button"
              key={tag}
              className={selectedTag === tag ? "filter-chip active" : "filter-chip"}
              onClick={() => setSelectedTag(tag)}
            >
              {tag}
            </button>
          ))}
        </div>
        <div className="budget-filter">
          <span>Max price ${maxBudget}</span>
          <input
            type="range"
            min="120"
            max="450"
            value={maxBudget}
            onChange={(event) => setMaxBudget(Number(event.target.value))}
          />
        </div>
      </section>

      <main className="results-shell">
        <section className="results-column">
          <div className="results-head">
            <div>
              <p className="results-overline">Over {visibleHotels.length || hotels.length} stays</p>
              <h1>Stays in {destination}</h1>
              <p>{formatShortDate(checkIn)} - {formatShortDate(checkOut)} · {guests} guests · {searchSummary}</p>
            </div>
            <div className="signal-box">
              <span>Price signal</span>
              <strong>{pricePrediction ? pricePrediction.trend : "steady"}</strong>
              <p>{pricePrediction ? pricePrediction.bestBookingWindow : "Compare refundable options before checkout."}</p>
            </div>
          </div>

          {error ? <div className="status-banner error">{error}</div> : null}
          {isLoading ? <div className="status-banner">Loading stays and AI recommendations...</div> : null}

          <div className="listing-stack">
            {visibleHotels.map((hotel, index) => {
              const totalPrice = hotel.nightlyPrice * totalNights;
              const highlighted = selectedHotelId === hotel.id || hoveredHotelId === hotel.id;
              const galleryIndex = galleryIndexByHotel[hotel.id] ?? 0;
              const mapPosition = getMapPosition(index);
              const isSaved = savedHotels.has(hotel.id);
              const hotelTags = hotel.tags ?? [];
              const amenities = hotel.amenities ?? [];

              return (
                <article
                  key={hotel.id}
                  className={highlighted ? "listing-card active" : "listing-card"}
                  onMouseEnter={() => setHoveredHotelId(hotel.id)}
                  onMouseLeave={() => setHoveredHotelId(null)}
                >
                  <div className={`listing-image ${getGalleryTone(index, galleryIndex)}`}>
                    <div className="gallery-toolbar">
                      <button type="button" className={isSaved ? "save-badge saved" : "save-badge"} onClick={() => toggleSaved(hotel.id)}>
                        {isSaved ? "♥ Saved" : "♡ Save"}
                      </button>
                      <div className="gallery-dots">
                        {[0, 1, 2].map((dot) => (
                          <span key={dot} className={galleryIndex === dot ? "gallery-dot active" : "gallery-dot"} />
                        ))}
                      </div>
                    </div>

                    <div className="gallery-caption">
                      <div className="image-caption">{hotel.area}</div>
                      <div className="gallery-arrows">
                        <button type="button" onClick={() => changeGallery(hotel.id, -1)} aria-label="Previous photo">‹</button>
                        <button type="button" onClick={() => changeGallery(hotel.id, 1)} aria-label="Next photo">›</button>
                      </div>
                    </div>
                  </div>

                  <div className="listing-copy">
                    <div className="listing-meta">
                      <div>
                        <p className="muted-text">{hotel.provider} · {hotel.area}</p>
                        <h2>{hotel.name}</h2>
                        <p>{amenities.slice(0, 3).join(" · ")}</p>
                      </div>
                      <div className="rating-pill">★ {hotel.rating}</div>
                    </div>

                    <div className="listing-tags">
                      {hotelTags.map((tag) => (
                        <span key={tag}>{tag}</span>
                      ))}
                    </div>

                    <div className="listing-details">
                      <p className="policy-text">{hotel.cancellationPolicy}</p>
                      <p className="map-hint">Map marker: {mapPosition.top} / {mapPosition.left}</p>
                    </div>

                    <div className="listing-footer">
                      <div>
                        <p className="price-line"><strong>${hotel.nightlyPrice}</strong> night</p>
                        <p>${totalPrice} total before taxes</p>
                      </div>
                      <button type="button" onClick={() => onHotelSelect(hotel)}>
                        {selectedHotelId === hotel.id ? "Selected" : "Reserve overview"}
                      </button>
                    </div>
                  </div>
                </article>
              );
            })}
          </div>
        </section>

        <aside className="map-column">
          <div className="map-card">
            <div className="map-surface">
              <div className="map-grid" />
              {visibleHotels.map((hotel, index) => {
                const highlighted = selectedHotelId === hotel.id || hoveredHotelId === hotel.id;
                const position = getMapPosition(index);
                return (
                  <button
                    type="button"
                    key={hotel.id}
                    className={highlighted ? "map-pin active" : "map-pin"}
                    style={position}
                    onClick={() => onHotelSelect(hotel)}
                    onMouseEnter={() => setHoveredHotelId(hotel.id)}
                    onMouseLeave={() => setHoveredHotelId(null)}
                  >
                    ${hotel.nightlyPrice}
                  </button>
                );
              })}
              <div className="map-label">Interactive stay map</div>
            </div>
          </div>

          <div className="info-card standout">
            <p className="card-label">AI co-host</p>
            <h3>{chatResponse ? chatResponse.reply : "Ask the assistant about refundable deals and itinerary tradeoffs."}</h3>
            {chatResponse ? (
              <ul>
                {chatResponse.agentInsights.map((insight) => (
                  <li key={insight.agentName}>{insight.agentName}: {insight.summary}</li>
                ))}
              </ul>
            ) : null}
          </div>

          <div className="info-grid">
            <section className="info-card">
              <p className="card-label">Trip plan</p>
              <h3>{itinerary ? `${itinerary.days.length} day outline` : "Itinerary"}</h3>
              <p>{itinerary ? itinerary.overview : "Generate a trip flow from selected stays."}</p>
            </section>
            <section className="info-card">
              <p className="card-label">Booking quote</p>
              <h3>{bookingQuote ? `$${bookingQuote.totalPrice}` : "No quote yet"}</h3>
              <p>{bookingQuote ? bookingQuote.policyHighlights[0] : "Select a stay to compare quote details."}</p>
            </section>
            <section className="info-card">
              <p className="card-label">Dashboard</p>
              <h3>{dashboard ? dashboard.welcomeMessage : "Traveler dashboard"}</h3>
            </section>
            <section className="info-card">
              <p className="card-label">Next prompts</p>
              <ul>
                {nextQuestions.map((question) => (
                  <li key={question}>{question}</li>
                ))}
              </ul>
            </section>
          </div>
        </aside>
      </main>
    </div>
  );
}
