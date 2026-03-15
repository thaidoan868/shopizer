package vn.io.oldmoon.shopizer.common.event.publisher;

@Service
@Slf4j
@RequiredArgsConstructor
public class ShopizerRabbitMqEventPublisherImp implements ShopizerApplicationEventPublisher {
  private final RabbitTemplate rabbitTemplate;

  @Value("${rabbit.mq.exchange}")
  private String exchange;

  /** Publishes an event to a specific exchange with a routing key. */
  public <T> void publish(MessageDescriptor<T> descriptor, T payload) {
    try {
      rabbitTemplate.convertAndSend(exchange, descriptor.routingKey(), payload);
    } catch (Exception e) {
      log.error("Failed to publish event to RabbitMQ", e);
      // In a real app, you might want to implement a retry mechanism here
      throw new RuntimeException("Message delivery failed", e);
    }
  }

  /**
   * The magic: The compiler ensures 'payload' matches the type defined in the MessageDescriptor.
   */
  public <T> void publish(MessageDescriptor<T> descriptor, T payload) {}
}
