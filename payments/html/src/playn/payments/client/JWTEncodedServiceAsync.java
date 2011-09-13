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
package playn.payments.client;

import playn.payments.core.InAppPayments.PurchaseRequest;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface JWTEncodedServiceAsync {

  /**
   * @param iat. The time when the JWT was issued, specified in seconds since
   *          the epoch.
   * @param exp. The time when the purchase will expire, specified in seconds
   *          since the epoch. This is a standard JWT field.
   * @param request. The item being purchased.
   * @param callback. Pass the encoded jwt value
   * @throws IllegalArgumentException
   */
  void encodeJWT(String iat, String exp, PurchaseRequest.Serialized request, AsyncCallback<String> callback) throws IllegalArgumentException;
}
