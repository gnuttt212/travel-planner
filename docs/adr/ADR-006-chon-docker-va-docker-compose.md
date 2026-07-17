# ADR-006: Chọn Docker và Docker Compose cho môi trường phát triển và triển khai

- Date: 2026-07-17
- Status: Accepted
- Authors: Development Team

## Context

Dự án cần một cách chuẩn hóa để chạy các dịch vụ nền như PostgreSQL, Redis và các môi trường phát triển khác trên nhiều máy tính khác nhau. Nếu không có công cụ container hóa, việc thiết lập môi trường sẽ phụ thuộc nhiều vào cấu hình cục bộ và dễ xảy ra lỗi không nhất quán.

## Decision

Chúng tôi quyết định sử dụng Docker và Docker Compose để chuẩn hóa môi trường phát triển và hỗ trợ triển khai ban đầu.

Docker Compose sẽ được dùng để khởi chạy các dịch vụ cần thiết như:

- PostgreSQL
- Redis
- các dịch vụ phụ trợ khác nếu cần

## Consequences

### Positive

- Môi trường phát triển thống nhất giữa các thành viên.
- Dễ khởi động và dừng các dịch vụ phụ trợ.
- Giảm lỗi phát sinh do khác biệt hệ điều hành hoặc cấu hình máy cục bộ.

### Negative

- Cần cài đặt Docker trên máy phát triển.
- Một số trường hợp cần cấu hình thêm cho môi trường production.

## Alternatives considered

### 1. Chạy trực tiếp trên máy cục bộ

Đơn giản hơn ban đầu, nhưng dễ gây sai lệch cấu hình giữa các máy.

### 2. Chỉ dùng máy ảo

Khó quản lý và không linh hoạt bằng Docker.

## Implementation Notes

Docker Compose sẽ là công cụ chính để chuẩn hóa môi trường phát triển. Các dịch vụ cần thiết sẽ được định nghĩa trong file docker-compose.yml và có thể được khởi chạy bằng một lệnh đơn giản.
