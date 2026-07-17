# Architecture Decision Records

Thư mục này lưu trữ các quyết định kiến trúc cho dự án Travel Planner.

## Cách đọc tài liệu ADR

- Mỗi ADR tập trung vào một quyết định cụ thể.
- ADR mới nên được đặt tên theo định dạng rõ ràng và có trạng thái phù hợp.
- Khi một quyết định bị thay thế, ADR cũ nên được đánh dấu là Deprecated hoặc Superseded.

## Nhóm ADR

### 1. Cơ bản và nền tảng
- [ADR-001](ADR-001-ghi-chep-quyet-dinh-kien-truc.md): Ghi lại các quyết định kiến trúc bằng ADR
- [ADR-002](ADR-002-chon-kien-truc-modular-monolith.md): Chọn kiến trúc Modular Monolith cho backend
- [ADR-003](ADR-003-chon-postgresql-va-pgvector.md): Chọn PostgreSQL kết hợp pgvector cho lưu trữ dữ liệu và tìm kiếm ngữ nghĩa
- [ADR-004](ADR-004-chon-react-vite-va-typescript.md): Chọn React, Vite và TypeScript cho frontend
- [ADR-005](ADR-005-chon-jwt-va-spring-security.md): Chọn JWT kết hợp Spring Security cho xác thực và phân quyền

### 2. Công nghệ và nền tảng triển khai
- [ADR-006](ADR-006-chon-docker-va-docker-compose.md): Chọn Docker và Docker Compose cho môi trường phát triển và triển khai
- [ADR-007](ADR-007-chon-redis-cho-cache-va-rate-limiting.md): Chọn Redis cho cache và rate limiting
- [ADR-008](ADR-008-chon-websocket-cho-collaboration.md): Chọn WebSocket cho tính năng collaboration real-time
- [ADR-009](ADR-009-chon-openapi-swagger-cho-api-docs.md): Chọn OpenAPI / Swagger cho tài liệu API

### 3. Cấu trúc và thiết kế ứng dụng
- [ADR-010](ADR-010-chon-cau-truc-domain-driven-va-layered-architecture.md): Chọn cấu trúc Domain-Driven và Layered Architecture cho backend
- [ADR-011](ADR-011-chon-bucket4j-cho-rate-limiting.md): Chọn Bucket4j cho rate limiting API
- [ADR-012](ADR-012-chon-export-pdf-va-ics.md): Chọn PDF và ICS làm định dạng xuất dữ liệu chính
- [ADR-013](ADR-013-chon-ai-integration-strategy-voi-gemini-openweather-va-ors.md): Chọn Gemini, OpenWeather và OpenRouteService cho tích hợp AI và dịch vụ bên ngoài

### 4. Phát triển, vận hành và chất lượng
- [ADR-014](ADR-014-chon-deployment-va-production-architecture.md): Chọn kiến trúc deployment và production phù hợp cho hệ thống
- [ADR-015](ADR-015-chon-monitoring-va-logging.md): Chọn monitoring và logging để giám sát hệ thống
- [ADR-016](ADR-016-chon-ci-cd-cho-deployment-tu-dong.md): Chọn CI/CD để tự động hóa build, test và deployment
- [ADR-017](ADR-017-chon-strategy-testing.md): Chọn strategy testing cho hệ thống

### 5. Bảo mật và quản trị dữ liệu
- [ADR-018](ADR-018-chon-secrets-management-va-environment-variables.md): Chọn secrets management và environment variables cho cấu hình nhạy cảm
- [ADR-019](ADR-019-chon-security-hardening-cho-application.md): Chọn security hardening cho ứng dụng
- [ADR-020](ADR-020-chon-feature-flags-cho-phat-trien-va-release.md): Chọn feature flags cho phát triển và release
- [ADR-021](ADR-021-chon-observability-va-distributed-tracing.md): Chọn observability và distributed tracing cho hệ thống
- [ADR-022](ADR-022-chon-disaster-recovery-va-backup-strategy.md): Chọn disaster recovery và backup strategy cho hệ thống
- [ADR-023](ADR-023-chon-backward-compatibility-va-versioning.md): Chọn backward compatibility và versioning cho API và contract
- [ADR-028](ADR-028-chon-refresh-token-cho-authentication.md): Chọn refresh token cho xác thực dài hạn
- [ADR-029](ADR-029-chon-data-governance-va-privacy-compliance.md): Chọn data governance và privacy compliance cho hệ thống

### 6. Mở rộng và phát triển dài hạn
- [ADR-024](ADR-024-chon-api-gateway-cho-he-thong.md): Chọn API Gateway cho hệ thống
- [ADR-025](ADR-025-chon-luoi-di-chuyen-sang-microservices.md): Chọn lộ trình chuyển đổi sang microservices khi hệ thống phát triển
- [ADR-026](ADR-026-chon-event-driven-architecture-cho-collaboration-va-notification.md): Chọn Event-Driven Architecture cho collaboration và notification
- [ADR-027](ADR-027-chon-data-migration-strategy-cho-phat-trien-he-thong.md): Chọn data migration strategy cho phát triển và thay đổi schema
- [ADR-030](ADR-030-chon-multi-tenancy-cho-phan-hang-doanh-nghiep.md): Chọn multi-tenancy cho phân khúc doanh nghiệp và B2B
- [ADR-031](ADR-031-chon-enterprise-readiness-va-governance.md): Chọn enterprise readiness và governance cho hệ thống
- [ADR-032](ADR-032-chon-workflow-approval-cho-adr-va-thay-doi-kien-truc.md): Chọn workflow approval cho ADR và thay đổi kiến trúc

## Tài liệu liên quan
- [Architecture Overview](../architecture-overview.md)
