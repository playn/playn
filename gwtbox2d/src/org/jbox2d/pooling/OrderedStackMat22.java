package org.jbox2d.pooling;

import org.jbox2d.common.Mat22;

public class OrderedStackMat22 extends OrderedStack<Mat22> {

  public OrderedStackMat22(int argStackSize, int argContainerSize) {
    super(argStackSize, argContainerSize);
    pool = new Mat22[argStackSize];
    for (int i = 0; i < argStackSize; i++) {
      pool[i] = new Mat22();
    }
    container = new Mat22[argContainerSize];
    for (int i = 0; i < argContainerSize; i++) {
      container[i] = new Mat22();
    }
  }

  @Override
  protected Mat22[] createArray(int argSize, Mat22[] argOld) {
    if (argOld != null) {
      Mat22[] sk = new Mat22[argSize];
      for (int i = 0; i < argOld.length; i++) {
        sk[i] = argOld[i];
      }
      for (int i = argOld.length; i < argSize; i++) {
        sk[i] = new Mat22();
      }
      return sk;
    } else {
      Mat22[] sk = new Mat22[argSize];
      for (int i = 0; i < argSize; i++) {
        sk[i] = new Mat22();
      }
      return sk;
    }
  }

}
