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
package playn.payments.html;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.rpc.AsyncCallback;

import playn.payments.client.JWTEncodedService;
import playn.payments.client.JWTEncodedServiceAsync;
import playn.payments.core.InAppPayments;

public class HtmlInAppPayments implements InAppPayments {

  private PurchaseCallback purchaseCallback;
  
  private final JWTEncodedServiceAsync encodeJWTService = GWT.create(JWTEncodedService.class);
  
  /**
   * 
   * @param iat The time when the JWT was issued, specified in seconds since
   *          the epoch.
   * @param exp The time when the purchase will expire, specified in seconds
   *          since the epoch. This is a standard JWT field.
   * @param request The item being purchased.
   * @param encodedJWTcallback Get the parsed JWT
   */
  @Override
  public void encodeJWT(String iat, String exp, PurchaseRequest request, final EncodeJWTCallback encodeJWTCallback) {
    PurchaseRequest.Serialized ser = new PurchaseRequest.Serialized(request);
    encodeJWTService.encodeJWT(iat, exp, ser, new AsyncCallback<String>() {
      @Override
      public void onFailure(Throwable caught) {
        encodeJWTCallback.failureHandler(caught.getMessage());
      }

      @Override
      public void onSuccess(String result) {
        encodeJWTCallback.successHandler(result);
      }
      
    });
  }
  
  /**
   * Initiates the purchase flow.
   * 
   * @param purchaseInformation as json web token
   */
  @Override
  public void buy(String purchaseInformation) {
    invokeGoogleIAPService(this, purchaseInformation);
  }

  private native void invokeGoogleIAPService(HtmlInAppPayments payments, String purchaseInformation) /*-{
		onSuccess = function(result) {
			payments.@playn.payments.html.HtmlInAppPayments::onSuccess(Lplayn/payments/html/HtmlInAppPayments$PurchaseResponseJSObject;)(result);
		};

		onFailure = function(result) {
			payments.@playn.payments.html.HtmlInAppPayments::onFailure(Lplayn/payments/html/HtmlInAppPayments$PurchaseResponseJSObject;)(result);
		};

		$wnd.goog.payments.inapp.buy({
			'jwt' : purchaseInformation,
			'success' : onSuccess,
			'failure' : onFailure
		});
  }-*/;

  @Override
  public void setCallback(PurchaseCallback callback) {
    this.purchaseCallback = callback;
  }

  static class PurchaseRequestJSObject extends JavaScriptObject implements  PurchaseRequest{
    protected PurchaseRequestJSObject() {
      
    }
    
    @Override
    public final native String name()  /*-{
      return this["name"];
    }-*/;

    @Override
    public final native String description() /*-{
      return this["description"];
    }-*/;

    @Override
    public final native String price() /*-{
      return this["price"];
    }-*/;

    @Override
    public final native String currencyCode() /*-{
      return this["currencyCode"];
    }-*/;

    @Override
    public final native String sellerData() /*-{
      return this["sellerData"];
    }-*/;
  }
  
  static class PurchaseResponseJSObject extends JavaScriptObject implements  PurchaseResponse{
    protected PurchaseResponseJSObject() {
      
    }
    @Override
    public final native PurchaseRequestJSObject request() /*-{
      return this["request"];
    }-*/;

    @Override
    public final native String orderId() /*-{
      return this["orderId"];
    }-*/;

    @Override
    public final native String jwt()/*-{
      return this["jwt"];
    }-*/;
  }

  /**
   * Callback function in the native javascript function. 
   * It will be called if payment flow succeeded.
   * @param result Object in the javascript.
   */
  public void onSuccess(PurchaseResponseJSObject result) {
    purchaseCallback.successHandler(result);
  }

  /**
   * Callback function in the native javascript function.
   * It will be called if payment flow failed.
   * @param result Object in the javascript.
   */
  public void onFailure(PurchaseResponseJSObject result) {
    purchaseCallback.failureHandler(result);
  }
}