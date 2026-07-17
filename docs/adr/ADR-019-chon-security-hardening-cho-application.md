# ADR-019: Chọn security hardening cho ứng dụng

- Date: 2026-07-17
- Status: Accepted
- Authors: Development Team

## Context

Một ứng dụng web hiện đại cần có các biện pháp bảo vệ trước các mối đe dọa phổ biến như truy cập trái phép, tấn công brute force, lộ thông tin và các lỗi bảo mật phổ biến trong xử lý input.

## Decision

Chúng tôi quyết định áp dụng các biện pháp security hardening cơ bản cho ứng dụng, bao gồm:

- xác thực và phân quyền chuẩn cho API;
- kiểm soát đầu vào và validation;
- giới hạn rate limiting cho endpoint nhạy cảm;
- bảo vệ thông tin nhạy cảm và secrets;
- cấu hình bảo mật hợp lý cho Spring Security và các dependency liên quan.

## Consequences

### Positive

- Tăng mức độ bảo mật tổng thể của hệ thống.
- Giảm nguy cơ bị khai thác các lỗ hổng phổ biến.
- Tạo nền tảng tốt cho việc triển khai production an toàn.

### Negative

- Cần đầu tư thêm thời gian trong phát triển và kiểm thử bảo mật.
- Một số quy tắc bảo mật có thể làm chậm quá trình phát triển nếu không được cân nhắc hợp lý.

## Alternatives considered

### 1. Không áp dụng security hardening bắt buộc

Đơn giản hơn, nhưng làm tăng rủi ro bảo mật đáng kể.

### 2. Áp dụng bảo mật quá mức ngay từ đầu

Có thể gây phức tạp và làm chậm phát triển trong giai đoạn đầu.

## Implementation Notes

Các biện pháp bảo mật sẽ được áp dụng theo mức độ phù hợp với từng giai đoạn phát triển, ưu tiên các rủi ro cao trước khi mở rộng hệ thống.
