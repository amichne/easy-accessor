package io.amichne.easy.accessor.accessors;

import io.amichne.easy.accessor.pojos.requests.UniqueRequest;
import io.reactivex.rxjava3.core.Single;

public interface Accessor<T> {
  public Single<T> get(UniqueRequest request);
}
