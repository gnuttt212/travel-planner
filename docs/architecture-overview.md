# Architecture Overview

## Mục đích

Tài liệu này cung cấp một bản tổng hợp kiến trúc cấp cao cho dự án Travel Planner, dựa trên các quyết định đã ghi nhận trong thư mục ADR. Mục tiêu là giúp các thành viên hiểu được cấu trúc hệ thống, các thành phần chính, mối quan hệ giữa chúng và hướng phát triển lâu dài của dự án.

## 1. Tổng quan hệ thống

Travel Planner là một ứng dụng hỗ trợ lên kế hoạch du lịch thông minh, kết hợp frontend hiện đại, backend theo kiến trúc Modular Monolith và các dịch vụ bên ngoài như AI, thời tiết và bản đồ.

Hệ thống được thiết kế theo các nguyên tắc chính:

- dễ phát triển và bảo trì;
- có thể mở rộng theo thời gian;
- có mức độ bảo mật phù hợp cho môi trường production;
- hỗ trợ tích hợp AI và dịch vụ bên ngoài;
- có thể chuyển dần sang kiến trúc phân tán hơn khi cần.

## 2. Kiến trúc cấp cao

### Frontend

- React + Vite + TypeScript
- Giao diện gồm các màn hình chính như đăng nhập, onboarding, danh sách điểm đến và planner
- Tương tác với backend thông qua REST API và WebSocket

### Backend

- Spring Boot 3
- Kiến trúc Modular Monolith
- Các domain nghiệp vụ được tách rõ như user, itinerary, budget, collaboration, booking và interaction
- Mỗi domain có cấu trúc tầng logic rõ ràng: controller, service, repository, domain model, DTO

### Data layer

- PostgreSQL làm cơ sở dữ liệu chính cho dữ liệu nghiệp vụ
- pgvector dùng cho tìm kiếm ngữ nghĩa và gợi ý điểm đến
- Redis dùng cho cache và rate limiting
- Docker Compose dùng để chuẩn hóa môi trường phát triển

### External integrations

- Google Gemini cho AI generation và recommendation
- OpenWeatherMap cho dự báo thời tiết
- OpenRouteService cho tối ưu tuyến đường

## 3. Luồng nghiệp vụ chính

### Authentication

- Người dùng đăng nhập qua JWT
- Access token dùng cho request ngắn hạn
- Refresh token dùng để duy trì phiên làm việc an toàn hơn

### Trip planning

- Người dùng tạo hoặc chỉnh sửa kế hoạch du lịch
- Backend xử lý dữ liệu itinerary, budget và collaboration
- Các dịch vụ AI và mapping có thể được kích hoạt để tạo gợi ý hoặc tối ưu tuyến đường

### Collaboration

- Nhiều người dùng có thể cùng làm việc trên một kế hoạch
- WebSocket dùng để đồng bộ trạng thái thời gian thực
- Event-driven pattern có thể được dùng cho các luồng phản ứng và notification

### Export

- Kế hoạch có thể được xuất sang PDF hoặc ICS
- Điều này hỗ trợ chia sẻ, in ấn hoặc thêm vào lịch cá nhân

## 4. Các mối quan tâm xuyên suốt

### Security

- Spring Security cho xác thực và phân quyền
- JWT và refresh token để quản lý phiên
- Rate limiting và secrets management để bảo vệ hệ thống

### Reliability

- Logging, monitoring và observability được thiết kế từ đầu
- Backup và disaster recovery được xem là yêu cầu cần có cho môi trường production
- CI/CD giúp tự động hóa build, test và deployment

### Maintainability

- ADR dùng để ghi lại quyết định kiến trúc
- Cấu trúc domain-driven giúp mã nguồn rõ ràng và dễ mở rộng
- Feature flags hỗ trợ release kiểm soát và rollout từng bước

## 5. Hướng phát triển trong tương lai

Hệ thống hiện tại được thiết kế để bắt đầu theo hướng Modular Monolith, nhưng có thể evolve theo lộ trình sau:

1. Giữ Modular Monolith cho giai đoạn đầu
2. Dùng API Gateway khi hệ thống có nhiều service hơn
3. Tách dần một số domain thành service riêng nếu cần scale độc lập
4. Mở rộng sang multi-tenancy khi phục vụ doanh nghiệp hoặc khách hàng B2B

## 6. Kết luận

Kiến trúc hiện tại của Travel Planner tập trung vào sự cân bằng giữa tốc độ phát triển, tính bảo trì, bảo mật và khả năng mở rộng. Đây là một kiến trúc phù hợp cho MVP và nền tảng sản phẩm trong giai đoạn đầu, đồng thời có thể phát triển thành một hệ thống lớn hơn khi nhu cầu tăng lên.

## Tài liệu liên quan

- [System Context Diagram](system-context-diagram.md)
- [C4 Model](c4-model.md)
- [ADR Index](adr/README.md)
