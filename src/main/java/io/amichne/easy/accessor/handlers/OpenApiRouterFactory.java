package io.amichne.easy.accessor.handlers;

import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;

import io.amichne.easy.accessor.exceptions.handlers.HandlerAlreadyRegisteredError;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.rxjava3.core.Completable;
import io.vertx.core.Handler;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.core.http.HttpServerResponse;
import io.vertx.rxjava3.ext.web.RoutingContext;
import io.vertx.rxjava3.ext.web.openapi.RouterBuilder;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import javax.validation.constraints.NotNull;

public class OpenApiRouterFactory {
  @NotNull
  private final Vertx vertx;
  @NotNull
  private final String sourceFile;
  @NotNull
  private final Map<String, Consumer<RoutingContext>> handlerMap;
  @NotNull
  private final Consumer<RoutingContext> defaultFailureHandler = context -> {
    HttpResponseStatus internalServerError = INTERNAL_SERVER_ERROR;
    HttpServerResponse response = context.response();
    response.headers().addAll(context.request().headers());
    response
        .setStatusMessage(internalServerError.reasonPhrase())
        .setStatusCode(internalServerError.code())
        .end()
        .subscribe();
  };
  private final Consumer<RoutingContext> customFailureHandler;

  private OpenApiRouterFactory(Builder builder) {
    vertx = builder.vertx;
    sourceFile = builder.sourceFile;
    customFailureHandler = builder.customFailureHandler;
    handlerMap = new HashMap<>();
  }

  public static Builder builder() {
    return new Builder();
  }

  public void registerRoute(@NotNull String operationId,
                            @NotNull Consumer<RoutingContext> handler) {
    if (!handlerMap.containsKey(operationId)) {
      handlerMap.put(operationId, handler);
    } else {
      throw new HandlerAlreadyRegisteredError(operationId);
    }
  }

  public Completable load() {
    return Completable.create(
        emitter -> RouterBuilder.create(vertx, sourceFile)
            .doOnSuccess(this::addHandlers)
            .subscribe(routerBuilder -> emitter.onComplete(), emitter::onError)
            .dispose());
  }

  private void addHandlers(RouterBuilder routerBuilder) {
    for (String key : handlerMap.keySet()) {
      routerBuilder
          .operation(key)
          .handler(routingContext -> handlerMap.get(key).accept(routingContext))
          .failureHandler(getFailureHandler());
    }
  }

  private Handler<RoutingContext> getFailureHandler() {
    return (customFailureHandler == null ? defaultFailureHandler : customFailureHandler)::accept;
  }

  public static final class Builder {
    private @NotNull Vertx vertx;
    private @NotNull String sourceFile;
    private Consumer<RoutingContext> customFailureHandler;

    private Builder() {
    }

    public Builder withVertx(@NotNull Vertx val) {
      vertx = val;
      return this;
    }

    public Builder withSourceFile(@NotNull String val) {
      sourceFile = val;
      return this;
    }

    public Builder withCustomFailureHandler(Consumer<RoutingContext> customFailureHandler) {
      this.customFailureHandler = customFailureHandler;
      return this;
    }

    public OpenApiRouterFactory build() {
      return new OpenApiRouterFactory(this);
    }
  }
}
