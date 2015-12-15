/**
 * Copyright 2010 The PlayN Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package playn.java;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import java.util.Arrays;
import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import pythagoras.f.MathUtil;

import playn.core.Assets;
import playn.core.Image;
import playn.core.ImageImpl;
import playn.core.Scale;
import playn.core.Sound;

/**
 * Loads Java assets via the classpath.
 */
public class JavaAssets extends Assets {

  private final JavaPlatform plat;
  private File[] directories = {};

  private String pathPrefix = "assets/";
  private Scale assetScale = null;

  /**
   * Creates a new java assets.
   */
  public JavaAssets(JavaPlatform plat) {
    super(plat.exec());
    this.plat = plat;
  }

  /**
   * Configures the prefix prepended to asset paths before fetching them from the classpath. For
   * example, if your assets are in {@code src/main/java/com/mygame/assets} (or in {@code
   * src/main/resources/com/mygame/assets}), you can pass {@code com/mygame/assets} to this method
   * and then load your assets without prefixing their path with that value every time. The value
   * supplied to this method should not contain leading or trailing slashes. Note that this prefix
   * should always use '/' as a path separator as it is used to construct URLs, not filesystem
   * paths.
   * <p>NOTE: the path prefix is not used when searching extra directories</p>
   */
  public void setPathPrefix(String prefix) {
    if (prefix.startsWith("/") || prefix.endsWith("/")) {
      throw new IllegalArgumentException("Prefix must not start or end with '/'.");
    }
    pathPrefix = (prefix.length() == 0) ? prefix : (prefix + "/");
  }

  /**
   * Returns the currently configured path prefix. Note that this value will always have a trailing
   * slash.
   */
  public String getPathPrefix() {
    return pathPrefix;
  }

  /**
   * Adds the given directory to the search path for resources.
   * <p>TODO: remove? get?</p>
   */
  public void addDirectory(File dir) {
    File[] ndirs = new File[directories.length + 1];
    System.arraycopy(directories, 0, ndirs, 0, directories.length);
    ndirs[ndirs.length - 1] = dir;
    directories = ndirs;
  }

  /**
   * Configures the default scale to use for assets. This allows one to specify an intermediate
   * graphics scale (like 1.5) and scale the 2x imagery down to 1.5x instead of scaling the 1.5x
   * imagery up (or displaying nothing at all).
   */
  public void setAssetScale(float scaleFactor) {
    this.assetScale = new Scale(scaleFactor);
  }

  /**
   * Loads a Java font from {@code path}. Currently only TrueType ({@code .ttf}) fonts are
   * supported.
   *
   * @param path the path to the font resource (relative to the asset manager's path prefix).
   * @throws Exception if an error occurs loading or decoding the font.
   */
  public Font getFont(String path) throws Exception {
    return requireResource(path).createFont();
  }

  @Override public Image getRemoteImage(final String url, int width, int height) {
    final JavaImage image = new JavaImage(plat, true, width, height, url);
    exec.invokeAsync(new Runnable() {
      public void run () {
        try {
          BufferedImage bmp = ImageIO.read(new URL(url));
          image.succeed(new ImageImpl.Data(Scale.ONE, bmp, bmp.getWidth(), bmp.getHeight()));
        } catch (Exception error) {
          image.fail(error);
        }
      }
    });
    return image;
  }

  @Override
  public Sound getSound(String path) {
    return getSound(path, false);
  }

  @Override
  public Sound getMusic(String path) {
    return getSound(path, true);
  }

  @Override
  public String getTextSync(String path) throws Exception {
    return requireResource(path).readString();
  }

  @Override
  public ByteBuffer getBytesSync(String path) throws Exception {
    return requireResource(path).readBytes();
  }

  protected Sound getSound(String path, boolean music) {
    Exception err = null;
    for (String suff : SUFFIXES) {
      final String soundPath = path + suff;
      try {
        return plat.audio().createSound(requireResource(soundPath), music);
      } catch (Exception e) {
        err = e; // note the error, and loop through and try the next format
      }
    }
    plat.log().warn("Sound load error " + path + ": " + err);
    return new Sound.Error(err);
  }

  /**
   * Attempts to locate the resource at the given path, and returns a wrapper which allows its data
   * to be efficiently read.
   *
   * <p>First, the path prefix is prepended (see {@link #setPathPrefix(String)}) and the the class
   * loader checked. If not found, then the extra directories, if any, are checked, in order. If
   * the file is not found in any of the extra directories either, then an exception is thrown.
   */
  protected Resource requireResource(String path) throws IOException {
    URL url = getClass().getClassLoader().getResource(pathPrefix + path);
    if (url != null) {
      return url.getProtocol().equals("file") ?
        new FileResource(new File(URLDecoder.decode(url.getPath(), "UTF-8"))) :
        new URLResource(url);
    }
    for (File dir : directories) {
      File f = new File(dir, path).getCanonicalFile();
      if (f.exists()) {
        return new FileResource(f);
      }
    }
    throw new FileNotFoundException(path);
  }

  static byte[] toByteArray(InputStream in) throws IOException {
    try {
      byte[] buffer = new byte[512];
      int size = 0, read = 0;
      while ((read = in.read(buffer, size, buffer.length-size)) > 0) {
        size += read;
        if (size == buffer.length) buffer = Arrays.copyOf(buffer, size*2);
      }
      // trim the zeros from the end of the buffer
      if (size < buffer.length) {
        buffer = Arrays.copyOf(buffer, size);
      }
      return buffer;
    } finally {
      in.close();
    }
  }

  protected BufferedImage scaleImage(BufferedImage image, float viewImageRatio) {
    int swidth = MathUtil.iceil(viewImageRatio * image.getWidth());
    int sheight = MathUtil.iceil(viewImageRatio * image.getHeight());
    BufferedImage scaled = new BufferedImage(swidth, sheight, BufferedImage.TYPE_INT_ARGB_PRE);
    Graphics2D gfx = scaled.createGraphics();
    gfx.drawImage(image.getScaledInstance(swidth, sheight, java.awt.Image.SCALE_SMOOTH), 0, 0, null);
    gfx.dispose();
    return scaled;
  }

  protected Scale assetScale() {
    return (assetScale != null) ? assetScale : plat.graphics().scale();
  }

  abstract static class Resource {
    public abstract BufferedImage readImage() throws IOException;
    public abstract InputStream openStream() throws IOException;
    public AudioInputStream openAudioStream() throws Exception {
      return AudioSystem.getAudioInputStream(openStream());
    }
    public Font createFont() throws Exception {
      return Font.createFont(Font.TRUETYPE_FONT, openStream());
    }
    public ByteBuffer readBytes() throws IOException {
      return ByteBuffer.wrap(toByteArray(openStream()));
    }
    public String readString() throws Exception {
      return new String(toByteArray(openStream()), "UTF-8");
    }
  }

  protected static class URLResource extends Resource {
    public final URL url;
    public URLResource(URL url) {
      this.url = url;
    }
    public InputStream openStream() throws IOException {
      return url.openStream();
    }
    public BufferedImage readImage() throws IOException {
      return ImageIO.read(url);
    }
  }

  protected static class FileResource extends Resource {
    public final File file;
    public FileResource(File file) {
      this.file = file;
    }
    public FileInputStream openStream() throws IOException {
      return new FileInputStream(file);
    }
    public BufferedImage readImage() throws IOException {
      return ImageIO.read(file);
    }
    @Override public AudioInputStream openAudioStream() throws Exception {
      return AudioSystem.getAudioInputStream(file);
    }
    @Override public Font createFont() throws Exception {
      return Font.createFont(Font.TRUETYPE_FONT, file);
    }
    @Override public ByteBuffer readBytes() throws IOException {
      try (FileInputStream in = openStream();
           FileChannel fc = in.getChannel()) {
        ByteBuffer buf = ByteBuffer.allocateDirect((int)fc.size()); // no >2GB files
        fc.read(buf);
        return buf;
      }
    }
  }

  @Override protected ImageImpl.Data load (String path) throws Exception {
    Exception error = null;
    for (Scale.ScaledResource rsrc : assetScale().getScaledResources(path)) {
      try {
        BufferedImage image = requireResource(rsrc.path).readImage();
        // if image is at a higher scale factor than the view, scale to the view display factor
        Scale viewScale = plat.graphics().scale(), imageScale = rsrc.scale;
        float viewImageRatio = viewScale.factor / imageScale.factor;
        if (viewImageRatio < 1) {
          image = scaleImage(image, viewImageRatio);
          imageScale = viewScale;
        }
        if (plat.config.convertImagesOnLoad) {
          BufferedImage convertedImage = JavaGraphics.convertImage(image);
          if (convertedImage != image) {
            plat.log().debug("Converted image: " + path + " [type=" + image.getType() + "]");
            image = convertedImage;
          }
        }
        return new ImageImpl.Data(imageScale, image, image.getWidth(), image.getHeight());
      } catch (FileNotFoundException fnfe) {
        error = fnfe; // keep going, checking for lower resolution images
      }
    }
    plat.log().warn("Could not load image: " + path + " [error=" + error + "]");
    throw error != null ? error : new FileNotFoundException(path);
  }

  @Override protected ImageImpl createImage (boolean async, int rwid, int rhei, String source) {
    return new JavaImage(plat, async, rwid, rhei, source);
  }

  protected static final String[] SUFFIXES = { ".wav", ".mp3" };
}
