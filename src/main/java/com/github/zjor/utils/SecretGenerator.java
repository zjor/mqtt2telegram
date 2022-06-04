package com.github.zjor.utils;

import org.hashids.Hashids;

import java.util.Random;

public class SecretGenerator {

    public static final int MIN_LENGTH = 24;

    private static final Hashids hashIds;
    private static final Random random;

    static {
        var now = System.currentTimeMillis();
        random = new Random(now);
        hashIds = new Hashids(String.valueOf(now), MIN_LENGTH);
    }

    public static String next() {
        return hashIds.encode(System.currentTimeMillis() + Math.abs(random.nextLong()) % 10000);
    }
}
