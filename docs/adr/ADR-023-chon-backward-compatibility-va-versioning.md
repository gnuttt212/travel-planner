# ADR-023: Chọn backward compatibility và versioning cho API và contract

- Date: 2026-07-17
- Status: Accepted
- Authors: Development Team

## Context

Khi hệ thống phát triển, API và contract giữa frontend, backend và các service phụ trợ có thể thay đổi. Nếu thay đổi không được kiểm soát, các client cũ có thể bị ảnh hưởng và gây gián đoạn trải nghiệm người dùng.

## Decision

Chúng tôi quyết định áp dụng nguyên tắc backward compatibility cho các thay đổi API quan trọng và thiết lập versioning rõ ràng khi cần.

Các nguyên tắc chính:

- không xóa hoặc thay đổi đột ngột các field hoặc endpoint đang được dùng;
- khi cần thay đổi lớn, dùng version mới cho API;
- tài liệu và changelog cần được cập nhật kèm theo thay đổi.

## Consequences

### Positive

- Giảm rủi ro phá vỡ client cũ.
- Dễ dàng triển khai thay đổi từng bước.
- Hỗ trợ bảo trì và mở rộng lâu dài.

### Negative

- Cần quản lý API version và contract rõ ràng.
- Có thể làm tăng số lượng endpoint hoặc logic cần duy trì.

## Alternatives considered

### 1. Thay đổi API trực tiếp mà không quan tâm tới client cũ

Nhanh hơn, nhưng dễ gây lỗi và gián đoạn.

### 2. Không có versioning

Dễ bị hỗn loạn khi hệ thống phát triển lớn.

## Implementation Notes

Versioning sẽ được áp dụng cho các thay đổi API có ảnh hưởng lớn, trong khi các thay đổi nhỏ sẽ ưu tiên giữ compatibility nếu có thể.
