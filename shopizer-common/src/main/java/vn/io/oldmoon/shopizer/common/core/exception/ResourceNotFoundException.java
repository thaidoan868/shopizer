package vn.io.oldmoon.shopizer.common.core.exception;

/** findById(id) returns no results. */
public class ResourceNotFoundException extends ServiceException {
  public ResourceNotFoundException(String resourceName, String id) {
    super(String.format("Resource %s with identifier [%s] was not found.", resourceName, id));
  }
}
