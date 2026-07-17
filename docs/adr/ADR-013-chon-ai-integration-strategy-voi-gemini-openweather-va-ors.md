# ADR-013: Chọn Gemini, OpenWeather và OpenRouteService cho tích hợp AI và dịch vụ bên ngoài

- Date: 2026-07-17
- Status: Accepted
- Authors: Development Team

## Context

Hệ thống cần tích hợp nhiều dịch vụ bên ngoài để nâng cao trải nghiệm du lịch, như tạo lịch trình bằng AI, dự báo thời tiết và tối ưu tuyến đường. Việc lựa chọn đúng nhà cung cấp giúp tăng chất lượng tính năng và giảm rủi ro về độ ổn định và chi phí.

## Decision

Chúng tôi quyết định sử dụng các dịch vụ sau:

- Google Gemini cho việc tạo nội dung AI và gợi ý lịch trình;
- OpenWeatherMap cho dự báo thời tiết;
- OpenRouteService cho tối ưu hóa tuyến đường và giải quyết bài toán TSP.

Lựa chọn này phù hợp với mục tiêu tạo ra một hệ thống du lịch thông minh, có thể cá nhân hóa nội dung và tối ưu lộ trình.

## Consequences

### Positive

- Tăng giá trị trải nghiệm người dùng bằng AI và dữ liệu thời tiết thực tế.
- Hỗ trợ tối ưu hóa lịch trình tự động.
- Có nhiều khả năng nâng cấp và mở rộng tính năng trong tương lai.

### Negative

- Phụ thuộc vào các dịch vụ bên ngoài và API key.
- Cần quản lý chi phí và giới hạn request.
- Có thể bị ảnh hưởng bởi độ trễ hoặc sự thay đổi API của nhà cung cấp.

## Alternatives considered

### 1. Chỉ dùng rule-based logic nội bộ

Đơn giản hơn nhưng ít linh hoạt và thiếu tính “thông minh” so với mục tiêu của hệ thống.

### 2. Dùng các dịch vụ AI khác thay thế

Có thể phù hợp, nhưng Gemini đã có lợi thế về tích hợp và khả năng xử lý prompt tốt cho nhu cầu của dự án.

## Implementation Notes

Tích hợp các dịch vụ bên ngoài sẽ được thực hiện thông qua các adapter hoặc service layer riêng, nhằm giảm phụ thuộc trực tiếp vào từng nhà cung cấp và dễ thay thế khi cần.
