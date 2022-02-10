package io.amichne.easy.accessor.exceptions;

/**
 * The base exception class for all custom exceptions defined by this library
 */
public abstract class EasyAccessError extends RuntimeException {
  public EasyAccessError() {
  }

  public EasyAccessError(String message) {
    super(message);
  }

  public EasyAccessError(String message, Throwable cause) {
    super(message, cause);
  }

  public EasyAccessError(Throwable cause) {
    super(cause);
  }
}
