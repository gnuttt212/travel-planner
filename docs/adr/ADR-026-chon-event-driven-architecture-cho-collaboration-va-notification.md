# ADR-026: Chọn Event-Driven Architecture cho collaboration và notification

- Date: 2026-07-17
- Status: Accepted
- Authors: Development Team

## Context

Hệ thống có các hoạt động cần phản ứng nhanh với các sự kiện như người dùng thay đổi kế hoạch, cập nhật ngân sách, gửi thông báo hoặc thay đổi trạng thái collaboration. Nếu xử lý trực tiếp bằng cách gọi tuần tự giữa các module, hệ thống có thể trở nên khó mở rộng và chậm hơn khi tính năng tăng trưởng.

## Decision

Chúng tôi quyết định áp dụng Event-Driven Architecture ở mức hợp lý cho các luồng collaboration và notification.

Các sự kiện chính sẽ bao gồm:

- itinerary.updated
- budget.updated
- collaboration.member.changed
- notification.sent

Việc phát hành và xử lý sự kiện sẽ giúp các module tách rời nhau tốt hơn, đồng thời tạo nền tảng cho mở rộng về sau.

## Consequences

### Positive

- Tăng tính tách rời giữa các module.
- Dễ mở rộng cho các tính năng thông báo và xử lý phản ứng.
- Hỗ trợ tích hợp thêm hệ thống bên ngoài trong tương lai.

### Negative

- Đòi hỏi thiết kế event schema và xử lý lỗi rõ ràng.
- Có thể làm tăng độ phức tạp so với cách gọi trực tiếp thông thường.

## Alternatives considered

### 1. Gọi trực tiếp giữa các module

Đơn giản hơn ở giai đoạn đầu, nhưng khó mở rộng khi số lượng luồng nghiệp vụ tăng.

### 2. Dùng event-driven hoàn toàn ngay từ đầu

Quá nặng và phức tạp cho nhu cầu hiện tại.

## Implementation Notes

Event-Driven Architecture sẽ được áp dụng ở mức nhẹ, ưu tiên cho các luồng có tính phản ứng và giao tiếp không đồng bộ. Khi cần, có thể dùng message broker hoặc queue trong tương lai.
