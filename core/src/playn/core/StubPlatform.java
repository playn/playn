/**
 * Copyright 2012 The PlayN Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package playn.core;

import java.util.HashMap;
import java.util.Map;

import playn.core.json.JsonImpl;
import playn.core.util.Callback;

/**
 * A stub implementation of {@link Platform} that provides implementations of those services that
 * can be usefully implemented for unit tests, and throws {@link UnsupportedOperationException} for
 * the rest. This can usefully be extended in tests to provide test implementations for just the
 * aspects of the platform that are needed to support the code under test.
 *
 * <p> The services that are implemented are:
 * <ul><li> {@link #type} - reports {@link Platform.Type#STUB}
 * <li> {@link #time} - returns current time
 * <li> {@link #random} - returns {@link Math#random}
 * <li> {@link #invokeLater} - invokes the supplied runnable immediately on the calling thread
 * <li> {@link #setLifecycleListener} - tracks the supplied listener, never generates events
 * <li> {@link #log} - writes logs to {@code stderr}
 * <li> {@link #storage} - maintains an in-memory storage map
 * <li> {@link #json} - provides full JSON parsing and formatting
 * <li> {@link #pointer} - allows listener registration, never generates events
 * <li> {@link #mouse} - allows listener registration, never generates events
 * <li> {@link #touch} - allows listener registration, never generates events
 * <li> {@link #keyboard} - allows listener registration, never generates events
 * </ul>
 */
public class StubPlatform implements Platform {

  private final Log log = new LogImpl() {
    @Override
    protected void logImpl(Level level, String msg, Throwable e) {
      String prefix;
      switch (level) {
      default:
      case DEBUG: prefix = "D: "; break;
      case INFO: prefix = ""; break;
      case WARN: prefix = "W: "; break;
      case ERROR: prefix = "E: "; break;
      }
      System.err.println(prefix + msg);
      if (e != null)
        e.printStackTrace(System.err);
    }
  };

  private Storage storage = new Storage() {
    private final Map<String,String> _data = new HashMap<String,String>();

    @Override public void setItem(String key, String data) throws RuntimeException {
      _data.put(key, data);
    }
    @Override public void removeItem(String key) {
      _data.remove(key);
    }
    @Override public String getItem(String key) {
      return _data.get(key);
    }
    @Override public Batch startBatch() {
      return new BatchImpl(this);
    }
    @Override public Iterable<String> keys() {
      return _data.keySet();
    }
    @Override public boolean isPersisted() {
      return true;
    }
  };

  private Keyboard keyboard = new Keyboard() {
    @Override public void setListener(Listener listener) {} // noop
    @Override public boolean hasHardwareKeyboard() { return false; }
    @Override
    public void getText(Keyboard.TextType textType, String label, String initialValue,
                        Callback<String> callback) {
      callback.onSuccess(null);
    }
  };

  private Touch touch = new TouchStub();
  private Mouse mouse = new MouseStub();
  private Json json = new JsonImpl();
  private Pointer pointer = new PointerImpl() {};
  private final long start = System.currentTimeMillis();

  protected PlayN.LifecycleListener _lifecycleListener;

  @Override
  public void run(Game game) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Platform.Type type() {
    return Platform.Type.STUB;
  }

  @Override
  public double time() {
    return (double) System.currentTimeMillis();
  }

  @Override
  public int tick() {
    return (int)(System.currentTimeMillis() - start);
  }

  @Override
  public float random() {
    return (float) Math.random();
  }

  @Override
  public void openURL(String url) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setPropagateEvents(boolean propagate) {
  }

  @Override
  public void invokeLater(Runnable runnable) {
    runnable.run();
  }

  @Override
  public void setLifecycleListener(PlayN.LifecycleListener listener) {
    _lifecycleListener = listener;
  }

  @Override
  public Audio audio() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Graphics graphics() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Assets assets() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Json json() {
    return json;
  }

  @Override
  public Keyboard keyboard() {
    return keyboard;
  }

  @Override
  public Log log() {
    return log;
  }

  @Override
  public Net net() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Pointer pointer() {
    return pointer;
  }

  @Override
  public Mouse mouse() {
    return mouse;
  }

  @Override
  public Touch touch() {
    return touch;
  }

  @Override
  public Storage storage() {
    return storage;
  }

  @Override
  public Analytics analytics() {
    throw new UnsupportedOperationException();
  }

  @Override
  public RegularExpression regularExpression() {
    throw new UnsupportedOperationException();
  }
}
