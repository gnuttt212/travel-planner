/**
 * Module Dat dich vu (Booking) - dong vai tro Gateway/Adapter goi ra
 * cac nha cung cap ben thu ba (ve may bay, khach san).
 *
 * Vi day la du an portfolio, KHONG tich hop API that cua hang ve/khach
 * san (can hop dong doi tac, rat phuc tap). Thay vao do, hay tao mot
 * FakeBookingProvider implement chung mot interface BookingProvider,
 * de van the hien duoc thiet ke Adapter Pattern ma khong can API that.
 *
 * Goi y cau truc:
 *   - BookingProvider (interface): search(), book(), cancel()
 *   - FakeFlightProvider, FakeHotelProvider (implements BookingProvider)
 *   - BookingService goi qua interface, khong phu thuoc provider cu the
 *     -> de sau nay thay bang API that (Skyscanner, Booking.com...) ma
 *        khong sua code goi noi (Open/Closed Principle)
 */
package com.travelplanner.booking;
