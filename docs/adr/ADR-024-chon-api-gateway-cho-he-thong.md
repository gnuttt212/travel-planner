# ADR-024: Chọn API Gateway cho hệ thống

- Date: 2026-07-17
- Status: Accepted
- Authors: Development Team

## Context

Khi hệ thống phát triển và có nhiều service hoặc endpoint cần quản lý, việc điều hướng request, xác thực, rate limiting và bảo mật ở từng service riêng lẻ sẽ trở nên rắc rối. Cần một lớp trung gian tập trung để quản lý lưu lượng và giao tiếp với client.

## Decision

Chúng tôi quyết định sử dụng API Gateway làm lớp trung gian cho các request từ client đến backend.

API Gateway sẽ chịu trách nhiệm cho các chức năng chung như:

- routing request đến đúng service hoặc module;
- xác thực và phân quyền tập trung;
- rate limiting và bảo vệ tổng thể;
- logging và monitoring request;
- chuẩn hóa giao diện giữa client và backend.

## Consequences

### Positive

- Tăng tính nhất quán trong quản lý request và bảo mật.
- Giảm độ phức tạp khi hệ thống có nhiều service hơn.
- Dễ mở rộng cho các tính năng cross-cutting như auth, throttling và tracing.

### Negative

- API Gateway trở thành một thành phần trung tâm cần được vận hành cẩn thận.
- Nếu cấu hình sai, có thể gây điểm lỗi đơn lẻ cho toàn bộ hệ thống.

## Alternatives considered

### 1. Không dùng API Gateway

Đơn giản hơn ở giai đoạn đầu, nhưng không phù hợp khi hệ thống lớn dần.

### 2. Dùng API Gateway ngay từ đầu với cấu hình quá phức tạp

Có thể làm tăng độ phức tạp không cần thiết cho dự án ban đầu.

## Implementation Notes

Trong giai đoạn đầu, API Gateway có thể được triển khai ở mức tối giản hoặc dùng như lớp trung gian logic trong môi trường phát triển. Khi hệ thống lớn hơn, nó sẽ trở thành thành phần bắt buộc cho việc quản lý traffic và bảo mật tập trung.
