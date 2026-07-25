# 🗺️ ROADMAP — Travel Planner

> Tài liệu duy nhất mô tả lộ trình phát triển sản phẩm. Mọi quyết định kiến trúc
> được ghi nhận tại [ADR Log](docs/adr/README.md). Cập nhật lần cuối: 2026-07-25.

---

## Phase 1 — MVP: Rule-Based Recommendation ✅ DONE

**Mục tiêu:** Xây dựng sản phẩm MVP hoạt động end-to-end với dữ liệu mock, sẵn sàng demo.

| Việc cụ thể | Trạng thái |
|---|---|
| Kiến trúc Modular Monolith 3 module (`identity`, `planning`, `recommendation`) | ✅ |
| ScoringEngine (5 trọng số: Rating/Distance/Hours/Preference/Budget) | ✅ |
| SlotAllocator (ghép lịch trình theo khung giờ, tính travel time bằng Haversine) | ✅ |
| PlanBuilder (sinh 3 phương án: Balanced, Foodie, Saver) | ✅ |
| Frontend: Context Cards 5 bước (GSAP animation) | ✅ |
| Frontend: Results page (Leaflet map + plan variants) | ✅ |
| Frontend: Timeline View + Trip Detail | ✅ |
| Frontend: MyTrips (danh sách chuyến đi) | ✅ |
| Dark Theme / Glassmorphism CSS | ✅ |
| JWT Authentication + BCrypt | ✅ |
| Seed data: 52 địa điểm TP.HCM (mock, nhập tay — không từ API thật) | ✅ |
| Rate Limiting (Bucket4j, 5 req/min) | ✅ |

**Phụ thuộc:** Không cần API key bên ngoài. Chỉ cần Docker (PostgreSQL + Redis).

**Lưu ý quan trọng về data:**
- Dữ liệu 52 địa điểm trong `seed_destinations.json` là **mock nhập tay**. Tọa độ lấy từ Google Maps nhưng
  các chỉ số `avgRating`, `reviewCount`, `avgCostPerPerson` là **giả lập** để thuật toán hoạt động.
- Khoảng cách giữa các điểm đến được tính bằng **đường chim bay (Haversine)**, không phải đường đi thực tế.
- **Không có AI/LLM** nào được tích hợp ở Phase này. Toàn bộ logic gợi ý và sắp xếp lịch trình
  do `ScoringEngine` + `SlotAllocator` (rule-based, Java thuần) thực hiện.

---

## Phase 2 — Deploy Public + Tích hợp Routing & Thời tiết ✅ DONE

**Mục tiêu:** Đưa ứng dụng lên môi trường public để test được thật, thay Haversine bằng khoảng cách đường đi thật.

| Việc cụ thể | Ước tính |
|---|---|
| Deploy backend lên VPS/Cloud (Railway, Render, hoặc EC2) | 2 ngày |
| Deploy frontend lên Vercel/Netlify (static hosting) | 1 ngày |
| Cấu hình CI/CD cơ bản (GitHub Actions: build → deploy) | 1 ngày |
| Tích hợp **OpenRouteService API** vào `SlotAllocator` — thay Haversine bằng driving/cycling distance thật | 2 ngày |
| Tích hợp **OpenWeatherMap API** — nếu trời mưa, ưu tiên địa điểm `indoor = true` | 2 ngày |
| Viết integration test cho ORS + OWM service | 1 ngày |

**Phụ thuộc (API Keys cần lấy trước):**
- `ORS_API_KEY`: Đăng ký miễn phí tại [openrouteservice.org](https://openrouteservice.org/dev/#/home)
- `OPENWEATHERMAP_API_KEY`: Đăng ký miễn phí tại [openweathermap.org/api](https://openweathermap.org/api)
- `JWT_SECRET`: Tự tạo chuỗi ngẫu nhiên mạnh (≥ 64 ký tự) cho production.

**Ước tính tổng:** ~1.5 tuần

---

## Phase 3 — AI Enhancement: Gemini diễn đạt kết quả

**Mục tiêu:** Dùng Gemini để **diễn đạt kết quả bằng ngôn ngữ tự nhiên**, KHÔNG thay thế ScoringEngine.

| Việc cụ thể | Ước tính |
|---|---|
| Tạo `GeminiNarrativeService` — nhận output từ `PlanBuilder`, trả về mô tả tự nhiên cho mỗi plan variant | 2 ngày |
| Frontend: Hiển thị đoạn mô tả AI bên cạnh mỗi phương án thay vì chỉ có số liệu | 1 ngày |
| Fallback: Nếu Gemini API lỗi/chậm, vẫn hiển thị kết quả rule-based bình thường | 1 ngày |
| Rate limit & caching cho Gemini calls (tránh tốn quota) | 1 ngày |

**Phụ thuộc:**
- `GEMINI_API_KEY`: Đăng ký tại [Google AI Studio](https://aistudio.google.com/app/apikey)

**Vai trò của Gemini (quan trọng):**
- Gemini CHỈ đóng vai trò "copywriter" — nhận danh sách địa điểm đã được ScoringEngine chọn và
  viết thành đoạn văn gợi ý hấp dẫn.
- ScoringEngine vẫn là bộ não quyết định. Gemini KHÔNG được phép thay đổi thứ tự hoặc loại bỏ địa điểm.

**Ước tính tổng:** ~1 tuần

---

## Phase 4 — UX Nâng cao: Drag & Drop + Export

**Mục tiêu:** Cho phép người dùng tùy chỉnh lịch trình bằng tay và chia sẻ kết quả.

| Việc cụ thể | Ước tính |
|---|---|
| Drag & Drop trên Timeline (dnd-kit đã có trong deps) — cho phép kéo thả thay đổi thứ tự | 2 ngày |
| Khi kéo thả, tự động recalculate travel time và cập nhật bản đồ | 1 ngày |
| Xuất lịch trình sang PDF (OpenPDF — backend) | 2 ngày |
| Xuất lịch trình sang ICS (calendar file) | 1 ngày |
| Share link lịch trình (public read-only URL) | 1 ngày |

**Phụ thuộc:** Không có API key mới.

**Ước tính tổng:** ~1.5 tuần

---

## Phase 5 — Data thật & Scale

**Mục tiêu:** Thay dữ liệu mock bằng nguồn thật, mở rộng số lượng thành phố.

| Việc cụ thể | Ước tính |
|---|---|
| Viết script crawl/import dữ liệu từ **OpenStreetMap (Overpass API)** hoặc **Google Places API** | 3 ngày |
| Mở rộng seed data: Hà Nội, Đà Nẵng, Đà Lạt (mỗi thành phố ~50 địa điểm) | 2 ngày |
| Chuẩn hóa pipeline: raw data → cleaned data → seed JSON | 1 ngày |
| Hệ thống review/rating từ user thật thay vì số liệu giả lập | 3 ngày |

**Phụ thuộc:**
- Google Places API key (nếu dùng) — có phí.
- Hoặc dùng Overpass API (OpenStreetMap) — miễn phí nhưng data ít chi tiết hơn.

**Ước tính tổng:** ~2 tuần

---

## 🔧 Kỹ thuật nợ (Technical Debt)

Danh sách các vấn đề kỹ thuật cần xử lý, sắp theo mức ưu tiên:

| # | Vấn đề | Mức độ | Xử lý ở Phase |
|---|---|---|---|
| 1 | `jwt.secret` trong `application.yml` chính đã bỏ default — nhưng `application-dev.yml` vẫn có fallback `dev-only-secret-do-not-use-in-production` | Trung bình | Đã xử lý (Phase 1) — chỉ ảnh hưởng môi trường dev |
| 2 | Dữ liệu 52 địa điểm là **mock nhập tay**, rating/cost/reviewCount đều giả lập | Cao | Phase 5 |
| 3 | Khoảng cách tính bằng **Haversine (đường chim bay)**, không phải đường đi thực tế | Cao | Phase 2 (ORS) |
| 4 | Thời gian di chuyển ước tính bằng vận tốc trung bình cố định (25/30/15 km/h) | Trung bình | Phase 2 (ORS) |
| 5 | Gemini/ORS/OWM config đã comment out trong `application.yml` — cần uncomment khi tích hợp | Thấp | Phase 2 & 3 |
| 6 | Chưa có unit test cho `ScoringEngine`, `SlotAllocator`, `RecommendationService` | Cao | Phase 2 |
| 7 | `ScoringEngine` dùng hardcode `GLOBAL_AVG_RATING = 3.5` thay vì tính dynamic từ DB | Thấp | Phase 5 |
| 8 | `Destination.getTagList()` tạo `ObjectMapper` mới mỗi lần gọi — cần inject hoặc cache | Thấp | Phase 4 |
| 9 | Hibernate `ddl-auto: update` — cần chuyển sang Flyway/Liquibase migration trước production | Cao | Phase 2 |
| 10 | Chưa có logging/monitoring (OpenTelemetry, structured logging) | Trung bình | Phase 5 |
| 11 | Chuyển từ `RestTemplate` sang `RestClient` (HTTP Client mới của Spring) cho `OpenRouteServiceClient` | Thấp | Phase 5 |

---

## Tóm tắt Timeline

```
Phase 1 (MVP)         ██████████████████████ DONE
Phase 2 (Deploy+API)  ██████████████████████ DONE
Phase 3 (Gemini)      ░░░░░░      ~1 tuần
Phase 4 (DnD+Export)  ░░░░░░░░░░  ~1.5 tuần
Phase 5 (Real Data)   ░░░░░░░░░░░░░░ ~2 tuần
                      ─────────────────────────
                      Tổng ước tính: ~7 tuần (sau Phase 1)
```
