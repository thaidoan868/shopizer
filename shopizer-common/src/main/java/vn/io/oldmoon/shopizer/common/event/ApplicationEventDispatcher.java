package vn.io.oldmoon.shopizer.common.event;

public class ApplicationEventDispatcher {

  private final List<EventPublisher> publishers;

  public ApplicationEventDispatcher(
      List<EventPublisher> publishers, List<MessageDescriptor<?>> descriptors) {
    this.publishers = publishers;

    this.descriptors =
        descriptors.stream().collect(Collectors.toMap(MessageDescriptor::eventType, d -> d));
  }

  public void dispatch(ApplicationEvent event) {

    MessageDescriptor descriptor = descriptors.get(event.getClass());

    if (descriptor == null) {
      throw new RuntimeException("No descriptor for " + event.getClass());
    }

    for (EventPublisher publisher : publishers) {
      if (publisher.supports(descriptor)) {
        publisher.publish(descriptor, event);
      }
    }
  }
}
