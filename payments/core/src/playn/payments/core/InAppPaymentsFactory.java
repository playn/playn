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

public class InAppPaymentsFactory {
  
  private static InAppPayments payments = null;

  /**
   * Initialize payments instance in the backend
   * @param payments.
   */
  public static void buildInAppPayments(InAppPayments payments) {
    InAppPaymentsFactory.payments = payments;
  }
  
  public static InAppPayments payments() {
    return payments;
  }
}
