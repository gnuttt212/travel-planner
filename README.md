# 🌍 Travel Planner

> Ứng dụng lên kế hoạch du lịch thông minh — được xây dựng trên kiến trúc Modular Monolith với Spring Boot 4 và React 19.

Travel Planner giúp người dùng tạo lịch trình du lịch cá nhân hóa dựa trên thuật toán chấm điểm đa biến (Rating, Distance, Hours, Preference, Budget) và giao diện trực quan, hiện đại.

---

## ✨ Tính năng chính MVP (Phase 1)

| Tính năng | Mô tả |
|---|---|
| 🗺️ **Context Cards Flow** | Thu thập nhu cầu của người dùng qua 5 bước (Mục đích, Thời gian, Nhóm, Ngân sách, Vị trí) thay vì form truyền thống. |
| 🧠 **Scoring Engine** | Thuật toán đánh giá địa điểm dựa trên 5 trọng số: Rating (Bayesian), Distance (Haversine), Opening Hours, User Preference, và Budget. |
| 📅 **Slot Allocator** | Tự động sắp xếp lịch trình thông minh dựa trên thời gian di chuyển, thời gian ước tính tại điểm đến. |
| 📍 **Bản đồ tương tác** | Tích hợp Leaflet hiển thị các điểm đến và vẽ tuyến đường trực quan. |
| ⏱️ **Timeline View** | Hiển thị lịch trình dạng trục thời gian dọc (Vertical Timeline) đầy đủ thông tin chi phí và thời gian. |
| 🎨 **Premium UI/UX** | Giao diện Dark Theme, Glassmorphism và hiệu ứng chuyển động mượt mà (GSAP). |
| 🔐 **Xác thực an toàn** | Sử dụng JWT (JSON Web Token) kết hợp BCrypt. |

---

## 🏗️ Kiến trúc hệ thống

Hệ thống được thiết kế theo kiến trúc **Modular Monolith** cực kỳ tinh gọn và dễ mở rộng, bao gồm 3 module độc lập:

1. **`identity`**: Quản lý thông tin người dùng, xác thực (Authentication), và sở thích du lịch (Preferences).
2. **`planning`**: Quản lý các Domain Entity chính như `Trip`, `TripActivity` và `TravelContext`.
3. **`recommendation`**: Trái tim của hệ thống chứa `ScoringEngine`, `SlotAllocator`, và logic lập kế hoạch tự động (`PlanBuilder`).

---

## 🚀 Hướng dẫn cài đặt & chạy

### Yêu cầu
- **Java** 22+
- **Node.js** 20+ & npm
- **Docker** & Docker Compose

### 1. Khởi động hạ tầng Database

Hệ thống sử dụng PostgreSQL và Redis:

```bash
docker-compose up -d
```

### 2. Cấu hình API Keys (Chuẩn bị cho Phase 2)

Copy file `src/main/resources/application.yml` và thay đổi các cấu hình nếu cần (mặc định đã chạy được cho Phase 1 nhờ dữ liệu mock/seed).

```yaml
# Cấu hình JWT an toàn cho production
jwt.secret: "your-strong-secret-key-here"
```

### 3. Khởi chạy Backend

Backend sẽ tự động seed (nạp) 52 địa điểm thực tế tại TP.HCM vào cơ sở dữ liệu nếu bảng `destinations` chưa có dữ liệu.

```bash
./mvnw spring-boot:run
```
- API chạy tại: `http://localhost:8080/api/v1`

### 4. Khởi chạy Frontend

```bash
cd frontend
npm install
npm run dev
```
- Ứng dụng chạy tại: `http://localhost:3000` (hoặc `3001` nếu cổng 3000 bị bận).

> **Lưu ý CORS:** Backend đã được cấu hình mặc định cho phép các cổng `3000`, `3001`, và `5173`.

---

## 🛠️ Công nghệ sử dụng

### Backend
- **Spring Boot 4.1.0**
- **Spring Security 6.x** + JWT (JJWT)
- **Spring Data JPA** + Hibernate
- **PostgreSQL 16**
- **Redis 7** + Bucket4j (Rate Limiting)
- **Lombok**

### Frontend
- **React 19.2** + TypeScript
- **Vite 8.1**
- **React Router 7.18**
- **Axios** (với JWT interceptors)
- **Leaflet & React-Leaflet**
- **GSAP** (Hiệu ứng animation mượt mà)
- **CSS Custom Properties** (Thiết kế Dark Theme / Glassmorphism)

---

## 🗺️ Lộ trình (Roadmap)

- [x] **Phase 1 (MVP)**: Hoàn tất kiến trúc Modular Monolith, Context Cards UI, Scoring Engine tự động, Timeline View, và hệ thống seed data.
- [ ] **Phase 2**: Tích hợp OpenRouteService API (tính toán khoảng cách chính xác thay vì đường chim bay) và OpenWeatherMap API (điều chỉnh lịch trình theo thời tiết).
- [ ] **Phase 3**: Thêm tính năng kéo thả (Drag & Drop) vào Timeline để điều chỉnh lịch trình bằng tay. 
- [ ] **Phase 4**: Xuất PDF / ICS và chia sẻ link lịch trình.

---

## 📄 License
Dự án này được phát triển phục vụ mục đích xây dựng sản phẩm chất lượng cao (Startup Portfolio).
