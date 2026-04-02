package vn.io.oldmoon.shopizer.common.event;

public record UserCreatedEvent(String userId, String email) implements ApplicationEvent {}
