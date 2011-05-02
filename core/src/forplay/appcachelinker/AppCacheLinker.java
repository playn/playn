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

import com.google.gwt.core.ext.LinkerContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.linker.Artifact;
import com.google.gwt.core.ext.linker.ArtifactSet;
import com.google.gwt.core.ext.linker.EmittedArtifact;
import com.google.gwt.core.linker.IFrameLinker;

import java.util.Date;

/**
 * AppCacheLinker - linker for public path resources in the Application Cache. <br/>
 * To use:
 * <ol>
 * <li>Add {@code manifest="YOURMODULENAME/appcache.nocache.manifest"} to the
 * {@code <html>} tag in your base html file. E.g.,
 * {@code <html manifest="mymodule/appcache.nocache.manifest">}</li>
 * <li>Add a mime-mapping to your web.xml file (this is for AppEngine, adjust
 * accordingly):<br/>
 * 
 * <pre>{@code <mime-mapping>
 * <extension>manifest</extension>
 * <mime-type>text/cache-manifest</mime-type>
 * </mime-mapping>
 * }</pre>
 * </li>
 * </ol>
 * 
 * On every compile, this linker will regenerate the appcache.nocache.manifest
 * file with files from the public path of your module.
 */
public class AppCacheLinker extends IFrameLinker {

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
    toReturn.add(emitLandingPageCacheManifest(context, logger, toReturn));

    return toReturn;
  }

  /**
   * Creates the cache-manifest resource specific for the landing page.
   */
  private Artifact<?> emitLandingPageCacheManifest(LinkerContext context, TreeLogger logger,
      ArtifactSet artifacts) throws UnableToCompleteException {
    // Create a string of cacheable resources
    StringBuilder publicSourcesSb = new StringBuilder();

    // Iterate over all emitted artifacts, and collect all cacheable artifacts
    for (Artifact<?> artifact : artifacts) {
      if (artifact instanceof EmittedArtifact) {
        EmittedArtifact ea = (EmittedArtifact) artifact;
        String pathName = /* context.getModuleFunctionName() + "/" + */ea.getPartialPath();
        if (pathName.endsWith("symbolMap") || pathName.endsWith(".xml.gz")) {
          // skip these resources
        } else {
          publicSourcesSb.append(pathName + "\n");
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
    sb.append("\n\n");
    sb.append("# All other resources require the user to be online.\n");
    sb.append("NETWORK:\n");
    sb.append("*\n");

    logger.log(TreeLogger.INFO, "Make sure you have the following"
        + " attribute added to your landing page's <html> tag: <html manifest=\""
        + context.getModuleFunctionName() + "/" + MANIFEST + "\">");

    // Create the manifest as a new artifact and return it:
    return emitString(logger, sb.toString(), MANIFEST);
  }
}
