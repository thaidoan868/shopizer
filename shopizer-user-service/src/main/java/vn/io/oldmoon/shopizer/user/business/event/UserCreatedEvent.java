package vn.io.oldmoon.shopizer.user.business.event;

import vn.io.oldmoon.shopizer.common.event.ApplicationEvent;

public record UserCreatedEvent(
    String userId, String email, String username, String firstName, String lastName)
    implements ApplicationEvent {}
