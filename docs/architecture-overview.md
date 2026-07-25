# Architecture Overview

## Mục đích

Tài liệu này cung cấp một bản tổng hợp kiến trúc cấp cao cho dự án Travel Planner MVP, phản ánh các thay đổi trong Phase 1. 

## 1. Tổng quan hệ thống

Travel Planner MVP là một ứng dụng hỗ trợ lên kế hoạch du lịch thông minh, tập trung vào trải nghiệm người dùng tối ưu (Context Cards) và thuật toán gợi ý (Scoring Engine) đa biến.

Hệ thống được thiết kế theo các nguyên tắc chính:
- Tinh gọn và tập trung vào các domain cốt lõi.
- Dễ phát triển, bảo trì và kiểm thử.
- Thuật toán rõ ràng, có thể giải thích được thay vì phụ thuộc hoàn toàn vào Black-box AI.
- Sẵn sàng tích hợp API bên thứ 3 trong các Phase tiếp theo.

## 2. Kiến trúc cấp cao

### Frontend
- **React 19 + Vite 8 + TypeScript**
- **Giao diện**: Flow thu thập thông tin dạng Context Cards (GSAP animations), bản đồ Leaflet, và Timeline View.
- Tương tác với backend thông qua REST API (sử dụng Axios với JWT interceptor).

### Backend
- **Spring Boot 4.1.0**
- **Kiến trúc Modular Monolith** với 3 domain nghiệp vụ chính:
  1. `identity`: Xử lý xác thực (Auth), User, và Preferences.
  2. `planning`: Quản lý các entities cốt lõi như Trip, TripActivity, và TravelContext.
  3. `recommendation`: Trái tim thuật toán, chứa ScoringEngine, SlotAllocator và logic sinh kế hoạch.

### Data Layer
- **PostgreSQL 16**: Lưu trữ toàn bộ dữ liệu nghiệp vụ (Trips, Destinations).
- **Redis 7**: Cache và Rate Limiting (Bucket4j).

## 3. Luồng nghiệp vụ chính (Trip Planning Flow)

1. **Thu thập bối cảnh (Context Collection)**: Người dùng nhập thông tin qua 5 bước (Mục đích, Thời gian, Nhóm, Ngân sách, Vị trí).
2. **Chấm điểm (Scoring)**: `ScoringEngine` đánh giá các điểm đến dựa trên 5 yếu tố: Rating, Khoảng cách (Distance), Giờ mở cửa (Hours), Sở thích (Preference), và Ngân sách (Budget).
3. **Phân bổ (Allocation)**: `SlotAllocator` sắp xếp các điểm đến có điểm cao nhất vào khung giờ hợp lý.
4. **Xây dựng kế hoạch (Plan Building)**: Trả về 3 phương án (Variants) cho người dùng lựa chọn (Balanced, Foodie, Saver).

## 4. Các mối quan tâm xuyên suốt

- **Security**: JWT Stateless Authentication & BCrypt.
- **Maintainability**: Code được chia theo domain-driven (identity, planning, recommendation), giảm bớt sự liên kết chồng chéo.
- **Performance**: Thuật toán tính toán trực tiếp trên memory sau khi load các candidate, đảm bảo tốc độ phản hồi < 2s.

## Tài liệu liên quan

- [System Context Diagram](system-context-diagram.md)
- [C4 Model](c4-model.md)
