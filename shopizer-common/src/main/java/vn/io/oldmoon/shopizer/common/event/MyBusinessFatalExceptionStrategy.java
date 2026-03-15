package vn.io.oldmoon.shopizer.common.event;

public class MyBusinessFatalExceptionStrategy
    extends ConditionalRejectingErrorHandler.DefaultExceptionStrategy {
  @Override
  public boolean isUserCauseFatal(Throwable cause) {
    // Add your specific business exceptions that should NEVER be retried
    return cause instanceof MyPermanentBusinessException
        || cause instanceof IllegalStateException
        || super.isUserCauseFatal(cause);
  }
}
