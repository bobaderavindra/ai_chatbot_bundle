# TravelWithAI Architecture

## Product direction

This backend is structured around the same three points where Agoda's AI creates leverage:

1. Discovery: conversational travel search with filters, recommendations, and next-step prompts.
2. Decision support: hotel-detail intelligence such as amenities, breakfast, location, and family fit.
3. Checkout assistance: policy clarity, cancellation rules, cashback eligibility, and price-drop-off recovery.

The current implementation is a runnable MVP backend with mock providers. Real Agoda, Booking.com, flight, and activity feeds should be plugged in behind `TravelInventoryProvider` and additional provider adapters.

## Recommended target architecture

### Frontend

- Next.js or React SPA
- Search page with conversational query bar
- Listing page with hotel cards and AI refinement chips
- Hotel details page with persistent property assistant
- Checkout page with policy and offer assistant
- Dashboard page with saved trips, alerts, and quick actions

### Backend services

- API Gateway: auth, rate limiting, aggregation, request tracing
- Search Service: hotel, flight, and activity federation
- Itinerary Service: trip plan generation and map enrichment
- Booking Assistant Service: context-aware Q&A at hotel and checkout steps
- User Profile Service: preferences, memory, budgets, family settings
- Price Intelligence Service: time-series forecasting and booking-time recommendations

### AI orchestration

- Planner Agent: creates itinerary
- Booking Agent: answers policy and payment questions
- Price Agent: predicts rate movement
- Local Guide Agent: suggests attractions and nearby experiences

Keep prompt orchestration separate from provider connectors. The chatbot should always receive:

- user intent
- current booking stage
- selected hotel or room context
- price and promotion context
- user profile memory

## API starter endpoints

- `POST /api/travel/search`
- `POST /api/travel/itinerary`
- `POST /api/travel/booking-assistant`
- `GET /api/travel/dashboard/{userId}`

## Real provider integration plan

Replace `MockTravelInventoryProvider` with adapters such as:

- `AgodaInventoryProvider`
- `BookingInventoryProvider`
- `FlightsInventoryProvider`
- `ActivitiesInventoryProvider`

Normalize provider responses into the domain models before exposing them to the AI or UI. This avoids leaking vendor-specific formats into prompt logic and frontend rendering.

## Immediate next implementation steps

1. Add persistent storage for users, chats, bookings, and cached travel inventory.
2. Add OpenAI or another LLM client for prompt-based responses instead of rule-based mock answers.
3. Build a Next.js frontend with a search page, hotel list, details page, checkout page, and dashboard.
4. Add authentication and per-user chat memory.
5. Introduce a price forecasting service using your time-series model.
