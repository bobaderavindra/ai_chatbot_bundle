# Phase 1 System Design

## Overall architecture

```text
React Frontend
    |
Spring Boot API Gateway
    |
------------------------------------------------
| AI Service | Booking Service | Pricing Service |
------------------------------------------------
    |
---------------------------------------------------------------
| PostgreSQL | Redis | Vector DB | External Hotel/Maps/Payment |
---------------------------------------------------------------
```

This repository now reflects that shape at code level:

- `ai`: context-aware chat and multi-agent orchestration
- `booking`: quote and checkout-facing booking flows
- `pricing`: price prediction service
- `travel`: search, itinerary, and dashboard aggregation

## AI agents

- Planner Agent: itinerary and sequencing
- Hotel Agent: hotel selection and policy fit
- Price Agent: booking timing and fare pressure
- Local Guide Agent: local experiences and nearby options

The current implementation uses deterministic mock logic. Replace these with LLM-backed prompt executors later without changing the controller contracts.

## Data layer

Target production stack:

- PostgreSQL: users, bookings, hotel cache, itinerary persistence
- Redis: session cache, result caching, short-lived checkout context
- Vector DB: chat memory and retrieval augmentation

`compose.yaml` now includes PostgreSQL, Redis, and Weaviate containers for local Phase 1 infrastructure.

## Frontend target

Frontend scaffold is placed in `frontend/` and is designed for:

- Home page with conversational search
- Agoda-style hotel cards and filters
- Itinerary side panel
- Price prediction insights
- Persistent AI assistant

## Immediate build sequence

1. Wire the React app to the Spring Boot endpoints.
2. Persist users, bookings, and chat history in PostgreSQL.
3. Replace mock provider adapters with real hotel, map, and payment integrations.
4. Replace heuristic AI with LLM and vector-memory backed orchestration.
