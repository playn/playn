package playn.core.gl;

import playn.core.AbstractLayer;
import playn.core.Transform;

public abstract class LayerGL extends AbstractLayer {
  private final Transform savedLocal = new Transform();
  
  protected Transform localTransform(Transform parentTransform) {
    savedLocal.copy(parentTransform);
    savedLocal.translate(originX, originY);
    savedLocal.transform(transform.m00(), transform.m01(), transform.m10(),
        transform.m11(), transform.tx() - originX, transform.ty() - originY);
    savedLocal.translate(-originX, -originY);
    return savedLocal;
  }
  
  public abstract void paint(Transform parentTransform, float parentAlpha);

}
