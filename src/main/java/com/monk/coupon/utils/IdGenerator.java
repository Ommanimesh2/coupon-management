package com.monk.coupon.utils;

import org.apache.commons.lang3.RandomStringUtils;

import java.time.Instant;
import java.util.Random;

public class IdGenerator {

    public static String generateTimeBasedUniqueId(String prefix) {
        long epoch = Instant.now().getEpochSecond();
        return String.format("%s%d_%d", prefix, epoch, new Random().nextInt(1000));
    }

    public static String generateId(String prefix) {
        return prefix + RandomStringUtils.randomAlphanumeric(10);
    }
}