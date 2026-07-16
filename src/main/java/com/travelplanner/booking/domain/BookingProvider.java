package com.travelplanner.booking.domain;

/**
 * Interface dinh nghia kha nang dat dich vu (Adapter Pattern).
 *
 * Moi nha cung cap (flight, hotel, car rental...) chi can implement
 * interface nay. BookingService se goi qua interface, khong phu thuoc
 * vao implementation cu the -> de dang thay the hoac them nha cung cap moi.
 */
public interface BookingProvider {

    /**
     * Dat dich vu.
     *
     * @param request thong tin dat dich vu (diem di, diem den, ngay, so nguoi...)
     * @return ket qua dat dich vu (ma xac nhan, trang thai, gia...)
     */
    BookingResult book(BookingRequest request);

    /**
     * Loai dich vu ma provider nay ho tro.
     */
    String getProviderType();
}
