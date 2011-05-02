package forplay.core;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractCachingAssetManager extends AbstractAssetManager {
  
  Map<String, Object> cache = new HashMap<String, Object>();

  @Override
  protected final Image doGetImage(String path) {
    Object object = null;
    if ((object = cache.get(path)) == null) {
      object = loadImage(path);
      cache.put(path, object);
    }

    return (Image) object;
  }

  protected abstract Image loadImage(String path);

  @Override
  protected final Sound doGetSound(String path) {
    Object object = null;
    if ((object = cache.get(path)) == null) {
      object = loadSound(path);
      cache.put(path, object);
    }

    return (Sound) object;
  }

  protected abstract Sound loadSound(String path);
}
