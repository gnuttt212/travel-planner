# ADR-016: Chọn CI/CD để tự động hóa build, test và deployment

- Date: 2026-07-17
- Status: Accepted
- Authors: Development Team

## Context

Quá trình phát triển hệ thống sẽ trở nên hiệu quả hơn nếu build, test và deployment được tự động hóa. Nếu làm thủ công, thời gian phát hành sẽ chậm, dễ phát sinh lỗi và khó kiểm soát chất lượng.

## Decision

Chúng tôi quyết định thiết lập quy trình CI/CD để tự động hóa các bước:

- build ứng dụng;
- chạy unit test và integration test;
- đóng gói artifact;
- triển khai lên môi trường phù hợp.

## Consequences

### Positive

- Tăng tốc độ phát hành và giảm lỗi thủ công.
- Tăng độ tin cậy cho quá trình release.
- Dễ dàng theo dõi trạng thái build và deployment.

### Negative

- Cần đầu tư thời gian ban đầu để thiết lập pipeline.
- Cần duy trì cấu hình pipeline khi hệ thống thay đổi.

## Alternatives considered

### 1. Deploy thủ công

Đơn giản hơn ban đầu, nhưng chậm và dễ sai.

### 2. CI/CD hoàn toàn bằng công cụ bên ngoài phức tạp

Có thể mạnh, nhưng không cần thiết ở giai đoạn đầu.

## Implementation Notes

Pipeline sẽ được cấu hình để chạy tự động khi có thay đổi trên branch chính hoặc khi có pull request, nhằm đảm bảo chất lượng trước khi release.
