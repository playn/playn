/**
 * Copyright 2014 The PlayN Authors
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
package playn.bugvm;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.bugvm.apple.foundation.NSData;
import com.bugvm.apple.foundation.NSErrorException;

import static playn.bugvm.OpenAL.*;

/**
 * Loads CAFF audio data and uploads it to an OpenAL buffer.
 *
 * <p>Adapted from {@code https://github.com/zite/OpenALHelper}.</p>
 */
public class CAFLoader {

  /** Contains data from the Audio Description chunk of a CAFF file. */
  public static class CAFDesc {

    /** The number of sample frames per second of the data. You can combine this value with the
     * frames per packet to determine the amount of time represented by a packet. This value must
     * be nonzero. */
    public double sampleRate;

    /** A four-character code indicating the general kind of data in the stream. This value must be
     * nonzero. */
    public String formatID;

    /** Flags specific to each format. May be set to 0 to indicate no format flags. */
    public int formatFlags;

    /** The number of bytes in a packet of data. For formats with a variable packet size, this
     * field is set to 0. In that case, the file must include a Packet Table chunk “Packet Table
     * Chunk.” Packets are always aligned to a byte boundary. */
    public int bytesPerPacket;

    /** The number of sample frames in each packet of data. For compressed formats, this field
     * indicates the number of frames encoded in each packet. For formats with a variable number of
     * frames per packet, this field is set to 0 and the file must include a Packet Table chunk
     * “Packet Table Chunk.” */
    public int framesPerPacket;

    /** The number of channels in each frame of data. This value must be nonzero. */
    public int channelsPerFrame;

    /** The number of bits of sample data for each channel in a frame of data. This field must be
     * set to 0 if the data format (for instance any compressed format) does not contain separate
     * samples for each channel. */
    public int bitsPerChannel;

    public CAFDesc(byte[] data) {
      this(ByteBuffer.wrap(data).order(ByteOrder.BIG_ENDIAN));
    }

    public CAFDesc(ByteBuffer buf) {
      sampleRate = buf.getDouble();
      formatID = getString(buf, 4);
      formatFlags = buf.getInt();
      bytesPerPacket = buf.getInt();
      framesPerPacket = buf.getInt();
      channelsPerFrame = buf.getInt();
    }

    public int getALFormat() {
      switch (channelsPerFrame) {
      case  1: return (bitsPerChannel == 8) ? AL_FORMAT_MONO8   : AL_FORMAT_MONO16;
      case  2: return (bitsPerChannel == 8) ? AL_FORMAT_STEREO8 : AL_FORMAT_STEREO16;
      default: return AL_FORMAT_STEREO16;
      }
    }

    @Override public String toString() {
      return String.format(
        "CAFHeader: sampleRate=%f formatID=%s formatFlags=%x bytesPerPacket=%d " +
        "framesPerPacket=%d channelsPerFrame=%d bitsPerChannel=%d", sampleRate, formatID,
        formatFlags, bytesPerPacket, framesPerPacket, channelsPerFrame, bitsPerChannel);
    }
  }

  public static void load(File path, int bufferId) {
    NSData data = null;
    try {
      // mmap (if possible) the audio file for efficient reading/uploading
      data = NSData.read(path, BugAssets.READ_OPTS);
      load(data.asByteBuffer(), path.getName(), bufferId);
    } catch (NSErrorException e) {
      throw new RuntimeException(e.toString());
    } finally {
      // now dispose the mmap'd file to free up resources
      if (data != null) {
        data.dispose();
      }
    }
  }

  public static void load(ByteBuffer data, String source, int bufferId) {
    // read the CAFF metdata to find out the audio format and the data offset/length
    ByteBuffer buf = data.duplicate().order(ByteOrder.BIG_ENDIAN);
    if (!getString(buf, 4).equals("caff")) {
      throw new RuntimeException("Input file not CAFF: " + source);
    }
    buf.position(buf.position()+4); // skip rest of caf file header

    CAFDesc desc = null;
    int offset = 8, dataOffset = 0, dataLength = 0;
    do {
      String type = getString(buf, 4);
      int size = (int)buf.getLong();
      offset += 12;

      if (type.equals("data")) {
        // "data" chunk size may be unspecified, in that case it means the rest of file is the
        // "data" chunk
        if (size <= 0) {
          size = buf.limit() - offset;
        }
        dataOffset = offset;
        dataLength = size;

      } else if (type.equals("desc")) {
        desc = new CAFDesc(buf);
        if ("ima4".equalsIgnoreCase(desc.formatID))
          throw new RuntimeException("Cannot use compressed CAFF. " +
                  "Use AIFC for compressed audio on iOS.");
      }

      offset += size;
      buf.position(offset);
    } while (dataOffset == 0);

    // upload the audio data to OpenAL straight from the buffer
    data.position(dataOffset);
    data.limit(dataLength);
    alBufferData(bufferId, desc.getALFormat(), data, dataLength, (int)desc.sampleRate);

    // finally freak out if OpenAL didn't like what we sent it
    int error = alGetError();
    if (error != AL_NO_ERROR) {
      throw new RuntimeException("AL error " + error);
    }
  }

  protected static String getString (ByteBuffer buf, int length) {
    byte[] data = new byte[length];
    buf.get(data);
    try {
      return new String(data, "UTF-8");
    } catch (UnsupportedEncodingException uee) {
      throw new RuntimeException(uee);
    }
  }
}
