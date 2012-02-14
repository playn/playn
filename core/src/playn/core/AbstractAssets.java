/**
 * Copyright 2010 The PlayN Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package playn.core;

public abstract class AbstractAssets implements Assets {

  private int totalRequestsCount = 0;
  private int successCount = 0;
  private int errorsCount = 0;

  @SuppressWarnings("rawtypes")
  private ResourceCallback callback = new ResourceCallback() {
    @Override
    public void done(Object resource) {
      ++successCount;
    }

    @Override
    public void error(Throwable e) {
      ++errorsCount;
    }
  };

  @SuppressWarnings("unchecked")
  @Override
  public final Image getImage(String path) {
    incrementRequestCount();
    Image image = doGetImage(path);
    image.addCallback(callback);
    return image;
  }

  protected abstract Image doGetImage(String path);

  @Override
  public final Sound getSound(String path) {
    incrementRequestCount();
    Sound sound = doGetSound(path);
    sound.addCallback(callback);
    return sound;
  }

  protected abstract Sound doGetSound(String path);

  @Override
  public void getText(String path, ResourceCallback<String> callback) {
    doGetText(path, callback);
  }

  protected abstract void doGetText(String path, ResourceCallback<String> callback);

  @Override
  public final boolean isDone() {
    boolean done = (this.totalRequestsCount == this.errorsCount + this.successCount);
    return done;
  }

  @Override
  public final int getPendingRequestCount() {
    return this.totalRequestsCount - this.errorsCount - this.successCount;
  }

  private void incrementRequestCount() {
    ++totalRequestsCount;
  }
}
