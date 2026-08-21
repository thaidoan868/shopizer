package vn.io.oldmoon.shopizer.common.event;

public interface EventPublisher {
  /**
   * Publishes an event to the message broker.
   *
   * @param event The event data to send.
   */
  void publish(Object event, String routingKey);
}
