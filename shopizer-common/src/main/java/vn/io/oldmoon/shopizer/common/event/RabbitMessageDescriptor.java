package vn.io.oldmoon.shopizer.common.event;

public record RabbitMessageDescriptor<E extends ApplicationEvent>(
    Class<E> eventClass, String exchange, String routingKey, String deadLetterRoutingKey)
    implements MessageDescriptor {}
