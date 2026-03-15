package vn.io.oldmoon.shopizer.user.business.event.user;

public record UserCreatedEvent(String userId, String email) implements ApplicationEvent {}
