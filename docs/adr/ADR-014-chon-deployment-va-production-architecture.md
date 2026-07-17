# ADR-014: Chọn kiến trúc deployment và production phù hợp cho hệ thống

- Date: 2026-07-17
- Status: Accepted
- Authors: Development Team

## Context

Sau khi phát triển và kiểm thử local, hệ thống cần có một cách triển khai ổn định trên môi trường production. Yêu cầu cần đáp ứng là dễ triển khai, có khả năng mở rộng, dễ giám sát và bảo mật tốt.

## Decision

Chúng tôi quyết định triển khai hệ thống trên nền tảng container-based deployment, ưu tiên Docker container kết hợp orchestration đơn giản hoặc dịch vụ cloud phù hợp với quy mô hiện tại.

Lý do chọn:

- container giúp môi trường chạy thống nhất giữa dev và production;
- dễ scale và nâng cấp dịch vụ;
- thuận tiện cho việc triển khai backend, database và dịch vụ phụ trợ.

## Consequences

### Positive

- Triển khai và rollback dễ dàng hơn.
- Môi trường production ổn định và nhất quán.
- Dễ mở rộng khi lưu lượng tăng.

### Negative

- Cần có quy trình quản lý container và cấu hình production.
- Tốn thêm thời gian cho vận hành và giám sát.

## Alternatives considered

### 1. Triển khai trực tiếp trên VM

Đơn giản ban đầu nhưng khó quản lý và mở rộng khi số lượng service tăng.

### 2. Triển khai hoàn toàn theo microservices ngay từ đầu

Mạnh về phân tách, nhưng quá nặng cho giai đoạn đầu.

## Implementation Notes

Production deployment sẽ ưu tiên chạy backend và các dịch vụ phụ trợ trong container, với cấu hình environment riêng và cơ chế health check để giám sát trạng thái hệ thống.
