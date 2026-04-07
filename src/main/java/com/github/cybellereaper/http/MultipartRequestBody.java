package com.github.cybellereaper.http;

import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

final class MultipartRequestBody {
    private final String boundary = "----------------" + UUID.randomUUID();
    private final List<byte[]> chunks = new ArrayList<>();

    String boundary() {
        return boundary;
    }

    MultipartRequestBody addJsonPart(String name, String json) {
        String safeName = sanitizeQuotedHeaderValue(name, "name");
        String header = "--" + boundary + "\r\n"
                + "Content-Disposition: form-data; name=\"" + safeName + "\"\r\n"
                + "Content-Type: application/json\r\n\r\n";
        chunks.add(header.getBytes(StandardCharsets.UTF_8));
        chunks.add(json.getBytes(StandardCharsets.UTF_8));
        chunks.add("\r\n".getBytes(StandardCharsets.UTF_8));
        return this;
    }

    MultipartRequestBody addFilePart(String fieldName, String fileName, String contentType, byte[] content) {
        String safeFieldName = sanitizeQuotedHeaderValue(fieldName, "fieldName");
        String safeFileName = sanitizeQuotedHeaderValue(fileName, "fileName");
        String safeContentType = sanitizeContentType(contentType);
        String header = "--" + boundary + "\r\n"
                + "Content-Disposition: form-data; name=\"" + safeFieldName + "\"; filename=\"" + safeFileName + "\"\r\n"
                + "Content-Type: " + safeContentType + "\r\n\r\n";
        chunks.add(header.getBytes(StandardCharsets.UTF_8));
        chunks.add(content);
        chunks.add("\r\n".getBytes(StandardCharsets.UTF_8));
        return this;
    }

    HttpRequest.BodyPublisher toPublisher() {
        chunks.add(("--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));
        return HttpRequest.BodyPublishers.ofByteArrays(chunks);
    }

    private static String sanitizeQuotedHeaderValue(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value
                .replace("\r", "_")
                .replace("\n", "_")
                .replace("\"", "_")
                .replace("\0", "_");
    }

    private static String sanitizeContentType(String contentType) {
        if (contentType == null || contentType.isBlank()) {
            return "application/octet-stream";
        }
        String sanitized = contentType
                .replace("\r", "")
                .replace("\n", "")
                .replace("\0", "");
        return sanitized.isBlank() ? "application/octet-stream" : sanitized;
    }
}
