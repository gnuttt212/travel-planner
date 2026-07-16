# Travel Planner AI

An advanced, AI-powered travel planning application built with a Modular Monolith architecture on Spring Boot 3 and a modern React frontend. The system leverages Vector Databases, Generative AI, and Open-Source GIS technologies to provide highly personalized, weather-aware, and perfectly optimized travel itineraries.

## Key Features

* ** AI-Powered RAG Itinerary Generation:** Uses Google's Gemini AI combined with a PostgreSQL `pgvector` database to generate highly detailed, personalized itineraries based on user preferences.
* ** Weather-Aware AI Prompting:** Integrates with OpenWeatherMap. If rain is forecast, the system dynamically alters the AI prompt to prioritize indoor activities (museums, cafes).
* ** Smart Recommendation Engine:**
  * **Vector Search:** Matches destinations with the user's weighted tags.
  * **Bayesian Average:** Factors in destination ratings.
  * **Continuous Feedback Loop:** Automatically updates popularity scores based on user interactions (View, Save, Select) via a scheduled job.
  * **Collaborative Filtering:** Uses Cosine Similarity to recommend destinations that "users similar to you" have selected.
* ** Open-Source GIS Stack:**
  * **Map UI:** React-Leaflet rendering OpenStreetMap tiles.
  * **Geocoding:** Nominatim API for location lookups.
  * **TSP Optimization:** OpenRouteService (ORS) VROOM API reorganizes your itinerary to minimize travel time (with a Haversine/Nearest Neighbor fallback).
* ** Real-Time Collaboration:** WebSocket + STOMP integration allows multiple users (Owners/Editors) to edit the same trip simultaneously.
* ** Budget Tracking:** Complete expense management with category and daily summaries.
* ** Booking (Adapter Pattern):** A flexible mock booking module designed with the Open/Closed Principle to easily swap flight and hotel providers.
* ** Security & Rate Limiting:** JWT-based authentication. High-cost APIs (Gemini, ORS) are protected by a **Bucket4j + Redis** rate limiter.
* ** Export Options:** Export itineraries to **PDF** or directly to **Google/Apple Calendar (ICS)**.

---

## Architecture

The backend follows a strict **Modular Monolith** pattern. Each domain (`user`, `itinerary`, `budget`, `collaboration`, `booking`, `interaction`) is isolated, ensuring high maintainability and easy transition to microservices in the future.

### System Architecture Diagram

```mermaid
graph TD
    %% Define styles
    classDef frontend fill:#3b82f6,stroke:#1e3a8a,stroke-width:2px,color:#fff;
    classDef backend fill:#10b981,stroke:#047857,stroke-width:2px,color:#fff;
    classDef database fill:#f59e0b,stroke:#b45309,stroke-width:2px,color:#fff;
    classDef external fill:#8b5cf6,stroke:#5b21b6,stroke-width:2px,color:#fff;
    
    %% Frontend
    User([ User]) --> |HTTPS / WSS| ReactFrontend
    
    subgraph "Frontend Layer"
        ReactFrontend[ React + Vite App\nLeaflet | DnD-Kit | Axios]:::frontend
    end
    
    ReactFrontend --> |REST APIs| ApiGateway
    ReactFrontend --> |STOMP/WS| WebSocketHub
    
    %% Backend
    subgraph "Spring Boot Backend (Modular Monolith)"
        ApiGateway[ Security Filter Chain\nJWT Auth]:::backend
        RateLimiter[ Bucket4j Rate Limiter]:::backend
        
        ApiGateway --> RateLimiter
        RateLimiter --> Modules
        
        subgraph Modules ["Business Modules"]
            AuthMod[ User/Auth Module]:::backend
            ItineraryMod[🗺️ Itinerary Module\nVector Search | CF]:::backend
            BudgetMod[ Budget Module]:::backend
            CollabMod[ Collaboration Module]:::backend
            BookingMod[ Booking Module\nAdapter Pattern]:::backend
            FeedbackMod[ Interaction/Feedback\nScheduled Jobs]:::backend
        end
        
        WebSocketHub(( WebSocket Config)):::backend
        CollabMod -.-> WebSocketHub
    end

    %% Databases
    subgraph "Data Storage"
        PostgreSQL[( PostgreSQL 16\npgvector)]:::database
        Redis[( Redis\nCache / Rate Limiter)]:::database
    end
    
    Modules --> PostgreSQL
    RateLimiter --> Redis
    
    %% External Services
    subgraph "External APIs"
        Gemini[ Google Gemini AI]:::external
        OpenWeather[ OpenWeatherMap]:::external
        ORS[ OpenRouteService]:::external
        Nominatim[ OSM Nominatim]:::external
    end
    
    ItineraryMod --> |RAG Prompting| Gemini
    ItineraryMod --> |Forecast| OpenWeather
    ItineraryMod --> |TSP Optimization| ORS
    ItineraryMod --> |Geocoding| Nominatim
```

---

## 📦 Project Structure

```text
travel-planner/
├── frontend/                     # React Vite Application
│   ├── src/
│   │   ├── pages/                # Login, Onboarding, Destinations, Planner
│   │   ├── api.ts                # Axios instance with JWT interceptor
│   │   └── index.css             # Vanilla CSS UI
├── src/main/java/com/travelplanner/
│   ├── common/                   # Shared Configs (Security, Rate Limiting, WS)
│   ├── user/                     # User Auth & Preferences (pgvector)
│   ├── itinerary/                # Destinations, Route Optimization, Exporting
│   ├── budget/                   # Expense tracking
│   ├── collaboration/            # Real-time WebSockets
│   ├── booking/                  # Adapter pattern for booking providers
│   └── interaction/              # Feedback loop & Popularity scheduler
└── docker-compose.yml            # PostgreSQL (pgvector) and Redis setup
```

## 🛠️ Getting Started

### Prerequisites
- Java 17
- Node.js & npm
- Docker (for PostgreSQL & Redis)

### 1. Setup Infrastructure
Start the required databases using Docker:
```bash
docker-compose up -d
```

### 2. Configure Environment Variables
Open `src/main/resources/application.yml` and insert your API keys:
```yaml
gemini.api.key: "YOUR_GEMINI_API_KEY_HERE"
ors.api.key: "YOUR_ORS_API_KEY_HERE"
openweathermap.api.key: "YOUR_OWM_API_KEY_HERE"
```

### 3. Start the Backend
```bash
./mvnw spring-boot:run
```
*(The backend runs on `http://localhost:8080`)*

### 4. Start the Frontend
```bash
cd frontend
npm install
npm run dev
```
*(The frontend runs on `http://localhost:5173`)*

## 📚 Technical Highlights
- **Adapter Pattern:** Used in the `Booking` module to dynamically inject providers (`FakeFlightProvider`, `FakeHotelProvider`) conforming to the Open/Closed Principle.
- **Traveling Salesperson Problem (TSP):** Handled via `OpenRouteService` VROOM API, with a robust mathematical fallback (Nearest Neighbor + Haversine formula) written in pure Java.
- **AI Recommendation Engine:** A 3-step pipeline combining Hard Filters (Budget/Date) -> Vector Semantic Search (pgvector) -> Collaborative Filtering (Cosine Similarity of User Preferences).
