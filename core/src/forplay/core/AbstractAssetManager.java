package forplay.core;

public abstract class AbstractAssetManager implements AssetManager {
  
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
    Sound sound = doGetSound(path);
    // TODO
    // incrementRequestCount();
    // sound.setCallback(callback);
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
