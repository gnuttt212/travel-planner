# API Endpoints

This document lists the REST API endpoints available in Travel Planner, grouped by service area.

## API Tree

- `/api/v1`
  - `/auth`
    - `POST /register`
    - `POST /login`
  - `/profile`
    - `GET /`
    - `PUT /`
    - `GET /friends`
    - `GET /search`
    - `/friend-requests`
      - `GET /incoming`
      - `GET /outgoing`
      - `POST /`
      - `PATCH /{id}/accept`
      - `PATCH /{id}/reject`
      - `GET /stream`
    - `DELETE /friends/{email}`
  - `/messages`
    - `POST /`
    - `GET /with/{otherEmail}`
  - `/comments`
    - `POST /`
    - `GET /trip/{tripId}`
  - `/reactions`
    - `POST /`
    - `DELETE ?targetType={type}&targetId={id}`
    - `GET ?targetType={type}&targetId={id}`
  - `/planning`
    - `POST /recommend`
  - `/trips`
    - `POST /`
    - `GET /`
    - `GET /{id}`
    - `DELETE /{id}`
    - `GET /users/{email}/trips`
    - `/export`
      - `GET /{id}/export/ics`
      - `GET /{id}/export/pdf`
  - `/destinations`
    - `GET /cities`
  - `/admin`
    - `GET /users`

## API Authentication

- All authenticated requests require a valid JWT access token.
- Clients must send the token in the HTTP header:
  - `Authorization: Bearer <token>`
- The JWT is issued by `POST /api/v1/auth/login` and also returned after user registration.
- Public endpoints that do not require auth:
  - `POST /api/v1/auth/register`
  - `POST /api/v1/auth/login`
  - `GET /swagger-ui/**`
  - `GET /v3/api-docs/**`
  - `GET /actuator/**`

## Roles

- `ROLE_USER`
  - Default role assigned to newly registered users.
  - Can access profile, friend, message, comment, reaction, planning, and trip management endpoints.
- `ROLE_ADMIN`
  - Granted to administrator accounts only.
  - Required for admin-only endpoints such as `/api/v1/admin/users`.
  - Also authorized to delete trips owned by other users via `/api/v1/trips/{id}`.

## Authentication

### POST /api/v1/auth/register
- Request: `AuthRequest` (`email`, `password`)
- Response: `ApiResponse<AuthResponse>`
- Description: Register a new user account.

### POST /api/v1/auth/login
- Request: `AuthRequest` (`email`, `password`)
- Response: `ApiResponse<AuthResponse>`
- Description: Authenticate and receive a JWT access token.

## User Profile & Social

### GET /api/v1/profile
- Response: `ApiResponse<ProfileResponse>`
- Description: Get the current authenticated user's profile.

### PUT /api/v1/profile
- Request: `UserProfileUpdateRequest`
- Response: `ApiResponse<ProfileResponse>`
- Description: Update the authenticated user's profile information.

### GET /api/v1/profile/friends
- Response: `ApiResponse<List<UserSearchResult>>`
- Description: Get the current user's friend list.

### GET /api/v1/profile/search?q={query}
- Query: `q` (optional)
- Response: `ApiResponse<List<UserSearchResult>>`
- Description: Search for other users by name or email.

### GET /api/v1/profile/friend-requests/incoming
- Response: `ApiResponse<List<FriendRequestDto>>`
- Description: List incoming friend requests.

### GET /api/v1/profile/friend-requests/outgoing
- Response: `ApiResponse<List<FriendRequestDto>>`
- Description: List outgoing friend requests.

### POST /api/v1/profile/friend-requests
- Request: `FriendRequest` (`email`)
- Response: `ApiResponse<Void>`
- Description: Send a friend request to another user.

### PATCH /api/v1/profile/friend-requests/{id}/accept
- Response: `ApiResponse<Void>`
- Description: Accept a pending friend request.

### PATCH /api/v1/profile/friend-requests/{id}/reject
- Response: `ApiResponse<Void>`
- Description: Reject a pending friend request.

### DELETE /api/v1/profile/friends/{email}
- Response: `ApiResponse<Void>`
- Description: Remove a user from the current user's friend list.

### GET /api/v1/profile/friend-requests/stream
- Response: `SSE` stream of friend request events
- Description: Subscribe to real-time friend request notifications.

## Messaging

### POST /api/v1/messages
- Request: `SendMessageRequest` (`to`, `content`)
- Response: `ApiResponse<MessageDto>`
- Description: Send a chat message to another user.

### GET /api/v1/messages/with/{otherEmail}
- Response: `ApiResponse<List<MessageDto>>`
- Description: Load the message conversation with another user.

## Comments

### POST /api/v1/comments
- Request: `AddCommentRequest` (`tripId`, `content`)
- Response: `ApiResponse<CommentDto>`
- Description: Add a comment to a trip.

### GET /api/v1/comments/trip/{tripId}
- Response: `ApiResponse<List<CommentDto>>`
- Description: List comments for a specific trip.

## Reactions

### POST /api/v1/reactions
- Request: `ReactRequest` (`targetType`, `targetId`, `type`)
- Response: `ApiResponse<ReactionDto>`
- Description: Add or update a reaction for a target entity.

### DELETE /api/v1/reactions?targetType={type}&targetId={id}
- Response: `ApiResponse<Void>`
- Description: Remove the current user's reaction from a target.

### GET /api/v1/reactions?targetType={type}&targetId={id}
- Response: `ApiResponse<List<ReactionDto>>`
- Description: Get all reactions for a target entity.

## Trip Planning & Management

### POST /api/v1/planning/recommend
- Request: `TripRequest`
- Response: `ApiResponse<List<TripPlanResponse>>`
- Description: Get recommended trip plan variants from the recommendation engine.

### POST /api/v1/trips
- Request: `TripRequest`
- Response: `ApiResponse<TripResponse>`
- Description: Create a new trip from a selected plan.

### GET /api/v1/trips
- Response: `ApiResponse<List<TripResponse>>`
- Description: Get trips owned by the authenticated user.

### GET /api/v1/trips/{id}
- Response: `ApiResponse<TripResponse>`
- Description: Get details for a specific trip.

### DELETE /api/v1/trips/{id}
- Response: `204 No Content`
- Description: Delete a trip. Owners or ADMIN users may delete.

### GET /api/v1/users/{email}/trips
- Response: `ApiResponse<List<TripResponse>>`
- Description: Get trips created by another user.

## Export

### GET /api/v1/trips/{id}/export/ics
- Response: `text/calendar`
- Description: Export a trip as an ICS calendar file.

### GET /api/v1/trips/{id}/export/pdf
- Response: `application/pdf`
- Description: Export a trip as a PDF document.

## Destinations

### GET /api/v1/destinations/cities
- Response: `ApiResponse<List<String>>`
- Description: Get the list of distinct destination cities.

## Admin

### GET /api/v1/admin/users
- Response: `ResponseEntity<List<User>>`
- Description: List all users (ADMIN only).

## Notes

- Most endpoints require JWT authentication via `Authorization: Bearer <token>`.
- Public endpoints: `/api/v1/auth/register`, `/api/v1/auth/login`.
- Admin endpoints require the `ADMIN` role.
- Responses are wrapped in `ApiResponse<T>` for success metadata.
