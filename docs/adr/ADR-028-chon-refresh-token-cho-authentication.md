# ADR-028: Chọn refresh token cho xác thực dài hạn

- Date: 2026-07-17
- Status: Accepted
- Authors: Development Team

## Context

Hệ thống cần bảo mật và trải nghiệm người dùng tốt khi sử dụng JWT cho authentication. Tuy nhiên, access token nếu có thời gian sống ngắn sẽ khiến người dùng phải đăng nhập lại thường xuyên. Cần một cơ chế cho phép người dùng duy trì phiên làm việc mà vẫn kiểm soát bảo mật tốt.

## Decision

Chúng tôi quyết định sử dụng refresh token kết hợp với access token để quản lý authentication.

Cách triển khai:

- access token có thời gian sống ngắn;
- refresh token có thời gian sống dài hơn;
- refresh token sẽ được lưu an toàn và dùng để cấp lại access token khi hết hạn;
- việc revoke refresh token sẽ được hỗ trợ khi người dùng đăng xuất hoặc phát hiện token bị lộ.

## Consequences

### Positive

- Cải thiện trải nghiệm người dùng khi không cần đăng nhập lại thường xuyên.
- Giữ được mức độ bảo mật tốt hơn so với access token dài hạn.
- Dễ kiểm soát việc đăng xuất và vô hiệu hóa phiên.

### Negative

- Cần thêm logic quản lý refresh token và cơ chế revoke.
- Nếu triển khai sai, hệ thống có thể gặp rủi ro về token theft hoặc session hijacking.

## Alternatives considered

### 1. Chỉ dùng access token dài hạn

Đơn giản hơn, nhưng rủi ro bảo mật cao hơn.

### 2. Dùng session-based authentication hoàn toàn

Phù hợp với ứng dụng web truyền thống, nhưng kém linh hoạt hơn cho API và ứng dụng đa client.

## Implementation Notes

Refresh token sẽ được lưu ở server hoặc trong storage có bảo mật phù hợp, và cần có cơ chế rotate token để giảm rủi ro bị đánh cắp.
