# ADR-012: Chọn PDF và ICS làm định dạng xuất dữ liệu chính

- Date: 2026-07-17
- Status: Accepted
- Authors: Development Team

## Context

Hệ thống cần hỗ trợ người dùng xuất kế hoạch du lịch ra các định dạng có thể dùng ngay, đặc biệt là PDF để chia sẻ hoặc in và ICS để thêm vào lịch trên điện thoại hoặc máy tính.

## Decision

Chúng tôi quyết định hỗ trợ xuất dữ liệu dưới hai định dạng chính:

- PDF cho bản tóm tắt hoặc tài liệu có thể in và chia sẻ;
- ICS cho việc thêm chuyến đi vào lịch calendar.

PDF sẽ được tạo bằng thư viện OpenPDF, trong khi ICS sẽ được tạo theo chuẩn calendar phổ biến để tương thích với Google Calendar và Apple Calendar.

## Consequences

### Positive

- Người dùng có nhiều lựa chọn hơn trong việc lưu trữ và chia sẻ kế hoạch.
- Tăng tính thực tế và trải nghiệm người dùng.
- Hỗ trợ tốt cho các nhu cầu du lịch thực tế.

### Negative

- Cần duy trì logic render phù hợp cho từng định dạng.
- Một số tính năng định dạng cao cấp có thể cần thêm công sức phát triển.

## Alternatives considered

### 1. Chỉ hỗ trợ export PDF

Đủ cho việc in ấn, nhưng thiếu tính tiện lợi khi người dùng muốn nhập lịch vào calendar.

### 2. Chỉ hỗ trợ export JSON hoặc HTML

Dễ triển khai hơn, nhưng không thân thiện với người dùng cuối trong các tình huống thực tế.

## Implementation Notes

Tính năng export sẽ được tích hợp vào module itinerary và có thể mở rộng thêm các định dạng khác trong tương lai.
