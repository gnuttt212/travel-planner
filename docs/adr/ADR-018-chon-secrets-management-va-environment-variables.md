# ADR-018: Chọn secrets management và environment variables cho cấu hình nhạy cảm

- Date: 2026-07-17
- Status: Accepted
- Authors: Development Team

## Context

Hệ thống sử dụng nhiều thông tin nhạy cảm như API key, chuỗi kết nối database, token JWT và thông tin môi trường triển khai. Nếu lưu trực tiếp trong mã nguồn hoặc cấu hình cố định, hệ thống có nguy cơ bị rò rỉ thông tin và khó quản lý khi triển khai nhiều môi trường.

## Decision

Chúng tôi quyết định lưu trữ cấu hình nhạy cảm thông qua environment variables và ưu tiên các giải pháp quản lý secrets phù hợp khi hệ thống đi vào production.

Cách áp dụng:

- các giá trị nhạy cảm sẽ không được hardcode trong source code;
- môi trường development, staging và production sẽ dùng các biến môi trường riêng;
- trong giai đoạn đầu, các giá trị có thể được cấu hình qua file environment hoặc biến runtime;
- khi triển khai thực tế, có thể chuyển sang hệ thống secrets manager phù hợp.

## Consequences

### Positive

- Tăng bảo mật cho thông tin nhạy cảm.
- Dễ quản lý cấu hình giữa nhiều môi trường.
- Giảm nguy cơ lộ secrets khi chia sẻ code hoặc deploy.

### Negative

- Cần có quy trình rõ ràng để thiết lập biến môi trường cho từng môi trường.
- Việc debug có thể khó hơn nếu thiếu cấu hình đúng.

## Alternatives considered

### 1. Lưu secrets trực tiếp trong source code

Đơn giản nhưng không an toàn và không phù hợp với production.

### 2. Lưu secrets trong file cấu hình commit vào repository

Dễ quản lý ban đầu, nhưng có rủi ro cao về lộ thông tin.

## Implementation Notes

Các cấu hình như database URL, API key, JWT secret và các giá trị vận hành sẽ được đọc từ biến môi trường hoặc các file cấu hình không commit vào repository.
