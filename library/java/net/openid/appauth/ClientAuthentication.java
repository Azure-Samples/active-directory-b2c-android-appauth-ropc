/*
 * Copyright 2016 The AppAuth for Android Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.openid.appauth;

import android.support.annotation.NonNull;

import java.util.Map;

public interface ClientAuthentication {
    /**
     * Thrown when a mandatory property is missing from the registration response.
     */
    class UnsupportedAuthenticationMethod extends Exception {
        private String mAuthMethod;

        /**
         * Indicates that the specified client authentication method is unsupported.
         */
        public UnsupportedAuthenticationMethod(String field) {
            super("Unsupported client authentication method: " + field);
            mAuthMethod = field;
        }

        public String getUnsupportedAuthenticationMethod() {
            return mAuthMethod;
        }
    }

    /**
     * Constructs any extra parameters necessary to include in the request headers for the client
     * authentication.
     */
    Map<String, String> getRequestHeaders(@NonNull String clientId);

    /**
     * Constructs any extra parameters necessary to include in the request body for the client
     * authentication.
     */
    Map<String, String> getRequestParameters(@NonNull String clientId);
}
