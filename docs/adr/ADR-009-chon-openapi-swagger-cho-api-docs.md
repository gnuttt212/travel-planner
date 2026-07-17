# ADR-009: Chọn OpenAPI / Swagger cho tài liệu API

- Date: 2026-07-17
- Status: Accepted
- Authors: Development Team

## Context

Hệ thống backend sẽ cung cấp nhiều API cho frontend và các client khác. Cần có một cách để tài liệu hóa API rõ ràng, dễ duy trì và dễ kiểm tra bởi cả backend lẫn frontend.

## Decision

Chúng tôi quyết định sử dụng OpenAPI và Swagger UI để tạo tài liệu API cho hệ thống.

Lý do chọn:

- hỗ trợ tự động tạo tài liệu từ code;
- giúp frontend và backend hiểu rõ contract API;
- thuận tiện cho kiểm thử và tích hợp.

## Consequences

### Positive

- Tài liệu API luôn cập nhật gần với mã nguồn.
- Giảm lỗi do thiếu thông tin hoặc mismatch giữa frontend và backend.
- Dễ dàng chia sẻ API contract cho các thành viên trong nhóm.

### Negative

- Cần duy trì annotation và cấu hình phù hợp để tài liệu luôn đúng.
- Một số endpoint phức tạp có thể cần cấu hình thêm để hiển thị đúng.

## Alternatives considered

### 1. Tài liệu markdown thủ công

Dễ bắt đầu nhưng khó duy trì và dễ bị lỗi thời.

### 2. Không có tài liệu API

Tiết kiệm thời gian ban đầu, nhưng gây khó khăn cho phát triển và tích hợp lâu dài.

## Implementation Notes

Swagger UI sẽ được dùng để hiển thị tài liệu API cho các endpoint backend, giúp phát triển và kiểm thử thuận tiện hơn.
