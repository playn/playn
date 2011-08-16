package org.jbox2d.pooling;

import org.jbox2d.collision.AABB;

public class OrderedStackAABB extends OrderedStack<AABB> {

  public OrderedStackAABB(int argStackSize, int argContainerSize) {
    super(argStackSize, argContainerSize);
    pool = new AABB[argStackSize];
    for (int i = 0; i < argStackSize; i++) {
      pool[i] = new AABB();
    }
    container = new AABB[argContainerSize];
    for (int i = 0; i < argContainerSize; i++) {
      container[i] = new AABB();
    }
  }

  @Override
  protected AABB[] createArray(int argSize, AABB[] argOld) {
    if (argOld != null) {
      AABB[] sk = new AABB[argSize];
      for (int i = 0; i < argOld.length; i++) {
        sk[i] = argOld[i];
      }
      for (int i = argOld.length; i < argSize; i++) {
        sk[i] = new AABB();
      }
      return sk;
    } else {
      AABB[] sk = new AABB[argSize];
      for (int i = 0; i < argSize; i++) {
        sk[i] = new AABB();
      }
      return sk;
    }
  }

}
