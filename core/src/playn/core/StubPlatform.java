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

/**
 * A stub implementation of {@link Platform} that provides implementations of those services that
 * can be usefully implemented for unit tests, and throws {@link UnsupportedOperationException} for
 * the rest. This can usefully be extended in tests to provide test implementations for just the
 * aspects of the platform that are needed to support the code under test.
 *
 * <p> The services that are implemented are:
 * <ul><li> {@link #type} - reports {@link Platform.Type#STUB}
 * <li> {@link #time} - returns current time
 * <li> {@link #invokeLater} - invokes the supplied runnable immediately on the calling thread
 * <li> {@link #input} - allows listener registration, never generates events
 * <li> {@link #log} - writes logs to {@code stderr}
 * <li> {@link #json} - provides full JSON parsing and formatting
 * <li> {@link #storage} - maintains an in-memory storage map
 * </ul>
 */
public class StubPlatform extends Platform {

  private Storage storage = new Storage() {
    private final Map<String,String> _data = new HashMap<String,String>();

    @Override public void setItem (String key, String data) throws RuntimeException {
      _data.put(key, data);
    }
    @Override public void removeItem (String key) {
      _data.remove(key);
    }
    @Override public String getItem (String key) {
      return _data.get(key);
    }
    @Override public Batch startBatch () {
      return new BatchImpl(this);
    }
    @Override public Iterable<String> keys () {
      return _data.keySet();
    }
    @Override public boolean isPersisted () {
      return true;
    }
  };

  private Input input = new Input(this);
  private Json json = new JsonImpl();
  private Log log = new Log() {
    @Override
    protected void logImpl (Level level, String msg, Throwable e) {
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
  private Exec exec = new Exec.Default(this) {
    @Override public void invokeLater (Runnable action) { action.run(); } // now is later!
  };
  private final long start = System.currentTimeMillis();

  @Override public Platform.Type type () {
    return Platform.Type.STUB;
  }

  @Override public double time () {
    return (double)System.currentTimeMillis();
  }
  @Override public int tick () {
    return (int)(System.currentTimeMillis() - start);
  }

  @Override public void openURL (String url) { throw new UnsupportedOperationException(); }

  @Override public Assets assets () { throw new UnsupportedOperationException(); }
  @Override public Audio audio () { throw new UnsupportedOperationException(); }
  @Override public Graphics graphics () { throw new UnsupportedOperationException(); }
  @Override public Net net () { throw new UnsupportedOperationException(); }

  @Override public Exec exec () { return exec; }
  @Override public Input input () { return input; }
  @Override public Json json () { return json; }
  @Override public Log log () { return log; }
  @Override public Storage storage () { return storage; }
}
