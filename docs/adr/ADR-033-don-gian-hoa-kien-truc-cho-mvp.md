# ADR-033: Đơn giản hóa kiến trúc và thay đổi Core Engine cho Phase 1 MVP

- Date: 2026-07-25
- Status: Accepted
- Authors: Development Team

## Context

Trong quá trình phát triển ban đầu, hệ thống Travel Planner được thiết kế với tham vọng rất lớn: tích hợp AI (Google Gemini) để sinh lịch trình thông qua kỹ thuật RAG, sử dụng Vector Database (pgvector) để tìm kiếm ngữ nghĩa, và hỗ trợ cộng tác thời gian thực (WebSocket).

Tuy nhiên, đối với mục tiêu của Phase 1 (Minimum Viable Product - MVP) hướng tới một sản phẩm Startup Portfolio trong thời gian ngắn, kiến trúc trên bộc lộ nhiều điểm yếu:
1. Độ trễ cao và thiếu ổn định khi phụ thuộc vào dịch vụ LLM của bên thứ ba (Black-box AI). Hệ thống khó can thiệp sâu vào thuật toán xếp lịch và điều chỉnh dữ liệu đầu ra.
2. Chi phí duy trì hạ tầng (Vector DB, WebSocket) cao và phân tán tài nguyên phát triển thay vì tập trung vào trải nghiệm người dùng lõi.
3. Số lượng module phân tán (`auth`, `user`, `booking`, `collaboration`, `interaction`, v.v.) làm phức tạp hóa quá trình phát triển.

## Decision

Chúng tôi quyết định thay đổi hoàn toàn kiến trúc lõi cho Phase 1 MVP:

1. **Thay thế AI bằng Custom Scoring Engine**: Hủy bỏ việc gọi API Gemini và pgvector. Chuyển sang tự xây dựng `ScoringEngine` dựa trên thuật toán chấm điểm đa biến (Rating, Khoảng cách, Giờ mở cửa, Sở thích, Ngân sách) và `SlotAllocator` để tự động sắp xếp lịch trình.
2. **Tinh gọn Module (Modular Monolith)**: Giảm từ hàng loạt module phức tạp xuống chỉ còn 3 module cốt lõi:
   - `identity`: User, Auth, Preferences.
   - `planning`: Trip, TripActivity, TravelContext.
   - `recommendation`: Trái tim thuật toán (Scoring, Allocation).
3. **Thay thế UI/UX**: Chuyển sang mô hình *Context Cards* (hỏi từng bước) thay vì form nhập liệu tĩnh để nâng cao trải nghiệm người dùng.
4. **Vô hiệu hóa các tính năng nâng cao (Tạm thời)**: Đình chỉ WebSocket collaboration, Booking, và hệ thống theo dõi Budget.

Quyết định này chính thức vô hiệu hóa (Supersedes) các quyết định trước đó đối với Phase 1: ADR-003, ADR-008, ADR-013, ADR-025, ADR-026, ADR-030.

## Consequences

### Positive

- **Full Control**: Kiểm soát 100% logic thuật toán lên lịch trình (Explainable AI), dễ dàng tinh chỉnh, gỡ lỗi.
- **Performance**: Việc chấm điểm diễn ra trực tiếp trên RAM và PostgreSQL truyền thống mang lại tốc độ phản hồi cực nhanh.
- **Maintainability**: Codebase giảm từ hàng chục domain phức tạp xuống kiến trúc tối giản, phù hợp cho quy mô nhóm nhỏ và bảo trì dài hạn.

### Negative

- Đánh đổi tính năng xử lý ngôn ngữ tự nhiên (NLP). Người dùng không thể gõ một câu mô tả dài mà phải tương tác qua UI cố định.
- Thuật toán yêu cầu Database phải được làm sạch và chuẩn hóa cao độ (ví dụ: tọa độ chính xác, giờ mở cửa JSON chuẩn).

## Alternatives considered

### 1. Giữ nguyên Gemini AI (RAG)
Mặc dù thông minh, nhưng AI thỉnh thoảng sinh ra lỗi JSON (hallucination) khiến Frontend bị crash. Hơn nữa, việc xếp lịch trình yêu cầu tính logic tuyến tính rất cao (thời gian di chuyển, giờ mở cửa), điều mà LLM hiện tại chưa xử lý hoàn hảo bằng các thuật toán lập trình truyền thống.

### 2. Tách thành Microservices
Tách `recommendation` ra một service Python riêng để tiện làm Machine Learning. Tuy nhiên, điều này phá vỡ ADR-002 (Modular Monolith) và tăng gấp đôi công sức DevOps, không phù hợp cho Phase 1.

## Implementation Notes

- Xóa toàn bộ code cũ liên quan đến `collaboration`, `booking`, `interaction`, `itinerary`.
- Chuyển `application.yml` sang sử dụng DB chuẩn (PostgreSQL không cần pgvector extension).
- Xây dựng `DataSeeder` mạnh mẽ để đảm bảo luôn có khoảng 50+ địa điểm thực tế tại TP.Hồ Chí Minh kèm dữ liệu chuẩn để thuật toán có thể hoạt động hiệu quả khi review.
