package vn.io.oldmoon.shopizer.user.business.event.registration;

import vn.io.oldmoon.shopizer.common.event.ApplicationEvent;

public record CustomerCreatedEvent(String keycloakUserId) implements ApplicationEvent {}
