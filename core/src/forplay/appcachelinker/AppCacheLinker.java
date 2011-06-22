/**
 * Copyright 2011 The ForPlay Authors
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

package forplay.appcachelinker;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;

import com.google.gwt.core.ext.LinkerContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.linker.Artifact;
import com.google.gwt.core.ext.linker.ArtifactSet;
import com.google.gwt.core.ext.linker.EmittedArtifact;
import com.google.gwt.core.linker.CrossSiteIframeLinker;

/**
 * AppCacheLinker - linker for public path resources in the Application Cache. <br/>
 * To use:
 * <ol>
 * <li>Add {@code manifest="YOURMODULENAME/appcache.nocache.manifest"} to the
 * {@code <html>} tag in your main html file. E.g.,
 * {@code <html manifest="mymodule/appcache.nocache.manifest">}</li>
 * <li>Add a mime-mapping to your web.xml file:
 * <p>
 * 
 * <pre>{@code <mime-mapping>
 * <extension>manifest</extension>
 * <mime-type>text/cache-manifest</mime-type>
 * </mime-mapping>
 * }</pre>
 * </li>
 * </ol>
 * <p>
 * On every compile, this linker will regenerate the appcache.nocache.manifest
 * file with files from the public path of your module.
 * <p>
 * This linker has some default behavior with respect to which files will be
 * included in the manifest, which can be modified by overriding
 * {@link #accept(String)}.
 * <p>
 * To add additional static files to the manifest, override
 * {@link #staticCachedFiles()}.
 * <p>
 * Note: This linker currently extends {@link CrossSiteIframeLinker}. For better
 * JavaScript debugging, this should be changed to extend
 * {@link com.google.gwt.core.linker.DirectInstallLinker} instead once
 * http://code.google.com/p/chromium/issues/detail?id=87005 has been fixed.
 */
public class AppCacheLinker extends CrossSiteIframeLinker {

  private static final HashSet<String> DEFAULT_EXTENSION_WHITELIST = new HashSet<String>(
      Arrays.asList(new String[] {
          // .wav files explicitly excluded, since HTML games use .mp3
          "js", "html", "jpg", "jpeg", "png", "gif", "mp3", "ogg", "mov", "avi", "wmv",
          "webm", "css", "json", "flv", "swf",}));

  private static final String MANIFEST = "appcache.nocache.manifest";

  @Override
  public String getDescription() {
    return "AppCacheLinker";
  }

  /*
   * Standard linker that also outputs a manifest file based on the public
   * resources
   */
  @Override
  public ArtifactSet link(TreeLogger logger, LinkerContext context, ArtifactSet artifacts)
      throws UnableToCompleteException {
    ArtifactSet toReturn = super.link(logger, context, artifacts);

    // Create the general cache-manifest resource for the landing page:
    toReturn.add(emitLandingPageCacheManifest(context, logger, toReturn, staticCachedFiles()));

    return toReturn;
  }

  /**
   * Determines whether our not the given should be included in the app cache
   * manifest. Subclasses may override this method in order to filter out
   * specific file patterns.
   * 
   * @param file the path of the resource being considered
   * @return true if the file should be included in the manifest
   */
  protected boolean accept(String path) {

    // GWT Development Mode files
    if (path.equals("hosted.html") || path.endsWith(".devmode.js")) {
      return false;
    }

    // Default or welcome file
    if (path.equals("/")) {
      return true;
    }
    
    // Whitelisted file extension
    int pos = path.lastIndexOf('.');
    if (pos != -1) {
      String extension = path.substring(pos + 1);
      if (DEFAULT_EXTENSION_WHITELIST.contains(extension)) {
        return true;
      }
    }
    
    // Not included by default
    return false;
  }

  /**
   * Creates the cache-manifest resource specific for the landing page.
   */
  private Artifact<?> emitLandingPageCacheManifest(LinkerContext context, TreeLogger logger,
      ArtifactSet artifacts, String[] staticFiles) throws UnableToCompleteException {
    // Create a string of cacheable resources
    StringBuilder publicSourcesSb = new StringBuilder();
    StringBuilder publicStaticSourcesSb = new StringBuilder();

    // Iterate over all emitted artifacts, and collect all cacheable artifacts
    for (@SuppressWarnings("rawtypes")
    Artifact artifact : artifacts) {
      if (artifact instanceof EmittedArtifact) {
        EmittedArtifact ea = (EmittedArtifact) artifact;
        String path = "/" + context.getModuleFunctionName() + "/" + ea.getPartialPath();

        if (accept(path)) {
          publicSourcesSb.append(path + "\n");
        }
      }
    }

    // Iterate over all static files
    if (staticFiles != null) {
      for (String staticFile : staticFiles) {
        if (accept(staticFile)) {
          publicStaticSourcesSb.append(staticFile + "\n");
        }
      }
    }

    // build cache list
    StringBuilder sb = new StringBuilder();
    sb.append("CACHE MANIFEST\n");
    sb.append("# Unique id #" + (new Date()).getTime() + "." + Math.random() + "\n");
    // we have to generate this unique id because the resources can change but
    // the hashed cache.html files can remain the same.
    sb.append("# Note: must change this every time for cache to invalidate\n");
    sb.append("\n");
    sb.append("CACHE:\n");
    sb.append(publicSourcesSb.toString());
    sb.append("# Static cached files\n");
    sb.append(publicStaticSourcesSb.toString());
    sb.append("\n\n");
    sb.append("# All other resources require the user to be online.\n");
    sb.append("NETWORK:\n");
    sb.append("*\n");

    logger.log(TreeLogger.DEBUG, "Make sure you have the following"
        + " attribute added to your landing page's <html> tag: <html manifest=\""
        + context.getModuleFunctionName() + "/" + MANIFEST + "\">");

    // Create the manifest as a new artifact and return it:
    return emitString(logger, sb.toString(), MANIFEST);
  }

  /**
   * Override this method to force the linker to include additional files in the
   * manifest.
   * 
   * @return array of additional files to include in the manifest.
   */
  protected String[] staticCachedFiles() {
    return null;
  }
}
