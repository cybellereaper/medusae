package com.github.cybellereaper.http;

import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MultipartRequestBodyTest {
    @Test
    void sanitizesMultipartHeadersToPreventHeaderInjection() {
        MultipartRequestBody body = new MultipartRequestBody()
                .addJsonPart("payload\r\nx", "{\"safe\":true}")
                .addFilePart("files[0]\r\nInjected: 1", "a\"\r\nb.txt", "text/plain\r\nX-Evil: 1", new byte[]{1, 2, 3});

        String rendered = publishToString(body);

        assertFalse(rendered.contains("\r\nInjected: 1"));
        assertFalse(rendered.contains("\r\nX-Evil: 1"));
        assertTrue(rendered.contains("filename=\"a___b.txt\""));
    }

    private static String publishToString(MultipartRequestBody body) {
        HttpBodyCollector collector = new HttpBodyCollector();
        body.toPublisher().subscribe(collector);
        collector.await();
        return new String(collector.joinedBytes(), StandardCharsets.UTF_8);
    }

    private static final class HttpBodyCollector implements Flow.Subscriber<ByteBuffer> {
        private final CompletableFuture<Void> completed = new CompletableFuture<>();
        private final List<byte[]> buffers = new ArrayList<>();

        @Override
        public void onSubscribe(Flow.Subscription subscription) {
            subscription.request(Long.MAX_VALUE);
        }

        @Override
        public void onNext(ByteBuffer item) {
            byte[] bytes = new byte[item.remaining()];
            item.get(bytes);
            buffers.add(bytes);
        }

        @Override
        public void onError(Throwable throwable) {
            completed.completeExceptionally(throwable);
        }

        @Override
        public void onComplete() {
            completed.complete(null);
        }

        void await() {
            completed.join();
        }

        byte[] joinedBytes() {
            int total = buffers.stream().mapToInt(b -> b.length).sum();
            byte[] out = new byte[total];
            int offset = 0;
            for (byte[] buffer : buffers) {
                System.arraycopy(buffer, 0, out, offset, buffer.length);
                offset += buffer.length;
            }
            return out;
        }
    }
}
