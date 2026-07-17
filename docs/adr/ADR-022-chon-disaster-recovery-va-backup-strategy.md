# ADR-022: Chọn disaster recovery và backup strategy cho hệ thống

- Date: 2026-07-17
- Status: Accepted
- Authors: Development Team

## Context

Hệ thống lưu trữ dữ liệu quan trọng như thông tin người dùng, kế hoạch du lịch, ngân sách và collaboration data. Nếu xảy ra lỗi phần cứng, lỗi phần mềm, mất dữ liệu hoặc sự cố môi trường, cần có phương án phục hồi để giảm thiểu tổn thất.

## Decision

Chúng tôi quyết định áp dụng chiến lược backup và disaster recovery cơ bản cho dữ liệu chính và cấu hình vận hành.

Các nguyên tắc chính:

- dữ liệu quan trọng cần được backup định kỳ;
- backup phải có thể phục hồi được trong thời gian hợp lý;
- các cấu hình và secrets cần được lưu riêng và bảo mật;
- quy trình recovery phải được kiểm tra và ghi lại.

## Consequences

### Positive

- Giảm nguy cơ mất dữ liệu nghiêm trọng.
- Hỗ trợ hệ thống phục hồi nhanh trong trường hợp sự cố.
- Tăng độ tin cậy cho môi trường production.

### Negative

- Cần chi phí và công sức cho việc thiết lập và duy trì backup.
- Nếu không kiểm tra thường xuyên, quy trình recovery có thể không hoạt động đúng.

## Alternatives considered

### 1. Không có backup plan

Đơn giản hơn, nhưng rủi ro mất dữ liệu rất cao.

### 2. Backup thủ công không có quy trình chuẩn

Có thể hoạt động tạm thời, nhưng khó tin cậy và khó kiểm soát.

## Implementation Notes

Backup và recovery strategy sẽ được thiết kế phù hợp với loại dữ liệu và môi trường triển khai, ưu tiên dữ liệu nghiệp vụ và cấu hình quan trọng trước.
