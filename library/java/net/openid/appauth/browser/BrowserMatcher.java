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

package net.openid.appauth.browser;

import android.support.annotation.NonNull;

/**
 * Determines whether a {@link BrowserDescriptor} matches some set of criteria.
 * Implementations of this type can be used to control the set of browsers used by AppAuth
 * for authorization.
 */
public interface BrowserMatcher {

    /**
     * @return true if the browser matches some set of criteria.
     */
    boolean matches(@NonNull BrowserDescriptor descriptor);

}
