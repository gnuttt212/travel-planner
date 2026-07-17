# ADR-030: Chọn multi-tenancy cho phân khúc doanh nghiệp và B2B

- Date: 2026-07-17
- Status: Accepted
- Authors: Development Team

## Context

Trong tương lai, hệ thống có thể được mở rộng sang các khách hàng doanh nghiệp, tổ chức hoặc môi trường có nhiều tenant riêng biệt. Khi đó, cần có khả năng tách dữ liệu, cấu hình và quyền truy cập giữa các tenant một cách an toàn và có thể mở rộng.

## Decision

Chúng tôi quyết định thiết kế hệ thống với khả năng hỗ trợ multi-tenancy ở mức phù hợp trong tương lai, ưu tiên mô hình shared application với dữ liệu được phân tách rõ ràng theo tenant.

Các nguyên tắc chính:

- ứng dụng dùng chung, dữ liệu được phân hoạch theo tenant;
- mỗi tenant có cấu hình và quyền riêng;
- bảo mật và isolation sẽ được xem là yêu cầu bắt buộc khi triển khai multi-tenancy.

## Consequences

### Positive

- Mở rộng tốt cho phân khúc doanh nghiệp và B2B.
- Giảm chi phí vận hành so với triển khai ứng dụng riêng cho mỗi khách hàng.
- Dễ quản lý nâng cấp và cập nhật chung.

### Negative

- Cần thiết kế cơ chế isolation tốt để tránh rò dữ liệu giữa tenant.
- Complexity tăng lên ở tầng dữ liệu, auth và cấu hình.

## Alternatives considered

### 1. Mỗi tenant có một instance riêng

Tách biệt tốt về dữ liệu và vận hành, nhưng chi phí cao và khó scale.

### 2. Không chuẩn bị multi-tenancy

Đơn giản ban đầu nhưng trở thành hạn chế khi mở rộng sang doanh nghiệp.

## Implementation Notes

Multi-tenancy sẽ không được triển khai ngay ở giai đoạn đầu nếu không cần thiết, nhưng kiến trúc sẽ được thiết kế để dễ bổ sung khi cần.
