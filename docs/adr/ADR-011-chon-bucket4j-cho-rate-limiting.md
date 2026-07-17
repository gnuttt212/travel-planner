# ADR-011: Chọn Bucket4j cho rate limiting API

- Date: 2026-07-17
- Status: Accepted
- Authors: Development Team

## Context

Hệ thống có các API có thể bị khai thác hoặc bị overload bởi lưu lượng truy cập bất thường, đặc biệt là các endpoint liên quan đến AI và dịch vụ bên ngoài. Cần một cơ chế rate limiting đủ linh hoạt để kiểm soát truy cập và bảo vệ tài nguyên hệ thống.

## Decision

Chúng tôi quyết định sử dụng Bucket4j để triển khai rate limiting cho API.

Bucket4j được chọn vì:

- dễ tích hợp vào Spring Boot;
- hỗ trợ giới hạn số request theo thời gian;
- có thể kết hợp với Redis để chia sẻ state giữa các instance;
- phù hợp cho các hệ thống cần kiểm soát traffic ở mức ứng dụng.

## Consequences

### Positive

- Bảo vệ hệ thống khỏi spam và overload.
- Giúp kiểm soát chi phí cho các API bên ngoài như AI và map service.
- Dễ cấu hình cho từng endpoint hoặc nhóm endpoint.

### Negative

- Cần cấu hình chính xác để tránh ảnh hưởng đến người dùng hợp lệ.
- Nếu dùng cùng Redis thì cần quản lý thêm dependency và cấu hình.

## Alternatives considered

### 1. Rate limiting ở tầng gateway hoặc reverse proxy

Hiệu quả cho toàn bộ hệ thống nhưng có thể thiếu linh hoạt cho logic nghiệp vụ cụ thể.

### 2. Không dùng rate limiting

Đơn giản, nhưng không bảo vệ đủ trước các tình huống tăng traffic đột ngột.

## Implementation Notes

Bucket4j sẽ được dùng kết hợp với Redis để áp dụng rate limiting cho các API nhạy cảm và các dịch vụ sử dụng tài nguyên đắt đỏ.
