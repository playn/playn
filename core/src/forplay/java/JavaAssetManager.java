/**
 * Copyright 2010 The ForPlay Authors
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
package forplay.java;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import javax.imageio.ImageIO;

import forplay.core.AbstractAssetManager;
import forplay.core.ForPlay;
import forplay.core.Image;
import forplay.core.ResourceCallback;
import forplay.core.Sound;

/**
 * TODO(jgw): Make it possible to add more filesystem roots.
 */
public class JavaAssetManager extends AbstractAssetManager {

  private String pathPrefix = "war";

  public void setPathPrefix(String prefix) {
    pathPrefix = prefix;
  }

  /**
   * @param collection is ignored
   * @param key the path to the file
   */
  @Override
  protected Image doGetImage(String path) {
    File imgFile = new File(pathPrefix, path);
    try {
      BufferedImage img = ImageIO.read(imgFile);
      return new JavaImage(img);
    } catch (Exception e) {
      ForPlay.log().warn("Could not load image at " + imgFile, e);
      return new JavaImage(e);
    }
  }

  @Override
  protected Sound doGetSound(String path) {
    // Java won't play *.mp3, so for now use *.wav exclusively
    path += ".wav";
    File file = new File(pathPrefix, path);
    return ((JavaAudio) ForPlay.audio()).createSound(file);
  }

  @Override
  protected void doGetText(String path, ResourceCallback<String> callback) {
    File textFile = new File(pathPrefix, path);
    try {
      BufferedReader reader = new BufferedReader(new FileReader(textFile));
      StringBuilder sb = new StringBuilder();
      String line = null;
      while ((line = reader.readLine()) != null) {
        sb.append(line);
      }
      callback.done(sb.toString());
    } catch (Throwable e) {
      callback.error(e);
    }
  }
}
