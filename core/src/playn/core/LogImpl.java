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

/**
 * A common base class for {@link Log} implementations.
 */
public abstract class LogImpl implements Log {

  private Collector collector;
  private Level minLevel = Level.DEBUG;

  @Override
  public void setCollector(Collector collector) {
    this.collector = collector;
  }

  @Override
  public void setMinLevel(Level level) {
    assert level != null;
    minLevel = level;
  }

  @Override
  public void debug(String msg) {
    debug(msg, null);
  }

  @Override
  public void debug(String msg, Throwable e) {
    log(Level.DEBUG, msg, e);
  }

  @Override
  public void info(String msg) {
    info(msg, null);
  }

  @Override
  public void info(String msg, Throwable e) {
    log(Level.INFO, msg, e);
  }

  @Override
  public void warn(String msg) {
    warn(msg, null);
  }

  @Override
  public void warn(String msg, Throwable e) {
    log(Level.WARN, msg, e);
  }

  @Override
  public void error(String msg) {
    error(msg, null);
  }

  @Override
  public void error(String msg, Throwable e) {
    log(Level.ERROR, msg, e);
  }

  protected void log(Level level, String msg, Throwable e) {
    if (collector != null)
      collector.logged(level, msg, e);
    if (level.ordinal() >= minLevel.ordinal())
      logImpl(level, msg, e);
  }

  protected abstract void logImpl(Level level, String msg, Throwable e);
}
