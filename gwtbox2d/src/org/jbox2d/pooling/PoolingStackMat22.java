package org.jbox2d.pooling;

import org.jbox2d.common.Mat22;

public class PoolingStackMat22 extends PoolingStack<Mat22>{
  private final Mat22[] pool;
  private int index;
  private final int size;
  private final PoolContainer<Mat22> container;
  
  public PoolingStackMat22(int size) {
    this.size = size;
    this.pool = new Mat22[size];
    this.index = 0;
    this.container = new PoolContainer<Mat22>();
    
    for (int i = 0; i < size; i++) {
      pool[i] = new Mat22();
    }
  }
  
  @Override
  public Mat22 pop() {
    assert(index < size) : "End of stack reached, there is probably a leak somewhere";
    return pool[index++];
  }

  @Override @SuppressWarnings("fallthrough")
  public org.jbox2d.pooling.PoolingStack.PoolContainer<Mat22> pop(int argNum) {
    assert(index + argNum < size) : "End of stack reached, there is probably a leak somewhere";
    
    switch (argNum) {
      case 9:
        container.p8 = pool[index++];
      case 8:
        container.p7 = pool[index++];
      case 7:
        container.p6 = pool[index++];
      case 6:
        container.p5 = pool[index++];
      case 5:
        container.p4 = pool[index++];
      case 4:
        container.p3 = pool[index++];
      case 3:
        container.p2 = pool[index++];
      case 2:
        container.p1 = pool[index++];
      case 1:
        container.p0 = pool[index++];
        break;
      default:
        assert(false);
    }
    return container;
  }

  @Override
  public void push(int argNum) {
    index -= argNum;
    assert (index >= 0) : "Beginning of stack reached, push/pops are unmatched";
  }
}
