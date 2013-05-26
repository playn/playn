/**
 * Copyright 2011 The PlayN Authors
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
package playn.java;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

import playn.core.BatchImpl;
import playn.core.Storage;

/**
 * JavaStorage is backed by a properties file stored in the temp directory.
 *
 * TODO(pdr): probably want better handling on where the file is stored
 */
class JavaStorage implements Storage {

  private final JavaPlatform platform;
  private final File tempFile;
  private final Properties properties;
  private boolean isPersisted = false; // false by default

  JavaStorage(JavaPlatform platform, JavaPlatform.Config config) {
    this.platform = platform;
    this.tempFile = new File(new File(System.getProperty("java.io.tmpdir")),
                             config.storageFileName + ".tmp");
    this.properties = maybeRetrieveProperties();
  }

  @Override
  public void setItem(String key, String value) throws RuntimeException {
    properties.setProperty(key, value);
    maybePersistProperties(properties);
  }

  @Override
  public void removeItem(String key) {
    properties.remove(key);
    maybePersistProperties(properties);
  }

  @Override
  public String getItem(String key) {
    return properties.getProperty(key);
  }

  @Override
  public Batch startBatch() {
    return new BatchImpl(this) {
      @Override protected void setImpl(String key, String data) {
        properties.setProperty(key, data);
      }
      @Override protected void removeImpl(String key) {
        properties.remove(key);
      }
      @Override protected void onAfterCommit() {
        maybePersistProperties(properties);
      }
    };
  }

  @Override
  public Iterable<String> keys() {
    return properties.stringPropertyNames();
  }

  @Override
  public boolean isPersisted() {
    return isPersisted;
  }

  private void maybePersistProperties(Properties properties) {
    try {
      properties.store(new FileOutputStream(tempFile), null);
      isPersisted = true;
    } catch (Exception e) {
      platform.log().info("Error persisting properties: " + e.getMessage());
      isPersisted = false;
    }
  }

  private Properties maybeRetrieveProperties() {
    Properties properties = new Properties();
    if (tempFile.exists()) {
      try {
        properties.load(new FileInputStream(tempFile));
        isPersisted = true;
      } catch(Exception e) {
        platform.log().info("Error retrieving file: " + e.getMessage());
        isPersisted = false;
      }
    } else {
      // Attempt to write newly created properties immediately to make the isPersisted valid
      maybePersistProperties(properties);
    }
    return properties;
  }
}
