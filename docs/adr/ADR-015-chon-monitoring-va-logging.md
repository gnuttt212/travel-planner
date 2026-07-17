# ADR-015: Chọn monitoring và logging để giám sát hệ thống

- Date: 2026-07-17
- Status: Accepted
- Authors: Development Team

## Context

Khi hệ thống chạy ở môi trường production, việc theo dõi trạng thái ứng dụng, lỗi và hiệu suất là rất quan trọng. Nếu không có monitoring và logging, việc phát hiện sự cố sẽ chậm và khó xác định nguyên nhân.

## Decision

Chúng tôi quyết định tích hợp logging và monitoring cơ bản cho toàn bộ hệ thống.

Các thành phần chính sẽ gồm:

- log application cho request, lỗi và sự kiện quan trọng;
- metrics cho hiệu suất và độ ổn định hệ thống;
- health check để theo dõi trạng thái service.

## Consequences

### Positive

- Giúp phát hiện sự cố nhanh hơn.
- Dễ dàng theo dõi hiệu suất và lỗi production.
- Hỗ trợ debug và phân tích sự cố hiệu quả hơn.

### Negative

- Tạo thêm khối lượng dữ liệu cần lưu trữ và quản lý.
- Cần cấu hình và duy trì đúng cách để tránh ảnh hưởng hiệu năng.

## Alternatives considered

### 1. Không tích hợp monitoring

Đơn giản nhưng không đủ để vận hành hệ thống ở môi trường production.

### 2. Chỉ dùng logging thuần túy

Có thể phát hiện lỗi, nhưng thiếu metrics và cảnh báo đầy đủ.

## Implementation Notes

Monitoring và logging sẽ được triển khai ở tầng ứng dụng và có thể mở rộng sang các nền tảng giám sát tập trung trong tương lai.
