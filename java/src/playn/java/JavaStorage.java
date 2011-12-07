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

import playn.core.PlayN;
import playn.core.Storage;

/**
 * JavaStorage is backed by a properties file stored in the temp directory.
 *
 * TODO(pdr): probably want better handling on where the file is stored
 */
class JavaStorage implements Storage {
  private static String tempDir = System.getProperty("java.io.tmpdir");
  private static String tempFile = "playn.tmp";
  private boolean isPersisted = false; // false by default
  private Properties properties;

  public void init() {
    properties = maybeRetrieveProperties();
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
  public Iterable<String> keys() {
    return properties.stringPropertyNames();
  }

  @Override
  public boolean isPersisted() {
    return isPersisted;
  }

  private void maybePersistProperties(Properties properties) {
    try {
      properties.store(new FileOutputStream(new File(tempDir, tempFile)), null);
      isPersisted = true;
    } catch (Exception e) {
      PlayN.log().info("Error persisting properties: " + e.getMessage());
      isPersisted = false;
    }
  }

  private Properties maybeRetrieveProperties() {
    Properties properties = new Properties();
    if (new File(tempFile).exists()) {
      try {
        properties.load(new FileInputStream(new File(tempDir, tempFile)));
        isPersisted = true;
      } catch(Exception e) {
        PlayN.log().info("Error retrieving file: " + e.getMessage());
        isPersisted = false;
      }
    } else {
      // Attempt to write newly created properties immediately to make the isPersisted valid
      maybePersistProperties(properties);
    }
    return properties;
  }
}
