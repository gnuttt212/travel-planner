# ADR-010: Chọn cấu trúc Domain-Driven và Layered Architecture cho backend

- Date: 2026-07-17
- Status: Accepted
- Authors: Development Team

## Context

Backend của hệ thống Travel Planner có nhiều nghiệp vụ riêng biệt như người dùng, lịch trình, ngân sách, cộng tác và đặt chỗ. Nếu không có cấu trúc rõ ràng, mã nguồn có thể trở nên khó hiểu, khó mở rộng và dễ phát sinh phụ thuộc chéo giữa các module.

Cần một cách tổ chức code giúp tách biệt rõ trách nhiệm nghiệp vụ, giảm độ phụ thuộc và đồng thời vẫn phù hợp với kiến trúc Modular Monolith.

## Decision

Chúng tôi quyết định tổ chức backend theo mô hình Domain-Driven Design (DDD) kết hợp với Layered Architecture trong từng domain.

Mỗi domain sẽ được tổ chức theo các lớp chính:

- controller: tiếp nhận request và điều phối xử lý;
- service: chứa logic nghiệp vụ;
- repository: truy cập dữ liệu;
- domain/entity: các thực thể và quy tắc nghiệp vụ;
- dto: chuyển đổi dữ liệu giữa tầng;
- mapper/adapter: xử lý chuyển đổi và tích hợp.

Mục tiêu là giữ mỗi domain tập trung vào một trách nhiệm chuyên biệt và giảm phụ thuộc giữa các domain.

## Consequences

### Positive

- Mã nguồn có cấu trúc rõ ràng và dễ bảo trì.
- Dễ mở rộng thêm tính năng mới trong từng domain.
- Giảm hiện tượng logic bị phân tán giữa các module.
- Hỗ trợ việc testing và onboarding tốt hơn.

### Negative

- Cần tuân thủ nghiêm ngặt về boundary và quy ước code.
- Có thể làm tăng số lượng class và file ban đầu.

## Alternatives considered

### 1. Cấu trúc theo layer tổng quát không phân domain

Đơn giản hơn nhưng khó quản lý khi hệ thống có nhiều nghiệp vụ khác nhau.

### 2. Cấu trúc hoàn toàn theo DDD với nhiều aggregate và bounded context phức tạp

Mạnh về mô hình hóa nhưng quá nặng cho giai đoạn đầu của dự án.

## Implementation Notes

Trong giai đoạn hiện tại, mỗi domain sẽ có cấu trúc cơ bản giống nhau, nhưng vẫn giữ được mức độ độc lập. Khi hệ thống phát triển, các domain có thể được tách dần nếu cần chuyển sang kiến trúc phân tán hơn.
