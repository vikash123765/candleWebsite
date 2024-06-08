package com.vikash.mobileCaseBackend.utils;


import java.util.UUID;
public class GuestSessionTokenGenerator {
    public static String generateSessionToken() {
        return UUID.randomUUID().toString();
    }
}
