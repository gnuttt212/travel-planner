package com.travelplanner.planning.domain;

/**
 * Enum xác định mức độ hiển thị của một Trip.
 * Dùng để kiểm soát ai được phép xem trip trong endpoint
 * GET /api/v1/users/{email}/trips và các endpoint liên quan.
 *
 * - PUBLIC: bất kỳ user đã đăng nhập nào cũng xem được
 * - FRIENDS_ONLY: chỉ owner và bạn bè của owner mới xem được
 * - PRIVATE: chỉ owner và ADMIN mới xem được
 */
public enum TripVisibility {
    PUBLIC,
    FRIENDS_ONLY,
    PRIVATE
}
