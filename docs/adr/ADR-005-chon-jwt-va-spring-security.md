# ADR-005: Chọn JWT kết hợp Spring Security cho xác thực và phân quyền

- Date: 2026-07-17
- Status: Accepted
- Authors: Development Team

## Context

Hệ thống cần có cơ chế xác thực người dùng để bảo vệ các API và giới hạn quyền truy cập vào dữ liệu cá nhân. Trong giai đoạn đầu, cần một giải pháp dễ tích hợp, đủ an toàn và phù hợp với kiến trúc hiện tại.

Yêu cầu quan trọng là hệ thống phải hỗ trợ xác thực stateless để dễ triển khai trên nhiều môi trường và thuận tiện cho việc mở rộng về sau.

## Decision

Chúng tôi quyết định sử dụng JWT (JSON Web Token) kết hợp với Spring Security để thực hiện xác thực và phân quyền cho API.

Lý do lựa chọn:

- JWT phù hợp cho mô hình API stateless.
- Spring Security cung cấp các cơ chế bảo mật chuẩn và dễ tích hợp với Spring Boot.
- Hệ thống có thể mở rộng sang nhiều loại client khác nhau như web, mobile hoặc third-party service.

## Consequences

### Positive

- Bảo mật tốt cho API và dữ liệu người dùng.
- Dễ tích hợp với frontend và các môi trường deploy khác nhau.
- Giảm phụ thuộc vào session lưu trên server.

### Negative

- Cần quản lý chặt chẽ thời gian sống của token và cơ chế refresh token.
- Nếu triển khai sai, hệ thống có thể gặp rủi ro về token leakage hoặc unauthorized access.

## Alternatives considered

### 1. Session-based authentication

Phù hợp với ứng dụng web truyền thống, nhưng kém linh hoạt hơn cho API và các client khác nhau.

### 2. OAuth2 chỉ dùng thư viện bên ngoài

Mạnh mẽ nhưng phức tạp hơn so với nhu cầu hiện tại của dự án.

## Implementation Notes

JWT sẽ được dùng cho việc xác thực request đến API. Các endpoint nhạy cảm sẽ được bảo vệ bằng role và permission cơ bản trong Spring Security.
