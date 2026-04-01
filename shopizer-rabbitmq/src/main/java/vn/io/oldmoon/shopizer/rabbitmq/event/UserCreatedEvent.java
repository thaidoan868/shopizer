package vn.io.oldmoon.shopizer.rabbitmq.event;

public record UserCreatedEvent(String userId, String email) implements ApplicationEvent {}
