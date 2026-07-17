# ADR-020: Chọn feature flags cho phát triển và release

- Date: 2026-07-17
- Status: Accepted
- Authors: Development Team

## Context

Trong quá trình phát triển, một số tính năng mới có thể cần được bật tắt linh hoạt mà không cần deploy lại toàn bộ hệ thống. Điều này đặc biệt hữu ích khi kiểm thử tính năng mới, rollout từng phần hoặc xử lý các vấn đề production.

## Decision

Chúng tôi quyết định sử dụng feature flags để kiểm soát việc bật/tắt các tính năng mới một cách linh hoạt.

Feature flags sẽ được dùng cho:

- tính năng đang phát triển;
- release từng phần cho người dùng thử nghiệm;
- tắt nhanh tính năng khi xảy ra sự cố.

## Consequences

### Positive

- Giảm rủi ro khi triển khai tính năng mới.
- Cho phép rollout dần dần và kiểm soát tốt hơn.
- Dễ đảo ngược khi cần.

### Negative

- Cần quản lý cấu hình và lifecycle của flags.
- Nếu dùng quá nhiều flags không kiểm soát, hệ thống có thể trở nên khó hiểu.

## Alternatives considered

### 1. Không dùng feature flags

Đơn giản hơn, nhưng khó kiểm soát release và rollback.

### 2. Dùng feature flags quá rộng và không có chính sách quản lý

Có thể làm tăng độ phức tạp và khó duy trì.

## Implementation Notes

Feature flags sẽ được giới hạn cho các tính năng có rủi ro cao hoặc cần rollout kiểm soát. Mỗi flag nên có thời gian sống rõ ràng và được kiểm soát bởi quy trình phù hợp.
