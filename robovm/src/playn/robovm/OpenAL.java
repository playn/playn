/**
 * Copyright 2014 The PlayN Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package playn.robovm;

import java.nio.Buffer;
import java.nio.IntBuffer;

import org.robovm.rt.bro.Bro;
import org.robovm.rt.bro.annotation.*;
import org.robovm.rt.bro.ptr.VoidPtr;

@Library("OpenAL")
public class OpenAL {
  static {
    Bro.bind(OpenAL.class);
  }

  // Context management

  // ALC_API ALCcontext *    ALC_APIENTRY alcCreateContext( ALCdevice *device, const ALCint* attrlist );
  @Bridge public static native long alcCreateContext(@Pointer long device, int[] attrList);
  // ALC_API ALCboolean      ALC_APIENTRY alcMakeContextCurrent( ALCcontext *context );
  @Bridge public static native boolean alcMakeContextCurrent(@Pointer long context);
  // ALC_API void            ALC_APIENTRY alcProcessContext( ALCcontext *context );
  @Bridge public static native void alcProcessContext(@Pointer long context);
  // ALC_API void            ALC_APIENTRY alcSuspendContext( ALCcontext *context );
  @Bridge public static native void alcSuspendContext(@Pointer long context);
  // ALC_API void            ALC_APIENTRY alcDestroyContext( ALCcontext *context );
  @Bridge public static native void alcDestroyContext(@Pointer long context);
  // ALC_API ALCcontext *    ALC_APIENTRY alcGetCurrentContext( void );
  @Bridge public static native @Pointer long alcGetCurrentContext();
  // ALC_API ALCdevice*      ALC_APIENTRY alcGetContextsDevice( ALCcontext *context );
  @Bridge public static native @Pointer long alcGetContextsDevice(@Pointer long context);

  // Device management

  // ALC_API ALCdevice *     ALC_APIENTRY alcOpenDevice( const ALCchar *devicename );
  @Bridge public static native @Pointer long alcOpenDevice(String deviceName);
  // ALC_API ALCboolean      ALC_APIENTRY alcCloseDevice( ALCdevice *device );
  @Bridge public static native void alcCloseDevice(@Pointer long device);

  // Error support

  // ALC_API ALCenum         ALC_APIENTRY alcGetError( ALCdevice *device );
  @Bridge public static native int alcGetError(@Pointer long device);

  // Extension support

  // ALC_API ALCboolean      ALC_APIENTRY alcIsExtensionPresent( ALCdevice *device, const ALCchar *extname );
  @Bridge public static native boolean alcIsExtensionPresent(@Pointer long device, String extName);
  // ALC_API void  *         ALC_APIENTRY alcGetProcAddress( ALCdevice *device, const ALCchar *funcname );
  @Bridge public static native VoidPtr alcGetProcAddress(@Pointer long device, String funcName);
  // ALC_API ALCenum         ALC_APIENTRY alcGetEnumValue( ALCdevice *device, const ALCchar *enumname );
  @Bridge public static native int alcGetEnumValue(@Pointer long device, String enumName);

  // Query functions

  // ALC_API const ALCchar * ALC_APIENTRY alcGetString( ALCdevice *device, ALCenum param );
  @Bridge public static native String alcGetString(@Pointer long device, int param);
  // ALC_API void            ALC_APIENTRY alcGetIntegerv( ALCdevice *device, ALCenum param, ALCsizei size, ALCint *data );
  @Bridge public static native void alcGetString(@Pointer long device, int param, int size, IntBuffer data);

  // Capture functions

  // ALC_API ALCdevice*      ALC_APIENTRY alcCaptureOpenDevice( const ALCchar *devicename, ALCuint frequency, ALCenum format, ALCsizei buffersize );
  @Bridge public static native @Pointer long alcCaptureOpenDevice(String deviceName, int frequency, int format, int bufferSize);
  // ALC_API ALCboolean      ALC_APIENTRY alcCaptureCloseDevice( ALCdevice *device );
  @Bridge public static native boolean alcCaptureCloseDevice(@Pointer long device);
  // ALC_API void            ALC_APIENTRY alcCaptureStart( ALCdevice *device );
  @Bridge public static native void alcCaptureStart(@Pointer long device);
  // ALC_API void            ALC_APIENTRY alcCaptureStop( ALCdevice *device );
  @Bridge public static native void alcCaptureStop(@Pointer long device);
  // ALC_API void            ALC_APIENTRY alcCaptureSamples( ALCdevice *device, ALCvoid *buffer, ALCsizei samples );
  @Bridge public static native void alcCaptureSamples(@Pointer long device, Buffer buffer, int samples);

  /* Boolean False. */
  public static final int AL_FALSE = 0;
  /** Boolean True. */
  public static final int AL_TRUE = 1;

  /** Indicate Source has relative coordinates. */
  public static final int AL_SOURCE_RELATIVE = 0x202;

  /**
   * Directional source, inner cone angle, in degrees.
   * Range:    [0-360]
   * Default:  360
   */
  public static final int AL_CONE_INNER_ANGLE = 0x1001;

  /**
   * Directional source, outer cone angle, in degrees.
   * Range:    [0-360]
   * Default:  360
   */
  public static final int AL_CONE_OUTER_ANGLE = 0x1002;

  /**
   * Specify the pitch to be applied, either at source,
   *  or on mixer results, at listener.
   * Range:   [0.5-2.0]
   * Default: 1.0
   */
  public static final int AL_PITCH = 0x1003;

  /**
   * Specify the current location in three dimensional space.
   * OpenAL, like OpenGL, uses a right handed coordinate system,
   *  where in a frontal default view X (thumb) points right,
   *  Y points up (index finger), and Z points towards the
   *  viewer/camera (middle finger).
   * To switch from a left handed coordinate system, flip the
   *  sign on the Z coordinate.
   * Listener position is always in the world coordinate system.
   */
  public static final int AL_POSITION = 0x1004;

  /** Specify the current direction. */
  public static final int AL_DIRECTION = 0x1005;

  /** Specify the current velocity in three dimensional space. */
  public static final int AL_VELOCITY = 0x1006;

  /**
   * Indicate whether source is looping.
   * Type: ALboolean?
   * Range:   [AL_TRUE, AL_FALSE]
   * Default: FALSE.
   */
  public static final int AL_LOOPING = 0x1007;

  /**
   * Indicate the buffer to provide sound samples.
   * Type: ALuint.
   * Range: any valid Buffer id.
   */
  public static final int AL_BUFFER = 0x1009;

  /**
   * Indicate the gain (volume amplification) applied.
   * Type:   ALfloat.
   * Range:  ]0.0-  ]
   * A value of 1.0 means un-attenuated/unchanged.
   * Each division by 2 equals an attenuation of -6dB.
   * Each multiplicaton with 2 equals an amplification of +6dB.
   * A value of 0.0 is meaningless with respect to a logarithmic
   *  scale; it is interpreted as zero volume - the channel
   *  is effectively disabled.
   */
  public static final int AL_GAIN = 0x100A;

  /*
   * Indicate minimum source attenuation
   * Type: ALfloat
   * Range:  [0.0 - 1.0]
   *
   * Logarthmic
   */
  public static final int AL_MIN_GAIN = 0x100D;

  /**
   * Indicate maximum source attenuation
   * Type: ALfloat
   * Range:  [0.0 - 1.0]
   *
   * Logarthmic
   */
  public static final int AL_MAX_GAIN = 0x100E;

  /**
   * Indicate listener orientation.
   *
   * at/up
   */
  public static final int AL_ORIENTATION = 0x100F;

  /**
   * Source state information.
   */
  public static final int AL_SOURCE_STATE = 0x1010;
  public static final int AL_INITIAL = 0x1011;
  public static final int AL_PLAYING = 0x1012;
  public static final int AL_PAUSED = 0x1013;
  public static final int AL_STOPPED = 0x1014;

  /**
   * Buffer Queue params
   */
  public static final int AL_BUFFERS_QUEUED = 0x1015;
  public static final int AL_BUFFERS_PROCESSED = 0x1016;

  /**
   * Source buffer position information
   */
  public static final int AL_SEC_OFFSET = 0x1024;
  public static final int AL_SAMPLE_OFFSET = 0x1025;
  public static final int AL_BYTE_OFFSET = 0x1026;

  /*
   * Source type (Static, Streaming or undetermined)
   * Source is Static if a Buffer has been attached using AL_BUFFER
   * Source is Streaming if one or more Buffers have been attached using alSourceQueueBuffers
   * Source is undetermined when it has the NULL buffer attached
   */
  public static final int AL_SOURCE_TYPE = 0x1027;
  public static final int AL_STATIC = 0x1028;
  public static final int AL_STREAMING = 0x1029;
  public static final int AL_UNDETERMINED = 0x1030;

  /** Sound samples: format specifier. */
  public static final int AL_FORMAT_MONO8 = 0x1100;
  public static final int AL_FORMAT_MONO16 = 0x1101;
  public static final int AL_FORMAT_STEREO8 = 0x1102;
  public static final int AL_FORMAT_STEREO16 = 0x1103;

  /**
   * source specific reference distance
   * Type: ALfloat
   * Range:  0.0 - +inf
   *
   * At 0.0, no distance attenuation occurs.  Default is
   * 1.0.
   */
  public static final int AL_REFERENCE_DISTANCE = 0x1020;

  /**
   * source specific rolloff factor
   * Type: ALfloat
   * Range:  0.0 - +inf
   *
   */
  public static final int AL_ROLLOFF_FACTOR = 0x1021;

  /**
   * Directional source, outer cone gain.
   *
   * Default:  0.0
   * Range:    [0.0 - 1.0]
   * Logarithmic
   */
  public static final int AL_CONE_OUTER_GAIN = 0x1022;

  /**
   * Indicate distance above which sources are not attenuated using the inverse clamped distance
   * model.
   *
   * Default: +inf
   * Type: ALfloat
   * Range:  0.0 - +inf
   */
  public static final int AL_MAX_DISTANCE = 0x1023;

  /**
   * Sound samples: frequency, in units of Hertz [Hz].
   * This is the number of samples per second. Half of the sample frequency marks the maximum
   * significant frequency component.
   */
  public static final int AL_FREQUENCY = 0x2001;
  public static final int AL_BITS = 0x2002;
  public static final int AL_CHANNELS = 0x2003;
  public static final int AL_SIZE = 0x2004;

  /**
   * Buffer state.
   *
   * Not supported for public use (yet).
   */
  public static final int AL_UNUSED = 0x2010;
  public static final int AL_PENDING = 0x2011;
  public static final int AL_PROCESSED = 0x2012;

  /** Errors: No Error. */
  public static final int AL_NO_ERROR = AL_FALSE;

  /**
   * Invalid Name paramater passed to AL call.
   */
  public static final int AL_INVALID_NAME = 0xA001;

  /**
   * Invalid parameter passed to AL call.
   */
  public static final int AL_INVALID_ENUM = 0xA002;

  /**
   * Invalid enum parameter value.
   */
  public static final int AL_INVALID_VALUE = 0xA003;

  /**
   * Illegal call.
   */
  public static final int AL_INVALID_OPERATION = 0xA004;

  /**
   * No mojo.
   */
  public static final int AL_OUT_OF_MEMORY = 0xA005;

  /** Context strings: Vendor Name. */
  public static final int AL_VENDOR = 0xB001;
  public static final int AL_VERSION = 0xB002;
  public static final int AL_RENDERER = 0xB003;
  public static final int AL_EXTENSIONS = 0xB004;

  /** Global tweakage. */

  /**
   * Doppler scale.  Default 1.0
   */
  public static final int AL_DOPPLER_FACTOR = 0xC000;

  /**
   * Tweaks speed of propagation.
   */
  public static final int AL_DOPPLER_VELOCITY = 0xC001;

  /**
   * Speed of Sound in units per second
   */
  public static final int AL_SPEED_OF_SOUND = 0xC003;

  /**
   * Distance models
   *
   * used in conjunction with DistanceModel
   *
   * implicit: NONE, which disances distance attenuation.
   */
  public static final int AL_DISTANCE_MODEL = 0xD000;
  public static final int AL_INVERSE_DISTANCE = 0xD001;
  public static final int AL_INVERSE_DISTANCE_CLAMPED = 0xD002;
  public static final int AL_LINEAR_DISTANCE = 0xD003;
  public static final int AL_LINEAR_DISTANCE_CLAMPED = 0xD004;
  public static final int AL_EXPONENT_DISTANCE = 0xD005;
  public static final int AL_EXPONENT_DISTANCE_CLAMPED = 0xD006;

  /*
   * Renderer State management
   */
  // AL_API void AL_APIENTRY alEnable( ALenum capability );
  // AL_API void AL_APIENTRY alDisable( ALenum capability );
  // AL_API ALboolean AL_APIENTRY alIsEnabled( ALenum capability );

  /*
   * State retrieval
   */
  // AL_API const ALchar* AL_APIENTRY alGetString( ALenum param );
  // AL_API void AL_APIENTRY alGetBooleanv( ALenum param, ALboolean* data );
  // AL_API void AL_APIENTRY alGetIntegerv( ALenum param, ALint* data );
  // AL_API void AL_APIENTRY alGetFloatv( ALenum param, ALfloat* data );
  // AL_API void AL_APIENTRY alGetDoublev( ALenum param, ALdouble* data );
  // AL_API ALboolean AL_APIENTRY alGetBoolean( ALenum param );
  // AL_API ALint AL_APIENTRY alGetInteger( ALenum param );
  // AL_API ALfloat AL_APIENTRY alGetFloat( ALenum param );
  // AL_API ALdouble AL_APIENTRY alGetDouble( ALenum param );

  /*
   * Error support.
   * Obtain the most recent error generated in the AL state machine.
   */
  // AL_API ALenum AL_APIENTRY alGetError( void );
  @Bridge public static native int alGetError();

  /*
   * Extension support.
   * Query for the presence of an extension, and obtain any appropriate
   * function pointers and enum values.
   */
  // AL_API ALboolean AL_APIENTRY alIsExtensionPresent( const ALchar* extname );
  // AL_API void* AL_APIENTRY alGetProcAddress( const ALchar* fname );
  // AL_API ALenum AL_APIENTRY alGetEnumValue( const ALchar* ename );


  /*
   * LISTENER
   * Listener represents the location and orientation of the
   * 'user' in 3D-space.
   *
   * Properties include: -
   *
   * Gain         AL_GAIN         ALfloat
   * Position     AL_POSITION     ALfloat[3]
   * Velocity     AL_VELOCITY     ALfloat[3]
   * Orientation  AL_ORIENTATION  ALfloat[6] (Forward then Up vectors)
   */

  /*
   * Set Listener parameters
   */
  // AL_API void AL_APIENTRY alListenerf( ALenum param, ALfloat value );
  // AL_API void AL_APIENTRY alListener3f( ALenum param, ALfloat value1, ALfloat value2, ALfloat value3 );
  // AL_API void AL_APIENTRY alListenerfv( ALenum param, const ALfloat* values );
  // AL_API void AL_APIENTRY alListeneri( ALenum param, ALint value );
  // AL_API void AL_APIENTRY alListener3i( ALenum param, ALint value1, ALint value2, ALint value3 );
  // AL_API void AL_APIENTRY alListeneriv( ALenum param, const ALint* values );

  /*
   * Get Listener parameters
   */
  // AL_API void AL_APIENTRY alGetListenerf( ALenum param, ALfloat* value );
  // AL_API void AL_APIENTRY alGetListener3f( ALenum param, ALfloat *value1, ALfloat *value2, ALfloat *value3 );
  // AL_API void AL_APIENTRY alGetListenerfv( ALenum param, ALfloat* values );
  // AL_API void AL_APIENTRY alGetListeneri( ALenum param, ALint* value );
  // AL_API void AL_APIENTRY alGetListener3i( ALenum param, ALint *value1, ALint *value2, ALint *value3 );
  // AL_API void AL_APIENTRY alGetListeneriv( ALenum param, ALint* values );

  /**
   * SOURCE
   * Sources represent individual sound objects in 3D-space.
   * Sources take the PCM data provided in the specified Buffer,
   * apply Source-specific modifications, and then
   * submit them to be mixed according to spatial arrangement etc.
   *
   * Properties include: -
   *
   * Gain                              AL_GAIN                 ALfloat
   * Min Gain                          AL_MIN_GAIN             ALfloat
   * Max Gain                          AL_MAX_GAIN             ALfloat
   * Position                          AL_POSITION             ALfloat[3]
   * Velocity                          AL_VELOCITY             ALfloat[3]
   * Direction                         AL_DIRECTION            ALfloat[3]
   * Head Relative Mode                AL_SOURCE_RELATIVE      ALint (AL_TRUE or AL_FALSE)
   * Reference Distance                AL_REFERENCE_DISTANCE   ALfloat
   * Max Distance                      AL_MAX_DISTANCE         ALfloat
   * RollOff Factor                    AL_ROLLOFF_FACTOR       ALfloat
   * Inner Angle                       AL_CONE_INNER_ANGLE     ALint or ALfloat
   * Outer Angle                       AL_CONE_OUTER_ANGLE     ALint or ALfloat
   * Cone Outer Gain                   AL_CONE_OUTER_GAIN      ALint or ALfloat
   * Pitch                             AL_PITCH                ALfloat
   * Looping                           AL_LOOPING              ALint (AL_TRUE or AL_FALSE)
   * MS Offset                         AL_MSEC_OFFSET          ALint or ALfloat
   * Byte Offset                       AL_BYTE_OFFSET          ALint or ALfloat
   * Sample Offset                     AL_SAMPLE_OFFSET        ALint or ALfloat
   * Attached Buffer                   AL_BUFFER               ALint
   * State (Query only)                AL_SOURCE_STATE         ALint
   * Buffers Queued (Query only)       AL_BUFFERS_QUEUED       ALint
   * Buffers Processed (Query only)    AL_BUFFERS_PROCESSED    ALint
   */

  /* Create Source objects */
  // AL_API void AL_APIENTRY alGenSources( ALsizei n, ALuint* sources );
  @Bridge public static native void alGenSources(int n, int[] sources);

  /* Delete Source objects */
  // AL_API void AL_APIENTRY alDeleteSources( ALsizei n, const ALuint* sources );
  /* Verify a handle is a valid Source */
  // AL_API ALboolean AL_APIENTRY alIsSource( ALuint sid );

  /*
   * Set Source parameters
   */
  // AL_API void AL_APIENTRY alSourcef( ALuint sid, ALenum param, ALfloat value );
  @Bridge public static native void alSourcef(int sid, int param, float value);
  // AL_API void AL_APIENTRY alSource3f( ALuint sid, ALenum param, ALfloat value1, ALfloat value2, ALfloat value3 );
  @Bridge public static native void alSource3f(int sid, int param, float value1, float value2, float value3);
  // AL_API void AL_APIENTRY alSourcefv( ALuint sid, ALenum param, const ALfloat* values );
  @Bridge public static native void alSourcefv(int sid, int param, float[] values);
  // AL_API void AL_APIENTRY alSourcei( ALuint sid, ALenum param, ALint value );
  @Bridge public static native void alSourcei(int sid, int param, int value);
  // AL_API void AL_APIENTRY alSource3i( ALuint sid, ALenum param, ALint value1, ALint value2, ALint value3 );
  @Bridge public static native void alSource3i(int sid, int param, int value1, int value2, int value3);
  // AL_API void AL_APIENTRY alSourceiv( ALuint sid, ALenum param, const ALint* values );
  @Bridge public static native void alSourceiv(int sid, int param, int[] values);

  /*
   * Get Source parameters
   */
  // AL_API void AL_APIENTRY alGetSourcef( ALuint sid, ALenum param, ALfloat* value );
  @Bridge public static native void alGetSourcef(int sid, int param, float[] value);
  // AL_API void AL_APIENTRY alGetSource3f( ALuint sid, ALenum param, ALfloat* value1, ALfloat* value2, ALfloat* value3);
  @Bridge public static native void alGetSource3f(int sid, int param, float[] value1, float[] value2, float[] value3);
  // AL_API void AL_APIENTRY alGetSourcefv( ALuint sid, ALenum param, ALfloat* values );
  @Bridge public static native void alGetSourcefv(int sid, int param, float[] values);
  // AL_API void AL_APIENTRY alGetSourcei( ALuint sid,  ALenum param, ALint* value );
  @Bridge public static native void alGetSourcei(int sid, int param, int[] value);
  // AL_API void AL_APIENTRY alGetSource3i( ALuint sid, ALenum param, ALint* value1, ALint* value2, ALint* value3);
  @Bridge public static native void alGetSource3i(int sid, int param, int[] value1, int[] value2, int[] value3);
  // AL_API void AL_APIENTRY alGetSourceiv( ALuint sid,  ALenum param, ALint* values );
  @Bridge public static native void alGetSourceiv(int sid, int param, int[] values);

  /*
   * Source vector based playback calls
   */

  /* Play, replay, or resume (if paused) a list of Sources */
  // AL_API void AL_APIENTRY alSourcePlayv( ALsizei ns, const ALuint *sids );
  @Bridge public static native void alSourcePlayv(int ns, int[] sids);
  /* Stop a list of Sources */
  // AL_API void AL_APIENTRY alSourceStopv( ALsizei ns, const ALuint *sids );
  @Bridge public static native void alSourceStopv(int ns, int[] sids);
  /* Rewind a list of Sources */
  // AL_API void AL_APIENTRY alSourceRewindv( ALsizei ns, const ALuint *sids );
  @Bridge public static native void alSourceRewindv(int ns, int[] sids);
  /* Pause a list of Sources */
  // AL_API void AL_APIENTRY alSourcePausev( ALsizei ns, const ALuint *sids );
  @Bridge public static native void alSourcePausev(int ns, int[] sids);

  /*
   * Source based playback calls
   */

  /* Play, replay, or resume a Source */
  // AL_API void AL_APIENTRY alSourcePlay( ALuint sid );
  @Bridge public static native void alSourcePlay(int sid);
  /* Stop a Source */
  // AL_API void AL_APIENTRY alSourceStop( ALuint sid );
  @Bridge public static native void alSourceStop(int sid);
  /* Rewind a Source (set playback postiton to beginning) */
  // AL_API void AL_APIENTRY alSourceRewind( ALuint sid );
  @Bridge public static native void alSourceRewind(int sid);
  /* Pause a Source */
  // AL_API void AL_APIENTRY alSourcePause( ALuint sid );
  @Bridge public static native void alSourcePause(int sid);

  /*
   * Source Queuing
   */
  // AL_API void AL_APIENTRY alSourceQueueBuffers( ALuint sid, ALsizei numEntries, const ALuint *bids );
  // AL_API void AL_APIENTRY alSourceUnqueueBuffers( ALuint sid, ALsizei numEntries, ALuint *bids );

  /**
   * BUFFER
   * Buffer objects are storage space for sample data.
   * Buffers are referred to by Sources. One Buffer can be used
   * by multiple Sources.
   *
   * Properties include: -
   *
   * Frequency (Query only)    AL_FREQUENCY      ALint
   * Size (Query only)         AL_SIZE           ALint
   * Bits (Query only)         AL_BITS           ALint
   * Channels (Query only)     AL_CHANNELS       ALint
   */

  /* Create Buffer objects */
  // AL_API void AL_APIENTRY alGenBuffers( ALsizei n, ALuint* buffers );
  @Bridge public static native void alGenBuffers(int n, int[] buffers);

  public static int alGenBuffer() {
    int[] result = new int[1];
    alGenBuffers(1, result);
    return result[0];
  }

  /* Delete Buffer objects */
  // AL_API void AL_APIENTRY alDeleteBuffers( ALsizei n, const ALuint* buffers );
  @Bridge public static native void alDeleteBuffers(int n, int[] buffers);

  public static void alDeleteBuffer(int id) {
    alDeleteBuffers(1, new int[] { id });
  }

  /* Verify a handle is a valid Buffer */
  // AL_API ALboolean AL_APIENTRY alIsBuffer( ALuint bid );

  /* Specify the data to be copied into a buffer */
  // AL_API void AL_APIENTRY alBufferData( ALuint bid, ALenum format, const ALvoid* data, ALsizei size, ALsizei freq );
  @Bridge public static native void alBufferData(int bid, int format, Buffer data, int size, int freq);

  /*
   * Set Buffer parameters
   */
  // AL_API void AL_APIENTRY alBufferf( ALuint bid, ALenum param, ALfloat value );
  // AL_API void AL_APIENTRY alBuffer3f( ALuint bid, ALenum param, ALfloat value1, ALfloat value2, ALfloat value3 );
  // AL_API void AL_APIENTRY alBufferfv( ALuint bid, ALenum param, const ALfloat* values );
  // AL_API void AL_APIENTRY alBufferi( ALuint bid, ALenum param, ALint value );
  // AL_API void AL_APIENTRY alBuffer3i( ALuint bid, ALenum param, ALint value1, ALint value2, ALint value3 );
  // AL_API void AL_APIENTRY alBufferiv( ALuint bid, ALenum param, const ALint* values );

  /*
   * Get Buffer parameters
   */
  // AL_API void AL_APIENTRY alGetBufferf( ALuint bid, ALenum param, ALfloat* value );
  // AL_API void AL_APIENTRY alGetBuffer3f( ALuint bid, ALenum param, ALfloat* value1, ALfloat* value2, ALfloat* value3);
  // AL_API void AL_APIENTRY alGetBufferfv( ALuint bid, ALenum param, ALfloat* values );
  // AL_API void AL_APIENTRY alGetBufferi( ALuint bid, ALenum param, ALint* value );
  // AL_API void AL_APIENTRY alGetBuffer3i( ALuint bid, ALenum param, ALint* value1, ALint* value2, ALint* value3);
  // AL_API void AL_APIENTRY alGetBufferiv( ALuint bid, ALenum param, ALint* values );

  /*
   * Global Parameters
   */
  // AL_API void AL_APIENTRY alDopplerFactor( ALfloat value );
  // AL_API void AL_APIENTRY alDopplerVelocity( ALfloat value );
  // AL_API void AL_APIENTRY alSpeedOfSound( ALfloat value );
  // AL_API void AL_APIENTRY alDistanceModel( ALenum distanceModel );
}
