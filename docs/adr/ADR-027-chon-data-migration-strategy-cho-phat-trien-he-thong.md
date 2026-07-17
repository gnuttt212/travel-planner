# ADR-027: Chọn data migration strategy cho phát triển và thay đổi schema

- Date: 2026-07-17
- Status: Accepted
- Authors: Development Team

## Context

Khi hệ thống phát triển, schema dữ liệu có thể thay đổi để hỗ trợ thêm tính năng mới hoặc cải tiến cấu trúc. Nếu không có chiến lược migration rõ ràng, việc thay đổi database có thể gây lỗi, mất dữ liệu hoặc làm gián đoạn hệ thống.

## Decision

Chúng tôi quyết định áp dụng migration-based approach cho thay đổi schema dữ liệu.

Các nguyên tắc chính:

- mỗi thay đổi schema sẽ được ghi lại dưới dạng migration riêng;
- migration phải có thể chạy lại một cách an toàn;
- thay đổi nên được áp dụng theo quy trình kiểm thử trước khi release;
- dữ liệu hiện có cần được chuyển đổi một cách có kiểm soát.

## Consequences

### Positive

- Giảm nguy cơ phá hỏng dữ liệu khi thay đổi schema.
- Dễ theo dõi và rollback thay đổi khi cần.
- Hỗ trợ phát triển liên tục và deployment an toàn.

### Negative

- Cần duy trì quy trình migration nghiêm ngặt.
- Nếu không quản lý tốt, số lượng migration có thể tăng nhanh.

## Alternatives considered

### 1. Thay đổi schema thủ công trực tiếp trên database

Nhanh hơn ban đầu, nhưng nguy hiểm và khó kiểm soát.

### 2. Không có migration strategy

Không phù hợp với hệ thống có dữ liệu quan trọng và phát triển liên tục.

## Implementation Notes

Migration sẽ được dùng cho cả thay đổi cấu trúc bảng và dữ liệu, đặc biệt khi hỗ trợ các tính năng mới như nâng cấp schema cho itinerary, budget hoặc collaboration.
