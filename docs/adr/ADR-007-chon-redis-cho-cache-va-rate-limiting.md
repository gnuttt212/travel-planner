# ADR-007: Chọn Redis cho cache và rate limiting

- Date: 2026-07-17
- Status: Accepted
- Authors: Development Team

## Context

Hệ thống có các tính năng cần hiệu suất cao và kiểm soát lưu lượng truy cập, như rate limiting cho API và cache cho dữ liệu truy vấn thường xuyên. Cần một giải pháp lưu trữ dữ liệu tạm thời nhanh, dễ tích hợp và phù hợp với môi trường phân tán.

## Decision

Chúng tôi quyết định sử dụng Redis làm hệ thống cache và lưu trữ dữ liệu rate limiting.

Redis được chọn vì:

- tốc độ đọc/ghi cao;
- hỗ trợ cấu trúc dữ liệu phù hợp cho rate limiting;
- dễ tích hợp với Spring Boot;
- phù hợp cho các tình huống cần cache dữ liệu tạm thời hoặc giảm tải cho hệ thống.

## Consequences

### Positive

- Cải thiện hiệu năng hệ thống khi truy cập dữ liệu lặp lại.
- Hỗ trợ kiểm soát truy cập và bảo vệ API khỏi traffic bất thường.
- Dễ tích hợp vào kiến trúc hiện tại.

### Negative

- Cần thêm một dịch vụ phụ trợ để vận hành.
- Cần chú ý đến quản lý dữ liệu hết hạn và độ tin cậy của cache.

## Alternatives considered

### 1. Không dùng cache

Đơn giản hơn nhưng ảnh hưởng tới hiệu năng khi lưu lượng tăng.

### 2. Dùng bộ nhớ local trong ứng dụng

Nhanh nhưng không phù hợp cho môi trường phân tán và khó chia sẻ giữa các instance.

## Implementation Notes

Redis sẽ được dùng cho cache dữ liệu tạm thời và cho các cơ chế rate limiting như Bucket4j trong hệ thống backend.
