/**
 * Copyright 2013 The PlayN Authors
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
package playn.ios;

import cli.System.Array;
import cli.System.BitConverter;
import cli.System.IO.BinaryReader;
import cli.System.IO.File;
import cli.System.IO.MemoryStream;
import cli.System.IntPtr;

import cli.OpenTK.Audio.OpenAL.AL;
import cli.OpenTK.Audio.OpenAL.ALError;
import cli.OpenTK.Audio.OpenAL.ALFormat;
import cli.OpenTK.Audio.OpenAL.XRamExtension;

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
      BinaryReader br = new BinaryReader(new MemoryStream(data));
      sampleRate = BitConverter.ToDouble(reverse(br.ReadBytes(8)), 0);
      formatID = new String(br.ReadChars(4));
      formatFlags = BitConverter.ToInt32(reverse(br.ReadBytes(4)), 0);
      bytesPerPacket = BitConverter.ToInt32(reverse(br.ReadBytes(4)), 0);
      framesPerPacket = BitConverter.ToInt32(reverse(br.ReadBytes(4)), 0);
      channelsPerFrame = BitConverter.ToInt32(reverse(br.ReadBytes(4)), 0);
      bitsPerChannel = BitConverter.ToInt32(reverse(br.ReadBytes(4)), 0);
    }

    public ALFormat GetALFormat() {
      switch (channelsPerFrame) {
      case 1:
        return (bitsPerChannel == 8) ?
          ALFormat.wrap(ALFormat.Mono8) : ALFormat.wrap(ALFormat.Mono16);
      case 2:
        return (bitsPerChannel == 8) ?
          ALFormat.wrap(ALFormat.Stereo8) : ALFormat.wrap(ALFormat.Stereo16);
      default:
        return ALFormat.wrap(ALFormat.Stereo16);
      }
    }

    @Override public String toString() {
      return String.format(
        "CAFHeader: sampleRate=%f formatID=%s formatFlags=%x bytesPerPacket=%d " +
        "framesPerPacket=%d channelsPerFrame=%d bitsPerChannel=%d", sampleRate, formatID,
        formatFlags, bytesPerPacket, framesPerPacket, channelsPerFrame, bitsPerChannel);
    }
  }

  public static void load(String path, int bufferId) {
    XRamExtension XRam = new XRamExtension();
    if (XRam.get_IsInitialized())
      XRam.SetBufferMode(1, new int[] { bufferId },
                         XRamExtension.XRamStorage.wrap(XRamExtension.XRamStorage.Hardware));

    BinaryReader br = new BinaryReader(File.OpenRead(path));
    byte[] bytes = new byte[1];
    if (!new String(br.ReadChars(4)).equals("caff"))
      throw new RuntimeException("Input file not CAFF: " + path);

    br.ReadBytes(4); // rest of caf file header
    CAFDesc desc = null;

    do {
      String type = new String(br.ReadChars(4));
      int size = (int)BitConverter.ToInt64(reverse(br.ReadBytes(8)), 0);

      if (type.equals("data")) {
        bytes = new byte[size];
        bytes = br.ReadBytes(size);
      } else if (type.equals("desc")) {
        desc = new CAFDesc(br.ReadBytes(size));
        if ("ima4".equalsIgnoreCase(desc.formatID))
          throw new RuntimeException("Cannot use compressed CAFF. " +
                                     "Use AIFC for compressed audio on iOS.");
      } else {
        br.ReadBytes(size);
      }
    } while (bytes.length == 1);

    br.Close();

    // TODO: pin instead of alloc and copy
    IntPtr ptr = cli.System.Runtime.InteropServices.Marshal.AllocHGlobal(bytes.length);
    cli.System.Runtime.InteropServices.Marshal.Copy(bytes, 0, ptr, bytes.length);
    AL.BufferData(bufferId, desc.GetALFormat(), ptr, bytes.length, (int)desc.sampleRate);

    ALError error = AL.GetError();
    if (error.Value != ALError.NoError) {
      throw new RuntimeException(error.ToString());
    }

    // TODO: free ptr?
  }

  protected static byte[] reverse (byte[] data) {
    // in C# we could cast byte[] to Array, but Java doesn't know about that
    Array.Reverse((Array)(Object)data);
    return data;
  }
}
