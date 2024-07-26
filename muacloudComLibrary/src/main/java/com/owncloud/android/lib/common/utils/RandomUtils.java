
package com.owncloud.android.lib.common.utils;

import java.util.Random;
import java.util.UUID;


public class RandomUtils {

    private static final String CANDIDATECHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ" +
            "1234567890-+/_=.:";


    public static String generateRandomString(int length) {
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            sb.append(CANDIDATECHARS.charAt(random.nextInt(CANDIDATECHARS.length())));
        }

        return sb.toString();
    }


    public static int generateRandomInteger(int min, int max) {
        Random r = new Random();
        return r.nextInt(max - min) + min;
    }


    public static String generateRandomUUID() {
        return UUID.randomUUID().toString();
    }
}