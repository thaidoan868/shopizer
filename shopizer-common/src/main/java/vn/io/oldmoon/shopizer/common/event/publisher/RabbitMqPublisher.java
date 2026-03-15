package vn.io.oldmoon.shopizer.common.event.publisher;

import vn.io.oldmoon.shopizer.common.event.RabbitMessageDescriptor;

@Service
public class RabbitMqPublisher implements EventPublisher {

  private final RabbitTemplate rabbitTemplate;

  public RabbitMqPublisher(RabbitTemplate rabbitTemplate) {
    this.rabbitTemplate = rabbitTemplate;
  }

  @Override
  public boolean supports(MessageDescriptor descriptor) {
    return descriptor instanceof RabbitMessageDescriptor;
  }

  @Override
  public void publish(RabbitMessageDescriptor<?> descriptor, ApplicationEvent event) {
    rabbitTemplate.convertAndSend(descriptor.exchange(), descriptor.routingKey(), event);
  }
}
