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
package playn.payments.server;

import java.security.InvalidKeyException;
import java.security.SignatureException;
import java.util.Calendar;

import org.joda.time.Instant;

import net.oauth.jsontoken.JsonToken;
import net.oauth.jsontoken.crypto.HmacSHA256Signer;
import playn.payments.client.JWTEncodedService;
import playn.payments.core.InAppPayments.PurchaseRequest;

import com.google.gson.JsonObject;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

@SuppressWarnings("serial")
public class JWTEncodedServiceImp extends RemoteServiceServlet implements JWTEncodedService {
  /**
   * Must be "Google". The "aud" field is a standard JWT field that identifies
   * the target audience for the JWT.
   */
  private static String aud = null;

  /**
   * Your Seller Identifier. The "iss" field is a standard JWT field that
   * identifies the issuer of the JWT
   */
  private static String iss = null;

  /**
   * The type of request. Must be "google/payments/inapp/item/v1". This is a
   * standard JWT field.
   */
  private static String typ = null;
  
  /**
   * Seller Secret
   */
  private static String secret = null;

  /**
   * Products validation date, default value is 1 hour which equals 3600 seconds.
   */
  private static long timespan = 3600000L;
  
  private void checkMetaData(String data) throws NullPurchaseMetaDataException {
    if (data == null || data == "") {
      throw new NullPurchaseMetaDataException("iss");
    }  
  }
  
  private JsonToken createToken(String iat, String exp, PurchaseRequest purchaseRequest)
      throws InvalidKeyException, NullPurchaseMetaDataException {
    // Current time and signing algorithm
    Calendar cal = Calendar.getInstance();
    HmacSHA256Signer signer = new HmacSHA256Signer(iss, null, secret.getBytes());

    // Configure JSON token
    JsonToken token = new JsonToken(signer);
    token.setAudience(aud);
    token.setParam("typ", typ);

    if (iat == null) {
      token.setIssuedAt(new Instant(cal.getTimeInMillis()));
    } else {
      token.setIssuedAt(new Instant(Long.parseLong(iat)));
    }
    
    if (exp == null) {
      token.setExpiration(new Instant(cal.getTimeInMillis() + timespan));
    } else {
      token.setExpiration(new Instant(Long.parseLong(exp)));
    }
   
    // Configure request object
    JsonObject request = new JsonObject();
    request.addProperty("name", purchaseRequest.name());
    request.addProperty("description", purchaseRequest.description());
    request.addProperty("price", purchaseRequest.price());
    request.addProperty("currencyCode", purchaseRequest.currencyCode());
    request.addProperty("sellerData", purchaseRequest.sellerData());

    JsonObject payload = token.getPayloadAsJsonObject();
    payload.add("request", request);
    return token;
  }

  /**
   * 
   * @param iat. The time when the JWT was issued, specified in seconds since
   *          the epoch.
   * @param exp. The time when the purchase will expire, specified in seconds
   *          since the epoch. This is a standard JWT field.
   * @param request. The item being purchased.
   * @throws NullPurchaseMetaDataException 
   */
  @Override
  public String encodeJWT(String iat, String exp, PurchaseRequest.Serialized request)
      throws IllegalArgumentException, NullPurchaseMetaDataException {
    if (aud == null)
      aud = getServletConfig().getInitParameter("aud");
    checkMetaData(aud);
    
    if (iss == null)
      iss = getServletConfig().getInitParameter("iss");
    checkMetaData(iss);
    
    if (typ == null)
      typ = getServletConfig().getInitParameter("typ");
    checkMetaData(typ);
    
    if (secret == null) 
      secret = getServletConfig().getInitParameter("secret");
    checkMetaData(secret);
    
    String jwt = null;
    PurchaseRequest req = new PurchaseRequest.Impl(request);
    try {
      JsonToken jsonToken = createToken(iat, exp, req);
      jwt = jsonToken.serializeAndSign();
    } catch (InvalidKeyException e) {
      e.printStackTrace();
    } catch (SignatureException e) {
      e.printStackTrace();
    }
    return jwt;
  }

}
