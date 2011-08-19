
/*
 * Copyright (C) 2008 Archie L. Cobbs <archie@dellroad.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * $Id: ExternalInterface.java 12 2008-02-23 20:49:24Z archie.cobbs $
 */

package flash.external;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * @see <a href="http://livedocs.adobe.com/flash/8/main/00002200.html#wp232104">ActionScript 2.0 Language Reference</a>
 */
public final class ExternalInterface {

    private ExternalInterface() {
    }

    /**
     * Determine if the external interface is available.
     */
    public static native boolean isAvailable() /*-{
        return flash.external.ExternalInterface.available;
    }-*/;

    /**
     * Register callback for invocations from the container.
     *
     * @param methodName name of the method that will be called
     *  by the container
     * @param listener instance that will receive container invocations
     */
    public static native boolean addCallback(String methodName,
      CallbackListener listener) /*-{
        return flash.external.ExternalInterface.addCallback(methodName, null,
          function() {
            listener.@flash.external.CallbackListener::invoked()();
          });
    }-*/;

    /**
     * Invoke a function in the container.
     * At most three parameters are supported.
     *
     * @param methodName name of the method to invoke
     * @param params zero or more arrays each containing a method parameter
     *  as the first element in the array
     * @return an array containing the returned value as its first element
     */
    public static JavaScriptObject call(String methodName, JavaScriptObject... params) {
        if (!ExternalInterface.isAvailable())
            return null;
        JavaScriptObject param0 = params.length > 0 ? params[0] : null;
        JavaScriptObject param1 = params.length > 1 ? params[1] : null;
        JavaScriptObject param2 = params.length > 2 ? params[2] : null;
        return call(methodName, param0, param1, param2);
    }

    private static native JavaScriptObject call(String methodName,
     JavaScriptObject param0, JavaScriptObject param1, JavaScriptObject param2) /*-{
        if (param2 !== null)
            return [flash.external.ExternalInterface.call(methodName, param0[0], param1[0], param2[0])];
        if (param1 !== null)
            return [flash.external.ExternalInterface.call(methodName, param0[0], param1[0])];
        if (param0 !== null)
            return [flash.external.ExternalInterface.call(methodName, param0[0])];
        return [flash.external.ExternalInterface.call(methodName)];
    }-*/;
}

