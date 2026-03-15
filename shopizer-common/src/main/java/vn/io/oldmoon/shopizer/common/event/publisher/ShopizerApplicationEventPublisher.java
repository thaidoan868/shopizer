package vn.io.oldmoon.shopizer.common.event.publisher;

public interface ShopizerApplicationEventPublisher {

  void publishEvent(ApplicationEvent applicationEvent);

  void publishEventUnsynchronized(ApplicationEvent applicationEvent);
}
