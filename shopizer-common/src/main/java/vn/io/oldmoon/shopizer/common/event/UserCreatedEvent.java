package vn.io.oldmoon.shopizer.common.event;

public record UserCreatedEvent(
    String userId, String email, String username, String firstName, String lastName)
    implements ApplicationEvent {}
