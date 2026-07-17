# ADR-032: Chọn workflow approval cho ADR và thay đổi kiến trúc

- Date: 2026-07-17
- Status: Accepted
- Authors: Development Team

## Context

Các quyết định kiến trúc có ảnh hưởng lớn đến hệ thống và đội ngũ phát triển. Nếu không có quy trình phê duyệt rõ ràng, thay đổi kiến trúc có thể diễn ra thiếu kiểm soát, gây xung đột và ảnh hưởng đến chất lượng hệ thống.

## Decision

Chúng tôi quyết định áp dụng một workflow approval cơ bản cho các ADR mới và các thay đổi kiến trúc quan trọng.

Quy trình đề xuất:

- đề xuất ADR mới hoặc thay đổi kiến trúc cần được ghi nhận rõ ràng;
- cần có trao đổi và review bởi các bên liên quan;
- quyết định cuối cùng sẽ được ghi nhận bằng trạng thái Accepted, Deprecated hoặc Superseded;
- thay đổi ảnh hưởng lớn cần có sự đồng thuận trước khi triển khai.

## Consequences

### Positive

- Tăng tính minh bạch và kiểm soát cho thay đổi kiến trúc.
- Giảm rủi ro quyết định sai hoặc không đồng bộ.
- Hỗ trợ governance và hiểu rõ lịch sử thay đổi.

### Negative

- Cần thêm thời gian cho review và phê duyệt.
- Có thể làm chậm một số thay đổi nhỏ nếu quy trình quá nặng.

## Alternatives considered

### 1. Không có approval workflow

Nhanh hơn, nhưng tăng rủi ro và thiếu kiểm soát.

### 2. Workflow approval quá cứng và phức tạp

Đảm bảo chặt chẽ nhưng không phù hợp với quy mô và giai đoạn hiện tại.

## Implementation Notes

Workflow approval sẽ được áp dụng ở mức phù hợp với phạm vi dự án, ưu tiên các quyết định có ảnh hưởng lớn đến kiến trúc, vận hành hoặc bảo mật.
