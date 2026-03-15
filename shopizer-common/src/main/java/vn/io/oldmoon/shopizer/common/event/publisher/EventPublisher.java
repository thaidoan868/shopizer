package vn.io.oldmoon.shopizer.common.event.publisher;

public interface EventPublisher {

  boolean supports(MessageDescriptor<?> descriptor);

  void publish(MessageDescriptor<?> descriptor, ApplicationEvent event);
}
