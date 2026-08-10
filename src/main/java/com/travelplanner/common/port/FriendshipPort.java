package com.travelplanner.common.port;

public interface FriendshipPort {
    /**
     * Kiểm tra xem hai user có phải là bạn bè hay không.
     * @param emailA Email của người thứ nhất
     * @param emailB Email của người thứ hai
     * @return true nếu là bạn bè, false nếu không
     */
    boolean areFriends(String emailA, String emailB);
}
