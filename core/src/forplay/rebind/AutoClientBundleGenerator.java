/**
 * Copyright 2011 The ForPlay Authors
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
package forplay.rebind;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.ext.Generator;
import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.NotFoundException;
import com.google.gwt.core.ext.typeinfo.TypeOracle;
import com.google.gwt.dev.util.collect.HashMap;
import com.google.gwt.resources.client.ClientBundleWithLookup;
import com.google.gwt.resources.client.DataResource;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.ResourcePrototype;
import com.google.gwt.resources.client.TextResource;
import com.google.gwt.user.rebind.ClassSourceFileComposerFactory;
import com.google.gwt.user.rebind.SourceWriter;

import forplay.core.Asserts;

/**
 * Automatically generate a client bundle from the resources available in the provided interface's
 * package directory and below.
 */
public class AutoClientBundleGenerator extends Generator {

  private static final Map<String, String> EXTENSION_MAP = new HashMap<String, String>();

  private static FileFilter fileFilter = new FileFilter() {
    @Override
    public boolean accept(File file) {
      if (file.isDirectory()) {
        // Skip .svn directories
        if (file.getName().equals(".svn")) {
          return false;
        }
        // By default descend into all directories
        return true;
      } else {
        // Include only all explicitly mapped extensions
        String extension = getExtension(file.getName());
        return EXTENSION_MAP.containsKey(extension);
      }
    }
  };

  private static final String WEB_INF_CLASSES = "WEB-INF/classes/";

  static {
    EXTENSION_MAP.put(".png", "image/png");
    EXTENSION_MAP.put(".gif", "image/gif");
    EXTENSION_MAP.put(".jpg", "image/jpeg");

    /*
     * Do not include WAV files, since HTML5 audio playback ended events are not yet a part of GWT
     * 2.2.0, which means we won't know when these sounds stop playing.
     * 
     * EXTENSION_MAP.put(".wav", "audio/wav");
     */
    EXTENSION_MAP.put(".mp3", "audio/mp3");

    EXTENSION_MAP.put(".json", "text/json");
  }

  private static String getContentType(TreeLogger logger, File file) {
    String name = file.getName().toLowerCase();
    int pos = name.lastIndexOf('.');
    String extension = pos == -1 ? "" : name.substring(pos);
    String contentType = EXTENSION_MAP.get(extension);
    if (contentType == null) {
      logger.log(
          TreeLogger.WARN,
          "No Content Type mapping for files with '" + extension
              + "' extension. Please add a mapping to the "
              + AutoClientBundleGenerator.class.getCanonicalName() + " class.");
      contentType = "application/octet-stream";
    }
    return contentType;
  }

  /**
   * Determine whether the provided method name is valid in Java.
   */
  private static boolean isValidMethodName(String methodName) {
    return methodName.matches("^[a-zA-Z_$][a-zA-Z0-9_$]*$"); // TODO: fix regex
  }

  /**
   * Strip off the filename extension, if it's present.
   */
  private static String stripExtension(String filename) {
    return filename.replaceFirst("\\.[^.]+$", "");
  }

  /**
   * Get the filename extension or return an empty string if there's no extension.
   */
  private static String getExtension(String filename) {
    return filename.replaceFirst(".*(\\.[^.]+)$", "$1");
  }

  @Override
  public String generate(TreeLogger logger, GeneratorContext context, String typeName)
      throws UnableToCompleteException {
    TypeOracle typeOracle = context.getTypeOracle();

    JClassType userType;
    try {
      userType = typeOracle.getType(typeName);
    } catch (NotFoundException e) {
      logger.log(TreeLogger.ERROR, "Unable to find metadata for type: " + typeName, e);
      throw new UnableToCompleteException();
    }
    String packageName = userType.getPackage().getName();
    String className = userType.getName();
    className = className.replace('.', '_');

    if (userType.isInterface() == null) {
      logger.log(TreeLogger.ERROR, userType.getQualifiedSourceName() + " is not an interface", null);
      throw new UnableToCompleteException();
    }

    ClassSourceFileComposerFactory composerFactory = new ClassSourceFileComposerFactory(
        packageName, className + "Impl");
    composerFactory.addImplementedInterface(userType.getQualifiedSourceName());

    composerFactory.addImport(ClientBundleWithLookup.class.getName());
    composerFactory.addImport(DataResource.class.getName());
    composerFactory.addImport(GWT.class.getName());
    composerFactory.addImport(ImageResource.class.getName());
    composerFactory.addImport(ResourcePrototype.class.getName());
    composerFactory.addImport(TextResource.class.getName());

    File warDirectory = getWarDirectory(logger);
    Asserts.check(warDirectory.isDirectory());

    File classesDirectory = new File(warDirectory, WEB_INF_CLASSES);
    Asserts.check(classesDirectory.isDirectory());

    File resourcesDirectory = new File(classesDirectory, packageName.replace('.', '/'));
    Asserts.check(resourcesDirectory.isDirectory());

    String baseClassesPath = classesDirectory.getPath();
    logger.log(TreeLogger.DEBUG, "baseClassesPath: " + baseClassesPath);

    Set<File> files = preferMp3(getFiles(resourcesDirectory, fileFilter));
    Set<String> methodNames = new HashSet<String>();

    PrintWriter pw = context.tryCreate(logger, packageName, className + "Impl");
    if (pw != null) {
      SourceWriter sw = composerFactory.createSourceWriter(context, pw);

      // write out jump methods

      sw.println("public ResourcePrototype[] getResources() {");
      sw.indent();
      sw.println("return MyBundle.INSTANCE.getResources();");
      sw.outdent();
      sw.println("}");

      sw.println("public ResourcePrototype getResource(String name) {");
      sw.indent();
      sw.println("return MyBundle.INSTANCE.getResource(name);");
      sw.outdent();
      sw.println("}");

      // write out static ClientBundle interface

      sw.println("static interface MyBundle extends ClientBundleWithLookup {");
      sw.indent();
      sw.println("MyBundle INSTANCE = GWT.create(MyBundle.class);");

      for (File file : files) {
        String filepath = file.getPath();
        String relativePath = filepath.replace(baseClassesPath, "").replace('\\', '/').replaceFirst(
            "^/", "");
        String filename = file.getName();
        String contentType = getContentType(logger, file);
        String methodName = stripExtension(filename);

        if (!isValidMethodName(methodName)) {
          logger.log(TreeLogger.WARN, "Skipping invalid method name (" + methodName + ") due to: "
              + relativePath);
          continue;
        }
        if (!methodNames.add(methodName)) {
          logger.log(TreeLogger.WARN, "Skipping duplicate method name due to: " + relativePath);
          continue;
        }

        logger.log(TreeLogger.DEBUG, "Generating method for: " + relativePath);

        Class<? extends ResourcePrototype> returnType = getResourcePrototype(contentType);

        // generate method
        sw.println();

        if (returnType == DataResource.class) {
          if (contentType.startsWith("audio/")) {
            // Prevent the use of data URLs, which Flash won't play
            sw.println("@DataResource.DoNotEmbed");
          } else {
            // Specify an explicit MIME type, for use in the data URL
            sw.println("@DataResource.MimeType(\"" + contentType + "\")");
          }
        }

        sw.println("@Source(\"" + relativePath + "\")");

        sw.println(returnType.getName() + " " + methodName + "();");
      }

      sw.outdent();
      sw.println("}");

      sw.commit(logger);
    }
    return composerFactory.getCreatedClassName();
  }

  /**
   * Filter file set, preferring *.mp3 files where alternatives exist.
   */
  private HashSet<File> preferMp3(Set<File> files) {
    HashMap<String, File> map = new HashMap<String, File>();
    for (File file : files) {
      String path = stripExtension(file.getPath());
      if (file.getName().endsWith(".mp3") || !map.containsKey(path)) {
        map.put(path, file);
      }
    }
    return new HashSet<File>(map.values());
  }

  /**
   * Recursively get a list of files in the provided directory.
   */
  private HashSet<File> getFiles(File dir, FileFilter filter) {
    Asserts.checkNotNull(dir);
    Asserts.checkNotNull(filter);
    Asserts.checkArgument(dir.isDirectory());

    HashSet<File> fileList = new HashSet<File>();
    File[] files = dir.listFiles(filter);
    for (int i = 0; i < files.length; i++) {
      File f = files[i];
      if (f.isFile()) {
        // f = file
        fileList.add(f);
      } else {
        // f = directory
        if (filter.accept(f)) {
          fileList.addAll(getFiles(f, filter));
        }
      }
    }
    return fileList;
  }

  private Class<? extends ResourcePrototype> getResourcePrototype(String contentType) {
    Class<? extends ResourcePrototype> returnType;
    if (contentType.startsWith("image/")) {
      returnType = ImageResource.class;
    } else if (contentType.startsWith("text/")) {
      returnType = TextResource.class;
    } else {
      returnType = DataResource.class;
    }
    return returnType;
  }

  /**
   * When invoking the GWT compiler from GPE, the working directory is the Eclipse project
   * directory. However, when launching a GPE project, the working directory is the project 'war'
   * directory. This methods returns the war directory in either case in a fairly naive and
   * non-robust manner.
   */
  private File getWarDirectory(TreeLogger logger) throws UnableToCompleteException {
    File currentDirectory = new File(".");
    try {
      String canonicalPath = currentDirectory.getCanonicalPath();
      logger.log(TreeLogger.INFO, "Current directory in which this generator is executing: "
          + canonicalPath);
      if (canonicalPath.endsWith("war")) {
        return currentDirectory;
      } else {
        return new File("war");
      }
    } catch (IOException e) {
      logger.log(TreeLogger.ERROR, "Failed to get canonical path", e);
      throw new UnableToCompleteException();
    }
  }

}
