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
| 🧑‍🤝‍🧑 **Mạng xã hội & hợp tác** | Quản lý bạn bè, yêu cầu kết bạn, profile cá nhân, chat nhanh, bình luận và reaction. |
| 🧭 **Accessible search UI** | City combobox + transport selector được xây dựng với ARIA, keyboard navigation và behavior rõ ràng. |
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
- **Node.js** 20+ và **npm**
- **Docker** & Docker Compose
- **PostgreSQL** và **Redis** (có thể chạy qua `docker-compose.yml`)

### 1. Khởi động hạ tầng Database

Chạy Docker Compose để tạo PostgreSQL và Redis:

```bash
docker-compose up -d
```

### 1a. Chạy production với Docker Compose

Bạn có thể dùng tệp `.env` để quản lý các biến môi trường production. Sao chép `env.example` thành `.env` và cập nhật giá trị phù hợp.

```bash
cp env.example .env
docker-compose up -d
```

Nếu bạn muốn chạy backend cùng với PostgreSQL và Redis trong container, sử dụng tệp `docker-compose.override.yml` đã bổ sung sẵn và chạy:

```bash
docker-compose --env-file .env up -d
```

Backend container sẽ khởi động với profile production nếu `SPRING_PROFILES_ACTIVE=prod` được đặt trong `.env`.

> Lưu ý: `docker-compose.override.yml` mount toàn bộ thư mục repo vào `/app` và chạy `./mvnw spring-boot:run -Dspring-boot.run.profiles=prod`.

### 2. Thiết lập biến môi trường

Frontend và backend sử dụng các biến môi trường sau:

- `DB_URL` - JDBC URL cho PostgreSQL
- `DB_USERNAME` - tên user PostgreSQL
- `DB_PASSWORD` - mật khẩu PostgreSQL
- `JWT_SECRET` - secret cho JWT
- `ORS_API_KEY` - OpenRouteService API key (Phase 2)
- `OPENWEATHERMAP_API_KEY` - OpenWeatherMap API key (Phase 2)
- `GEMINI_API_KEY` - Gemini API key (Phase 3)
- `APP_ADMIN_EMAIL`, `APP_ADMIN_PASSWORD` - tài khoản admin seed mặc định

Lưu ý cho production:

- Production phải chạy với profile `prod` và không dùng `spring.jpa.hibernate.ddl-auto=update`.
- Backend đã được cấu hình để dùng Flyway migration trong production và `ddl-auto: validate`.
- `JWT_SECRET`, `ORS_API_KEY`, và `OPENWEATHERMAP_API_KEY` phải được cung cấp qua environment variables, không dùng giá trị mặc định.
- `application-prod.yml` sẽ được kích hoạt khi `spring.profiles.active=prod`.
- Nếu dùng Docker Compose, tạo tệp `.env` từ `env.example` và chạy `docker-compose up -d`.

Mặc định cấu hình đã sử dụng:

```yaml
spring:
  datasource:
    url: ${DB_URL:jdbc:postgresql://localhost:5433/travel_planner}
    username: ${DB_USERNAME:postgres}
    password: ${DB_PASSWORD:postgres}
jwt:
  secret: ${JWT_SECRET}
ors:
  api:
    key: ${ORS_API_KEY:}
openweathermap:
  api:
    key: ${OPENWEATHERMAP_API_KEY:}
```

### 3. Chuẩn bị backend

Backend có thể chạy trực tiếp bằng Maven Wrapper:

```bash
./mvnw.cmd spring-boot:run
```

Hoặc chạy production với profile `prod`:

```powershell
.\scripts\run-prod.ps1
```

API backend mặc định có sẵn tại:

`http://localhost:8080/api/v1`

> Lưu ý: cấu hình CORS đã cho phép các cổng `3000`, `3001`, và `5173`.

### 4. Chuẩn bị frontend

Trong thư mục `frontend`:

```bash
npm install
npm run dev
```

Frontend mặc định chạy tại:

`http://localhost:3000`

### 5. Kiểm tra build

- Backend: `./mvnw.cmd -DskipTests package`
- Frontend: `npm run build`

### 6. Tài liệu và cấu trúc

- `docs/README.md` — hub tài liệu kiến trúc
- `docs/api-endpoints.md` — API endpoints
- `docs/location-transport.md` — hướng dẫn component LocationTransport

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

## 🧩 Components

- **LocationTransport** — accessible city search combobox + segmented transport control used in the planning flow. Xem hướng dẫn và cách sử dụng tại `docs/location-transport.md`.
- **Profile & Friends** — giao diện profile người dùng, yêu cầu kết bạn, danh sách bạn bè và chat tích hợp.
- **Trip comments & reactions** — tính năng bình luận và reaction cho mỗi chuyến đi.

---

## 📄 License
Dự án này được phát triển phục vụ mục đích xây dựng sản phẩm chất lượng cao (Startup Portfolio).
