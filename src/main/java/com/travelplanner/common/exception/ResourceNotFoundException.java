package com.travelplanner.common.exception;

/**
 * Nem ra khi mot resource (Destination, Trip, User...) khong ton tai.
 * Duoc GlobalExceptionHandler bat va chuyen thanh HTTP 404.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public static ResourceNotFoundException of(String resourceName, String id) {
        return new ResourceNotFoundException(resourceName + " khong ton tai voi id: " + id);
    }
}
