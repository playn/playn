/**
 * Copyright 2010 The ForPlay Authors
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
package forplay.core;

/**
 * Thrown when there is a problem loading an asset.  Could be
 * network error, disk error, file not found, etc.
 */
@SuppressWarnings("serial")
@Deprecated
public class AssetLoadException extends RuntimeException {

	public AssetLoadException(String msg, Throwable exception) {
		super(msg, exception);
	}

  public AssetLoadException(String msg) {
    super(msg);
  }

}
