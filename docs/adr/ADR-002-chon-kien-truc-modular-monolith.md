# ADR-002: Chọn kiến trúc Modular Monolith cho backend

- Date: 2026-07-17
- Status: Accepted
- Authors: Development Team

## Context

Hệ thống Travel Planner cần có một kiến trúc backend dễ phát triển, dễ bảo trì và có khả năng mở rộng về sau. Trong giai đoạn đầu, đội ngũ cần ưu tiên tốc độ triển khai, độ rõ ràng về cấu trúc module và giảm chi phí vận hành so với một kiến trúc phân tán.

Nếu chọn kiến trúc microservices ngay từ đầu, hệ thống sẽ gặp nhiều rủi ro về độ phức tạp vận hành, triển khai và giao tiếp giữa các service. Ngược lại, một kiến trúc monolith truyền thống có thể khiến các module bị chồng chéo và khó kiểm soát khi hệ thống lớn dần.

## Decision

Chúng tôi quyết định triển khai backend theo mô hình Modular Monolith.

Cấu trúc sẽ được chia thành các module nghiệp vụ riêng biệt, bao gồm:

- user
- itinerary
- budget
- collaboration
- booking
- interaction

Mỗi module sẽ có trách nhiệm riêng và giao tiếp chủ yếu thông qua các boundary đã được xác định rõ. Điều này giúp hệ thống vẫn có cấu trúc rõ ràng, nhưng vẫn giữ được lợi thế của một single deployable application.

## Consequences

### Positive

- Dễ triển khai và vận hành hơn so với microservices.
- Cấu trúc module rõ ràng, dễ bảo trì và mở rộng.
- Có thể chuyển sang kiến trúc phân tán hơn khi hệ thống phát triển nếu cần.
- Giảm chi phí vận hành ban đầu.

### Negative

- Nếu hệ thống phát triển quá lớn, một monolith có thể trở nên khó mở rộng theo chiều dọc.
- Cần có quy tắc rõ ràng về boundary giữa các module để tránh phụ thuộc chéo.

## Alternatives considered

### 1. Microservices

Phù hợp với hệ thống lớn, phân tán và có nhiều team phát triển riêng. Tuy nhiên, chi phí vận hành và độ phức tạp ban đầu quá cao cho dự án ở giai đoạn đầu.

### 2. Monolith truyền thống

Đơn giản hơn nhưng khó kiểm soát khi nhiều module phát triển cùng lúc, đặc biệt với các tính năng như collaboration, itinerary và AI recommendation.

## Implementation Notes

Các module sẽ được tổ chức theo domain-driven structure và giữ các dependency giữa module ở mức tối thiểu. Nếu cần, có thể dùng các interface hoặc boundary abstraction để giảm coupling.
