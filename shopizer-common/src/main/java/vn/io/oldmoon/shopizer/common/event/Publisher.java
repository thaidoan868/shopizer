package vn.io.oldmoon.shopizer.common.event;

public interface Publisher {
  /**
   * Publishes an event to the message broker.
   *
   * @param event The event data to send.
   */
  void publish(ApplicationEvent event);
}
