package vn.io.oldmoon.shopizer.common.event;

public interface ApplicationEventListener<T extends ApplicationEvent> {
  /**
   * Handles incoming messages.
   *
   * @param event The event received from the queue.
   */
  void handle(T event) throws NoSuchMethodException;
}
