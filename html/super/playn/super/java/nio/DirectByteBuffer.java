/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package java.nio;

import com.google.gwt.typedarrays.client.ArrayBuffer;
import com.google.gwt.typedarrays.client.ArrayBufferView;
import com.google.gwt.typedarrays.client.Int8Array;

/**
 * DirectByteBuffer, DirectReadWriteByteBuffer and DirectReadOnlyHeapByteBuffer compose the
 * implementation of direct byte buffers.
 * <p> DirectByteBuffer implements all the shared readonly methods and is extended by the other two
 * classes. </p>
 * <p> All methods are marked final for runtime performance. </p>
 */
abstract class DirectByteBuffer extends ByteBuffer implements playn.html.HasArrayBufferView {

    Int8Array byteArray;

    DirectByteBuffer (int capacity) {
        this(ArrayBuffer.create(capacity), capacity, 0);
    }

    DirectByteBuffer (ArrayBuffer buf) {
        this(buf, buf.getByteLength(), 0);
    }

    DirectByteBuffer (ArrayBuffer buffer, int capacity, int offset) {
        super(capacity);
        byteArray = Int8Array.create(buffer, offset, capacity);
    }

    public ArrayBufferView getTypedArray () {
        return byteArray;
    }

    public int getElementSize () {
        return 1;
    }

    public int getElementType() {
        return 0x1400; // GL_BYTE
    }

    /*
     * Override ByteBuffer.get(byte[], int, int) to improve performance.
     *
     * (non-Javadoc)
     *
     * @see java.nio.ByteBuffer#get(byte[], int, int)
     */
    public final ByteBuffer get (byte[] dest, int off, int len) {
        int length = dest.length;
        if (off < 0 || len < 0 || (long)off + (long)len > length) {
            throw new IndexOutOfBoundsException();
        }
        if (len > remaining()) {
            throw new BufferUnderflowException();
        }

        for (int i = 0; i < len; i++) {
            dest[i + off] = get(position + i);
        }

        position += len;
        return this;
    }

    public final byte get () {
// if (position == limit) {
// throw new BufferUnderflowException();
// }
        return (byte)byteArray.get(position++);
    }

    public final byte get (int index) {
// if (index < 0 || index >= limit) {
// throw new IndexOutOfBoundsException();
// }
        return (byte)byteArray.get(index);
    }

    public final double getDouble () {
        return Numbers.longBitsToDouble(getLong());
    }

    public final double getDouble (int index) {
        return Numbers.longBitsToDouble(getLong(index));
    }

    public final float getFloat () {
        return Numbers.intBitsToFloat(getInt());
    }

    public final float getFloat (int index) {
        return Numbers.intBitsToFloat(getInt(index));
    }

    public final int getInt () {
        int newPosition = position + 4;
// if (newPosition > limit) {
// throw new BufferUnderflowException();
// }
        int result = getInt(position);
        position = newPosition;
        return result;
    }

    public final long getLong () {
        int newPosition = position + 8;
// if (newPosition > limit) {
// throw new BufferUnderflowException();
// }
        long result = getLong(position);
        position = newPosition;
        return result;
    }


    public final short getShort () {
        int newPosition = position + 2;
// if (newPosition > limit) {
// throw new BufferUnderflowException();
// }
        short result = getShort(position);
        position = newPosition;
        return result;
    }

    public final boolean isDirect () {
        return false;
    }

    public final int getInt (int baseOffset) {
        int bytes = 0;
        if (order == ByteOrder.BIG_ENDIAN) {
            for (int i = 0; i < 4; i++) {
                bytes = bytes << 8;
                bytes = bytes | (byteArray.get(baseOffset + i) & 0xFF);
            }
        } else {
            for (int i = 3; i >= 0; i--) {
                bytes = bytes << 8;
                bytes = bytes | (byteArray.get(baseOffset + i) & 0xFF);
            }
        }
        return bytes;
    }

    public final long getLong (int baseOffset) {
        long bytes = 0;
        if (order == ByteOrder.BIG_ENDIAN) {
            for (int i = 0; i < 8; i++) {
                bytes = bytes << 8;
                bytes = bytes | (byteArray.get(baseOffset + i) & 0xFF);
            }
        } else {
            for (int i = 7; i >= 0; i--) {
                bytes = bytes << 8;
                bytes = bytes | (byteArray.get(baseOffset + i) & 0xFF);
            }
        }
        return bytes;
    }

    public final short getShort (int baseOffset) {
        short bytes = 0;
        if (order == ByteOrder.BIG_ENDIAN) {
            bytes = (short)(byteArray.get(baseOffset) << 8);
            bytes |= (byteArray.get(baseOffset + 1) & 0xFF);
        } else {
            bytes = (short)(byteArray.get(baseOffset + 1) << 8);
            bytes |= (byteArray.get(baseOffset) & 0xFF);
        }
        return bytes;
    }

    public final ByteBuffer putInt (int baseOffset, int value) {
        if (order == ByteOrder.BIG_ENDIAN) {
            for (int i = 3; i >= 0; i--) {
                byteArray.set(baseOffset + i, (byte)(value & 0xFF));
                value = value >> 8;
            }
        } else {
            for (int i = 0; i <= 3; i++) {
                byteArray.set(baseOffset + i, (byte)(value & 0xFF));
                value = value >> 8;
            }
        }
        return this;
    }

    public final ByteBuffer putLong (int baseOffset, long value) {
        if (order == ByteOrder.BIG_ENDIAN) {
            for (int i = 7; i >= 0; i--) {
                byteArray.set(baseOffset + i, (byte)(value & 0xFF));
                value = value >> 8;
            }
        } else {
            for (int i = 0; i <= 7; i++) {
                byteArray.set(baseOffset + i, (byte)(value & 0xFF));
                value = value >> 8;
            }
        }
        return this;
    }

    public final ByteBuffer putShort(int baseOffset, short value) {
        if (order == ByteOrder.BIG_ENDIAN) {
            byteArray.set(baseOffset, (byte)((value >> 8) & 0xFF));
            byteArray.set(baseOffset + 1, (byte)(value & 0xFF));
        } else {
            byteArray.set(baseOffset + 1, (byte)((value >> 8) & 0xFF));
            byteArray.set(baseOffset, (byte)(value & 0xFF));
        }
        return this;
    }
    
    public final char getChar () {
      return (char)getShort();
    }

    public final char getChar (int index) {
      return (char)getShort(index);
       }

    public final ByteBuffer putChar (char value) {
      return putShort((short)value);
    }

    public final ByteBuffer putChar (int index, char value) {
      return putShort(index, (short)value);
    }
}
