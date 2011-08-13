package org.jbox2d.pooling;

import org.jbox2d.common.Vec3;

public class OrderedStackVec3 extends OrderedStack<Vec3> {

  public OrderedStackVec3(int argStackSize, int argContainerSize) {
    super(argStackSize, argContainerSize);
    pool = new Vec3[argStackSize];
    for (int i = 0; i < argStackSize; i++) {
      pool[i] = new Vec3();
    }
    container = new Vec3[argContainerSize];
    for (int i = 0; i < argContainerSize; i++) {
      container[i] = new Vec3();
    }
  }

  @Override
  protected Vec3[] createArray(int argSize, Vec3[] argOld) {
    if (argOld != null) {
      Vec3[] sk = new Vec3[argSize];
      for (int i = 0; i < argOld.length; i++) {
        sk[i] = argOld[i];
      }
      for (int i = argOld.length; i < argSize; i++) {
        sk[i] = new Vec3();
      }
      return sk;
    } else {
      Vec3[] sk = new Vec3[argSize];
      for (int i = 0; i < argSize; i++) {
        sk[i] = new Vec3();
      }
      return sk;
    }
  }

}
