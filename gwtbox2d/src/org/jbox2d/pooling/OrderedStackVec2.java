package org.jbox2d.pooling;

import org.jbox2d.common.Vec2;

public class OrderedStackVec2 extends OrderedStack<Vec2> {

  public OrderedStackVec2(int argStackSize, int argContainerSize) {
    super(argStackSize, argContainerSize);
    pool = new Vec2[argStackSize];
    for (int i = 0; i < argStackSize; i++) {
      pool[i] = new Vec2();
    }
    container = new Vec2[argContainerSize];
    for (int i = 0; i < argContainerSize; i++) {
      container[i] = new Vec2();
    }
  }

  @Override
  protected Vec2[] createArray(int argSize, Vec2[] argOld) {
    if (argOld != null) {
      Vec2[] sk = new Vec2[argSize];
      for (int i = 0; i < argOld.length; i++) {
        sk[i] = argOld[i];
      }
      for (int i = argOld.length; i < argSize; i++) {
        sk[i] = new Vec2();
      }
      return sk;
    } else {
      Vec2[] sk = new Vec2[argSize];
      for (int i = 0; i < argSize; i++) {
        sk[i] = new Vec2();
      }
      return sk;
    }
  }

}
