package com.github.cybellereaper.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

class DiscordAttachmentSecurityTest {
    @Test
    void constructorDefensivelyCopiesContent() {
        byte[] original = new byte[]{1, 2, 3};
        DiscordAttachment attachment = new DiscordAttachment("safe.txt", "text/plain", original);

        original[0] = 99;
        assertArrayEquals(new byte[]{1, 2, 3}, attachment.content());
    }

    @Test
    void contentAccessorReturnsCopy() {
        DiscordAttachment attachment = new DiscordAttachment("safe.txt", "text/plain", new byte[]{1, 2, 3});

        byte[] firstRead = attachment.content();
        byte[] secondRead = attachment.content();
        firstRead[0] = 42;

        assertNotSame(firstRead, secondRead);
        assertArrayEquals(new byte[]{1, 2, 3}, secondRead);
    }
}
