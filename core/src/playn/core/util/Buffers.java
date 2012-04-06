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
package playn.core.util;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

public class Buffers {

  public static ByteBuffer allocateNativeByteBuffer(int size) {
    return ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder());
  }

  public static IntBuffer allocateNativeIntBuffer(int size) {
    return allocateNativeByteBuffer(size * 4).asIntBuffer();
  }

  public static FloatBuffer allocateNativeFloatBuffer(int size) {
    return allocateNativeByteBuffer(size * 4).asFloatBuffer();
  }

  public static ShortBuffer allocateNativeShortBuffer(int size) {
    return allocateNativeByteBuffer(size * 2).asShortBuffer();
  }

  public static int getElementSize(Buffer buffer) {
      if ((buffer instanceof FloatBuffer) || (buffer instanceof IntBuffer)) {
          return 4;
      } else if (buffer instanceof ShortBuffer) {
          return 2;
      } else if (buffer instanceof ByteBuffer) {
          return 1;
      } else {
          throw new RuntimeException("Unrecognized buffer type: " + buffer.getClass());
      }
  }

  public static String toString(FloatBuffer buf) {
    StringBuilder sb = new StringBuilder("[");
    int pos = buf.position();
    int count = buf.remaining();
    if (count > 100) {
      count = 100;
    }
    for (int i = 0; i < count; i++) {
      if (i > 0) {
        sb.append(", ");
      }
      sb.append(buf.get(pos + i));
    }
    if (count != buf.remaining()) {
      sb.append("...");
    }
    sb.append("]");
    return sb.toString();
  }

  public static String toString(ShortBuffer buf) {
    StringBuilder sb = new StringBuilder("[");
    int pos = buf.position();
    int count = buf.remaining();
    if (count > 100) {
      count = 100;
    }
    for (int i = 0; i < count; i++) {
      if (i > 0) {
        sb.append(", ");
      }
      sb.append(buf.get(pos + i));
    }
    if (count != buf.remaining()) {
      sb.append("...");
    }
    sb.append("]");
    return sb.toString();
  }
}
