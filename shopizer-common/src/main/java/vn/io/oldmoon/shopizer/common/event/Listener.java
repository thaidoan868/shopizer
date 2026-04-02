package vn.io.oldmoon.shopizer.common.event;

public interface Listener {
  /**
   * Handles incoming messages.
   *
   * @param event The event received from the queue.
   */
  void handle(ApplicationEvent event);
}
