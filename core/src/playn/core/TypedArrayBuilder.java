/**
 * Copyright 2011 The PlayN Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package playn.core;

import java.util.HashMap;
import java.util.Map;

/**
 * Base {@link Json.TypedArray} implementation shared among platforms.
 */
public abstract class TypedArrayBuilder<A> {
  public abstract int length(A array);
  public abstract Boolean getBoolean(A array, int index);
  public abstract Integer getInt(A array, int index);
  public abstract Double getNumber(A array, int index);
  public abstract String getString(A array, int index);
  public abstract Json.Object getObject(A array, int index);

  public <T> Json.TypedArray<T> build(final A array, Class<T> type) {
    @SuppressWarnings("unchecked") final Getter<T> getter = (Getter<T>) getters.get(type);
    if (getter == null) {
      throw new IllegalArgumentException("Only json types may be used for TypedArray, not '" +
        type.getName() + "'");
    }

    return (array == null) ? null : new Json.TypedArray<T>() {
      @Override
      public int length() {
        return TypedArrayBuilder.this.length(array);
      }

      @Override
      protected T getImpl(int index) {
        return getter.get(TypedArrayBuilder.this, array, index);
      }
    };
  }

  private interface Getter<T> {
    <A> T get(TypedArrayBuilder<A> impl, A array, int index);
  }

  private static Map<Class<?>,Getter<?>> getters = new HashMap<Class<?>,Getter<?>>();
  static {
    getters.put(Boolean.class, new Getter<Boolean>() {
      @Override
      public <A> Boolean get(TypedArrayBuilder<A> impl, A array, int index) {
        return impl.getBoolean(array, index);
      }
    });
    getters.put(Integer.class, new Getter<Integer>() {
      @Override
      public <A> Integer get(TypedArrayBuilder<A> impl, A array, int index) {
        return impl.getInt(array, index);
      }
    });
    getters.put(Double.class, new Getter<Double>() {
      @Override
      public <A> Double get(TypedArrayBuilder<A> impl, A array, int index) {
        return impl.getNumber(array, index);
      }
    });
    getters.put(String.class, new Getter<String>() {
      @Override
      public <A> String get(TypedArrayBuilder<A> impl, A array, int index) {
        return impl.getString(array, index);
      }
    });
    getters.put(Json.Object.class, new Getter<Object>() {
      @Override
      public <A> Json.Object get(TypedArrayBuilder<A> impl, A array, int index) {
        return impl.getObject(array, index);
      }
    });
  }
}
