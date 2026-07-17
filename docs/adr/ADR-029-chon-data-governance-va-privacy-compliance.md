# ADR-029: Chọn data governance và privacy compliance cho hệ thống

- Date: 2026-07-17
- Status: Accepted
- Authors: Development Team

## Context

Hệ thống lưu trữ dữ liệu cá nhân và dữ liệu liên quan đến sở thích du lịch, lịch trình và hành vi người dùng. Khi hệ thống phát triển và có thể phục vụ nhiều thị trường, cần có nguyên tắc quản lý dữ liệu rõ ràng để bảo vệ quyền riêng tư của người dùng và tuân thủ quy định chung về bảo mật dữ liệu.

## Decision

Chúng tôi quyết định áp dụng các nguyên tắc data governance và privacy compliance cơ bản cho hệ thống.

Các nguyên tắc chính:

- chỉ thu thập dữ liệu cần thiết cho chức năng hệ thống;
- bảo vệ dữ liệu người dùng bằng các biện pháp bảo mật phù hợp;
- lưu trữ và xử lý dữ liệu theo nguyên tắc tối thiểu hóa dữ liệu;
- có quy trình xóa, chỉnh sửa hoặc export dữ liệu người dùng khi cần;
- ghi nhận và kiểm soát việc xử lý dữ liệu nhạy cảm.

## Consequences

### Positive

- Tăng độ tin cậy và uy tín của hệ thống đối với người dùng.
- Giảm rủi ro vi phạm bảo mật và quy định pháp lý.
- Tạo nền tảng tốt cho việc mở rộng sang môi trường enterprise hoặc quốc tế.

### Negative

- Cần thêm quy trình và kiểm soát trong phát triển và vận hành.
- Một số tính năng có thể phải điều chỉnh để phù hợp với quy định bảo mật dữ liệu.

## Alternatives considered

### 1. Không quan tâm đến privacy compliance

Đơn giản hơn, nhưng có rủi ro lớn về bảo mật và pháp lý.

### 2. Áp dụng quy định quá chặt ngay từ đầu

Có thể làm chậm phát triển và tăng chi phí ban đầu.

## Implementation Notes

Privacy và governance sẽ được áp dụng theo mức phù hợp với giai đoạn hiện tại, ưu tiên các dữ liệu người dùng, token, lịch trình và thông tin cá nhân.
