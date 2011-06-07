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
package forplay.java;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

import forplay.core.ForPlay;
import forplay.core.Storage;

/**
 * JavaStorage is backed by a properties file stored in the temp directory.
 * 
 * TODO(pdr): probably want better handling on where the file is stored
 */
class JavaStorage implements Storage {
  private static String tempFile = System.getProperty("java.io.tmpdir") + "forplay.tmp";
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
  public boolean isPersisted() {
    return isPersisted;
  }

  private void maybePersistProperties(Properties properties) {
    try {
      properties.store(new FileOutputStream(tempFile), null);
      isPersisted = true;
    } catch (Exception e) {
      ForPlay.log().info("Error persisting properties: " + e.getMessage());
      isPersisted = false;
    }
  }

  private Properties maybeRetrieveProperties() {
    Properties properties = new Properties();
    try {
      properties.load(new FileInputStream(tempFile));
      isPersisted = true;
    } catch(Exception e) {
      ForPlay.log().info("Error retrieving file: " + e.getMessage());
      isPersisted = false;
    }
    return properties;
  }
}
