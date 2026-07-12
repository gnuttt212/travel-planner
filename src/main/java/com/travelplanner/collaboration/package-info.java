/**
 * Module Cong tac nhom (Collaboration).
 *
 * Day la module nen dung WebSocket (Spring WebSocket + STOMP) de nhieu
 * nguoi cung chinh sua 1 lich trinh theo thoi gian thuc - diem nhan ky
 * thuat quan trong cho portfolio (xu ly dong thoi/concurrency).
 *
 * Goi y bo sung dependency: spring-boot-starter-websocket
 *
 * Goi y entity ban dau:
 *   - TripMember (tripId, userId, role: OWNER/EDITOR/VIEWER)
 *   - TripComment (tripId, userId, content, createdAt)
 *
 * Goi y endpoint:
 *   POST /api/v1/trips/{tripId}/members
 *   WS   /ws/trips/{tripId}   -> kenh realtime cho chinh sua/chat
 */
package com.travelplanner.collaboration;
