package vn.io.oldmoon.shopizer.common.event;

public interface ApplicationEventPublisher<T extends ApplicationEvent> {
  /**
   * Publishes an event to the message broker.
   *
   * @param event The event data to send.
   */
  void publish(T event);
}
