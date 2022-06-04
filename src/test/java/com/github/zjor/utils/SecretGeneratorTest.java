package com.github.zjor.utils;

import org.junit.Assert;
import org.junit.Test;

public class SecretGeneratorTest {

    @Test
    public void shouldGenerateNext() {
        var secret = SecretGenerator.next();
        Assert.assertTrue(secret.length() >= SecretGenerator.MIN_LENGTH);
    }

}