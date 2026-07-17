# ADR-003: Chọn PostgreSQL kết hợp pgvector cho lưu trữ dữ liệu và tìm kiếm ngữ nghĩa

- Date: 2026-07-17
- Status: Accepted
- Authors: Development Team

## Context

Hệ thống cần lưu trữ dữ liệu quan hệ thông thường như người dùng, chuyến đi, ngân sách và lịch trình, đồng thời hỗ trợ tìm kiếm gợi ý theo ngữ nghĩa cho các điểm đến. Đây là một yêu cầu phù hợp với việc kết hợp cơ sở dữ liệu quan hệ và vector search.

Để giảm độ phức tạp và tăng tính ổn định, cần chọn một nền tảng dữ liệu có thể hỗ trợ cả transactional data và vector similarity search trong cùng một hệ thống.

## Decision

Chúng tôi quyết định sử dụng PostgreSQL làm hệ quản trị cơ sở dữ liệu chính, kết hợp mở rộng pgvector để hỗ trợ tìm kiếm vector.

Lý do chọn:

- PostgreSQL phù hợp cho dữ liệu quan hệ và có khả năng mở rộng tốt.
- pgvector cho phép lưu trữ embedding và thực hiện tìm kiếm tương đồng hiệu quả.
- Hệ thống có thể chạy trên một stack tương đối đơn giản mà vẫn đủ mạnh cho yêu cầu hiện tại.
- Khả năng tích hợp tốt với Spring Boot và các công nghệ backend hiện tại.

## Consequences

### Positive

- Hỗ trợ tốt cho cả dữ liệu truyền thống và semantic search.
- Giảm nhu cầu dùng nhiều hệ thống cơ sở dữ liệu riêng biệt.
- Dễ quản lý và vận hành hơn so với việc dùng nhiều stack khác nhau.

### Negative

- Cấu hình và tối ưu hóa vector search cần được xem xét kỹ để đạt hiệu suất tốt.
- Một số tính năng nâng cao của vector database chuyên dụng có thể không bằng các hệ thống chuyên biệt.

## Alternatives considered

### 1. MongoDB

Phù hợp cho dữ liệu linh hoạt, nhưng không phải lựa chọn tốt nhất cho các dữ liệu quan hệ và query phức tạp trong hệ thống này.

### 2. Chỉ dùng PostgreSQL thuần túy

Có thể đáp ứng dữ liệu quan hệ nhưng không hỗ trợ vector search trực tiếp mà cần thiết kế thêm phức tạp.

## Implementation Notes

PostgreSQL sẽ được dùng cho dữ liệu nghiệp vụ chính, trong khi pgvector sẽ được dùng cho các tính năng gợi ý điểm đến và tìm kiếm theo ngữ cảnh.
