# ADR-017: Chọn strategy testing cho hệ thống

- Date: 2026-07-17
- Status: Accepted
- Authors: Development Team

## Context

Hệ thống có nhiều tầng khác nhau như API, service, repository và integration với dịch vụ bên ngoài. Cần một chiến lược kiểm thử phù hợp để đảm bảo chất lượng và giảm rủi ro khi thay đổi code.

## Decision

Chúng tôi quyết định áp dụng chiến lược testing đa tầng gồm:

- unit test cho logic nghiệp vụ;
- integration test cho các tầng service và repository;
- API test cho controller và endpoint;
- smoke test cho các luồng chính của hệ thống.

## Consequences

### Positive

- Tăng độ tin cậy cho hệ thống.
- Giảm nguy cơ lỗi khi thay đổi code.
- Hỗ trợ refactoring và mở rộng tính năng.

### Negative

- Cần đầu tư thời gian để viết và duy trì test.
- Một số test integration có thể chậm hơn so với unit test.

## Alternatives considered

### 1. Chỉ dùng manual testing

Nhanh hơn ban đầu, nhưng khó kiểm soát và dễ bỏ sót lỗi.

### 2. Chỉ tập trung unit test

Không đủ để phát hiện lỗi ở tầng integration và API.

## Implementation Notes

Test sẽ được chạy tự động trong pipeline CI/CD, với ưu tiên cho các luồng nghiệp vụ quan trọng như authentication, itinerary và collaboration.
