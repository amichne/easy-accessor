package io.amichne.easy.accessor.exceptions.handlers;

import io.amichne.easy.accessor.exceptions.EasyAccessError;

public class HandlerAlreadyRegisteredError extends EasyAccessError {
  public HandlerAlreadyRegisteredError(String operationId) {
    super("There is an existing handler registered for the operationId=" + operationId);
  }
}
