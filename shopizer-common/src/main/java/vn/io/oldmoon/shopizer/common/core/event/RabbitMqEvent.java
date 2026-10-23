package vn.io.oldmoon.shopizer.common.core.event;

public record RabbitMqEvent(Object event, String routingKey) {}

