package vn.io.oldmoon.shopizer.common.core.event;

public interface EventPublisher {
  /**
   * Publishes an event to the message broker.
   *
   * @param rabbitMqEvent The event wrapper containing the payload and routing key.
   */
  void publish(RabbitMqEvent rabbitMqEvent);
}


