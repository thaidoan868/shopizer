package vn.io.oldmoon.shopizer.user.business.event;

public class MessageRegistry {
  public static final String EXCHANGE = "app.main.exchange";

  // Define your mappings here
  public static final MessageDescriptor<UserRegisteredEvent> USER_CREATED =
      new MessageDescriptor<>("user.event.created", UserRegisteredEvent.class);

  public static final MessageDescriptor<OrderPlacedEvent> ORDER_PLACED =
      new MessageDescriptor<>("order.event.placed", OrderPlacedEvent.class);
}
