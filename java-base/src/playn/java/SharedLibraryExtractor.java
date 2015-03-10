/**
 * Copyright 2013 The PlayN Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package playn.java;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.CRC32;

/**
 * Extracts shared libraries from a natives jar file. Adapted from libGDX project code.
 */
public class SharedLibraryExtractor {

  private final boolean isWindows = System.getProperty("os.name").contains("Windows");
  private final boolean isLinux = System.getProperty("os.name").contains("Linux");
  private final boolean isMac = System.getProperty("os.name").contains("Mac");
  private final boolean is64Bit = System.getProperty("os.arch").equals("amd64");

  /**
   * Extracts the specified library into the specified temp directory (if it has not already been
   * extracted thereto, or if the CRC does not match).
   * @param libraryName The library to extract from the classpath or JAR.
   * @param dirName The name of the subdirectory where the file will be extracted. If null, the
   * file's CRC will be used.
   * @return The extracted file.
   */
  public File extractLibrary(String libraryName, String dirName) throws IOException {
    File javaLibPath = new File(System.getProperty("java.library.path"));
    for (String sourcePath : platformNames(libraryName)) {
      InputStream cinput;
      try {
        cinput = readFile(sourcePath);
      } catch (FileNotFoundException fnfe) {
        // attempt to fallback to file at java.library.path location
        File file = new File(javaLibPath, sourcePath);
        if (file.exists()) return file;
        continue; // otherwise try the next variant in the source path
      }

      String sourceCrc = crc(cinput);
      if (dirName == null)
        dirName = sourceCrc;

      File extractedDir = new File(System.getProperty("java.io.tmpdir") + "/playn" +
                                   System.getProperty("user.name") + "/" + dirName);
      File extractedFile = new File(extractedDir, new File(sourcePath).getName());
      String extractedCrc = null;
      if (extractedFile.exists()) {
        try {
          extractedCrc = crc(new FileInputStream(extractedFile));
        } catch (FileNotFoundException ignored) {
        }
      }

      // if file doesn't exist or the CRC doesn't match, extract it to the temp dir
      if (extractedCrc == null || !extractedCrc.equals(sourceCrc)) {
        extractedDir.mkdirs();
        copyTo(sourcePath, extractedFile);
        // a hack to cope with Mac OS Java dropping support for .jnilib files
        if (sourcePath.endsWith(".jnilib")) {
          String hackPath = sourcePath.replaceAll(".jnilib", ".dylib");
          copyTo(sourcePath, new File(extractedDir, new File(hackPath).getName()));
        }
      }

      return extractedFile;
    }
    throw new FileNotFoundException("Unable to find shared lib for '" + libraryName + "'");
  }

  /** Maps a platform independent library name to one or more platform dependent names. */
  private String[] platformNames(String libraryName) {
    if (isWindows) return new String[] { libraryName + (is64Bit ? "64.dll" : ".dll") };
    if (isLinux) return new String[] { "lib" + libraryName + (is64Bit ? "64.so" : ".so") };
    if (isMac) return new String[] { "lib" + libraryName + ".jnilib",
                                     "lib" + libraryName + ".dylib" };
    return new String[] { libraryName };
  }

  /** Returns a CRC of the remaining bytes in the stream. */
  private String crc(InputStream input) {
    if (input == null)
      throw new IllegalArgumentException("input cannot be null.");
    CRC32 crc = new CRC32();
    byte[] buffer = new byte[4096];
    try {
      while (true) {
        int length = input.read(buffer);
        if (length == -1) break;
        crc.update(buffer, 0, length);
      }
    } catch (Exception ex) {
      try {
        input.close();
      } catch (Exception ignored) {
      }
    }
    return Long.toString(crc.getValue());
  }

  private InputStream readFile(String path) throws IOException {
    InputStream input = getClass().getResourceAsStream("/" + path);
    if (input == null)
      throw new FileNotFoundException("Unable to read file for extraction: " + path);
    return input;
  }

  private void copyTo (String sourcePath, File target) {
    try {
      InputStream input = readFile(sourcePath);
      FileOutputStream output = new FileOutputStream(target);
      byte[] buffer = new byte[4096];
      while (true) {
        int length = input.read(buffer);
        if (length == -1) break;
        output.write(buffer, 0, length);
      }
      input.close();
      output.close();
    } catch (IOException ex) {
      throw new RuntimeException("Error extracting file: " + sourcePath, ex);
    }
  }
}
