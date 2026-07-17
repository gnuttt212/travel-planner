# ADR-004: Chọn React, Vite và TypeScript cho frontend

- Date: 2026-07-17
- Status: Accepted
- Authors: Development Team

## Context

Frontend của hệ thống cần có trải nghiệm phát triển tốt, cấu trúc rõ ràng, khả năng mở rộng và hỗ trợ xây dựng giao diện hiện đại cho các màn hình như đăng nhập, onboarding, danh sách điểm đến và planner.

Đội ngũ cần một stack frontend có tốc độ phát triển cao, ecosystem phong phú và phù hợp để tích hợp với REST API và WebSocket.

## Decision

Chúng tôi quyết định xây dựng giao diện bằng React kết hợp Vite và TypeScript.

Các lý do chính:

- React phù hợp cho việc xây dựng UI tương tác phức tạp.
- Vite mang lại thời gian phát triển nhanh và trải nghiệm dev tốt hơn so với các công cụ build truyền thống.
- TypeScript giúp tăng độ an toàn cho mã nguồn và dễ bảo trì hơn khi hệ thống mở rộng.

## Consequences

### Positive

- Tăng năng suất phát triển cho frontend.
- Cải thiện chất lượng mã nguồn thông qua type checking.
- Dễ tích hợp với các thư viện UI và công cụ hiện đại.

### Negative

- Cần có mức độ quen thuộc với TypeScript và React đối với các thành viên mới.
- Một số cấu hình ban đầu có thể phức tạp hơn so với việc dùng JavaScript thuần.

## Alternatives considered

### 1. Vue.js

Có ưu điểm về cú pháp đơn giản nhưng ecosystem và độ quen thuộc của đội ngũ hiện tại không cao bằng React.

### 2. Plain JavaScript

Đơn giản hơn, nhưng khó bảo trì và mở rộng khi giao diện trở nên phức tạp.

## Implementation Notes

Frontend sẽ dùng React Router cho điều hướng, Axios cho gọi API, và các thư viện hỗ trợ UI như Leaflet cho bản đồ và DnD Kit cho trải nghiệm sắp xếp lịch trình.
