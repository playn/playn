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
package playn.payments.core;

import java.io.Serializable;

public interface InAppPayments {

  /**
   * @param iat. The time when the JWT was issued, specified in seconds since
   *          the epoch.
   * @param exp. The time when the purchase will expire, specified in seconds
   *          since the epoch. This is a standard JWT field.
   * @param request. The item being purchased.
   * @param encodedJWTcallback. Get the parsed JWT
   */
  public void encodeJWT(String iat, String exp, PurchaseRequest request,
      EncodeJWTCallback encodedJWTcallback);

  interface EncodeJWTCallback {
    /*
     *@param JWT. Encoded JWT is returned if json is valid.
     */
    void successHandler(String JWT);

    /*
     *@error. Handle the encoding exception.
     */
    void failureHandler(String error);
  }

  /**
   * Initiates the purchase flow.
   * 
   * @param purchaseInformation as json web token
   */
  public void buy(String purchaseInformation);

  interface PurchaseCallback {
    /**
     * Called when a purchase is successfully completed, after the iframe is
     * closed.
     * 
     * @param result is an object that contains a request object, a response
     *          object, and a JWT.
     */
    void successHandler(PurchaseResponse result);

    /**
     * Called when a purchase fails to complete.
     * 
     * @param result is an object that may contain a request object
     */
    void failureHandler(PurchaseResponse result);
  }

  /**
   * A {@link Listener} implementation with NOOP stubs provided for each method.
   */
  class Adapter implements PurchaseCallback {
    @Override
    public void successHandler(PurchaseResponse result) { /* NOOP! */
    }

    @Override
    public void failureHandler(PurchaseResponse result) { /* NOOP! */
    }
  }

  public void setCallback(PurchaseCallback callback);

  interface PurchaseRequest {
    /**
     * The name of the item. This name is displayed prominently in the purchase
     * flow UI and can have no more than 50 characters.
     */
    String name();

    /**
     * Optional: Text that describes the item. This description is displayed in
     * the purchase flow UI and can have no more than 100 characters.
     */
    String description();

    /**
     * The purchase price of the item, with up to 2 decimal places.
     */
    String price();

    /**
     * A 3-character currency code that defines the billing currency. Currently
     * the only supported currency code is USD.
     */
    String currencyCode();

    /**
     * Optional: Data to be passed to your success and failure callbacks. The
     * string can have no more than 200 characters. You might want to include
     * something that lets you identify the buyer. Examples of other information
     * you might include are a promotion identifier or a SKU #.
     */
    String sellerData();

    class Serialized implements Serializable {
      String name;
      String description;
      String price;
      String currencyCode;
      String sellerData;

      private static final long serialVersionUID = 2805284943658356093L;

      public Serialized() {

      }

      public Serialized(PurchaseRequest request) {
        this.name = request.name();
        this.description = request.description();
        this.price = request.price();
        this.currencyCode = request.currencyCode();
        this.sellerData = request.sellerData();
      }
    }

    @SuppressWarnings("serial")
    class Impl extends Serialized implements PurchaseRequest {
      public Impl(String name, String description, String price, String currencyCode,
          String sellerData) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.currencyCode = currencyCode;
        this.sellerData = sellerData;
      }

      public Impl(Serialized ser) {
        this.name = ser.name;
        this.description = ser.description;
        this.price = ser.price;
        this.currencyCode = ser.currencyCode;
        this.sellerData = ser.sellerData;
      }

      @Override
      public String name() {
        return name;
      }

      @Override
      public String description() {
        return description;
      }

      @Override
      public String price() {
        return price;
      }

      @Override
      public String currencyCode() {
        return currencyCode;
      }

      @Override
      public String sellerData() {
        return sellerData;
      }
    }

    
  }

  interface PurchaseResponse {
    /*
     * Has the same fields and values as the request object that was passed to
     * buy() (in the jwt parameter). The field order and literal values might be
     * different from the original request. For example, "10.50" might change to
     * "10.5".
     */
    PurchaseRequest request();

    /*
     * orderId contains an order identifier from Google.
     */
    String orderId();

    /*
     * The same JWT that's passed to the server if you specify a postback URL.
     * This JWT includes all the information that's in the request and response
     * objects.
     */
    String jwt();

    class Impl implements PurchaseResponse {
      private String orderId;
      private String jwt;
      private PurchaseRequest request;

      public Impl(String orderId, String jwt, PurchaseRequest request) {
        this.orderId = orderId;
        this.jwt = jwt;
        this.request = request;
      }

      @Override
      public String orderId() {
        return orderId;
      }

      @Override
      public String jwt() {
        return jwt;
      }

      @Override
      public PurchaseRequest request() {
        return request;
      }
    }
  }
}