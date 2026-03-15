package vn.io.oldmoon.shopizer.user.business.event.listener;

@Service
@Slf4j
public class NotificationRegisterCustomerEventListener
    extends AbstractShopizerApplicationEventListener<RegisterCustomerEvent> {

  @RabbitListener(queues = MessageRegistry.ORDER_PLACED_QUEUE)
  public void handleOrder(OrderPlacedEvent event) {
    try {
      processOrder(event);
    } catch (Exception e) {
      log.error("Permanent failure for order {}. Moving to manual review.", event.getOrderId());
      // Do not re-throw if you don't want RabbitMQ to retry
      // Instead, send to a "failed-orders-queue" for a human to check
    }
  }
