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

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import playn.payments.core.InAppPayments.PurchaseRequest;

@RemoteServiceRelativePath("encodejwt")
public interface JWTEncodedService extends RemoteService  {
  
  /**
   * Check if meta data is assigned in the web.xml on the server side. 
   */
  public class NullPurchaseMetaDataException extends java.lang.Exception {
    private static final long serialVersionUID = 8383901821872620925L;
    
    public NullPurchaseMetaDataException() {
    }
    
    public NullPurchaseMetaDataException(String msg) {
      super(msg + " is missing. Please add its value in the web.xml.");
    }
  }
  
  /**
   * @param iat. The time when the JWT was issued, specified in seconds since
   *          the epoch.
   * @param exp. The time when the purchase will expire, specified in seconds
   *          since the epoch. This is a standard JWT field.
   * @param request. The item being purchased.
   * @param encodedJWTcallback. Get the parsed JWT
   * @return encoded JWT
   * @throws IllegalArgumentException
   * @throws NullPurchaseMetaDataException 
   */
  String encodeJWT(String iat, String exp, PurchaseRequest.Serialized request) throws IllegalArgumentException, NullPurchaseMetaDataException;
}
