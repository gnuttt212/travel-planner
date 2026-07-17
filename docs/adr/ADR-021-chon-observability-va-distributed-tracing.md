# ADR-021: Chọn observability và distributed tracing cho hệ thống

- Date: 2026-07-17
- Status: Accepted
- Authors: Development Team

## Context

Khi hệ thống có nhiều thành phần và dịch vụ phụ trợ, việc debug lỗi và hiểu luồng xử lý trở nên khó khăn hơn. Đặc biệt với các tính năng như collaboration real-time, AI integration và API gọi bên ngoài, cần một cách theo dõi rõ ràng các request qua nhiều tầng.

## Decision

Chúng tôi quyết định áp dụng observability cơ bản bao gồm:

- logging có cấu trúc;
- metrics và alerting;
- distributed tracing cho các request đi qua nhiều lớp và service.

Mục tiêu là có thể nhanh chóng xác định được request bị lỗi ở đâu, thời gian xử lý như thế nào và có vấn đề gì ở tầng nào.

## Consequences

### Positive

- Giúp debug và phân tích lỗi hiệu quả hơn.
- Hỗ trợ phát hiện bottleneck và tối ưu hiệu năng.
- Tăng khả năng vận hành hệ thống ở môi trường production.

### Negative

- Cần thêm công cụ và cấu hình cho observability.
- Có thể tạo thêm overhead nếu không thiết kế hợp lý.

## Alternatives considered

### 1. Chỉ dùng logging thông thường

Đơn giản hơn, nhưng không đủ cho việc theo dõi request xuyên nhiều tầng.

### 2. Không dùng observability

Dễ triển khai ban đầu, nhưng gây khó khăn trong vận hành sau này.

## Implementation Notes

Observability sẽ được triển khai từ giai đoạn đầu ở mức cơ bản và có thể mở rộng thành một hệ thống giám sát tập trung khi dự án phát triển lớn hơn.
