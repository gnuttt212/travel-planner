# ADR-001: Áp dụng Architecture Decision Records để ghi lại các quyết định kiến trúc

- Date: 2026-07-17
- Status: Accepted
- Authors: Development Team

## Context

Trong quá trình phát triển hệ thống Travel Planner, đội ngũ sẽ phải đưa ra nhiều quyết định kỹ thuật liên quan đến cấu trúc hệ thống, công nghệ, cách tích hợp, bảo mật và triển khai. Những quyết định này có thể ảnh hưởng trực tiếp đến khả năng bảo trì, mở rộng, kiểm thử và vận hành hệ thống trong tương lai.

Nếu các quyết định này không được ghi lại rõ ràng, các thành viên trong nhóm có thể không hiểu được lý do tại sao một giải pháp được chọn, đặc biệt khi hệ thống phát triển qua nhiều vòng thay đổi. Điều này có thể dẫn đến:

- sự lặp lại sai lệch trong quá trình phát triển;
- khó khăn trong việc hiểu lại bối cảnh quyết định;
- tăng chi phí bảo trì và làm chậm quá trình onboarding cho thành viên mới.

Vì vậy, cần có một phương pháp chuẩn hóa để ghi lại ngữ cảnh, mục tiêu, lựa chọn đã được chấp thuận và các hệ quả đi kèm của từng quyết định kiến trúc.

## Decision

Chúng tôi quyết định áp dụng Architecture Decision Records (ADR) như một phương pháp chuẩn để ghi lại các quyết định kiến trúc của dự án.

Mỗi ADR sẽ được viết dưới dạng một tài liệu riêng, có cấu trúc gồm các phần chính:

- Title: tên quyết định;
- Date: ngày ra quyết định;
- Status: trạng thái của quyết định (Accepted, Deprecated, Superseded, ...);
- Context: bối cảnh và vấn đề cần giải quyết;
- Decision: quyết định đã được chọn;
- Consequences: hệ quả tích cực và tiêu cực;
- Alternatives considered: các lựa chọn khác đã được xem xét.

Quy trình áp dụng như sau:

1. Khi có quyết định kiến trúc quan trọng cần ghi nhận, một ADR mới sẽ được tạo.
2. ADR phải mô tả rõ ràng lý do lựa chọn, điều kiện áp dụng và các ràng buộc liên quan.
3. ADR cần được lưu trong thư mục dành riêng cho tài liệu kiến trúc của dự án.
4. Khi có quyết định mới thay thế quyết định cũ, ADR cũ sẽ được đánh dấu là Superseded hoặc Deprecated.

## Consequences

### Positive

- Ghi nhận và lưu trữ lại các quyết định kiến trúc một cách có hệ thống.
- Giúp các thành viên hiểu rõ nguyên nhân, mục tiêu và bối cảnh của từng quyết định.
- Hỗ trợ bảo trì, kiểm thử và mở rộng hệ thống lâu dài.
- Tăng tính minh bạch và dễ dàng onboarding cho thành viên mới.
- Giảm nguy cơ đưa ra quyết định không nhất quán trong tương lai.

### Negative

- Đòi hỏi đội ngũ dành thêm thời gian để viết và duy trì tài liệu ADR.
- Nếu không được quản lý đúng cách, các ADR có thể trở nên thiếu cập nhật hoặc bị phân tán.

## Alternatives considered

### 1. Không ghi lại quyết định kiến trúc

Lựa chọn này đơn giản hơn về mặt vận hành, nhưng làm mất đi ngữ cảnh và lý do đằng sau các quyết định kỹ thuật. Điều này sẽ gây khó khăn cho việc bảo trì và hiểu hệ thống sau này.

### 2. Ghi chú bằng tài liệu tự do không có cấu trúc chuẩn

Phương pháp này có thể hoạt động ban đầu, nhưng dễ dẫn đến việc thông tin bị thiếu đồng nhất, khó tra cứu và khó duy trì theo thời gian.

## Implementation Notes

ADR sẽ được sử dụng như một phần của quy trình phát triển dự án, đặc biệt khi có thay đổi về công nghệ, cấu trúc hệ thống, giao diện, tích hợp dịch vụ hoặc quy trình triển khai.

Mục tiêu của ADR là không chỉ ghi lại “đã làm gì”, mà còn ghi lại “vì sao lại làm như vậy”.
