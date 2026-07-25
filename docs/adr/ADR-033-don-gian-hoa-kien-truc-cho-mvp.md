# ADR 033: Đơn giản hóa kiến trúc và thay đổi Core Engine cho Phase 1 MVP

## Trạng thái

**Được chấp thuận (Accepted)**

## Bối cảnh

Trong quá trình phát triển ban đầu, hệ thống Travel Planner được thiết kế với rất nhiều tính năng phức tạp như: Sinh lịch trình bằng AI RAG (Google Gemini), tìm kiếm Vector Search (pgvector), cộng tác thời gian thực (WebSocket), và nhiều module phân tán (booking, budget, collaboration, interaction).

Tuy nhiên, đối với mục tiêu của Phase 1 (Minimum Viable Product - MVP) phục vụ cho Startup Portfolio trong thời gian ngắn (3-4 tháng), sự phức tạp này mang lại nhiều rủi ro:
1. **Khó kiểm soát Output của LLM (Gemini)**: Việc sinh lịch trình bằng Prompt RAG thường khó đảm bảo định dạng JSON chuẩn xác 100% và khó tùy chỉnh sâu vào từng thuật toán sắp xếp của hệ thống.
2. **Chi phí và Vận hành hạ tầng**: Việc duy trì Vector DB (pgvector), tạo Embedding liên tục và duy trì WebSocket làm tăng chi phí và sự phức tạp của quá trình triển khai.
3. **Phân tán nguồn lực**: Việc phát triển các tính năng như Booking, Chat, hay Budget tracking làm lu mờ giá trị cốt lõi (Core Value) của ứng dụng là "Gợi ý và sắp xếp lịch trình du lịch thông minh".

Do đó, chúng ta cần một đợt tái cấu trúc (Refactor) toàn diện để tinh gọn hệ thống.

## Quyết định

Chúng tôi quyết định thay đổi hoàn toàn kiến trúc của Phase 1 MVP như sau:

1. **Thay thế AI bằng Custom Scoring Engine**: 
   - Hủy bỏ việc sử dụng Google Gemini và RAG để sinh lịch trình.
   - Hủy bỏ việc dùng `pgvector` và `sentence-transformers`.
   - Thiết lập một thuật toán nội bộ (`ScoringEngine`) dựa trên 5 trọng số: Rating (Bayesian), Khoảng cách (Haversine), Thời gian mở cửa, Sở thích cá nhân và Ngân sách. Cùng với đó là `SlotAllocator` để tự động ghép các điểm đến vào các khung giờ phù hợp.
2. **Giảm thiểu số lượng Module**: 
   - Xóa bỏ toàn bộ các module: `auth`, `user`, `booking`, `collaboration`, `interaction`, `budget`.
   - Gộp lại thành 3 module cốt lõi: 
     - `identity` (User & Auth)
     - `planning` (Trip & Context)
     - `recommendation` (Scoring & Allocation)
3. **Tạm ngưng các tính năng thời gian thực**: Xóa bỏ cấu hình WebSocket/STOMP. Tính năng cộng tác sẽ được xem xét lại ở các Phase sau.
4. **Cải tiến UI/UX**: Chuyển sang mô hình nhập liệu dạng *Context Cards* từng bước để tăng cường trải nghiệm người dùng, thay vì dùng một form nhập liệu dài.

## Hệ quả

**Tích cực:**
- **Full Control**: Chúng ta kiểm soát 100% logic thuật toán lên lịch trình thay vì phụ thuộc vào một hộp đen (black-box) LLM. Dễ dàng tinh chỉnh và giải thích (Explainable AI).
- **Tốc độ (Performance)**: Việc chấm điểm trên RAM và query Database truyền thống nhanh hơn và ổn định hơn rất nhiều so với việc gọi API LLM.
- **Dễ bảo trì**: Codebase giảm từ hàng chục class phức tạp xuống chỉ còn các Domain và Engine cơ bản, dễ dàng cho một nhóm nhỏ (hoặc sinh viên) vận hành và trình bày (Pitching).
- **Trải nghiệm người dùng tốt hơn**: Flow Context Cards mang lại cảm giác của một sản phẩm cao cấp.

**Tiêu cực:**
- Bỏ đi tính năng NLP (Nhập yêu cầu bằng ngôn ngữ tự nhiên) ban đầu. Người dùng phải chọn qua các option có sẵn.
- Thuật toán `ScoringEngine` yêu cầu dữ liệu điểm đến (Destinations) phải cực kỳ chuẩn xác và đầy đủ (Tọa độ, Opening Hours, Cost) mới có thể hoạt động tốt.

## Các ADR bị ảnh hưởng (Superseded/Deprecated)

Quyết định này chính thức **Vô hiệu hóa (Supersedes/Deprecates)** các quyết định sau đối với Phase 1:
- **ADR-003**: Chọn PostgreSQL và pgvector (Bỏ pgvector).
- **ADR-008**: Chọn WebSocket cho collaboration (Bỏ WebSocket).
- **ADR-013**: Chọn AI Integration Strategy với Gemini (Bỏ Gemini).
- Cùng với đó, đình chỉ vô thời hạn các ADR liên quan đến Enterprise/Microservices (ADR-025, ADR-026, ADR-030).
