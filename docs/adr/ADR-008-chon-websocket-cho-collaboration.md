# ADR-008: Chọn WebSocket cho tính năng collaboration real-time

- Date: 2026-07-17
- Status: Accepted
- Authors: Development Team

## Context

Hệ thống có yêu cầu cho phép nhiều người cùng chỉnh sửa một kế hoạch du lịch hoặc một chuyến đi trong thời gian thực. Cần một cơ chế truyền dữ liệu hai chiều giữa client và server để cập nhật trạng thái đồng bộ ngay lập tức.

## Decision

Chúng tôi quyết định sử dụng WebSocket để triển khai tính năng collaboration real-time.

WebSocket được chọn vì:

- hỗ trợ kết nối hai chiều liên tục;
- phù hợp cho các tính năng cập nhật trạng thái theo thời gian thực;
- có thể tích hợp tốt với Spring Boot và frontend hiện tại.

## Consequences

### Positive

- Cung cấp trải nghiệm cộng tác trực tiếp và mượt mà.
- Giảm độ trễ khi cập nhật dữ liệu giữa các client.
- Dễ mở rộng cho các tính năng như comment, đồng bộ thay đổi và thông báo real-time.

### Negative

- Cần quản lý kết nối và trạng thái session cẩn thận.
- Phức tạp hơn so với cách gọi API thông thường khi cần đồng bộ dữ liệu.

## Alternatives considered

### 1. Polling

Đơn giản hơn nhưng tiêu tốn tài nguyên và có độ trễ cao.

### 2. Server-Sent Events

Phù hợp cho luồng một chiều, nhưng không đủ tốt cho collaboration hai chiều.

## Implementation Notes

WebSocket sẽ được dùng cho các hoạt động collaboration, cập nhật thời gian thực và thông báo giữa người dùng khi họ cùng làm việc trên một kế hoạch.
