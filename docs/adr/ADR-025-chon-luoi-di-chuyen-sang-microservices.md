# ADR-025: Chọn lộ trình chuyển đổi sang microservices khi hệ thống phát triển

- Date: 2026-07-17
- Status: Accepted
- Authors: Development Team

## Context

Hệ thống hiện tại được thiết kế theo Modular Monolith vì phù hợp với giai đoạn đầu phát triển. Tuy nhiên, khi số lượng người dùng tăng, quy mô nghiệp vụ mở rộng và nhóm phát triển lớn hơn, một số module có thể trở nên khó mở rộng và vận hành riêng biệt.

Cần một lộ trình rõ ràng để chuyển dần sang kiến trúc phân tán nếu cần, mà không làm gián đoạn hệ thống đang chạy.

## Decision

Chúng tôi quyết định giữ nguyên Modular Monolith ở giai đoạn hiện tại, nhưng thiết kế hệ thống sao cho có thể chuyển dần sang microservices khi cần.

Các nguyên tắc chính:

- tách rõ boundary giữa các domain nghiệp vụ;
- giảm coupling giữa các module;
- ưu tiên giao diện và contract rõ ràng;
- không chuyển đổi đột ngột nếu chưa cần thiết.

## Consequences

### Positive

- Giữ được tốc độ phát triển ban đầu.
- Giảm rủi ro khi mở rộng hệ thống.
- Dễ chuyển đổi từng module một khi cần thiết.

### Negative

- Kiến trúc hiện tại có thể chưa tối ưu cho quy mô rất lớn.
- Cần có chiến lược refactoring khi bắt đầu tách service.

## Alternatives considered

### 1. Chuyển ngay sang microservices

Mạnh về phân tách nhưng quá phức tạp và rủi ro ở giai đoạn đầu.

### 2. Giữ nguyên monolith mãi mãi

Đơn giản nhưng có thể trở thành hạn chế khi hệ thống phát triển vượt ngưỡng.

## Implementation Notes

Lộ trình chuyển đổi sẽ dựa trên các dấu hiệu như số lượng traffic lớn, team phát triển lớn hơn, hoặc một module cần scale độc lập. Khi đó, các domain có thể được tách dần từng bước với giao diện và contract được chuẩn hóa trước.
