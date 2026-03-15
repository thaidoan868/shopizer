package vn.io.oldmoon.shopizer.common.event.publisher;

public record MessageDescriptor<T>(
    String routingKey, String deadLetterRoutingKey, Class<T> payloadType) {}
